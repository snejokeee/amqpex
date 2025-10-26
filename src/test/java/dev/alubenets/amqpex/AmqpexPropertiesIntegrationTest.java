package dev.alubenets.amqpex;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Integration test suite for {@link AmqpexProperties}.
 * Uses nested classes to test different property configurations
 * within a single outer test class file.
 */
class AmqpexPropertiesIntegrationTest {

    @Nested
    @SpringBootTest(classes = {AmqpexAutoConfiguration.class})
    @TestPropertySource(properties = {
        "amqpex.logging.incoming.enabled=false", // Test setting 'enabled' to false
        "amqpex.logging.incoming.max-body-size=2500"  // Test setting a custom 'max-body-size'
    })
    class WithCustomProperties {

        @Autowired
        private AmqpexProperties properties;

        @Test
        void shouldBindCustomPropertiesCorrectlyWithSpringBootMechanism() {
            // Assert that the bean was created and properties were bound correctly
            // by the Spring Boot @ConfigurationProperties mechanism using the values
            // defined in the nested class-level @TestPropertySource.
            assertThat(properties).isNotNull(); // Ensure the bean was created by the context
            assertThat(properties.getLogging().getIncoming().isEnabled()).isFalse();
            assertThat(properties.getLogging().getIncoming().getMaxBodySize()).isEqualTo(2500);
        }
    }

    @Nested
    @SpringBootTest(classes = {AmqpexAutoConfiguration.class})
    class WithDefaultProperties {

        @Autowired
        private AmqpexProperties properties;

        @Test
        void shouldUseDefaultValuesWithSpringBootMechanism() {
            // Assert default values are used when no external properties are provided
            // by the Spring Boot @ConfigurationProperties mechanism.
            assertThat(properties.getLogging().getIncoming().isEnabled()).isTrue(); // Default value
            assertThat(properties.getLogging().getIncoming().getMaxBodySize()).isEqualTo(1000); // Default value
        }
    }
}