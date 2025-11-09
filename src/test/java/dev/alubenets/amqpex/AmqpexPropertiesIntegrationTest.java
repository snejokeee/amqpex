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
        "amqpex.logging.incoming.enabled=false",
        "amqpex.logging.incoming.max-body-size=2500"
    })
    class WithCustomIncomingProperties {

        @Autowired
        private AmqpexProperties properties;

        /**
         * Tests that custom properties are correctly bound using the Spring Boot mechanism for incoming configuration.
         */
        @Test
        void shouldBindCustomIncomingPropertiesCorrectlyWithSpringBootMechanism() {
            assertThat(properties).isNotNull();
            assertThat(properties.getLogging().getIncoming().isEnabled()).isFalse();
            assertThat(properties.getLogging().getIncoming().getMaxBodySize()).isEqualTo(2500);
        }
    }

    @Nested
    @SpringBootTest(classes = {AmqpexAutoConfiguration.class})
    @TestPropertySource(properties = {
        "amqpex.logging.outgoing.enabled=false",
        "amqpex.logging.outgoing.max-body-size=3000"
    })
    class WithCustomOutgoingProperties {

        @Autowired
        private AmqpexProperties properties;

        /**
         * Tests that custom properties are correctly bound using the Spring Boot mechanism for outgoing configuration.
         */
        @Test
        void shouldBindCustomOutgoingPropertiesCorrectlyWithSpringBootMechanism() {
            assertThat(properties).isNotNull();
            assertThat(properties.getLogging().getOutgoing().isEnabled()).isFalse();
            assertThat(properties.getLogging().getOutgoing().getMaxBodySize()).isEqualTo(3000);
        }
    }

    @Nested
    @SpringBootTest(classes = {AmqpexAutoConfiguration.class})
    @TestPropertySource(properties = {
        "amqpex.logging.incoming.enabled=false",
        "amqpex.logging.incoming.max-body-size=2500",
        "amqpex.logging.outgoing.enabled=true",
        "amqpex.logging.outgoing.max-body-size=3000"
    })
    class WithCustomBothDirectionsProperties {

        @Autowired
        private AmqpexProperties properties;

        /**
         * Tests that custom properties are correctly bound for both incoming and outgoing configurations.
         */
        @Test
        void shouldBindCustomPropertiesForBothDirectionsCorrectlyWithSpringBootMechanism() {
            assertThat(properties).isNotNull();
            assertThat(properties.getLogging().getIncoming().isEnabled()).isFalse();
            assertThat(properties.getLogging().getIncoming().getMaxBodySize()).isEqualTo(2500);
            assertThat(properties.getLogging().getOutgoing().isEnabled()).isTrue();
            assertThat(properties.getLogging().getOutgoing().getMaxBodySize()).isEqualTo(3000);
        }
    }

    @Nested
    @SpringBootTest(classes = {AmqpexAutoConfiguration.class})
    class WithDefaultProperties {

        @Autowired
        private AmqpexProperties properties;

        /**
         * Tests that default values are used when no custom properties are provided.
         */
        @Test
        void shouldUseDefaultValuesWithSpringBootMechanism() {
            assertThat(properties.getLogging().getIncoming().isEnabled()).isTrue();
            assertThat(properties.getLogging().getIncoming().getMaxBodySize()).isEqualTo(1000);
            assertThat(properties.getLogging().getOutgoing().isEnabled()).isTrue();
            assertThat(properties.getLogging().getOutgoing().getMaxBodySize()).isEqualTo(1000);
        }
    }
}