package dev.alubenets.amqpex;

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

    @Test
    void shouldBindCustomValuesUsingDataBinder() {
        var properties = new AmqpexProperties();
        var binder = new DataBinder(properties);
        var propertyValues = new MutablePropertyValues();

        // Prepare a map of property names (as they would appear in application.properties)
        // Note: The keys must match the exact path expected by the binder for nested properties.
        // Spring Boot's @ConfigurationProperties uses relaxed binding rules (e.g., dots, hyphens),
        // but DataBinder needs the exact internal path based on the object structure.
        Map<String, Object> rawProperties = new HashMap<>();
        rawProperties.put("logging.incoming.enabled", "false");
        rawProperties.put("logging.incoming.maxBodySize", "2000");

        // Add values to MutablePropertyValues
        rawProperties.forEach(propertyValues::add);

        // Perform the binding
        binder.bind(propertyValues);

        // Assert the values were set correctly
        assertThat(properties.getLogging().getIncoming().isEnabled()).isFalse();
        assertThat(properties.getLogging().getIncoming().getMaxBodySize()).isEqualTo(2000);
    }

    @Test
    void shouldUseDefaultValuesWhenNotBoundUsingDataBinder() {
        var properties = new AmqpexProperties();
        // No properties are bound, so defaults should remain
        var binder = new DataBinder(properties);
        binder.bind(new MutablePropertyValues()); // Bind empty values

        // Assert default values
        assertThat(properties.getLogging().getIncoming().isEnabled()).isTrue(); // Default
        assertThat(properties.getLogging().getIncoming().getMaxBodySize()).isEqualTo(1000); // Default
    }

    @Test
    void shouldBindCustomValuesUsingDataBinderWithMap() {
        var properties = new AmqpexProperties();
        var binder = new DataBinder(properties);

        // Prepare a map directly
        Map<String, Object> rawProperties = Map.of(
            "logging.incoming.enabled", "true", // Note: Boolean value, DataBinder should handle conversion
            "logging.incoming.maxBodySize", 500  // Note: Integer value
        );

        // Add values to MutablePropertyValues
        var propertyValues = new MutablePropertyValues(rawProperties);

        // Perform the binding
        binder.bind(propertyValues);

        // Assert the values were set correctly
        assertThat(properties.getLogging().getIncoming().isEnabled()).isTrue();
        assertThat(properties.getLogging().getIncoming().getMaxBodySize()).isEqualTo(500);
    }
}