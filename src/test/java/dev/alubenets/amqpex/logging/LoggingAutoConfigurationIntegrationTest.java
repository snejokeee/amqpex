package dev.alubenets.amqpex.logging;

import dev.alubenets.amqpex.AmqpexAutoConfiguration;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.amqp.rabbit.config.ContainerCustomizer;
import org.springframework.amqp.rabbit.config.SimpleRabbitListenerContainerFactory;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.rabbit.listener.SimpleMessageListenerContainer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.amqp.RabbitAutoConfiguration;
import org.springframework.boot.autoconfigure.amqp.RabbitTemplateCustomizer;
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
 * This test verifies that incoming and outgoing logging post-processors are applied
 * or not applied based on configuration properties to Spring-managed beans.
 * It leverages the auto-configured RabbitTemplate and creates a test container
 * using the auto-configured factory to test the ContainerCustomizer.
 */
@SpringBootTest(
    classes = {
        AmqpexAutoConfiguration.class,
        RabbitAutoConfiguration.class, // Provides rabbitTemplate, rabbitListenerContainerFactory
        LoggingAutoConfiguration.class,
        LoggingAutoConfigurationIntegrationTest.TestConfig.class
    }
)
class LoggingAutoConfigurationIntegrationTest {

    @MockitoBean
    private ConnectionFactory mockConnectionFactory;

    @TestConfiguration
    static class TestConfig {

        // Create a container that will use the auto-configured factory
        // Spring will look for ContainerCustomizer beans and apply them during this bean's setup
        @Bean("testContainer")
        public SimpleMessageListenerContainer testContainer(
            SimpleRabbitListenerContainerFactory autoConfiguredFactory) { // This will inject the primary factory from RabbitAutoConfiguration
            SimpleMessageListenerContainer container = autoConfiguredFactory.createListenerContainer();
            container.setAutoStartup(false);
            return container;
        }
        // Do not define a RabbitTemplate here, rely on the auto-configured one
    }

    @Nested
    @TestPropertySource(properties = {
        "amqpex.logging.incoming.enabled=true",
        "amqpex.logging.outgoing.enabled=true"
    })
    class BothLoggingEnabled {

        @Autowired(required = false)
        private ContainerCustomizer<SimpleMessageListenerContainer> incomingLoggingContainerCustomizer;

        @Autowired(required = false)
        private RabbitTemplateCustomizer outgoingLoggingRabbitTemplateCustomizer;

        @Autowired
        private SimpleMessageListenerContainer testContainer;

        @Autowired // Rely on the auto-configured bean named 'rabbitTemplate'
        private RabbitTemplate rabbitTemplate;

        @Test
        void shouldCreateIncomingLoggingContainerCustomizerBean() {
            assertThat(incomingLoggingContainerCustomizer).isNotNull();
        }

        @Test
        void shouldCreateOutgoingLoggingRabbitTemplateCustomizerBean() {
            assertThat(outgoingLoggingRabbitTemplateCustomizer).isNotNull();
        }

        @Test
        void shouldAddIncomingLoggingPostProcessorToContainer() throws Exception {
            // Verify that the customizer bean exists
            assertThat(incomingLoggingContainerCustomizer).isNotNull();

            // The customizer should have already been applied by Spring during container creation/setup
            // Check the container's post-processors that were applied by the customizer
            Field postProcessorsField = testContainer.getClass()
                .getSuperclass()
                .getDeclaredField("afterReceivePostProcessors");
            postProcessorsField.setAccessible(true);
            Object postProcessorsObject = postProcessorsField.get(testContainer);

            assertThat(postProcessorsObject)
                .as("Container's 'afterReceivePostProcessors' field should not be null when incoming logging is enabled.")
                .isNotNull();

            if (postProcessorsObject instanceof Object[] postProcessorsArray) {
                assertThat(postProcessorsArray)
                    .as("Container's afterReceivePostProcessors array should contain an IncomingMessageLogger when enabled")
                    .anySatisfy(processor -> assertThat(processor).isInstanceOf(IncomingMessageLogger.class));
            } else if (postProcessorsObject instanceof List<?> postProcessorsList) {
                assertThat(postProcessorsList)
                    .as("Container's afterReceivePostProcessors list should contain an IncomingMessageLogger when enabled")
                    .anySatisfy(processor -> assertThat(processor).isInstanceOf(IncomingMessageLogger.class));
            } else {
                throw new AssertionError("Unexpected type for 'afterReceivePostProcessors': " + postProcessorsObject.getClass().getName());
            }
        }

        @Test
        void shouldAddOutgoingLoggingPostProcessorToRabbitTemplate() throws Exception {
            // Verify that the customizer bean exists
            assertThat(outgoingLoggingRabbitTemplateCustomizer).isNotNull();

            // The customizer should have already been applied by Spring during template creation/setup
            // Check the template's post-processors that were applied by the customizer
            Field postProcessorsField = rabbitTemplate.getClass()
                .getDeclaredField("beforePublishPostProcessors");
            postProcessorsField.setAccessible(true);
            Object postProcessorsObject = postProcessorsField.get(rabbitTemplate);

            assertThat(postProcessorsObject)
                .as("RabbitTemplate's 'beforePublishPostProcessors' field should not be null when outgoing logging is enabled.")
                .isNotNull();

            if (postProcessorsObject instanceof Object[] postProcessorsArray) {
                assertThat(postProcessorsArray)
                    .as("RabbitTemplate's beforePublishPostProcessors array should contain an OutgoingMessageLogger when enabled")
                    .anySatisfy(processor -> assertThat(processor).isInstanceOf(OutgoingMessageLogger.class));
            } else if (postProcessorsObject instanceof List<?> postProcessorsList) {
                assertThat(postProcessorsList)
                    .as("RabbitTemplate's beforePublishPostProcessors list should contain an OutgoingMessageLogger when enabled")
                    .anySatisfy(processor -> assertThat(processor).isInstanceOf(OutgoingMessageLogger.class));
            } else {
                throw new AssertionError("Unexpected type for 'beforePublishPostProcessors': " + postProcessorsObject.getClass().getName());
            }
        }
    }

    @Nested
    @TestPropertySource(properties = {
        "amqpex.logging.incoming.enabled=false",
        "amqpex.logging.outgoing.enabled=false"
    })
    class BothLoggingDisabled {

        @Autowired(required = false)
        private ContainerCustomizer<SimpleMessageListenerContainer> incomingLoggingContainerCustomizer;

        @Autowired(required = false)
        private RabbitTemplateCustomizer outgoingLoggingRabbitTemplateCustomizer;

        @Autowired
        private SimpleMessageListenerContainer testContainer;

        @Autowired
        private RabbitTemplate rabbitTemplate;

        @Test
        void shouldNotCreateIncomingLoggingContainerCustomizerBean() {
            assertThat(incomingLoggingContainerCustomizer).isNull();
        }

        @Test
        void shouldNotCreateOutgoingLoggingRabbitTemplateCustomizerBean() {
            assertThat(outgoingLoggingRabbitTemplateCustomizer).isNull();
        }

        @Test
        void shouldNotAddIncomingLoggingPostProcessorToContainer() throws Exception {
            // Verify that the customizer bean does not exist
            assertThat(incomingLoggingContainerCustomizer).isNull();

            // Check the container's post-processors (should not contain our logger)
            Field postProcessorsField = testContainer.getClass()
                .getSuperclass()
                .getDeclaredField("afterReceivePostProcessors");
            postProcessorsField.setAccessible(true);
            Object postProcessorsObject = postProcessorsField.get(testContainer);

            // The field can be null if no post-processors are configured at all
            if (postProcessorsObject != null) {
                if (postProcessorsObject instanceof Object[] postProcessorsArray) {
                    assertThat(postProcessorsArray)
                        .as("Container's afterReceivePostProcessors array should not contain an IncomingMessageLogger when disabled")
                        .noneSatisfy(processor -> assertThat(processor).isInstanceOf(IncomingMessageLogger.class));
                } else if (postProcessorsObject instanceof List<?> postProcessorsList) {
                    assertThat(postProcessorsList)
                        .as("Container's afterReceivePostProcessors list should not contain an IncomingMessageLogger when disabled")
                        .noneSatisfy(processor -> assertThat(processor).isInstanceOf(IncomingMessageLogger.class));
                } else {
                    throw new AssertionError("Unexpected type for 'afterReceivePostProcessors': " + postProcessorsObject.getClass().getName());
                }
            } // If postProcessorsObject is null, the condition is implicitly satisfied (no processors, so no our logger)
        }

        @Test
        void shouldNotAddOutgoingLoggingPostProcessorToRabbitTemplate() throws Exception {
            // Verify that the customizer bean does not exist
            assertThat(outgoingLoggingRabbitTemplateCustomizer).isNull();

            // Check the template's post-processors (should not contain our logger)
            Field postProcessorsField = rabbitTemplate.getClass()
                .getDeclaredField("beforePublishPostProcessors");
            postProcessorsField.setAccessible(true);
            Object postProcessorsObject = postProcessorsField.get(rabbitTemplate);

            // The field can be null if no post-processors are configured at all
            if (postProcessorsObject != null) {
                if (postProcessorsObject instanceof Object[] postProcessorsArray) {
                    assertThat(postProcessorsArray)
                        .as("RabbitTemplate's beforePublishPostProcessors array should not contain an OutgoingMessageLogger when disabled")
                        .noneSatisfy(processor -> assertThat(processor).isInstanceOf(OutgoingMessageLogger.class));
                } else if (postProcessorsObject instanceof List<?> postProcessorsList) {
                    assertThat(postProcessorsList)
                        .as("RabbitTemplate's beforePublishPostProcessors list should not contain an OutgoingMessageLogger when disabled")
                        .noneSatisfy(processor -> assertThat(processor).isInstanceOf(OutgoingMessageLogger.class));
                } else {
                    throw new AssertionError("Unexpected type for 'beforePublishPostProcessors': " + postProcessorsObject.getClass().getName());
                }
            } // If postProcessorsObject is null, the condition is implicitly satisfied (no processors, so no our logger)
        }
    }

    @Nested
    @TestPropertySource(properties = {
        "amqpex.logging.incoming.enabled=true",
        "amqpex.logging.outgoing.enabled=false"
    })
    class IncomingLoggingEnabledOutgoingDisabled {

        @Autowired(required = false)
        private ContainerCustomizer<SimpleMessageListenerContainer> incomingLoggingContainerCustomizer;

        @Autowired(required = false)
        private RabbitTemplateCustomizer outgoingLoggingRabbitTemplateCustomizer;

        @Autowired
        private SimpleMessageListenerContainer testContainer;

        @Autowired
        private RabbitTemplate rabbitTemplate;

        @Test
        void shouldCreateIncomingLoggingContainerCustomizerBean() {
            assertThat(incomingLoggingContainerCustomizer).isNotNull();
        }

        @Test
        void shouldNotCreateOutgoingLoggingRabbitTemplateCustomizerBean() {
            assertThat(outgoingLoggingRabbitTemplateCustomizer).isNull();
        }

        @Test
        void shouldAddIncomingLoggingPostProcessorToContainer() throws Exception {
            // Verify that the incoming customizer bean exists and outgoing does not
            assertThat(incomingLoggingContainerCustomizer).isNotNull();
            assertThat(outgoingLoggingRabbitTemplateCustomizer).isNull();

            // Check the container's post-processors (should contain our logger)
            Field postProcessorsField = testContainer.getClass()
                .getSuperclass()
                .getDeclaredField("afterReceivePostProcessors");
            postProcessorsField.setAccessible(true);
            Object postProcessorsObject = postProcessorsField.get(testContainer);

            assertThat(postProcessorsObject)
                .as("Container's 'afterReceivePostProcessors' field should not be null when incoming logging is enabled.")
                .isNotNull();

            if (postProcessorsObject instanceof Object[] postProcessorsArray) {
                assertThat(postProcessorsArray)
                    .as("Container's afterReceivePostProcessors array should contain an IncomingMessageLogger when enabled")
                    .anySatisfy(processor -> assertThat(processor).isInstanceOf(IncomingMessageLogger.class));
            } else if (postProcessorsObject instanceof List<?> postProcessorsList) {
                assertThat(postProcessorsList)
                    .as("Container's afterReceivePostProcessors list should contain an IncomingMessageLogger when enabled")
                    .anySatisfy(processor -> assertThat(processor).isInstanceOf(IncomingMessageLogger.class));
            } else {
                throw new AssertionError("Unexpected type for 'afterReceivePostProcessors': " + postProcessorsObject.getClass().getName());
            }
        }

        @Test
        void shouldNotAddOutgoingLoggingPostProcessorToRabbitTemplate() throws Exception {
            // Verify that the outgoing customizer bean does not exist
            assertThat(outgoingLoggingRabbitTemplateCustomizer).isNull();

            // Check the template's post-processors (should not contain our logger)
            Field postProcessorsField = rabbitTemplate.getClass()
                .getDeclaredField("beforePublishPostProcessors");
            postProcessorsField.setAccessible(true);
            Object postProcessorsObject = postProcessorsField.get(rabbitTemplate);

            // The field can be null if no post-processors are configured at all
            if (postProcessorsObject != null) {
                if (postProcessorsObject instanceof Object[] postProcessorsArray) {
                    assertThat(postProcessorsArray)
                        .as("RabbitTemplate's beforePublishPostProcessors array should not contain an OutgoingMessageLogger when disabled")
                        .noneSatisfy(processor -> assertThat(processor).isInstanceOf(OutgoingMessageLogger.class));
                } else if (postProcessorsObject instanceof List<?> postProcessorsList) {
                    assertThat(postProcessorsList)
                        .as("RabbitTemplate's beforePublishPostProcessors list should not contain an OutgoingMessageLogger when disabled")
                        .noneSatisfy(processor -> assertThat(processor).isInstanceOf(OutgoingMessageLogger.class));
                } else {
                    throw new AssertionError("Unexpected type for 'beforePublishPostProcessors': " + postProcessorsObject.getClass().getName());
                }
            } // If postProcessorsObject is null, the condition is implicitly satisfied (no processors, so no our logger)
        }
    }

    @Nested
    @TestPropertySource(properties = {
        "amqpex.logging.incoming.enabled=false",
        "amqpex.logging.outgoing.enabled=true"
    })
    class IncomingLoggingDisabledOutgoingEnabled {

        @Autowired(required = false)
        private ContainerCustomizer<SimpleMessageListenerContainer> incomingLoggingContainerCustomizer;

        @Autowired(required = false)
        private RabbitTemplateCustomizer outgoingLoggingRabbitTemplateCustomizer;

        @Autowired
        private SimpleMessageListenerContainer testContainer;

        @Autowired
        private RabbitTemplate rabbitTemplate;

        @Test
        void shouldNotCreateIncomingLoggingContainerCustomizerBean() {
            assertThat(incomingLoggingContainerCustomizer).isNull();
        }

        @Test
        void shouldCreateOutgoingLoggingRabbitTemplateCustomizerBean() {
            assertThat(outgoingLoggingRabbitTemplateCustomizer).isNotNull();
        }

        @Test
        void shouldNotAddIncomingLoggingPostProcessorToContainer() throws Exception {
            // Verify that the incoming customizer bean does not exist
            assertThat(incomingLoggingContainerCustomizer).isNull();

            // Check the container's post-processors (should not contain our logger)
            Field postProcessorsField = testContainer.getClass()
                .getSuperclass()
                .getDeclaredField("afterReceivePostProcessors");
            postProcessorsField.setAccessible(true);
            Object postProcessorsObject = postProcessorsField.get(testContainer);

            // The field can be null if no post-processors are configured at all
            if (postProcessorsObject != null) {
                if (postProcessorsObject instanceof Object[] postProcessorsArray) {
                    assertThat(postProcessorsArray)
                        .as("Container's afterReceivePostProcessors array should not contain an IncomingMessageLogger when disabled")
                        .noneSatisfy(processor -> assertThat(processor).isInstanceOf(IncomingMessageLogger.class));
                } else if (postProcessorsObject instanceof List<?> postProcessorsList) {
                    assertThat(postProcessorsList)
                        .as("Container's afterReceivePostProcessors list should not contain an IncomingMessageLogger when disabled")
                        .noneSatisfy(processor -> assertThat(processor).isInstanceOf(IncomingMessageLogger.class));
                } else {
                    throw new AssertionError("Unexpected type for 'afterReceivePostProcessors': " + postProcessorsObject.getClass().getName());
                }
            } // If postProcessorsObject is null, the condition is implicitly satisfied (no processors, so no our logger)
        }

        @Test
        void shouldAddOutgoingLoggingPostProcessorToRabbitTemplate() throws Exception {
            // Verify that the outgoing customizer bean exists
            assertThat(outgoingLoggingRabbitTemplateCustomizer).isNotNull();

            // Check the template's post-processors (should contain our logger)
            Field postProcessorsField = rabbitTemplate.getClass()
                .getDeclaredField("beforePublishPostProcessors");
            postProcessorsField.setAccessible(true);
            Object postProcessorsObject = postProcessorsField.get(rabbitTemplate);

            assertThat(postProcessorsObject)
                .as("RabbitTemplate's 'beforePublishPostProcessors' field should not be null when outgoing logging is enabled.")
                .isNotNull();

            if (postProcessorsObject instanceof Object[] postProcessorsArray) {
                assertThat(postProcessorsArray)
                    .as("RabbitTemplate's beforePublishPostProcessors array should contain an OutgoingMessageLogger when enabled")
                    .anySatisfy(processor -> assertThat(processor).isInstanceOf(OutgoingMessageLogger.class));
            } else if (postProcessorsObject instanceof List<?> postProcessorsList) {
                assertThat(postProcessorsList)
                    .as("RabbitTemplate's beforePublishPostProcessors list should contain an OutgoingMessageLogger when enabled")
                    .anySatisfy(processor -> assertThat(processor).isInstanceOf(OutgoingMessageLogger.class));
            } else {
                throw new AssertionError("Unexpected type for 'beforePublishPostProcessors': " + postProcessorsObject.getClass().getName());
            }
        }
    }
}