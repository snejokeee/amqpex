package dev.alubenets.amqpex.logging;

import dev.alubenets.amqpex.AmqpexProperties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.Ordered;

/**
 * A message post-processor that logs incoming messages for debugging purposes.
 * This processor runs with the highest precedence to capture the original state of messages.
 */
public final class IncomingMessageLogger
    extends LoggingMessagePostProcessor
    implements Ordered {

    private static final Logger DEFAULT_LOG = LoggerFactory.getLogger(IncomingMessageLogger.class);
    private final boolean enabled;
    private final boolean logHeaders;

    /**
     * Constructor for IncomingMessageLogger with default logger.
     *
     * @param properties the incoming message logging configuration properties
     */
    public IncomingMessageLogger(AmqpexProperties.LoggingConfiguration.Incoming properties) {
        this(DEFAULT_LOG, properties);
    }

    /**
     * Constructor for IncomingMessageLogger with custom logger.
     *
     * @param customLogger the custom logger to use for logging
     * @param properties   the incoming message logging configuration properties
     */
    public IncomingMessageLogger(
        Logger customLogger,
        AmqpexProperties.LoggingConfiguration.Incoming properties
    ) {
        super(customLogger, properties.getMaxBodySize());
        this.enabled = properties.isEnabled();
        this.logHeaders = properties.isLogHeaders();
    }

    @Override
    protected boolean isEnabled() {
        return enabled;
    }

    @Override
    protected String getDirectionName() {
        return "INCOMING";
    }

    @Override
    protected boolean shouldLogHeaders() {
        return logHeaders;
    }

    @Override
    public int getOrder() {
        return HIGHEST_PRECEDENCE;
    }
}