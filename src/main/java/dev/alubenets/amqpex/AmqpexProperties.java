package dev.alubenets.amqpex;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Configuration properties for AMQPex - Spring AMQP Extensions library.
 * Provides type-safe configuration for various AMQPex features.
 */
@ConfigurationProperties(prefix = "amqpex")
public class AmqpexProperties {

    private final LoggingConfiguration logging = new LoggingConfiguration();

    /**
     * Default constructor for AmqpexProperties.
     */
    public AmqpexProperties() {
    }

    /**
     * Gets the logging configuration.
     * @return the logging configuration
     */
    public LoggingConfiguration getLogging() {
        return logging;
    }

    /**
     * Configuration for logging-related settings.
     */
    public static class LoggingConfiguration {
        private final Incoming incoming = new Incoming();

        /**
         * Default constructor for LoggingConfiguration.
         */
        public LoggingConfiguration() {
        }

        /**
         * Gets the incoming message logging configuration.
         * @return the incoming message logging configuration
         */
        public Incoming getIncoming() {
            return incoming;
        }

        /**
         * Configuration for incoming message logging settings.
         */
        public static class Incoming {
            private boolean enabled = true;
            private int maxBodySize = 1000;

            /**
             * Default constructor for Incoming.
             */
            public Incoming() {
            }

            /**
             * Checks if incoming message logging is enabled.
             * @return true if incoming message logging is enabled, false otherwise
             */
            public boolean isEnabled() {
                return enabled;
            }

            /**
             * Sets whether incoming message logging is enabled.
             * @param enabled true to enable incoming message logging, false to disable
             */
            public void setEnabled(boolean enabled) {
                this.enabled = enabled;
            }

            /**
             * Gets the maximum body size to log.
             * @return the maximum body size to log
             */
            public int getMaxBodySize() {
                return maxBodySize;
            }

            /**
             * Sets the maximum body size to log.
             * @param maxBodySize the maximum body size to log
             */
            public void setMaxBodySize(int maxBodySize) {
                this.maxBodySize = maxBodySize;
            }
        }
    }
}