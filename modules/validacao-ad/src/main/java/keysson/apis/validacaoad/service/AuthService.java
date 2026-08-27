package keysson.apis.validacaoad.service;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.transaction.Transactional;
import keysson.apis.validacaoad.Utils.JwtUtil;
import keysson.apis.validacaoad.dto.PasswordResetEvent;
import keysson.apis.validacaoad.dto.request.LoginRequest;
import keysson.apis.validacaoad.dto.request.RequestUpdatePassword;
import keysson.apis.validacaoad.dto.response.LoginResponse;
import keysson.apis.validacaoad.exception.BusinessRuleException;
import keysson.apis.validacaoad.exception.enums.ErrorCode;
import keysson.apis.validacaoad.model.PasswordResetToken;
import keysson.apis.validacaoad.model.User;
import keysson.apis.validacaoad.repository.ValidacaoRepository;
import keysson.nexus.security.KeycloakService;

import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.List;

@Service("validacaoADAuthService")
public class AuthService {

    private static final Logger log = LoggerFactory.getLogger(AuthService.class);

    private final PasswordEncoder passwordEncoder;

    @Autowired
    @Qualifier("validacaoADRepository")
    private ValidacaoRepository validacaoRepository;

    @Autowired
    @Qualifier("validacaoADJwtUtil")
    private JwtUtil jwtUtil;

    @Autowired
    private KeycloakService keycloakService;

    @Autowired
    private HttpServletRequest httpRequest;

    @Autowired
    @Qualifier("validacaoADRabbitService")
    private RabbitService rabbitService;

    @Autowired
    private RabbitTemplate rabbitTemplate;

    @Autowired
    public AuthService(PasswordEncoder passwordEncoder) {
        this.passwordEncoder = passwordEncoder;
    }

    @Transactional
    public LoginResponse login(LoginRequest request) throws SQLException {
        // ──── PASSO 1: Tentar Keycloak ────
        try {
            KeycloakService.KeycloakToken kcToken = keycloakService.attemptLogin(
                    request.getUsername(),
                    request.getPassword(),
                    "multithread-portal"
            );
            if (kcToken != null) {
                log.info("keycloak_ad_login_success | username={}", request.getUsername());
                return new LoginResponse(kcToken.getAccessToken(), new java.util.Date(kcToken.getExpiresAt()), false);
            }
        } catch (Exception e) {
            log.debug("keycloak_ad_login_fallback | username={} reason={}", request.getUsername(), e.getMessage());
        }

        // ──── PASSO 2: Login PostgreSQL (fluxo atual) ────
        User user = validacaoRepository.findByUsername(request.getUsername());
        if (user == null) {
            log.warn("login_user_not_found | username={}", request.getUsername());
            throw new BusinessRuleException(ErrorCode.USER_NOT_FOUND);
        }

        Boolean checkPassword = passwordEncoder.matches(request.getPassword(), user.getPassword());

        if (checkPassword == false) {
            log.warn("login_bad_password | username={}", request.getUsername());
            throw new BusinessRuleException(ErrorCode.BAD_PASSWORD);
        }

        boolean isInitialAccess = user.isInitialAccess();
        if (isInitialAccess) {
            validacaoRepository.updateFirstAccess(false, user.getId());
        }

        // ──── PASSO 3: Migrar para Keycloak ────
        try {
            String kcUserId;

            if (keycloakService.userExists(request.getUsername())) {
                kcUserId = keycloakService.findUserId(request.getUsername());
                log.info("keycloak_ad_usuario_ja_existe | usuario={}", request.getUsername());
            } else {
                kcUserId = keycloakService.createUser(
                        user.getUsername(),
                        request.getPassword(),
                        user.getCompanyId(),
                        user.getConsumerId(),
                        "admin"
                );
            }

            if (kcUserId != null) {
                if (!keycloakService.userHasRealmRoles(kcUserId)) {
                    keycloakService.assignRoles(kcUserId, List.of("admin-staff"));
                    log.info("keycloak_ad_roles_atribuidas | userId={} usuario={}", user.getId(), user.getUsername());
                }
                log.info("keycloak_ad_migrado | userId={} usuario={}", user.getId(), user.getUsername());

                // ──── PASSO 4: Retentar Keycloak login ────
                KeycloakService.KeycloakToken kcToken = keycloakService.attemptLogin(
                        request.getUsername(),
                        request.getPassword(),
                        "multithread-portal"
                );
                if (kcToken != null) {
                    log.info("keycloak_ad_login_after_migration | username={}", request.getUsername());
                    return new LoginResponse(kcToken.getAccessToken(), new java.util.Date(kcToken.getExpiresAt()), isInitialAccess);
                }
            }
        } catch (Exception e) {
            log.error("keycloak_ad_migration_failed | username={} error={}", request.getUsername(), e.getMessage());
        }

        // ──── PASSO 5: Fallback seguro — token legado ────
        String token = jwtUtil.generateToken(
                user.getId(),
                user.getCompanyId(),
                user.getConsumerId());

        return new LoginResponse(token, jwtUtil.getExpirationDate(), isInitialAccess);
    }


    @Transactional
    public void updatePasswordUser(RequestUpdatePassword request) throws SQLException {

        String token = (String) httpRequest.getAttribute("CleanJwt");

        Integer userId = jwtUtil.extractUserId(token);
        if (userId == null) {
            log.warn("update_password_user_not_found | reason=token_without_user_id");
            throw new IllegalArgumentException("ID do usuário não encontrado no token.");
        }

        if (request.getNewPassword() == null || request.getNewPassword().length() < 6) {
            log.warn("update_password_weak | userId={}", userId);
            throw new IllegalArgumentException("A nova senha deve ter pelo menos 6 caracteres.");
        }

        String newPasswordHash = passwordEncoder.encode(request.getNewPassword());
        validacaoRepository.saveNewPassword(newPasswordHash, userId);

        String username = validacaoRepository.findUsernameById(userId);
        if (username != null) {
            keycloakService.updatePasswordByUsername(username, request.getNewPassword());
        }
    }

    @Transactional
    public void requestPasswordChange(String email) throws SQLException {
        User user = validacaoRepository.findByUsernameAndEmail(email);
        if (user == null) {
            log.warn("reset_user_not_found | email={}", email);
            throw new BusinessRuleException(ErrorCode.USER_NOT_FOUND);
        }

        int tokenInt = 100000 + (int) (Math.random() * 900000);
        String token = String.valueOf(tokenInt);
        LocalDateTime expiresAt = LocalDateTime.now().plusMinutes(3);

        validacaoRepository.saveResetToken((long) user.getId(), token, expiresAt);

        PasswordResetEvent event = new PasswordResetEvent(email, token, user.getUsername());
        rabbitTemplate.convertAndSend("password.reset.queue", event);
    }

    @Transactional
    public void validatePasswordReset(String token, String newPassword) throws SQLException {
        PasswordResetToken resetToken = validacaoRepository.findValidResetToken(token);
        if (resetToken == null) {
            log.warn("reset_token_invalid | token={}", token != null ? token.substring(0, Math.min(3, token.length())) + "***" : "null");
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
}
