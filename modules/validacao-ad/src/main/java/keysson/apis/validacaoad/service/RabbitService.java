package keysson.apis.validacaoad.service;

import keysson.apis.validacaoad.dto.FuncionarioCadastradoEvent;
import keysson.apis.validacaoad.dto.MensagensPendentes;
import keysson.apis.validacaoad.dto.PasswordResetEvent;
import keysson.apis.validacaoad.repository.RabbitRepository;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import java.sql.SQLException;

@Service("validacaoADRabbitService")
public class RabbitService {

    private final RabbitRepository rabbitRepository;

    @Autowired
    private RabbitTemplate rabbitTemplate;

    public RabbitService(@Qualifier("validacaoADRabbitRepository") RabbitRepository rabbitRepository) {
        this.rabbitRepository = rabbitRepository;
    }

    public void saveMessagesInBank(FuncionarioCadastradoEvent event, int status) throws SQLException {
        MensagensPendentes mensagenPendente = new MensagensPendentes();
        mensagenPendente.setIdFuncionario(event.getIdFuncionario());
        mensagenPendente.setName(event.getName());
        mensagenPendente.setEmail(event.getEmail());
        mensagenPendente.setCpf(event.getCpf());
        mensagenPendente.setUsername(event.getUsername());
        mensagenPendente.setStatus(status);

        rabbitRepository.saveMenssage(mensagenPendente);
    }
}
