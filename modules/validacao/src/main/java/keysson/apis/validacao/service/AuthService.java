package keysson.apis.validacao.service;

import jakarta.servlet.http.HttpServletRequest;
import keysson.nexus.security.JwtUtil;
import keysson.nexus.security.KeycloakService;
import keysson.apis.validacao.dto.PasswordResetEvent;
import keysson.apis.validacao.dto.request.LoginRequest;
import keysson.apis.validacao.dto.response.LoginResponse;
import keysson.apis.validacao.exception.BusinessRuleException;
import keysson.apis.validacao.exception.enums.ErrorCode;
import keysson.apis.validacao.model.PasswordResetToken;
import keysson.apis.validacao.model.User;
import keysson.apis.validacao.repository.ValidacaoRepository;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service("validacaoAuthService")
public class AuthService {

    private static final Logger log = LoggerFactory.getLogger(AuthService.class);

    private final PasswordEncoder passwordEncoder;

    @Autowired
    private ValidacaoRepository validacaoRepository;

    @Autowired
    private JwtUtil jwtUtil;

    @Autowired
    private KeycloakService keycloakService;

    @Autowired
    private RabbitTemplate rabbitTemplate;

    @Autowired
    private HttpServletRequest httpRequest;

    @Autowired
    public AuthService(PasswordEncoder passwordEncoder) {
        this.passwordEncoder = passwordEncoder;
    }

    public LoginResponse login(LoginRequest request) {
        // ──── PASSO 1: Tentar Keycloak ────
        try {
            KeycloakService.KeycloakToken kcToken = keycloakService.attemptLogin(
                    request.getUsername(),
                    request.getPassword(),
                    "multithread-gestao"
            );
            if (kcToken != null) {
                log.info("keycloak_login_success | username={}", request.getUsername());
                return new LoginResponse(kcToken.getAccessToken(), new java.util.Date(kcToken.getExpiresAt()));
            }
        } catch (Exception e) {
            log.debug("keycloak_login_fallback | username={} reason={}", request.getUsername(), e.getMessage());
        }

        // ──── PASSO 2: Login PostgreSQL (fluxo atual) ────
        User user = validacaoRepository.findByUsername(request.getUsername(), request.getIdEmpresa());
        int statuCompany = validacaoRepository.findStatusCompany(request.getIdEmpresa());
        if (user == null) {
            throw new BusinessRuleException(ErrorCode.USER_NOT_FOUND);
        }

        boolean checkPassword = passwordEncoder.matches(request.getPassword(), user.getPassword());

        if (!checkPassword) {
            throw new BusinessRuleException(ErrorCode.BAD_PASSWORD);
        }

        int status = user.getStatus();

        if (statuCompany == 1) {
            throw new BusinessRuleException(ErrorCode.CONTA_PENDENTE);
        }

        if (status == 1) {
            validacaoRepository.activeAccount(user.getId(), user.getCompanyId(), user.getUsername());
        }

        List<String> modules = validacaoRepository.findUserModules(user.getId(), request.getIdEmpresa());

        // ──── PASSO 3: Migrar para Keycloak ────
        try {
            String kcUserId;

            if (keycloakService.userExists(request.getUsername())) {
                kcUserId = keycloakService.findUserId(request.getUsername());
                log.info("keycloak_user_already_exists | username={}", request.getUsername());
            } else {
                kcUserId = keycloakService.createUser(
                        user.getUsername(),
                        request.getPassword(),
                        user.getCompanyId(),
                        user.getConsumerId(),
                        "client"
                );
            }

            if (kcUserId != null) {
                if (!keycloakService.userHasRealmRoles(kcUserId)) {
                    keycloakService.assignRoles(kcUserId, modules);
                    log.info("keycloak_roles_atribuidas | userId={} username={}", user.getId(), user.getUsername());
                }
                log.info("keycloak_migrado | userId={} username={}", user.getId(), user.getUsername());

                // ──── PASSO 4: Retentar Keycloak login ────
                KeycloakService.KeycloakToken kcToken = keycloakService.attemptLogin(
                        request.getUsername(),
                        request.getPassword(),
                        "multithread-gestao"
                );
                if (kcToken != null) {
                    log.info("keycloak_login_after_migration | username={}", request.getUsername());
                    return new LoginResponse(kcToken.getAccessToken(), new java.util.Date(kcToken.getExpiresAt()));
                }
            }
        } catch (Exception e) {
            log.error("keycloak_migration_failed | username={} error={}", request.getUsername(), e.getMessage());
        }

        // ──── PASSO 5: Fallback seguro — token legado ────
        String token = jwtUtil.generateToken(
                user.getId(),
                user.getCompanyId(),
                user.getConsumerId(),
                modules);

        return new LoginResponse(token, jwtUtil.getExpirationDate());
    }

    @Transactional
    public void validatePasswordReset(String token, String newPassword) {
        PasswordResetToken resetToken = validacaoRepository.findValidResetToken(token);
        if (resetToken == null) {
            throw new BusinessRuleException(ErrorCode.TOKEN_INVALIDO);
        }

        String newEncryptedPassword = passwordEncoder.encode(newPassword);
        validacaoRepository.saveNewPassword(newEncryptedPassword, resetToken.getUserId().intValue());
        validacaoRepository.markTokenAsUsed(token);

        String username = validacaoRepository.findUsernameById(resetToken.getUserId().intValue());
        if (username != null) {
            keycloakService.updatePasswordByUsername(username, newPassword);
        }
    }

    @Transactional
    public void requestPasswordChange(String email) {
        User user = validacaoRepository.findByUsernameAndEmail(email);
        if (user == null) {
            throw new BusinessRuleException(ErrorCode.USER_NOT_FOUND);
        }

        int tokenInt = 100000 + (int) (Math.random() * 900000);
        String token = String.valueOf(tokenInt);
        LocalDateTime expiresAt = LocalDateTime.now().plusMinutes(3);

        validacaoRepository.saveResetToken((long) user.getId(), token, expiresAt);

        PasswordResetEvent event = new PasswordResetEvent(email, token, user.getUsername());
        rabbitTemplate.convertAndSend("password.reset.queue", event);
    }
}
