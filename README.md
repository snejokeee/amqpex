# Amqpex

Spring AMQP Extensions Library

This library provides useful extensions and enhancements for Spring AMQP, aiming to simplify common tasks and improve developer experience when working with RabbitMQ in Spring applications.

## Current Features

### 1. Incoming Message Logging
- **Automatic Logging**: Logs exchange, routing key, message headers, message properties, and message body for readable formats (JSON, XML, text)
- **Configuration Options**:
  - `amqpex.logging.incoming.enabled` (default: true) - Enable/disable the logging feature
  - `amqpex.logging.incoming.maxBodySize` (default: 1000) - Maximum body size to log (prevents log flooding)
  - `amqpex.logging.incoming.log-headers` (default: true) - Enable/disable header logging
- **Robust Processing**: Handles character encoding properly and gracefully handles logging failures without disrupting message flow
- **Performance Conscious**: Runs with the highest precedence to capture original message state while only logging when debug level is enabled

### 2. Outgoing Message Logging
- **Automatic Logging**: Logs exchange, routing key, message headers, message properties, and message body for readable formats (JSON, XML, text) for outgoing messages
- **Configuration Options**:
  - `amqpex.logging.outgoing.enabled` (default: true) - Enable/disable the logging feature
  - `amqpex.logging.outgoing.maxBodySize` (default: 1000) - Maximum body size to log (prevents log flooding)
  - `amqpex.logging.outgoing.log-headers` (default: true) - Enable/disable header logging
- **Robust Processing**: Handles character encoding properly and gracefully handles logging failures without disrupting message flow
- **Performance Conscious**: Runs with the lowest precedence to capture final message state before sending while only logging when debug level is enabled

## Getting Started

### Adding the Dependency

The library is currently available as a local build. Publication to Maven Central is planned for the next release.

### Basic Configuration

To enable both incoming and outgoing message logging with default settings:

```yaml
amqpex:
  logging:
    incoming:
      enabled: true
      max-body-size: 1000
      log-headers: true
    outgoing:
      enabled: true
      max-body-size: 1000
      log-headers: true
```

Or via properties:

```properties
amqpex.logging.incoming.enabled=true
amqpex.logging.incoming.max-body-size=1000
amqpex.logging.incoming.log-headers=true
amqpex.logging.outgoing.enabled=true
amqpex.logging.outgoing.max-body-size=1000
amqpex.logging.outgoing.log-headers=true
```

The library uses Spring Boot's autoconfiguration, so simply adding it to your project will enable the logging functionality automatically if the conditions are met.

## Architecture

The library uses a well-designed abstract class pattern with Java 21's sealed classes for extensibility:

- `LoggingMessagePostProcessor` (sealed abstract class) - Provides common functionality for message logging
- `IncomingMessageLogger` - Concrete implementation for incoming message logging
- `OutgoingMessageLogger` - Concrete implementation for outgoing message logging
- The architecture is designed for future extensibility

## Planned Features

*   **Dead-letter-based message retries** (useful when server-side configuration is restricted)
*   **Advanced connection customization utilities**
*   **Helper functions for common message handling patterns**

## Technology Stack

*   **Java**: Java 21
*   **Framework**: Spring Boot 3.5.7
*   **Build**: Gradle (Kotlin DSL)
*   **Dependencies**: Spring AMQP, Spring Rabbit, SLF4J API (with proper Maven/Gradle scopes for library consumers)
*   **Testing**: JUnit 5, Mockito, AssertJ

## Development Workflow

This project uses a branching strategy to keep the `master` branch stable for releases.

*   **`master` branch:** Contains the latest stable release.
*   **`develop` branch:** The primary integration branch for new features and fixes.
*   **Feature branches:** Created from `develop` for specific tasks (e.g., `feature/add-retry`, `bugfix/issue-123`), merged back into `develop` via PR.
*   **Releases:** `develop` is merged into `master` when ready for release, followed by a Git tag (e.g., `v1.0.0`).

For more details, see [CONTRIBUTING.md](CONTRIBUTING.md).

## License

This project is licensed under the MIT License - see the [LICENSE](LICENSE) file for details.

## Contributing

We welcome contributions! Please see [CONTRIBUTING.md](CONTRIBUTING.md) for details on how to get started.
