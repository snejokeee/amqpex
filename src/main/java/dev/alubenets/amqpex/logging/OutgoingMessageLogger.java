package dev.alubenets.amqpex.logging;

import dev.alubenets.amqpex.AmqpexProperties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.Ordered;

/**
 * A message post-processor that logs outgoing messages for debugging purposes.
 * This processor runs with the lowest precedence to capture the final state of messages before sending.
 */
public final class OutgoingMessageLogger
    extends LoggingMessagePostProcessor
    implements Ordered {

    private static final Logger DEFAULT_LOG = LoggerFactory.getLogger(OutgoingMessageLogger.class);
    private final boolean enabled;

    public OutgoingMessageLogger(AmqpexProperties.LoggingConfiguration.Outgoing properties) {
        this(DEFAULT_LOG, properties);
    }

    public OutgoingMessageLogger(
        Logger customLogger,
        AmqpexProperties.LoggingConfiguration.Outgoing properties
    ) {
        super(customLogger, properties.getMaxBodySize());
        this.enabled = properties.isEnabled();
    }

    @Override
    protected boolean isEnabled() {
        return enabled;
    }

    @Override
    protected String getDirectionName() {
        return "OUTGOING";
    }

    @Override
    public int getOrder() {
        return LOWEST_PRECEDENCE;
    }
}