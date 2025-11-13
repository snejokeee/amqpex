package dev.alubenets.amqpex;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.NestedConfigurationProperty;

/**
 * Configuration properties for AMQPex - Spring AMQP Extensions library.
 * <p>
 * Provides type-safe configuration for various AMQPex features. This class follows
 * Spring Boot's configuration properties pattern.
 * </p>
 *
 * <p>
 * Example configuration:
 * </p>
 * <pre>
 * amqpex:
 *   logging:
 *     incoming:
 *       enabled: true
 *       max-body-size: 1500
 *     outgoing:
 *       enabled: false
 *       max-body-size: 2500
 * </pre>
 */
@ConfigurationProperties(prefix = "amqpex")
public class AmqpexProperties {

    private final LoggingConfiguration logging = new LoggingConfiguration();

    /**
     * Creates a new instance of AmqpexProperties with default values.
     * This constructor initializes the logging configuration with default settings.
     */
    public AmqpexProperties() {
        // Initialize with default values
    }

    /**
     * Gets the logging configuration.
     *
     * @return the logging configuration
     */
    public LoggingConfiguration getLogging() {
        return logging;
    }

    /**
     * Configuration for logging-related settings.
     */
    public static class LoggingConfiguration {

        @NestedConfigurationProperty
        private final Incoming incoming = new Incoming();

        @NestedConfigurationProperty
        private final Outgoing outgoing = new Outgoing();

        /**
         * Creates a new instance of LoggingConfiguration with default nested configurations.
         * Initializes incoming and outgoing logging configurations with default values.
         */
        public LoggingConfiguration() {
            // Initialize nested configurations with default values
        }

        /**
         * Gets the incoming message logging configuration.
         *
         * @return the incoming message logging configuration
         */
        public Incoming getIncoming() {
            return incoming;
        }

        /**
         * Gets the outgoing message logging configuration.
         *
         * @return the outgoing message logging configuration
         */
        public Outgoing getOutgoing() {
            return outgoing;
        }

        /**
         * Configuration for incoming message logging settings.
         * Contains properties for controlling incoming message logging behavior.
         */
        public static class Incoming {
            private boolean enabled = true;
            private int maxBodySize = 1000;

            /**
             * Creates a new instance of Incoming logging configuration with default values.
             * Sets enabled to true and maxBodySize to 1000 bytes.
             */
            public Incoming() {
                // Initialize with default values for incoming message logging
            }

            /**
             * Checks if incoming message logging is enabled.
             *
             * @return {@code true} if incoming message logging is enabled, {@code false} otherwise
             */
            public boolean isEnabled() {
                return enabled;
            }

            /**
             * Sets whether incoming message logging is enabled.
             *
             * @param enabled {@code true} to enable incoming message logging, {@code false} to disable
             */
            public void setEnabled(boolean enabled) {
                this.enabled = enabled;
            }

            /**
             * Gets the maximum body size to log for incoming messages.
             *
             * @return the maximum body size to log for incoming messages
             */
            public int getMaxBodySize() {
                return maxBodySize;
            }

            /**
             * Sets the maximum body size to log for incoming messages.
             *
             * @param maxBodySize the maximum body size to log for incoming messages
             */
            public void setMaxBodySize(int maxBodySize) {
                this.maxBodySize = maxBodySize;
            }
        }

        /**
         * Configuration for outgoing message logging settings.
         * Contains properties for controlling outgoing message logging behavior.
         */
        public static class Outgoing {
            private boolean enabled = true;
            private int maxBodySize = 1000;

            /**
             * Creates a new instance of Outgoing logging configuration with default values.
             * Sets enabled to true and maxBodySize to 1000 bytes.
             */
            public Outgoing() {
                // Initialize with default values for outgoing message logging
            }

            /**
             * Checks if outgoing message logging is enabled.
             *
             * @return {@code true} if outgoing message logging is enabled, {@code false} otherwise
             */
            public boolean isEnabled() {
                return enabled;
            }

            /**
             * Sets whether outgoing message logging is enabled.
             *
             * @param enabled {@code true} to enable outgoing message logging, {@code false} to disable
             */
            public void setEnabled(boolean enabled) {
                this.enabled = enabled;
            }

            /**
             * Gets the maximum body size to log for outgoing messages.
             *
             * @return the maximum body size to log for outgoing messages
             */
            public int getMaxBodySize() {
                return maxBodySize;
            }

            /**
             * Sets the maximum body size to log for outgoing messages.
             *
             * @param maxBodySize the maximum body size to log for outgoing messages
             */
            public void setMaxBodySize(int maxBodySize) {
                this.maxBodySize = maxBodySize;
            }
        }
    }
}