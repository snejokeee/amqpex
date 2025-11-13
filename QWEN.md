# AMQPex Project Overview

## Overview
AMQPex is a Spring AMQP Extensions library that provides useful extensions and enhancements for Spring AMQP, aiming to simplify common tasks and improve developer experience when working with RabbitMQ in Spring applications.

## Project Status
- **Build System**: Gradle (Kotlin DSL)
- **Language**: Java 21
- **Framework**: Spring Boot 3.5.7
- **Status**: Production ready with comprehensive test coverage 
- **Current Features**: Incoming and outgoing message logging with configuration options

## Key Features
1. **Incoming Message Logging**:
   - Logs exchange, routing key, message headers, message properties, and message body for readable formats (JSON, XML, text)
   - Configurable via `amqpex.logging.incoming.enabled`, `amqpex.logging.incoming.maxBodySize`, and `amqpex.logging.incoming.logHeaders`
   - Runs with highest precedence to capture original message state
   - Handles character encoding and body truncation to prevent log flooding

2. **Outgoing Message Logging**:
   - Logs exchange, routing key, message headers, message properties, and message body for readable formats (JSON, XML, text)
   - Configurable via `amqpex.logging.outgoing.enabled`, `amqpex.logging.outgoing.maxBodySize`, and `amqpex.logging.outgoing.logHeaders`
   - Runs with lowest precedence to capture final message state before sending
   - Handles character encoding and body truncation to prevent log flooding

## Project Structure
```
amqpex/
├── build.gradle.kts
├── settings.gradle.kts
├── gradlew
├── gradlew.bat
├── gradle/
│   └── wrapper/
├── src/
│   ├── main/
│   │   ├── java/
│   │   │   └── dev/alubenets/amqpex/
│   │   │       ├── AmqpexAutoConfiguration.java
│   │   │       ├── AmqpexProperties.java
│   │   │       └── logging/
│   │   │           ├── LoggingMessagePostProcessor.java
│   │   │           ├── IncomingMessageLogger.java
│   │   │           ├── OutgoingMessageLogger.java
│   │   │           ├── HeaderFormatter.java
│   │   │           └── LoggingAutoConfiguration.java
│   │   └── resources/
│   │       └── META-INF/spring/org.springframework.boot.autoconfigure.AutoConfiguration.imports
│   └── test/
│       └── java/
│           └── dev/alubenets/amqpex/
│               ├── AmqpexPropertiesTest.java
│               ├── AmqpexPropertiesIntegrationTest.java
│               └── logging/
│                   ├── IncomingMessageLoggerTest.java
│                   ├── OutgoingMessageLoggerTest.java
│                   ├── HeaderFormatterTest.java
│                   └── LoggingAutoConfigurationIntegrationTest.java
├── README.md
├── CONTRIBUTING.md
└── LICENSE
```

## Implemented Features
1. **Incoming Message Logging**:
   - Logs exchange, routing key, message headers, message properties, and message body for readable formats (JSON, XML, text)
   - Configurable via `amqpex.logging.incoming.enabled`, `amqpex.logging.incoming.maxBodySize`, and `amqpex.logging.incoming.logHeaders`
   - Runs with highest precedence to capture original message state
   - Handles character encoding and body truncation to prevent log flooding

2. **Outgoing Message Logging**:
   - Logs exchange, routing key, message headers, message properties, and message body for readable formats (JSON, XML, text)
   - Configurable via `amqpex.logging.outgoing.enabled`, `amqpex.logging.outgoing.maxBodySize`, and `amqpex.logging.outgoing.logHeaders`
   - Runs with lowest precedence to capture final message state before sending
   - Handles character encoding and body truncation to prevent log flooding

## Configuration Properties
- `amqpex.logging.incoming.enabled` (default: true) - Enable/disable incoming message logging
- `amqpex.logging.incoming.maxBodySize` (default: 1000) - Maximum body size to log for incoming messages
- `amqpex.logging.incoming.logHeaders` (default: true) - Enable/disable header logging for incoming messages
- `amqpex.logging.outgoing.enabled` (default: true) - Enable/disable outgoing message logging
- `amqpex.logging.outgoing.maxBodySize` (default: 1000) - Maximum body size to log for outgoing messages
- `amqpex.logging.outgoing.logHeaders` (default: true) - Enable/disable header logging for outgoing messages

## Architecture Pattern
- Abstract base class pattern using sealed classes for controlled extensibility
- `LoggingMessagePostProcessor` (sealed abstract class) provides common functionality for message logging
- `IncomingMessageLogger` extends the abstract class with specific incoming message behavior
- `OutgoingMessageLogger` extends the abstract class with specific outgoing message behavior
- All logging functionality runs with appropriate precedence to capture message state
- Message integrity maintained (messages are never modified during logging)

## Key Classes and Files

### Main Package (`dev.alubenets.amqpex`)
- `AmqpexAutoConfiguration` - Main Spring auto-configuration class that enables the extensions
- `AmqpexProperties` - Configuration properties class with nested structure for type-safe property binding, including header logging options

### Logging Package (`dev.alubenets.amqpex.logging`)
- `LoggingMessagePostProcessor` (sealed abstract class) - Provides common functionality for message logging with sealed class pattern for controlled extensibility
- `IncomingMessageLogger` - Concrete implementation for incoming message logging that extends the abstract base class
- `OutgoingMessageLogger` - Concrete implementation for outgoing message logging that extends the abstract base class
- `LoggingAutoConfiguration` - Configures the logging feature based on properties
- `HeaderFormatter` - Utility class for formatting message headers with robust handling of different value types, recursion depth limiting, and string escaping

### Configuration Files
- `META-INF/spring/org.springframework.boot.autoconfigure.AutoConfiguration.imports` - Registers auto-configurations

## Dependencies
- Spring Boot 3.5.7 (api scope) - provides auto-configuration and configuration properties support
- Spring AMQP (api scope) - core dependency for AMQP functionality, provides interfaces like MessagePostProcessor
- Spring Rabbit (api scope) - provides RabbitTemplate and other RabbitMQ-specific functionality
- SLF4J API (api scope) - logging facade required for users to see logs from the library
- RabbitMQ Client (compileOnly scope) - dependency only required when user has RabbitMQ in their classpath
- JUnit 5, Mockito, AssertJ for testing

## Dependency Management
The project follows proper library dependency management practices to ensure correct transitive dependencies for users:
- **api scope**: Used for dependencies that form part of the library's public API or are required for the library to function properly when used by others (e.g., SLF4J API, Spring AMQP)
- **compileOnly scope**: Used for dependencies needed at compile time but that should not be passed to library consumers (e.g., RabbitMQ Client, which is used in conditional annotations)
- **implementation scope**: Used for internal dependencies that are not part of the public API

## Test Coverage
All tests have been polished to maintain consistent style with proper JavaDoc:

1. **AmqpexPropertiesTest** - Unit tests for properties binding using Spring's DataBinder
   - `shouldBindCustomValuesUsingDataBinder()` - Tests custom property binding
   - `shouldUseDefaultValuesWhenNotBoundUsingDataBinder()` - Tests default values
   - `shouldBindCustomValuesUsingDataBinderWithMap()` - Tests direct map binding

2. **AmqpexPropertiesIntegrationTest** - Integration tests for properties in Spring context
   - `WithCustomProperties.shouldBindCustomPropertiesCorrectlyWithSpringBootMechanism()` - Tests Spring Boot property binding
   - `WithDefaultProperties.shouldUseDefaultValuesWithSpringBootMechanism()` - Tests default values in Spring context

3. **IncomingMessageLoggerTest** - Comprehensive unit tests for incoming message logging functionality using abstract class pattern
   - Basic functionality tests (enabled/disabled)
   - Content type handling tests (null, binary, readable types)
   - Body processing tests (truncation, empty)
   - Character encoding tests (decoding failure, custom charset)
   - Message integrity tests (unchanged message, logging failure resilience)
   - Parameterized test for readable content types

4. **OutgoingMessageLoggerTest** - Comprehensive unit tests for outgoing message logging functionality using abstract class pattern (mirrors IncomingMessageLoggerTest)
   - Basic functionality tests (enabled/disabled)
   - Content type handling tests (null, binary, readable types)
   - Body processing tests (truncation, empty)
   - Character encoding tests (decoding failure, custom charset)
   - Message integrity tests (unchanged message, logging failure resilience)
   - Parameterized test for readable content types

5. **HeaderFormatterTest** - Unit tests for header formatting functionality
   - Basic formatting tests (null, empty, string headers)
   - Value type tests (different data types, lists, arrays)
   - Complex formatting tests (nested structures, recursion depth)

6. **LoggingAutoConfigurationIntegrationTest** - Integration test for auto-configuration
   - Tests that both incoming and outgoing logging post processors are correctly added to their respective containers when enabled
   - Tests that logging post processors are not added when disabled

## Planned Features (from README)
1. **Dead-letter-based message retries** (when server-side configuration is restricted)
2. **Advanced connection customization utilities**
3. **Helper functions for common message handling patterns**

## Development and Build
- Build with: `./gradlew build`
- Run tests: `./gradlew test`
- Java compatibility: Java 21
- Dependencies managed via Spring Boot dependency management BOM

## Architecture Pattern
- Follows Spring Boot auto-configuration pattern
- Uses `@ConfigurationProperties` for type-safe property binding
- Implements `MessagePostProcessor` for message processing
- Uses `ContainerCustomizer` to apply post-processors to message listener containers
- Uses `RabbitTemplateCustomizer` to apply post-processors to RabbitTemplate for outgoing messages
- Employs proper separation of concerns with dedicated packages

## Code Quality
- All tests pass 
- Complete Javadoc documentation without any warnings
- Consistent code style with proper JavaDoc
- Proper error handling and logging
- Maintains message integrity (doesn't modify original messages)
- Resilient to logging subsystem failures