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
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.sql.SQLException;
import java.time.LocalDateTime;

@Service("validacaoADAuthService")
public class AuthService {

    private final PasswordEncoder passwordEncoder;

    @Autowired
    @Qualifier("validacaoADRepository")
    private ValidacaoRepository validacaoRepository;

    @Autowired
    @Qualifier("validacaoADJwtUtil")
    private JwtUtil jwtUtil;

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
        User user = validacaoRepository.findByUsername(request.getUsername());
        if (user == null) {
            throw new BusinessRuleException(ErrorCode.USER_NOT_FOUND);
        }

        Boolean checkPassword = passwordEncoder.matches(request.getPassword(), user.getPassword());

        if (checkPassword == false) {
            throw new BusinessRuleException(ErrorCode.BAD_PASSWORD);
        }

        boolean isInitialAccess = user.isInitialAccess();
        if (isInitialAccess) {
            validacaoRepository.updateFirstAccess(false, user.getId());
        }

        String token = jwtUtil.generateToken(
                user.getId(),
                user.getCompanyId(),
                user.getConsumerId());

        return new LoginResponse(token, jwtUtil.getExpirationDate(), isInitialAccess);

    }


    public void updatePasswordUser(RequestUpdatePassword request) throws SQLException {

        String token = (String) httpRequest.getAttribute("CleanJwt");

        Integer userId = jwtUtil.extractUserId(token);
        if (userId == null) {
            throw new IllegalArgumentException("ID do usuário não encontrado no token.");
        }

        if (request.getNewPassword() == null || request.getNewPassword().length() < 6) {
            throw new IllegalArgumentException("A nova senha deve ter pelo menos 6 caracteres.");
        }

        String newPasswordHash = passwordEncoder.encode(request.getNewPassword());
        validacaoRepository.saveNewPassword(newPasswordHash, userId);
    }

    @Transactional
    public void requestPasswordChange(String email) throws SQLException {
        // Busca o usuário pelo username e email na tabela contatos
        User user = validacaoRepository.findByUsernameAndEmail(email);
        if (user == null) {
            throw new BusinessRuleException(ErrorCode.USER_NOT_FOUND);
        }

        // Gera um token único
        int tokenInt = 100000 + (int) (Math.random() * 900000);
        String token = String.valueOf(tokenInt);
        LocalDateTime expiresAt = LocalDateTime.now().plusMinutes(3);

        // Salva o token no banco
        validacaoRepository.saveResetToken((long) user.getId(), token, expiresAt);

        // Cria o evento para enviar para a fila
        PasswordResetEvent event = new PasswordResetEvent(email, token, user.getUsername());

        rabbitTemplate.convertAndSend("password.reset.queue", event);
    }

    @Transactional
    public void validatePasswordReset(String token, String newPassword) throws SQLException {
        // Busca o token válido
        PasswordResetToken resetToken = validacaoRepository.findValidResetToken(token);
        if (resetToken == null) {
            throw new BusinessRuleException(ErrorCode.TOKEN_INVALIDO);
        }

        // Criptografa a nova senha
        String newEncryptedPassword = passwordEncoder.encode(newPassword);

        // Atualiza a senha do usuário
        validacaoRepository.saveNewPassword(newEncryptedPassword, resetToken.getUserId().intValue());

        // Marca o token como usado
        validacaoRepository.markTokenAsUsed(token);
    }
}
