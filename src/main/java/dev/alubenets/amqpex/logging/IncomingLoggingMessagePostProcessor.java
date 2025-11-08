package dev.alubenets.amqpex.logging;

import dev.alubenets.amqpex.AmqpexProperties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.core.MessagePostProcessor;
import org.springframework.core.Ordered;
import org.springframework.util.MimeTypeUtils;

/**
 * A message post-processor that logs incoming messages for debugging purposes.
 * This processor runs with the highest precedence to capture the original state of messages.
 */
public class IncomingLoggingMessagePostProcessor implements MessagePostProcessor, Ordered {

    private static final Logger log = LoggerFactory.getLogger(IncomingLoggingMessagePostProcessor.class);

    private final AmqpexProperties.LoggingConfiguration.Incoming properties;

    /**
     * Creates a new instance of IncomingLoggingMessagePostProcessor.
     * @param properties the incoming message logging configuration properties
     */
    public IncomingLoggingMessagePostProcessor(AmqpexProperties.LoggingConfiguration.Incoming properties) {
        this.properties = properties;
    }

    @Override
    public Message postProcessMessage(Message message) {
        if (log.isDebugEnabled() && properties.isEnabled()) {
            try {
                String readableBody = extractReadableBody(message);
                logMessageDetails(message, readableBody);
            } catch (Exception e) {
                log.warn("AMQPex: Failed to log incoming message: {}", e.getMessage());
            }
        }
        return message;
    }

    /**
     * Logs the details of the incoming message.
     *
     * @param message the message to log details for
     * @param readableBody the readable body content of the message, or null if not readable
     */
    private void logMessageDetails(Message message, String readableBody) {
        var props = message.getMessageProperties();

        log.debug(
            "AMQPex Incoming Message - Exchange: '{}', RoutingKey: '{}', ContentType: '{}', Body: {}",
            props.getReceivedExchange(),
            props.getReceivedRoutingKey(),
            props.getContentType(),
            readableBody != null ? truncateBody(readableBody) : "<Non-readable body>"
        );
    }

    /**
     * Extracts a readable body from the message if possible.
     *
     * @param message the message to extract body from
     * @return the readable body content or null if not readable
     */
    private String extractReadableBody(Message message) {
        var bodyBytes = message.getBody();
        var contentType = message.getMessageProperties().getContentType();

        if (bodyBytes == null || bodyBytes.length == 0) {
            return "<Empty>";
        }

        if (contentType == null || !isReadableContentType(contentType)) {
            return null;
        }

        String charset = determineCharset(message.getMessageProperties().getContentEncoding());
        try {
            return new String(bodyBytes, charset);
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * Truncates the body if it exceeds the maximum configured size.
     *
     * @param body the body to truncate
     * @return the truncated body with a suffix if it was truncated
     */
    private String truncateBody(String body) {
        if (body.length() <= properties.getMaxBodySize()) {
            return body;
        }
        return body.substring(0, properties.getMaxBodySize()) + "... [TRUNCATED]";
    }

    /**
     * Checks if the content type is readable (JSON, XML, or text).
     *
     * @param contentType the content type to check
     * @return true if the content type is readable, false otherwise
     */
    private boolean isReadableContentType(String contentType) {
        var lowerContentType = contentType.toLowerCase();
        return lowerContentType.startsWith(MimeTypeUtils.APPLICATION_JSON_VALUE) ||
            lowerContentType.startsWith(MimeTypeUtils.APPLICATION_XML_VALUE) ||
            lowerContentType.startsWith(MimeTypeUtils.TEXT_XML_VALUE) ||
            lowerContentType.startsWith("text/");
    }

    /**
     * Determines the charset to use for decoding the message body.
     *
     * @param contentEncoding the content encoding specified in message properties, or null
     * @return the charset to use for decoding
     */
    private String determineCharset(String contentEncoding) {
        if (contentEncoding != null && !contentEncoding.trim().isEmpty()) {
            try {
                java.nio.charset.Charset.forName(contentEncoding);
                return contentEncoding;
            } catch (Exception e) {
                // Fall back to UTF-8
            }
        }
        return "UTF-8";
    }

    @Override
    public int getOrder() {
        return HIGHEST_PRECEDENCE;
    }
}