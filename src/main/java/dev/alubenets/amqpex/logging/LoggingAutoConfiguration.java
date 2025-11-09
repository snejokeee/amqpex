package dev.alubenets.amqpex.logging;

import com.rabbitmq.client.ConnectionFactory;
import dev.alubenets.amqpex.AmqpexProperties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.config.ContainerCustomizer;
import org.springframework.amqp.rabbit.listener.SimpleMessageListenerContainer;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.amqp.RabbitAutoConfiguration;
import org.springframework.boot.autoconfigure.amqp.RabbitTemplateCustomizer;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;

/**
 * Auto-configuration for logging incoming and outgoing AMQP messages.
 * Sets up the logging post-processors based on configuration properties.
 */

/**
 * Auto-configuration for logging incoming and outgoing AMQP messages.
 * Sets up the logging post-processors based on configuration properties.
 */
@AutoConfiguration(
    before = {RabbitAutoConfiguration.class}
)
@ConditionalOnClass(ConnectionFactory.class)
public class LoggingAutoConfiguration {

    private static final Logger log = LoggerFactory.getLogger(LoggingAutoConfiguration.class);

    /**
     * Creates a new instance of LoggingAutoConfiguration.
     * This constructor is used by Spring to instantiate the logging autoconfiguration.
     */
    public LoggingAutoConfiguration() {
        // Default constructor for Spring autoconfiguration
    }

    /**
     * Creates a container customizer that adds the incoming message logging post-processor.
     *
     * @param properties the AMQPex properties
     * @return a container customizer for SimpleMessageListenerContainer
     */
    @Bean
    @ConditionalOnProperty(
        prefix = "amqpex.logging.incoming",
        name = "enabled",
        havingValue = "true",
        matchIfMissing = true
    )
    public ContainerCustomizer<SimpleMessageListenerContainer> incomingLoggingContainerCustomizer(AmqpexProperties properties) {
        log.debug("Creating incomingLoggingContainerCustomizer bean for SimpleMessageListenerContainer");
        return container -> {
            String listenerId = container.getListenerId();
            log.debug("Applying incoming logging post-processor to SimpleMLC: {} (HashCode: {})", listenerId, container.hashCode());
            container.addAfterReceivePostProcessors(new IncomingMessageLogger(properties.getLogging().getIncoming()));
        };
    }

    /**
     * Creates a RabbitTemplate customizer that adds the outgoing message logging post-processor.
     *
     * @param properties the AMQPex properties
     * @return a RabbitTemplate customizer
     */
    @Bean
    @ConditionalOnProperty(
        prefix = "amqpex.logging.outgoing",
        name = "enabled",
        havingValue = "true",
        matchIfMissing = true
    )
    public RabbitTemplateCustomizer outgoingLoggingRabbitTemplateCustomizer(AmqpexProperties properties) {
        log.debug("Creating outgoingLoggingRabbitTemplateCustomizer bean for RabbitTemplate");
        return template -> {
            log.debug("Applying outgoing logging post-processor to RabbitTemplate (HashCode: {})", template.hashCode());
            template.addBeforePublishPostProcessors(new OutgoingMessageLogger(properties.getLogging().getOutgoing()));
        };
    }
}