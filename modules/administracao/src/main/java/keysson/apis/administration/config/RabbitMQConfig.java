package keysson.apis.administration.config;

import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.QueueBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration("administrationRabbitMQConfig")
public class RabbitMQConfig {

    @Bean
    public Queue alteraStatusQueue() {
        return QueueBuilder.durable("alteraStatus.fila").build();
    }
}
