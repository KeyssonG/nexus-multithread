package keysson.apis.validacaoad.service;

import keysson.apis.validacaoad.dto.FuncionarioCadastradoEvent;
import keysson.apis.validacaoad.dto.request.RequestRegister;
import keysson.apis.validacaoad.dto.response.FuncionarioRegistroResultado;
import keysson.apis.validacaoad.exception.BusinessRuleException;
import keysson.apis.validacaoad.exception.enums.ErrorCode;
import keysson.apis.validacaoad.repository.RegisterRepository;
import keysson.nexus.security.KeycloakService;
import jakarta.transaction.Transactional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.security.SecureRandom;
import java.sql.SQLException;
import java.util.Base64;
import java.util.Random;
import java.util.UUID;

@Service("validacaoADRegisterService")
public class RegisterService {

    private static final Logger log = LoggerFactory.getLogger(RegisterService.class);

    private final RegisterRepository registerRepository;
    private final PasswordEncoder passwordEncoder;
    private final RabbitService rabbitService;

    @Autowired
    private RabbitTemplate rabbitTemplate;

    @Autowired
    private KeycloakService keycloakService;


    public RegisterService(@Qualifier("validacaoADRegisterRepository") RegisterRepository registerRepository, 
                           @Qualifier("validacaoADRabbitService") RabbitService rabbitService, 
                           RabbitTemplate rabbitTemplate,
                           PasswordEncoder passwordEncoder) {
        this.registerRepository = registerRepository;
        this.rabbitService = rabbitService;
        this.passwordEncoder = passwordEncoder;
        this.rabbitTemplate = rabbitTemplate;
    }

    @Transactional
    public void registerEmployee (RequestRegister requestRegister) throws BusinessRuleException, SQLException {

        if (registerRepository.existsByCpf(requestRegister.getCpf())) {
            throw new BusinessRuleException(ErrorCode.CPF_JA_CADASTRADO);
        }

        if (registerRepository.existsByUsername(requestRegister.getUsername())) {
            throw new BusinessRuleException(ErrorCode.USERNAME_JA_EXISTE);
        }

        if (registerRepository.existsByEmail(requestRegister.getEmail())) {
            throw new BusinessRuleException(ErrorCode.EMAIL_JA_CADASTRADO);
        }

        int numeroConta = gerarNumeroContaUnico();

        String plainPassword = generateRandomPassword();
        String encodedPassword = passwordEncoder.encode(plainPassword);

        FuncionarioRegistroResultado resultado = registerRepository.save(
                requestRegister.getNome(),
                requestRegister.getDataNascimento(),
                requestRegister.getEmail(),
                requestRegister.getCpf(),
                requestRegister.getSexo(),
                encodedPassword,
                requestRegister.getUsername(),
                requestRegister.getDepartamento(),
                numeroConta,
                requestRegister.getTelefone()
        );

        if (resultado.getIdFuncionario() == 0) {
            throw new BusinessRuleException(ErrorCode.ERRO_CADASTRAR_FUNCIONARIO);
        }

        if (resultado.getResultCode() == 0) {
            try {
                keycloakService.createUser(
                        requestRegister.getUsername(),
                        plainPassword,
                        0,
                        UUID.randomUUID(),
                        "admin",
                        requestRegister.getEmail()
                );
                log.info("keycloak_usuario_criado_no_registro_ad | usuario={}", requestRegister.getUsername());
            } catch (Exception e) {
                log.error("keycloak_falha_criar_usuario_no_registro_ad | usuario={} erro={}", requestRegister.getUsername(), e.getMessage());
            }

            FuncionarioCadastradoEvent event = new FuncionarioCadastradoEvent(
                    resultado.getIdFuncionario(),
                    requestRegister.getNome(),
                    requestRegister.getEmail(),
                    requestRegister.getCpf(),
                    requestRegister.getUsername(),
                    plainPassword
            );
            try {
                rabbitTemplate.convertAndSend("funcionario.fila", event);

                rabbitService.saveMessagesInBank(event, 1);
            } catch (Exception ex) {
                rabbitService.saveMessagesInBank(event, 0);
                throw new RuntimeException("Erro ao enviar mensagem ao RabbitMQ: " + ex.getMessage());
            }
        } else if (resultado.getResultCode() == 1) {
            throw new BusinessRuleException(ErrorCode.ERRO_CADASTRAR);
        }
    }
    private int gerarNumeroContaUnico() {
        Random random = new Random();
        int numero;

        do {
            numero = 100000 + random.nextInt(900000);
        } while (registerRepository.existsByNumeroConta(numero));

        return numero;
    }

    private String generateRandomPassword() {
        SecureRandom random = new SecureRandom();
        byte[] bytes = new byte[12];
        random.nextBytes(bytes);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes).substring(0, 12);
    }
}
