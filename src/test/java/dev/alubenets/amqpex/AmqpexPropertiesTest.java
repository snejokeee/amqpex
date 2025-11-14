package dev.alubenets.amqpex;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.MutablePropertyValues;
import org.springframework.validation.DataBinder;

import java.util.HashMap;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Unit test for {@link AmqpexProperties} using Spring's DataBinder directly.
 * This tests the underlying property binding mechanism but bypasses the full
 * @ConfigurationProperties Spring Boot infrastructure.
 */
class AmqpexPropertiesTest {

    @Nested
    class DefaultValues {
        /**
         * Tests that default values are applied for incoming configuration when properties are not bound.
         * Verifies that enabled is true and maxBodySize is 1000 by default.
         */
        @Test
        void shouldUseDefaultValuesForIncomingWhenNotBoundUsingDataBinder() {
            var properties = new AmqpexProperties();
            var binder = new DataBinder(properties);
            binder.bind(new MutablePropertyValues());

            assertThat(properties.getLogging().getIncoming().isEnabled()).isTrue();
            assertThat(properties.getLogging().getIncoming().getMaxBodySize()).isEqualTo(1000);
        }

        /**
         * Tests that default values are applied for outgoing configuration when properties are not bound.
         * Verifies that enabled is true and maxBodySize is 1000 by default.
         */
        @Test
        void shouldUseDefaultValuesForOutgoingWhenNotBoundUsingDataBinder() {
            var properties = new AmqpexProperties();
            var binder = new DataBinder(properties);
            binder.bind(new MutablePropertyValues());

            assertThat(properties.getLogging().getOutgoing().isEnabled()).isTrue();
            assertThat(properties.getLogging().getOutgoing().getMaxBodySize()).isEqualTo(1000);
        }
    }

    @Nested
    class IncomingConfigurationBinding {
        /**
         * Tests that custom values are correctly bound for incoming configuration when using DataBinder.
         * Verifies that enabled and maxBodySize properties are set according to the provided values.
         */
        @Test
        void shouldBindCustomValuesForIncomingUsingDataBinder() {
            var properties = new AmqpexProperties();
            var binder = new DataBinder(properties);
            var propertyValues = new MutablePropertyValues();

            Map<String, Object> rawProperties = new HashMap<>();
            rawProperties.put("logging.incoming.enabled", "false");
            rawProperties.put("logging.incoming.maxBodySize", "2000");

            rawProperties.forEach(propertyValues::add);

            binder.bind(propertyValues);

            assertThat(properties.getLogging().getIncoming().isEnabled()).isFalse();
            assertThat(properties.getLogging().getIncoming().getMaxBodySize()).isEqualTo(2000);
        }

        /**
         * Tests that custom values are correctly bound for incoming configuration when using DataBinder with a Map.
         * Verifies that enabled and maxBodySize properties are set according to the provided values.
         */
        @Test
        void shouldBindCustomValuesForIncomingUsingDataBinderWithMap() {
            var properties = new AmqpexProperties();
            var binder = new DataBinder(properties);

            Map<String, Object> rawProperties = Map.of(
                "logging.incoming.enabled", "true",
                "logging.incoming.maxBodySize", 500
            );

            var propertyValues = new MutablePropertyValues(rawProperties);

            binder.bind(propertyValues);

            assertThat(properties.getLogging().getIncoming().isEnabled()).isTrue();
            assertThat(properties.getLogging().getIncoming().getMaxBodySize()).isEqualTo(500);
        }
    }

    @Nested
    class OutgoingConfigurationBinding {
        /**
         * Tests that custom values are correctly bound for outgoing configuration when using DataBinder.
         * Verifies that enabled and maxBodySize properties are set according to the provided values.
         */
        @Test
        void shouldBindCustomValuesForOutgoingUsingDataBinder() {
            var properties = new AmqpexProperties();
            var binder = new DataBinder(properties);
            var propertyValues = new MutablePropertyValues();

            Map<String, Object> rawProperties = new HashMap<>();
            rawProperties.put("logging.outgoing.enabled", "false");
            rawProperties.put("logging.outgoing.maxBodySize", "2500");

            rawProperties.forEach(propertyValues::add);

            binder.bind(propertyValues);

            assertThat(properties.getLogging().getOutgoing().isEnabled()).isFalse();
            assertThat(properties.getLogging().getOutgoing().getMaxBodySize()).isEqualTo(2500);
        }

        /**
         * Tests that custom values are correctly bound for outgoing configuration when using DataBinder with a Map.
         * Verifies that enabled and maxBodySize properties are set according to the provided values.
         */
        @Test
        void shouldBindCustomValuesForOutgoingUsingDataBinderWithMap() {
            var properties = new AmqpexProperties();
            var binder = new DataBinder(properties);

            Map<String, Object> rawProperties = Map.of(
                "logging.outgoing.enabled", "false",
                "logging.outgoing.maxBodySize", 750
            );

            var propertyValues = new MutablePropertyValues(rawProperties);

            binder.bind(propertyValues);

            assertThat(properties.getLogging().getOutgoing().isEnabled()).isFalse();
            assertThat(properties.getLogging().getOutgoing().getMaxBodySize()).isEqualTo(750);
        }
    }

    @Nested
    class BothDirectionsConfiguration {
        /**
         * Tests that custom values are correctly bound for both incoming and outgoing configurations when using DataBinder with a Map.
         * Verifies that properties for both directions are set according to the provided values.
         */
        @Test
        void shouldBindCustomValuesForBothDirectionsUsingDataBinderWithMap() {
            var properties = new AmqpexProperties();
            var binder = new DataBinder(properties);

            Map<String, Object> rawProperties = Map.of(
                "logging.incoming.enabled", "true",
                "logging.incoming.maxBodySize", 500,
                "logging.outgoing.enabled", "false",
                "logging.outgoing.maxBodySize", 750
            );

            var propertyValues = new MutablePropertyValues(rawProperties);

            binder.bind(propertyValues);

            assertThat(properties.getLogging().getIncoming().isEnabled()).isTrue();
            assertThat(properties.getLogging().getIncoming().getMaxBodySize()).isEqualTo(500);
            assertThat(properties.getLogging().getOutgoing().isEnabled()).isFalse();
            assertThat(properties.getLogging().getOutgoing().getMaxBodySize()).isEqualTo(750);
        }
    }

    @Nested
    class ValueChangeSupport {
        /**
         * Tests that property values can be changed at runtime after binding.
         * Verifies that properties can be updated after initial binding and values are correctly reflected.
         */
        @Test
        void shouldSupportRuntimeValueChanges() {
            var properties = new AmqpexProperties();
            var binder = new DataBinder(properties);

            Map<String, Object> rawProperties = Map.of(
                "logging.incoming.maxBodySize", 1500
            );

            var propertyValues = new MutablePropertyValues(rawProperties);
            binder.bind(propertyValues);

            // Verify initial state
            assertThat(properties.getLogging().getIncoming().getMaxBodySize()).isEqualTo(1500);

            // Change value at runtime
            properties.getLogging().getIncoming().setMaxBodySize(2000);

            // Verify new value
            assertThat(properties.getLogging().getIncoming().getMaxBodySize()).isEqualTo(2000);
        }
    }

    @Nested
    class BooleanProperties {
        /**
         * Tests that boolean properties are correctly handled during binding.
         * Verifies that true and false boolean values are properly parsed and set.
         */
        @Test
        void shouldHandleBooleanPropertiesCorrectly() {
            var properties = new AmqpexProperties();
            var binder = new DataBinder(properties);

            Map<String, Object> rawProperties = Map.of(
                "logging.incoming.enabled", "false",
                "logging.outgoing.enabled", "true"
            );

            var propertyValues = new MutablePropertyValues(rawProperties);

            binder.bind(propertyValues);

            assertThat(properties.getLogging().getIncoming().isEnabled()).isFalse();
            assertThat(properties.getLogging().getOutgoing().isEnabled()).isTrue();
        }
    }
}