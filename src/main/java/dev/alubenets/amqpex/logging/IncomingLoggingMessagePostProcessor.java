package dev.alubenets.amqpex.logging;

import dev.alubenets.amqpex.AmqpexProperties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.core.MessagePostProcessor;
import org.springframework.core.Ordered;
import org.springframework.util.MimeTypeUtils;

/**
 * A MessagePostProcessor designed to log details of incoming messages *before*
 * other potential post-processors run, based on its order (HIGHEST_PRECEDENCE).
 * It logs the routing key, exchange, message properties, and attempts to log
 * the body if it's likely a readable format (JSON, XML, plain text).
 */
public class IncomingLoggingMessagePostProcessor implements MessagePostProcessor, Ordered {

    private static final Logger log = LoggerFactory.getLogger(IncomingLoggingMessagePostProcessor.class);

    private final AmqpexProperties.LoggingConfiguration.Incoming properties;
    private final int maxBodySize;

    // Constructor to accept the max body size from properties
    public IncomingLoggingMessagePostProcessor(AmqpexProperties.LoggingConfiguration.Incoming properties) {
        this.properties = properties;
        this.maxBodySize = properties.getMaxBodySize();
    }

    @Override
    public Message postProcessMessage(Message message) {
        if (log.isDebugEnabled()) { // Only proceed if debug logging is enabled
            try {
                var logMessage = new StringBuilder("\n--- AMQPex Incoming Message Log ---\n");

                // 1. Log Exchange and Routing Key (received details)
                logMessage.append("Received from Exchange: '").append(message.getMessageProperties().getReceivedExchange()).append("'\n");
                logMessage.append("Received with Routing Key: '").append(message.getMessageProperties().getReceivedRoutingKey()).append("'\n");

                // 2. Log Message Properties - delegate to helper method
                appendMessageProperties(logMessage, message);

                // 3. Attempt to Log Message Body if readable - delegate to helper method
                var bodyLog = extractReadableBody(message); // Uses this.maxBodySize internally
                if (bodyLog != null) {
                    logMessage.append("Message Body (first ").append(Math.min(maxBodySize, bodyLog.length())).append(" chars):\n") // Use instance variable
                        .append(bodyLog).append("\n");
                } else {
                    logMessage.append("Message Body: <Not logged - format not recognized as readable or body too large after decoding>\n");
                }

                logMessage.append("--- End AMQPex Incoming Message Log ---\n");

                log.debug(logMessage.toString());

            } catch (Exception e) {
                // Log a warning if the logging itself fails, but don't break the message flow
                log.warn("AMQPex: Failed to log incoming message details: {}", e.getMessage(), e);
                // Still return the original message
            }
        }
        // Always return the original message unchanged
        return message;
    }

    /**
     * Appends formatted message properties to the provided StringBuilder.
     *
     * @param logMessage The StringBuilder to append to.
     * @param message    The AMQP message whose properties to log.
     */
    private void appendMessageProperties(StringBuilder logMessage, Message message) {
        var props = message.getMessageProperties();
        logMessage.append("Message Properties:\n");
        logMessage.append("  Content Type: ").append(props.getContentType()).append("\n");
        logMessage.append("  Content Encoding: ").append(props.getContentEncoding()).append("\n");
        logMessage.append("  Correlation ID: ").append(props.getCorrelationId()).append("\n");
        logMessage.append("  Reply To: ").append(props.getReplyTo()).append("\n");
        logMessage.append("  Message ID: ").append(props.getMessageId()).append("\n");
        logMessage.append("  Timestamp: ").append(props.getTimestamp()).append("\n");
        logMessage.append("  Type: ").append(props.getType()).append("\n");
        logMessage.append("  App ID: ").append(props.getAppId()).append("\n");
        logMessage.append("  User ID: ").append(props.getUserId()).append("\n");
        logMessage.append("  Delivery Mode: ").append(props.getDeliveryMode()).append("\n");
        logMessage.append("  Priority: ").append(props.getPriority()).append("\n");
        logMessage.append("  Redelivered: ").append(props.isRedelivered()).append("\n");
        logMessage.append("  Delivery Tag: ").append(props.getDeliveryTag()).append("\n");
        logMessage.append("  Consumer Tag: ").append(props.getConsumerTag()).append("\n");
        logMessage.append("  Consumer Queue: ").append(props.getConsumerQueue()).append("\n");

        // Log headers if present
        if (props.getHeaders() != null && !props.getHeaders().isEmpty()) {
            logMessage.append("  Headers:\n");
            props.getHeaders().forEach((key, value) ->
                logMessage.append("    ").append(key).append(" = ").append(value).append("\n")
            );
        }
    }

    /**
     * Helper method to determine if the message body should be logged as a string.
     * Checks content-type and attempts UTF-8 decoding if it seems readable.
     *
     * @param message The incoming AMQP message.
     * @return The decoded body string if readable and not too long, otherwise null.
     */
    private String extractReadableBody(Message message) {
        var bodyBytes = message.getBody();
        var contentType = message.getMessageProperties().getContentType();
        var contentEncoding = message.getMessageProperties().getContentEncoding();

        // Handle empty body early
        if (bodyBytes == null || bodyBytes.length == 0) {
            return "<Empty Body>";
        }

        // Determine charset (default to UTF-8)
        var charset = determineCharset(contentEncoding);

        // If no content-type is specified, skip logging (as per requirement for parsable formats)
        if (contentType == null) {
            log.debug("AMQPex: No Content-Type header present, skipping body log.");
            return null;
        }

        // Check if content-type indicates a potentially readable format
        if (!isReadableContentType(contentType)) {
            log.debug("AMQPex: Content-Type '{}' is not recognized as a readable format, skipping body log.", contentType);
            return null;
        }

        // Content type suggests readability, attempt decoding
        return decodeAndTruncateBody(bodyBytes, charset); // Uses this.maxBodySize internally
    }

    /**
     * Determines the charset to use for decoding, validating the provided one or defaulting to UTF-8.
     *
     * @param contentEncoding The charset from the message properties.
     * @return The validated charset string, or "UTF-8" if invalid or null.
     */
    private String determineCharset(String contentEncoding) {
        if (contentEncoding != null && !contentEncoding.trim().isEmpty()) {
            try {
                java.nio.charset.Charset.forName(contentEncoding);
                return contentEncoding; // Return the validated encoding
            } catch (Exception e) {
                log.debug("AMQPex: Invalid content-encoding '{}', using default UTF-8", contentEncoding);
            }
        }
        return "UTF-8"; // Default fallback
    }

    /**
     * Checks if the given content type string indicates a readable format.
     * Uses Spring's MimeTypeUtils VALUE constants for comparison.
     *
     * @param contentType The content type string from the message properties.
     * @return true if the type is considered readable (JSON, XML, text), false otherwise.
     */
    private boolean isReadableContentType(String contentType) {
        var lowerContentType = contentType.toLowerCase(); // Use lowercase for comparison
        return lowerContentType.startsWith(MimeTypeUtils.APPLICATION_JSON_VALUE) ||
            lowerContentType.startsWith(MimeTypeUtils.APPLICATION_XML_VALUE) ||
            lowerContentType.startsWith(MimeTypeUtils.TEXT_XML_VALUE) ||
            lowerContentType.startsWith("text/");
    }

    /**
     * Attempts to decode the byte array using the specified charset and truncates if necessary.
     *
     * @param bodyBytes The raw message body bytes.
     * @param charset   The charset to use for decoding.
     * @return The decoded and potentially truncated string, or null if decoding fails.
     */
    private String decodeAndTruncateBody(byte[] bodyBytes, String charset) {
        try {
            var decodedBody = new String(bodyBytes, charset);
            // Truncate if too long - uses the instance variable 'maxBodySize'
            if (decodedBody.length() > maxBodySize) {
                log.debug("AMQPex: Body length ({}) exceeds max log length ({}), truncating.", decodedBody.length(), maxBodySize);
                return decodedBody.substring(0, maxBodySize) + "... [TRUNCATED]"; // Uses the instance variable
            }
            return decodedBody; // Log the decoded string
        } catch (Exception e) {
            log.debug("AMQPex: Could not decode body as {} for readable format check: {}", charset, e.getMessage());
            // Decoding failed, return null to skip logging the body
            return null;
        }
    }


    /**
     * Defines the order of execution for this post-processor.
     * Using HIGHEST_PRECEDENCE ensures it runs first among other after-receive post-processors,
     * logging the original message state.
     *
     * @return The order value, {@link Ordered#HIGHEST_PRECEDENCE}.
     */
    @Override
    public int getOrder() {
        return HIGHEST_PRECEDENCE; // Run first to log original message
    }
}