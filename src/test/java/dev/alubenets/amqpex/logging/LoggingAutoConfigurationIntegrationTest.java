package dev.alubenets.amqpex.logging;

import dev.alubenets.amqpex.AmqpexAutoConfiguration;
import dev.alubenets.amqpex.AmqpexProperties;
import org.junit.jupiter.api.Test;
import org.springframework.amqp.rabbit.config.SimpleRabbitListenerContainerFactory;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.listener.SimpleMessageListenerContainer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.amqp.RabbitAutoConfiguration; // Import standard RabbitMQ auto-configuration
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import java.lang.reflect.Field;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Integration test for {@link LoggingAutoConfiguration}.
 * Verifies that the ContainerCustomizer correctly adds the
 * IncomingLoggingMessagePostProcessor to listener containers,
 * using the standard RabbitAutoConfiguration setup.
 */
@SpringBootTest(
    classes = {
        AmqpexAutoConfiguration.class,
        RabbitAutoConfiguration.class, // Import standard RabbitMQ auto-configuration
        LoggingAutoConfiguration.class, // Load the auto-configuration under test
        LoggingAutoConfigurationIntegrationTest.TestConfig.class // Provide a test container bean
    }
)
@TestPropertySource(properties = {
    "amqpex.logging.incoming.enabled=true" // Enable the logging feature
})
class LoggingAutoConfigurationIntegrationTest {

    @Autowired
    private AmqpexProperties properties; // Optional: Verify properties are loaded

    // Inject a container bean that will be customized by the auto-config
    @Autowired
    private SimpleMessageListenerContainer testContainer;

    // Mock the ConnectionFactory so the container doesn't try to connect to a real broker
    @MockitoBean
    private ConnectionFactory mockConnectionFactory;

    @Test
    void shouldAddLoggingPostProcessorToContainer() throws Exception {
        // Use reflection to access the private 'afterReceivePostProcessors' field
        Field postProcessorsField = testContainer.getClass()
            .getSuperclass() // AbstractMessageListenerContainer
            .getDeclaredField("afterReceivePostProcessors");
        postProcessorsField.setAccessible(true); // Bypass private modifier

        // Get the value of the field
        // The internal type might be List<MessagePostProcessor> or MessagePostProcessor[]
        Object postProcessorsObject = postProcessorsField.get(testContainer);

        System.out.println("Retrieved afterReceivePostProcessors Object: " + postProcessorsObject); // Debug
        System.out.println("Retrieved Object Class: " + (postProcessorsObject != null ? postProcessorsObject.getClass() : "null")); // Debug

        // Check if the field is not null
        assertThat(postProcessorsObject)
            .as("Container's 'afterReceivePostProcessors' field should not be null. " +
                "This indicates the ContainerCustomizer from LoggingAutoConfiguration was applied by RabbitAutoConfiguration's setup.")
            .isNotNull();

        // Determine the type and assert based on that
        if (postProcessorsObject instanceof Object[] postProcessorsArray) {
            // Check if an instance of IncomingLoggingMessagePostProcessor exists in the array
            assertThat(postProcessorsArray)
                .as("Container's afterReceivePostProcessors array should contain an IncomingLoggingMessagePostProcessor")
                .anySatisfy(processor -> assertThat(processor).isInstanceOf(IncomingLoggingMessagePostProcessor.class));
        } else if (postProcessorsObject instanceof List<?> postProcessorsList) {
            // Check if an instance of IncomingLoggingMessagePostProcessor exists in the list
            assertThat(postProcessorsList)
                .as("Container's afterReceivePostProcessors list should contain an IncomingLoggingMessagePostProcessor")
                .anySatisfy(processor -> assertThat(processor).isInstanceOf(IncomingLoggingMessagePostProcessor.class));
        } else {
            // If it's neither an array nor a list, the internal structure might have changed.
            // This is an unexpected state.
            throw new AssertionError("Unexpected type for 'afterReceivePostProcessors': " + postProcessorsObject.getClass().getName());
        }
    }

    // --- Configuration to create a test container bean ---
    @TestConfiguration
    static class TestConfig {

        @Bean
        public SimpleMessageListenerContainer testContainer(
            SimpleRabbitListenerContainerFactory rabbitListenerContainerFactory) { // Inject the autoconfigured factory

            System.out.println("Creating testContainer using auto-configured factory...");
            // Create the container using the factory that knows how to apply customizers
            SimpleMessageListenerContainer container = rabbitListenerContainerFactory.createListenerContainer();
            System.out.println("Created container via auto-configured factory with HashCode: " + container.hashCode());
            // Optionally prevent auto-startup for tests
            container.setAutoStartup(false);
            return container;
        }
    }
}