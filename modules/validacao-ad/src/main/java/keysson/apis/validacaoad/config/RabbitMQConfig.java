package keysson.apis.validacaoad.config;



import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.QueueBuilder;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;


@Configuration("validacaoADRabbitMQConfig")
public class RabbitMQConfig {

    @Bean("validacaoADFuncionarioCadastradoQueue")
    public Queue funcionarioCadastradoQueue() {
        return QueueBuilder.durable("funcionario.fila").build();
    }

    @Bean("validacaoADPasswordResetQueue")
    public Queue passwordResetQueue() {
        return QueueBuilder.durable("password.reset.queue").build();
    }

    @Bean("validacaoADMessageConverter")
    public Jackson2JsonMessageConverter messageConverter() {
        return new Jackson2JsonMessageConverter();
    }

    @Bean("validacaoADRabbitTemplate")
    public RabbitTemplate rabbitTemplate(org.springframework.amqp.rabbit.connection.ConnectionFactory connectionFactory,
                                         @Qualifier("validacaoADMessageConverter") Jackson2JsonMessageConverter messageConverter) {
        RabbitTemplate rabbitTemplate = new RabbitTemplate(connectionFactory);
        rabbitTemplate.setMessageConverter(messageConverter);
        return rabbitTemplate;
    }
}