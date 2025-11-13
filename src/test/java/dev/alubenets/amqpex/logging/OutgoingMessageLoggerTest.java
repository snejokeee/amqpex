package dev.alubenets.amqpex.logging;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.read.ListAppender;
import dev.alubenets.amqpex.AmqpexProperties;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.core.MessageProperties;

import java.util.List;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Unit test for {@link OutgoingMessageLogger}.
 * <p>
 * Tests the core logic of message processing with actual logging verification.
 * All tests ensure that logging functionality works as expected while maintaining
 * message integrity and proper error handling.
 */
class OutgoingMessageLoggerTest {

    private AmqpexProperties.LoggingConfiguration.Outgoing outgoingProps;
    private OutgoingMessageLogger processor;
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
        logger = (Logger) LoggerFactory.getLogger(OutgoingMessageLogger.class);
        logger.setLevel(Level.DEBUG);

        listAppender = new ListAppender<>();
        listAppender.start();
        logger.addAppender(listAppender);

        outgoingProps = new AmqpexProperties.LoggingConfiguration.Outgoing();
        outgoingProps.setMaxBodySize(100);
        outgoingProps.setEnabled(true);
        processor = new OutgoingMessageLogger(outgoingProps);
    }

    @AfterEach
    void tearDown() {
        logger.detachAndStopAllAppenders();
    }

    /**
     * Tests basic functionality of the message post-processor.
     */
    @Nested
    class BasicFunctionality {

        /**
         * Tests that message details are properly logged when debug is enabled and the body is readable.
         * <p>
         * Verifies that exchange, routing key, content type, headers, and body content are all logged correctly
         * when the logging feature is enabled and the message contains readable content.
         */
        @Test
        void shouldLogMessageDetailsWhenDebugEnabledAndReadableBody() {
            var messageProps = new MessageProperties();
            messageProps.setContentType("application/json");
            messageProps.setReceivedExchange("test-exchange");
            messageProps.setReceivedRoutingKey("test.routing.key");
            messageProps.setHeader("test-header", "test-value");

            var body = "{\"key\":\"value\"}".getBytes();
            var message = new Message(body, messageProps);

            var result = processor.postProcessMessage(message);

            assertThat(result).isSameAs(message);

            List<ILoggingEvent> logEvents = listAppender.list;
            assertThat(logEvents).isNotEmpty();

            ILoggingEvent debugEvent = logEvents.stream()
                .filter(event -> event.getLevel() == Level.DEBUG)
                .filter(event -> event.getFormattedMessage().startsWith("OUTGOING Message"))
                .findFirst()
                .orElseThrow(() -> new AssertionError("Expected main DEBUG event not found"));

            String expectedLogMessage = "OUTGOING Message - Exchange: 'test-exchange', " +
                "RoutingKey: 'test.routing.key', ContentType: 'application/json', Headers: {test-header=test-value}, Body: {\"key\":\"value\"}";
            assertThat(debugEvent.getFormattedMessage()).isEqualTo(expectedLogMessage);
        }

        /**
         * Tests that no logging occurs when the logging feature is disabled.
         * <p>
         * Verifies that when the enabled property is set to false, no log events are generated.
         */
        @Test
        void shouldNotLogWhenDisabled() {
            outgoingProps.setEnabled(false);
            processor = new OutgoingMessageLogger(outgoingProps);

            var messageProps = new MessageProperties();
            messageProps.setContentType("application/json");
            var body = "{\"key\":\"value\"}".getBytes();
            var message = new Message(body, messageProps);

            var result = processor.postProcessMessage(message);

            assertThat(result).isSameAs(message);
            assertThat(listAppender.list).isEmpty();
        }

        /**
         * Tests that headers are not logged when header logging is disabled.
         * <p>
         * Verifies that when the logHeaders property is set to false, headers are hidden in logs.
         */
        @Test
        void shouldHideHeadersWhenHeaderLoggingDisabled() {
            outgoingProps.setLogHeaders(false);
            processor = new OutgoingMessageLogger(outgoingProps);

            var messageProps = new MessageProperties();
            messageProps.setContentType("application/json");
            messageProps.setReceivedExchange("test-exchange");
            messageProps.setReceivedRoutingKey("test.routing.key");
            messageProps.setHeader("test-header", "test-value");

            var body = "{\"key\":\"value\"}".getBytes();
            var message = new Message(body, messageProps);

            var result = processor.postProcessMessage(message);

            assertThat(result).isSameAs(message);

            List<ILoggingEvent> logEvents = listAppender.list;
            assertThat(logEvents).isNotEmpty();

            ILoggingEvent debugEvent = logEvents.stream()
                .filter(event -> event.getLevel() == Level.DEBUG)
                .filter(event -> event.getFormattedMessage().startsWith("OUTGOING Message"))
                .findFirst()
                .orElseThrow(() -> new AssertionError("Expected main DEBUG event not found"));

            String expectedLogMessage = "OUTGOING Message - Exchange: 'test-exchange', " +
                "RoutingKey: 'test.routing.key', ContentType: 'application/json', Headers: [HIDDEN], Body: {\"key\":\"value\"}";
            assertThat(debugEvent.getFormattedMessage()).isEqualTo(expectedLogMessage);
        }

        /**
         * Tests that no logging occurs when debug level is not enabled.
         * <p>
         * Verifies that when logger level is above DEBUG, no log events are generated.
         */
        @Test
        void shouldNotLogWhenDebugNotEnabled() {
            logger.setLevel(Level.INFO);

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
         * Provides test data for non-readable content type tests.
         *
         * @return stream of test arguments
         */
        static Stream<Arguments> nonReadableContentTypes() {
            return Stream.of(
                Arguments.of(null, "null"),
                Arguments.of("application/octet-stream", "application/octet-stream"),
                Arguments.of("image/png", "image/png")
            );
        }

        /**
         * Tests that when content is of a non-readable type, the body is marked as non-readable in logs.
         * <p>
         * Verifies that non-readable content types like null, binary formats, and images are treated as non-readable.
         *
         * @param contentType the content type to test (null or non-readable type)
         * @param expectedContentType the expected content type string in log message
         */
        @ParameterizedTest
        @MethodSource("nonReadableContentTypes")
        void shouldNotLogBodyIfContentTypeIsNotReadable(String contentType, String expectedContentType) {
            var messageProps = new MessageProperties();
            messageProps.setContentType(contentType);
            var body = "some body".getBytes();
            var message = new Message(body, messageProps);

            var result = processor.postProcessMessage(message);

            assertThat(result).isSameAs(message);

            List<ILoggingEvent> logEvents = listAppender.list;
            assertThat(logEvents).isNotEmpty();

            ILoggingEvent debugEvent = logEvents.stream()
                .filter(event -> event.getLevel() == Level.DEBUG)
                .filter(event -> event.getFormattedMessage().startsWith("OUTGOING Message"))
                .findFirst()
                .orElseThrow(() -> new AssertionError("Expected main DEBUG event not found"));

            String expectedLogMessage = String.format(
                "OUTGOING Message - Exchange: 'null', RoutingKey: 'null', ContentType: '%s', Headers: {}, Body: <Non-readable body>",
                expectedContentType);
            assertThat(debugEvent.getFormattedMessage()).isEqualTo(expectedLogMessage);
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
            assertThat(logEvents).isNotEmpty();

            ILoggingEvent debugEvent = logEvents.stream()
                .filter(event -> event.getLevel() == Level.DEBUG)
                .filter(event -> event.getFormattedMessage().startsWith("OUTGOING Message"))
                .findFirst()
                .orElseThrow(() -> new AssertionError("Expected main DEBUG event not found"));

            String expectedLogMessage = String.format(
                "OUTGOING Message - Exchange: 'null', RoutingKey: 'null', ContentType: '%s', Headers: {}, Body: %s",
                contentType, bodyContent);
            assertThat(debugEvent.getFormattedMessage()).isEqualTo(expectedLogMessage);
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
            outgoingProps.setMaxBodySize(10);
            processor = new OutgoingMessageLogger(outgoingProps);

            var messageProps = new MessageProperties();
            messageProps.setContentType("text/plain");
            var longBody = "This is a very long message body that will be truncated";
            var message = new Message(longBody.getBytes(), messageProps);

            var result = processor.postProcessMessage(message);

            assertThat(result).isSameAs(message);

            List<ILoggingEvent> logEvents = listAppender.list;
            assertThat(logEvents).isNotEmpty();

            ILoggingEvent debugEvent = logEvents.stream()
                .filter(event -> event.getLevel() == Level.DEBUG)
                .filter(event -> event.getFormattedMessage().startsWith("OUTGOING Message"))
                .findFirst()
                .orElseThrow(() -> new AssertionError("Expected main DEBUG event not found"));

            String expectedLogMessage = "OUTGOING Message - Exchange: 'null', RoutingKey: 'null', " +
                "ContentType: 'text/plain', Headers: {}, Body: This is a [TRUNCATED]";
            assertThat(debugEvent.getFormattedMessage()).isEqualTo(expectedLogMessage);
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
            assertThat(logEvents).isNotEmpty();

            ILoggingEvent debugEvent = logEvents.stream()
                .filter(event -> event.getLevel() == Level.DEBUG)
                .filter(event -> event.getFormattedMessage().startsWith("OUTGOING Message"))
                .findFirst()
                .orElseThrow(() -> new AssertionError("Expected main DEBUG event not found"));

            String expectedLogMessage = "OUTGOING Message - Exchange: 'null', RoutingKey: 'null', " +
                "ContentType: 'application/json', Headers: {}, Body: <Empty>";
            assertThat(debugEvent.getFormattedMessage()).isEqualTo(expectedLogMessage);
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
            assertThat(logEvents).isNotEmpty();

            ILoggingEvent debugEvent = logEvents.stream()
                .filter(event -> event.getLevel() == Level.DEBUG)
                .filter(event -> event.getFormattedMessage().startsWith("OUTGOING Message"))
                .findFirst()
                .orElseThrow(() -> new AssertionError("Expected main DEBUG event not found"));

            String actualLogMessage = debugEvent.getFormattedMessage();
            assertThat(actualLogMessage).startsWith("OUTGOING Message - Exchange: 'null', RoutingKey: 'null', ContentType: 'text/plain', Headers: {}, Body: ");
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

            ILoggingEvent debugEvent = logEvents.stream()
                .filter(event -> event.getLevel() == Level.DEBUG)
                .filter(event -> event.getFormattedMessage().startsWith("OUTGOING Message"))
                .findFirst()
                .orElseThrow(() -> new AssertionError("Expected main DEBUG event not found"));

            String expectedLogMessage = "OUTGOING Message - Exchange: 'null', RoutingKey: 'null', " +
                "ContentType: 'text/plain', Headers: {}, Body: Café";
            assertThat(debugEvent.getFormattedMessage()).isEqualTo(expectedLogMessage);
        }

        /**
         * Tests that message processing uses default charset when provided encoding is invalid.
         * <p>
         * Verifies that when an invalid character encoding is specified, the system falls back to UTF-8.
         */
        @Test
        void shouldUseDefaultCharsetIfContentEncodingIsInvalid() {
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

            ILoggingEvent mainEvent = logEvents.stream()
                .filter(event -> event.getLevel() == Level.DEBUG)
                .filter(event -> event.getFormattedMessage().startsWith("OUTGOING Message"))
                .findFirst()
                .orElseThrow(() -> new AssertionError("Expected main DEBUG event not found"));

            String expectedLogMessage = "OUTGOING Message - Exchange: 'null', RoutingKey: 'null', " +
                "ContentType: 'text/plain', Headers: {}, Body: some text";
            assertThat(mainEvent.getFormattedMessage()).isEqualTo(expectedLogMessage);
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
            assertThat(result.getBody()).isEqualTo(body);
            assertThat(result.getMessageProperties()).isSameAs(messageProps);

            List<ILoggingEvent> logEvents = listAppender.list;
            assertThat(logEvents).isNotEmpty();
        }

        /**
         * Tests that message processing continues to work even when logging subsystem fails.
         * <p>
         * Verifies that logging failures don't break message processing - the message is still returned unchanged.
         */
        @Test
        void shouldNotBreakWhenExceptionOccursDuringProcessing() {
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
}