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
        RabbitAutoConfiguration.class,
        LoggingAutoConfiguration.class,
        LoggingAutoConfigurationIntegrationTest.TestConfig.class
    }
)
class LoggingAutoConfigurationIntegrationTest {

    @MockitoBean
    private ConnectionFactory mockConnectionFactory;

    @TestConfiguration
    static class TestConfig {

        @Bean("testContainer")
        public SimpleMessageListenerContainer testContainer(
            SimpleRabbitListenerContainerFactory autoConfiguredFactory) {
            SimpleMessageListenerContainer container = autoConfiguredFactory.createListenerContainer();
            container.setAutoStartup(false);
            return container;
        }
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

        @Autowired
        private RabbitTemplate rabbitTemplate;

        /**
         * Tests that the incoming logging container customizer bean is created when the incoming logging feature is enabled.
         */
        @Test
        void shouldCreateIncomingLoggingContainerCustomizerBean() {
            assertThat(incomingLoggingContainerCustomizer).isNotNull();
        }

        /**
         * Tests that the outgoing logging rabbit template customizer bean is created when the outgoing logging feature is enabled.
         */
        @Test
        void shouldCreateOutgoingLoggingRabbitTemplateCustomizerBean() {
            assertThat(outgoingLoggingRabbitTemplateCustomizer).isNotNull();
        }

        /**
         * Tests that the incoming message logging post-processor is added to the message listener container.
         */
        @Test
        void shouldAddIncomingLoggingPostProcessorToContainer() throws Exception {
            assertThat(incomingLoggingContainerCustomizer).isNotNull();

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

        /**
         * Tests that the outgoing message logging post-processor is added to the RabbitTemplate.
         */
        @Test
        void shouldAddOutgoingLoggingPostProcessorToRabbitTemplate() throws Exception {
            assertThat(outgoingLoggingRabbitTemplateCustomizer).isNotNull();

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

        /**
         * Tests that the incoming logging container customizer bean is not created when the incoming logging feature is disabled.
         */
        @Test
        void shouldNotCreateIncomingLoggingContainerCustomizerBean() {
            assertThat(incomingLoggingContainerCustomizer).isNull();
        }

        /**
         * Tests that the outgoing logging rabbit template customizer bean is not created when the outgoing logging feature is disabled.
         */
        @Test
        void shouldNotCreateOutgoingLoggingRabbitTemplateCustomizerBean() {
            assertThat(outgoingLoggingRabbitTemplateCustomizer).isNull();
        }

        /**
         * Tests that the incoming message logging post-processor is not added to the message listener container when logging is disabled.
         */
        @Test
        void shouldNotAddIncomingLoggingPostProcessorToContainer() throws Exception {
            assertThat(incomingLoggingContainerCustomizer).isNull();

            Field postProcessorsField = testContainer.getClass()
                .getSuperclass()
                .getDeclaredField("afterReceivePostProcessors");
            postProcessorsField.setAccessible(true);
            Object postProcessorsObject = postProcessorsField.get(testContainer);

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
            }
        }

        @Test
        void shouldNotAddOutgoingLoggingPostProcessorToRabbitTemplate() throws Exception {
            assertThat(outgoingLoggingRabbitTemplateCustomizer).isNull();

            Field postProcessorsField = rabbitTemplate.getClass()
                .getDeclaredField("beforePublishPostProcessors");
            postProcessorsField.setAccessible(true);
            Object postProcessorsObject = postProcessorsField.get(rabbitTemplate);

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
            }
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

        /**
         * Tests that the incoming logging container customizer bean is created when the incoming logging feature is enabled.
         */
        @Test
        void shouldCreateIncomingLoggingContainerCustomizerBean() {
            assertThat(incomingLoggingContainerCustomizer).isNotNull();
        }

        /**
         * Tests that the outgoing logging rabbit template customizer bean is not created when the outgoing logging feature is disabled.
         */
        @Test
        void shouldNotCreateOutgoingLoggingRabbitTemplateCustomizerBean() {
            assertThat(outgoingLoggingRabbitTemplateCustomizer).isNull();
        }

        /**
         * Tests that the incoming message logging post-processor is added to the message listener container when incoming logging is enabled.
         */
        @Test
        void shouldAddIncomingLoggingPostProcessorToContainer() throws Exception {
            assertThat(incomingLoggingContainerCustomizer).isNotNull();
            assertThat(outgoingLoggingRabbitTemplateCustomizer).isNull();

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
            assertThat(outgoingLoggingRabbitTemplateCustomizer).isNull();

            Field postProcessorsField = rabbitTemplate.getClass()
                .getDeclaredField("beforePublishPostProcessors");
            postProcessorsField.setAccessible(true);
            Object postProcessorsObject = postProcessorsField.get(rabbitTemplate);

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
            }
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

        /**
         * Tests that the incoming logging container customizer bean is not created when the incoming logging feature is disabled.
         */
        @Test
        void shouldNotCreateIncomingLoggingContainerCustomizerBean() {
            assertThat(incomingLoggingContainerCustomizer).isNull();
        }

        /**
         * Tests that the outgoing logging rabbit template customizer bean is created when the outgoing logging feature is enabled.
         */
        @Test
        void shouldCreateOutgoingLoggingRabbitTemplateCustomizerBean() {
            assertThat(outgoingLoggingRabbitTemplateCustomizer).isNotNull();
        }

        /**
         * Tests that the incoming message logging post-processor is not added to the message listener container when incoming logging is disabled.
         */
        @Test
        void shouldNotAddIncomingLoggingPostProcessorToContainer() throws Exception {
            assertThat(incomingLoggingContainerCustomizer).isNull();

            Field postProcessorsField = testContainer.getClass()
                .getSuperclass()
                .getDeclaredField("afterReceivePostProcessors");
            postProcessorsField.setAccessible(true);
            Object postProcessorsObject = postProcessorsField.get(testContainer);

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
            }
        }

        /**
         * Tests that the outgoing message logging post-processor is added to the RabbitTemplate when outgoing logging is enabled.
         */
        @Test
        void shouldAddOutgoingLoggingPostProcessorToRabbitTemplate() throws Exception {
            assertThat(outgoingLoggingRabbitTemplateCustomizer).isNotNull();

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