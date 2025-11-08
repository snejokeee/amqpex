package dev.alubenets.amqpex.logging;

import com.rabbitmq.client.ConnectionFactory;
import dev.alubenets.amqpex.AmqpexProperties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.config.ContainerCustomizer;
import org.springframework.amqp.rabbit.listener.SimpleMessageListenerContainer;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;

@AutoConfiguration
@ConditionalOnClass(ConnectionFactory.class)
public class LoggingAutoConfiguration {

    private static final Logger log = LoggerFactory.getLogger(LoggingAutoConfiguration.class);

    @Bean
    @ConditionalOnProperty(
        prefix = "amqpex.logging.incoming",
        name = {"enabled"},
        havingValue = "true",
        matchIfMissing = true
    )
    @Order(Ordered.HIGHEST_PRECEDENCE)
    public ContainerCustomizer<SimpleMessageListenerContainer> loggingContainerCustomizer(AmqpexProperties properties) {
        log.debug("Creating loggingContainerCustomizer bean for SimpleMessageListenerContainer");
        return container -> {
            String listenerId = container.getListenerId();
            log.debug("Applying logging post-processor to SimpleMLC: {} (HashCode: {})", listenerId, container.hashCode());
            container.addAfterReceivePostProcessors(new IncomingLoggingMessagePostProcessor(properties.getLogging().getIncoming()));
        };
    }
}