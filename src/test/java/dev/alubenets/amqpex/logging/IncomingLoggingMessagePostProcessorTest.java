package dev.alubenets.amqpex.logging;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import dev.alubenets.amqpex.AmqpexProperties;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.core.MessageProperties;

import java.util.Collections;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Unit test for {@link IncomingLoggingMessagePostProcessor}.
 * Tests the core logic of message processing.
 */
class IncomingLoggingMessagePostProcessorTest {

    private AmqpexProperties.LoggingConfiguration.Incoming incomingProps;
    private IncomingLoggingMessagePostProcessor processor;

    @BeforeEach
    void setUp() {
        // Programmatically set the log level for the post-processor's logger to DEBUG
        Logger logger = (Logger) LoggerFactory.getLogger(IncomingLoggingMessagePostProcessor.class);
        logger.setLevel(Level.DEBUG);

        incomingProps = new AmqpexProperties.LoggingConfiguration.Incoming();
        incomingProps.setMaxBodySize(100);
        processor = new IncomingLoggingMessagePostProcessor(incomingProps);
    }

    @Test
    void shouldLogMessageDetailsWhenDebugEnabledAndReadableBody() {
        var messageProps = new MessageProperties();
        messageProps.setContentType("application/json");
        messageProps.setReceivedExchange("test-exchange");
        messageProps.setReceivedRoutingKey("test.routing.key");
        messageProps.setHeaders(Collections.singletonMap("custom-header", "value"));

        var body = "{\"key\":\"value\"}".getBytes();
        var message = new Message(body, messageProps);

        var result = processor.postProcessMessage(message);

        assertThat(result).isSameAs(message);
    }

    @Test
    void shouldNotLogBodyIfNotReadableFormat() {
        var messageProps = new MessageProperties();
        messageProps.setContentType("application/octet-stream");
        var body = "some binary data".getBytes();
        var message = new Message(body, messageProps);

        var result = processor.postProcessMessage(message);

        assertThat(result).isSameAs(message);
    }

    @Test
    void shouldTruncateBodyIfTooLong() {
        var messageProps = new MessageProperties();
        messageProps.setContentType("text/plain");
        var longBody = "A".repeat(150);
        var message = new Message(longBody.getBytes(), messageProps);

        var result = processor.postProcessMessage(message);

        assertThat(result).isSameAs(message);
    }

    @Test
    void shouldHandleEmptyBody() {
        var messageProps = new MessageProperties();
        messageProps.setContentType("application/json");
        var message = new Message(new byte[0], messageProps);

        var result = processor.postProcessMessage(message);

        assertThat(result).isSameAs(message);
    }

    @Test
    void shouldHandleDecodingFailure() {
        var messageProps = new MessageProperties();
        messageProps.setContentType("text/plain");
        messageProps.setContentEncoding("UTF-8");
        var invalidUtf8Body = new byte[]{(byte) 0xFF, (byte) 0xFE, (byte) 0xFD};
        var message = new Message(invalidUtf8Body, messageProps);

        var result = processor.postProcessMessage(message);

        assertThat(result).isSameAs(message);
    }

    // --- New Tests for Better Branch Coverage ---

    @Test
    void shouldNotLogBodyIfContentTypeIsNull() {
        var messageProps = new MessageProperties();
        // Do not set content type - it will be null
        var body = "some body".getBytes();
        var message = new Message(body, messageProps);

        var result = processor.postProcessMessage(message);

        assertThat(result).isSameAs(message);
        // This should trigger the path where contentType is null -> isReadableContentType returns false
    }

    @Test
    void shouldLogBodyWithValidCustomCharset() {
        var messageProps = new MessageProperties();
        messageProps.setContentType("text/plain");
        messageProps.setContentEncoding("ISO-8859-1"); // Use a common encoding different from UTF-8
        String originalBody = "Café"; // Contains character requiring ISO-8859-1
        var body = originalBody.getBytes(java.nio.charset.StandardCharsets.ISO_8859_1);
        var message = new Message(body, messageProps);

        var result = processor.postProcessMessage(message);

        assertThat(result).isSameAs(message);
        // This tests the path where contentEncoding is valid and used for decoding
    }

    @Test
    void shouldLogBodyWithDefaultCharsetIfContentEncodingIsInvalid() {
        var messageProps = new MessageProperties();
        messageProps.setContentType("text/plain");
        messageProps.setContentEncoding("INVALID_CHARSET_NAME"); // Invalid encoding name
        String originalBody = "some text";
        var body = originalBody.getBytes(); // Default UTF-8
        var message = new Message(body, messageProps);

        var result = processor.postProcessMessage(message);

        assertThat(result).isSameAs(message);
        // This tests the path where contentEncoding is invalid -> default UTF-8 is used
    }

    @Test
    void shouldNotLogBodyIfContentTypeIsNotReadable() {
        var messageProps = new MessageProperties();
        messageProps.setContentType("image/png"); // Explicitly non-readable type
        var body = "image data".getBytes();
        var message = new Message(body, messageProps);

        var result = processor.postProcessMessage(message);

        assertThat(result).isSameAs(message);
        // This tests the path where contentType is present but not considered readable
    }

    @Test
    void shouldLogBodyWithTextXmlContentType() {
        var messageProps = new MessageProperties();
        messageProps.setContentType("text/xml"); // Another readable type
        var body = "<xml>test</xml>".getBytes();
        var message = new Message(body, messageProps);

        var result = processor.postProcessMessage(message);

        assertThat(result).isSameAs(message);
        // This tests the path for text/xml
    }

    @Test
    void shouldLogBodyWithApplicationXmlContentType() {
        var messageProps = new MessageProperties();
        messageProps.setContentType("application/xml"); // Another readable type
        var body = "<xml>test</xml>".getBytes();
        var message = new Message(body, messageProps);

        var result = processor.postProcessMessage(message);

        assertThat(result).isSameAs(message);
        // This tests the path for application/xml
    }

    @Test
    void shouldLogBodyWithTextPlainContentType() {
        var messageProps = new MessageProperties();
        messageProps.setContentType("text/plain"); // Another readable type
        var body = "Plain text".getBytes();
        var message = new Message(body, messageProps);

        var result = processor.postProcessMessage(message);

        assertThat(result).isSameAs(message);
        // This tests the path for text/plain
    }
}