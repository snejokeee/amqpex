package dev.alubenets.amqpex;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "amqpex")
public class AmqpexProperties {

    private final LoggingConfiguration logging = new LoggingConfiguration();

    public LoggingConfiguration getLogging() {
        return logging;
    }

    public static class LoggingConfiguration {
        private final Incoming incoming = new Incoming();

        public Incoming getIncoming() {
            return incoming;
        }

        public static class Incoming {
            private boolean enabled = true;
            private int maxBodySize = 1000;

            public boolean isEnabled() {
                return enabled;
            }

            public void setEnabled(boolean enabled) {
                this.enabled = enabled;
            }

            public int getMaxBodySize() {
                return maxBodySize;
            }

            public void setMaxBodySize(int maxBodySize) {
                this.maxBodySize = maxBodySize;
            }
        }
    }
}