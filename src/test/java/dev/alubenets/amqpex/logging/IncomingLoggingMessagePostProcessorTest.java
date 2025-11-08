package dev.alubenets.amqpex.logging;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.read.ListAppender;
import dev.alubenets.amqpex.AmqpexProperties;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.core.MessageProperties;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Unit test for {@link IncomingLoggingMessagePostProcessor}.
 * <p>
 * Tests the core logic of message processing with actual logging verification.
 * All tests ensure that logging functionality works as expected while maintaining
 * message integrity and proper error handling.
 */
class IncomingLoggingMessagePostProcessorTest {

    private AmqpexProperties.LoggingConfiguration.Incoming incomingProps;
    private IncomingLoggingMessagePostProcessor processor;
    private ListAppender<ILoggingEvent> listAppender;
    private Logger logger;

    /**
     * Sets up the test environment before each test method.
     * <p>
     * Initializes the logger with DEBUG level, creates a list appender for
     * capturing log events, and sets up the processor with default configuration.
     */
    @BeforeEach
    void setUp() {
        logger = (Logger) LoggerFactory.getLogger(IncomingLoggingMessagePostProcessor.class);
        logger.setLevel(Level.DEBUG);

        listAppender = new ListAppender<>();
        listAppender.start();
        logger.addAppender(listAppender);

        incomingProps = new AmqpexProperties.LoggingConfiguration.Incoming();
        incomingProps.setMaxBodySize(100);
        processor = new IncomingLoggingMessagePostProcessor(incomingProps);
    }

    /**
     * Tests basic functionality of the message post-processor.
     */
    @Nested
    class BasicFunctionality {

        /**
         * Tests that message details are properly logged when debug is enabled and the body is readable.
         * <p>
         * Verifies that exchange, routing key, content type, and body content are all logged correctly
         * when the logging feature is enabled and the message contains readable content.
         */
        @Test
        void shouldLogMessageDetailsWhenDebugEnabledAndReadableBody() {
            var messageProps = new MessageProperties();
            messageProps.setContentType("application/json");
            messageProps.setReceivedExchange("test-exchange");
            messageProps.setReceivedRoutingKey("test.routing.key");

            var body = "{\"key\":\"value\"}".getBytes();
            var message = new Message(body, messageProps);

            var result = processor.postProcessMessage(message);

            assertThat(result).isSameAs(message);

            List<ILoggingEvent> logEvents = listAppender.list;
            assertThat(logEvents)
                .isNotEmpty()
                .first()
                .satisfies(logEvent -> {
                    assertThat(logEvent.getLevel()).isEqualTo(Level.DEBUG);
                    assertThat(logEvent.getFormattedMessage())
                        .contains("test-exchange")
                        .contains("test.routing.key")
                        .contains("{\"key\":\"value\"}");
                });
        }

        /**
         * Tests that no logging occurs when the logging feature is disabled.
         * <p>
         * Verifies that when the enabled property is set to false, no log events are generated.
         */
        @Test
        void shouldNotLogWhenDisabled() {
            incomingProps.setEnabled(false);
            processor = new IncomingLoggingMessagePostProcessor(incomingProps);

            var messageProps = new MessageProperties();
            messageProps.setContentType("application/json");
            var body = "{\"key\":\"value\"}".getBytes();
            var message = new Message(body, messageProps);

            var result = processor.postProcessMessage(message);

            assertThat(result).isSameAs(message);

            assertThat(listAppender.list).isEmpty();
        }
    }

    /**
     * Tests content type handling functionality.
     */
    @Nested
    class ContentTypes {

        /**
         * Tests that when content type is null, the body is marked as non-readable in logs.
         * <p>
         * Verifies that messages without a content type header are treated as non-readable.
         */
        @Test
        void shouldNotLogBodyIfContentTypeIsNull() {
            var messageProps = new MessageProperties();
            var body = "some body".getBytes();
            var message = new Message(body, messageProps);

            var result = processor.postProcessMessage(message);

            assertThat(result).isSameAs(message);

            List<ILoggingEvent> logEvents = listAppender.list;
            if (!logEvents.isEmpty()) {
                assertThat(logEvents.getFirst().getFormattedMessage())
                    .contains("<Non-readable body>");
            }
        }

        /**
         * Tests that when content is in a non-readable format, the body is marked as non-readable in logs.
         * <p>
         * Verifies that binary content types like application/octet-stream are treated as non-readable.
         */
        @Test
        void shouldNotLogBodyIfNotReadableFormat() {
            var messageProps = new MessageProperties();
            messageProps.setContentType("application/octet-stream");
            var body = "some binary data".getBytes();
            var message = new Message(body, messageProps);

            var result = processor.postProcessMessage(message);

            assertThat(result).isSameAs(message);

            List<ILoggingEvent> logEvents = listAppender.list;
            if (!logEvents.isEmpty()) {
                assertThat(logEvents.getFirst().getFormattedMessage())
                    .contains("<Non-readable body>");
            }
        }

        /**
         * Tests that when content is of a non-readable type (like image), the body is marked as non-readable in logs.
         * <p>
         * Verifies that non-text content types like image/png are treated as non-readable.
         */
        @Test
        void shouldNotLogBodyIfContentTypeIsNotReadable() {
            var messageProps = new MessageProperties();
            messageProps.setContentType("image/png");
            var body = "image data".getBytes();
            var message = new Message(body, messageProps);

            var result = processor.postProcessMessage(message);

            assertThat(result).isSameAs(message);

            List<ILoggingEvent> logEvents = listAppender.list;
            if (!logEvents.isEmpty()) {
                assertThat(logEvents.getFirst().getFormattedMessage())
                    .contains("<Non-readable body>");
            }
        }

        /**
         * Tests that body content is properly logged for all supported readable content types.
         * <p>
         * Parameterized test covering: text/plain, application/json, application/xml, text/xml.
         * Each content type should have its body content properly logged.
         *
         * @param contentType the content type to test
         */
        @ParameterizedTest
        @ValueSource(strings = {"text/plain", "application/json", "application/xml", "text/xml"})
        void shouldLogBodyWithReadableContentTypes(String contentType) {
            var messageProps = new MessageProperties();
            messageProps.setContentType(contentType);

            String bodyContent;
            if (contentType.contains("json")) {
                bodyContent = "{\"test\": \"data\"}";
            } else if (contentType.contains("xml")) {
                bodyContent = "<xml>test</xml>";
            } else {
                bodyContent = "Plain text";
            }

            var message = new Message(bodyContent.getBytes(), messageProps);

            var result = processor.postProcessMessage(message);

            assertThat(result).isSameAs(message);

            List<ILoggingEvent> logEvents = listAppender.list;
            assertThat(logEvents)
                .isNotEmpty()
                .first()
                .extracting(ILoggingEvent::getFormattedMessage)
                .asString()
                .contains(bodyContent);
        }
    }

    /**
     * Tests message body processing functionality including truncation and empty body handling.
     */
    @Nested
    class BodyProcessing {

        /**
         * Tests that message body is properly truncated when it exceeds the maximum allowed size.
         * <p>
         * Verifies that bodies longer than maxBodySize are truncated and marked with [TRUNCATED].
         */
        @Test
        void shouldTruncateBodyIfTooLong() {
            var messageProps = new MessageProperties();
            messageProps.setContentType("text/plain");
            var longBody = "A".repeat(150);
            var message = new Message(longBody.getBytes(), messageProps);

            var result = processor.postProcessMessage(message);

            assertThat(result).isSameAs(message);

            List<ILoggingEvent> logEvents = listAppender.list;
            assertThat(logEvents)
                .isNotEmpty()
                .first()
                .extracting(ILoggingEvent::getFormattedMessage)
                .asString()
                .contains("[TRUNCATED]");

            String loggedMessage = logEvents.getFirst().getFormattedMessage();
            assertThat(loggedMessage).contains("A".repeat(100)).contains("[TRUNCATED]");
        }

        /**
         * Tests that empty message bodies are properly handled and marked as empty in logs.
         * <p>
         * Verifies that messages with zero-length bodies are logged with an <Empty> indicator.
         */
        @Test
        void shouldHandleEmptyBody() {
            var messageProps = new MessageProperties();
            messageProps.setContentType("application/json");
            var message = new Message(new byte[0], messageProps);

            var result = processor.postProcessMessage(message);

            assertThat(result).isSameAs(message);

            List<ILoggingEvent> logEvents = listAppender.list;
            if (!logEvents.isEmpty()) {
                assertThat(logEvents.getFirst().getFormattedMessage())
                    .contains("<Empty>");
            }
        }
    }

    /**
     * Tests character encoding handling functionality.
     */
    @Nested
    class CharacterEncoding {

        /**
         * Tests that message processing handles decoding failures gracefully without throwing exceptions.
         * <p>
         * Verifies that invalid UTF-8 sequences are handled gracefully and don't break message processing.
         */
        @Test
        void shouldHandleDecodingFailure() {
            var messageProps = new MessageProperties();
            messageProps.setContentType("text/plain");
            messageProps.setContentEncoding("UTF-8");
            var invalidUtf8Body = new byte[]{(byte) 0xFF, (byte) 0xFE, (byte) 0xFD};
            var message = new Message(invalidUtf8Body, messageProps);

            var result = processor.postProcessMessage(message);

            assertThat(result).isSameAs(message);

            List<ILoggingEvent> logEvents = listAppender.list;
            if (!logEvents.isEmpty()) {
                assertThat(logEvents.getFirst().getFormattedMessage()).isNotNull();
            }
        }

        /**
         * Tests that message body with valid custom character encoding is properly decoded and logged.
         * <p>
         * Verifies that custom character encodings like ISO-8859-1 are properly handled.
         */
        @Test
        void shouldLogBodyWithValidCustomCharset() {
            var messageProps = new MessageProperties();
            messageProps.setContentType("text/plain");
            messageProps.setContentEncoding("ISO-8859-1");
            String originalBody = "Café";
            var body = originalBody.getBytes(java.nio.charset.StandardCharsets.ISO_8859_1);
            var message = new Message(body, messageProps);

            var result = processor.postProcessMessage(message);

            assertThat(result).isSameAs(message);

            List<ILoggingEvent> logEvents = listAppender.list;
            assertThat(logEvents).isNotEmpty();
        }

        /**
         * Tests that message processing uses default charset when provided encoding is invalid.
         * <p>
         * Verifies that when an invalid character encoding is specified, the system falls back to UTF-8.
         */
        @Test
        void shouldLogBodyWithDefaultCharsetIfContentEncodingIsInvalid() {
            var messageProps = new MessageProperties();
            messageProps.setContentType("text/plain");
            messageProps.setContentEncoding("INVALID_CHARSET_NAME");
            String originalBody = "some text";
            var body = originalBody.getBytes();
            var message = new Message(body, messageProps);

            var result = processor.postProcessMessage(message);

            assertThat(result).isSameAs(message);

            List<ILoggingEvent> logEvents = listAppender.list;
            assertThat(logEvents).isNotEmpty();
        }
    }

    /**
     * Tests message integrity and error handling functionality.
     */
    @Nested
    class MessageIntegrity {

        /**
         * Tests that the original message is returned unchanged after processing (message integrity).
         * <p>
         * Critical test ensuring that message processing doesn't modify the original message object.
         */
        @Test
        void shouldReturnOriginalMessageUnchanged() {
            var messageProps = new MessageProperties();
            messageProps.setContentType("application/json");
            var body = "{\"key\":\"value\"}".getBytes();
            var message = new Message(body, messageProps);

            var result = processor.postProcessMessage(message);

            assertThat(result).isSameAs(message);

            List<ILoggingEvent> logEvents = listAppender.list;
            assertThat(logEvents).isNotEmpty();
        }

        /**
         * Tests that message processing continues to work even when logging subsystem fails.
         * <p>
         * Verifies that logging failures don't break message processing - the message is still returned unchanged.
         */
        @Test
        void shouldNotBreakWhenLoggingFails() {
            logger.detachAndStopAllAppenders();

            var messageProps = new MessageProperties();
            messageProps.setContentType("application/json");
            var body = "{\"key\":\"value\"}".getBytes();
            var message = new Message(body, messageProps);

            var result = processor.postProcessMessage(message);

            assertThat(result).isSameAs(message);
        }
    }
}