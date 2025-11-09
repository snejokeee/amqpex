package dev.alubenets.amqpex.logging;

import org.slf4j.Logger;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.core.MessagePostProcessor;
import org.springframework.util.MimeTypeUtils;

/**
 * Abstract base class for message logging post processors.
 * Provides common logging functionality for direction-specific implementations.
 * This class is package-private to hide implementation details from library users.
 */
abstract sealed class LoggingMessagePostProcessor
    implements MessagePostProcessor
    permits IncomingMessageLogger {

    protected final Logger log;
    protected final int maxBodySize;

    protected LoggingMessagePostProcessor(Logger log, int maxBodySize) {
        this.log = log;
        this.maxBodySize = maxBodySize;
    }

    @Override
    public final Message postProcessMessage(Message message) {
        if (log.isDebugEnabled() && isEnabled()) {
            try {
                String readableBody = extractReadableBody(message);
                logMessageDetails(message, readableBody);
            } catch (Exception e) {
                log.warn("Failed to log {} message: {}", getDirectionName(), e.getMessage());
            }
        }
        return message;
    }

    /**
     * Checks if logging is enabled for this processor.
     *
     * @return true if logging is enabled, false otherwise
     */
    protected abstract boolean isEnabled();

    /**
     * Gets the name of the logging direction for logging purposes.
     *
     * @return the direction name
     */
    protected abstract String getDirectionName();

    /**
     * Logs the details of the message for the specific direction.
     * This method is final to prevent overriding and ensure consistent logging format.
     *
     * @param message      the message to log details for
     * @param readableBody the readable body content of the message, or null if not readable
     */
    private void logMessageDetails(Message message, String readableBody) {
        var messageProperties = message.getMessageProperties();
        var direction = getDirectionName();
        log.debug(
            "{} Message - Exchange: '{}', RoutingKey: '{}', ContentType: '{}', Body: {}",
            direction,
            messageProperties.getReceivedExchange(),
            messageProperties.getReceivedRoutingKey(),
            messageProperties.getContentType(),
            readableBody != null ? truncateBody(readableBody) : "<Non-readable body>"
        );
    }

    /**
     * Extracts a readable body from the message if possible.
     *
     * @param message the message to extract body from
     * @return the readable body content or null if not readable
     */
    protected String extractReadableBody(Message message) {
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
            log.debug("Failed to decode message body with charset '{}': {}", charset, e.getMessage());
            return null;
        }
    }

    /**
     * Truncates the body if it exceeds the maximum configured size.
     *
     * @param body the body to truncate
     * @return the truncated body with a suffix if it was truncated
     */
    protected String truncateBody(String body) {
        if (body.length() <= maxBodySize) {
            return body;
        }
        return body.substring(0, maxBodySize) + "[TRUNCATED]";
    }

    /**
     * Checks if the content type is readable (JSON, XML, or text).
     *
     * @param contentType the content type to check
     * @return true if the content type is readable, false otherwise
     */
    protected boolean isReadableContentType(String contentType) {
        if (contentType == null) {
            return false;
        }
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
    protected String determineCharset(String contentEncoding) {
        if (contentEncoding != null && !contentEncoding.trim().isEmpty()) {
            try {
                java.nio.charset.Charset.forName(contentEncoding);
                return contentEncoding;
            } catch (Exception e) {
                log.debug("Invalid charset '{}', falling back to UTF-8: {}", contentEncoding, e.getMessage());
            }
        }
        return "UTF-8";
    }
}