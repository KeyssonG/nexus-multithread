package keysson.apis.validacao.config;

import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.QueueBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration("validacaoRabbitMQConfig")
public class RabbitMQConfig {

    @Bean("validacaoFuncionarioClienteQueue")
    public Queue funcionarioClienteQueue() {
        return QueueBuilder.durable("funcionario-cliente.fila").build();
    }
}
