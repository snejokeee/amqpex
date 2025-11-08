package dev.alubenets.amqpex.logging;

import dev.alubenets.amqpex.AmqpexAutoConfiguration;
import dev.alubenets.amqpex.AmqpexProperties;
import org.junit.jupiter.api.Test;
import org.springframework.amqp.rabbit.config.SimpleRabbitListenerContainerFactory;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.listener.SimpleMessageListenerContainer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.amqp.RabbitAutoConfiguration;
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
        RabbitAutoConfiguration.class,
        LoggingAutoConfiguration.class,
        LoggingAutoConfigurationIntegrationTest.TestConfig.class
    }
)
@TestPropertySource(properties = {
    "amqpex.logging.incoming.enabled=true"
})
class LoggingAutoConfigurationIntegrationTest {

    @Autowired
    private AmqpexProperties properties;

    @Autowired
    private SimpleMessageListenerContainer testContainer;

    @MockitoBean
    private ConnectionFactory mockConnectionFactory;

    /**
     * Tests that the logging post processor is correctly added to the message listener container.
     */
    @Test
    void shouldAddLoggingPostProcessorToContainer() throws Exception {
        Field postProcessorsField = testContainer.getClass()
            .getSuperclass()
            .getDeclaredField("afterReceivePostProcessors");
        postProcessorsField.setAccessible(true);

        Object postProcessorsObject = postProcessorsField.get(testContainer);

        assertThat(postProcessorsObject)
            .as("Container's 'afterReceivePostProcessors' field should not be null. " +
                "This indicates the ContainerCustomizer from LoggingAutoConfiguration was applied by RabbitAutoConfiguration's setup.")
            .isNotNull();

        if (postProcessorsObject instanceof Object[] postProcessorsArray) {
            assertThat(postProcessorsArray)
                .as("Container's afterReceivePostProcessors array should contain an IncomingLoggingMessagePostProcessor")
                .anySatisfy(processor -> assertThat(processor).isInstanceOf(IncomingLoggingMessagePostProcessor.class));
        } else if (postProcessorsObject instanceof List<?> postProcessorsList) {
            assertThat(postProcessorsList)
                .as("Container's afterReceivePostProcessors list should contain an IncomingLoggingMessagePostProcessor")
                .anySatisfy(processor -> assertThat(processor).isInstanceOf(IncomingLoggingMessagePostProcessor.class));
        } else {
            throw new AssertionError("Unexpected type for 'afterReceivePostProcessors': " + postProcessorsObject.getClass().getName());
        }
    }

    @TestConfiguration
    static class TestConfig {

        @Bean
        public SimpleMessageListenerContainer testContainer(
            SimpleRabbitListenerContainerFactory rabbitListenerContainerFactory) {

            SimpleMessageListenerContainer container = rabbitListenerContainerFactory.createListenerContainer();
            container.setAutoStartup(false);
            return container;
        }
    }
}