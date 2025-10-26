package dev.alubenets.amqpex.logging;

import dev.alubenets.amqpex.AmqpexProperties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.core.MessagePostProcessor;
import org.springframework.core.Ordered;
import org.springframework.util.MimeTypeUtils;

public class IncomingLoggingMessagePostProcessor implements MessagePostProcessor, Ordered {

    private static final Logger log = LoggerFactory.getLogger(IncomingLoggingMessagePostProcessor.class);

    private final AmqpexProperties.LoggingConfiguration.Incoming properties;

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

    private String truncateBody(String body) {
        if (body.length() <= properties.getMaxBodySize()) {
            return body;
        }
        return body.substring(0, properties.getMaxBodySize()) + "... [TRUNCATED]";
    }

    private boolean isReadableContentType(String contentType) {
        var lowerContentType = contentType.toLowerCase();
        return lowerContentType.startsWith(MimeTypeUtils.APPLICATION_JSON_VALUE) ||
            lowerContentType.startsWith(MimeTypeUtils.APPLICATION_XML_VALUE) ||
            lowerContentType.startsWith(MimeTypeUtils.TEXT_XML_VALUE) ||
            lowerContentType.startsWith("text/");
    }

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