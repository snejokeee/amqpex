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

    /**
     * Tests that custom property values are correctly bound using Spring's DataBinder.
     */
    @Test
    void shouldBindCustomValuesUsingDataBinder() {
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
     * Tests that default property values are used when no properties are bound.
     */
    @Test
    void shouldUseDefaultValuesWhenNotBoundUsingDataBinder() {
        var properties = new AmqpexProperties();
        var binder = new DataBinder(properties);
        binder.bind(new MutablePropertyValues());

        assertThat(properties.getLogging().getIncoming().isEnabled()).isTrue();
        assertThat(properties.getLogging().getIncoming().getMaxBodySize()).isEqualTo(1000);
    }

    /**
     * Tests that custom property values are correctly bound using a direct map approach.
     */
    @Test
    void shouldBindCustomValuesUsingDataBinderWithMap() {
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