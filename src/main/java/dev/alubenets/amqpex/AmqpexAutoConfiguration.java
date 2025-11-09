package dev.alubenets.amqpex;

import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

/**
 * Main autoconfiguration class for AMQPex - Spring AMQP Extensions library.
 * This configuration enables and sets up the extension features for Spring AMQP.
 */
@AutoConfiguration
@EnableConfigurationProperties({AmqpexProperties.class})
public class AmqpexAutoConfiguration {

    /**
     * Creates a new instance of AmqpexAutoConfiguration.
     * This constructor is used by Spring to instantiate the autoconfiguration.
     */
    public AmqpexAutoConfiguration() {
        // Default constructor for Spring autoconfiguration
    }
}