package dev.alubenets.amqpex;

import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

@AutoConfiguration
@EnableConfigurationProperties(AmqpexProperties.class)
public class AmqpexAutoConfiguration {
}