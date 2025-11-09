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
   - Logs exchange, routing key, message properties, and message body for readable formats (JSON, XML, text)
   - Configurable via `amqpex.logging.incoming.enabled` and `amqpex.logging.incoming.maxBodySize`
   - Runs with highest precedence to capture original message state
   - Handles character encoding and body truncation to prevent log flooding

2. **Outgoing Message Logging**:
   - Logs exchange, routing key, message properties, and message body for readable formats (JSON, XML, text)
   - Configurable via `amqpex.logging.outgoing.enabled` and `amqpex.logging.outgoing.maxBodySize`
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
│                   └── LoggingAutoConfigurationIntegrationTest.java
├── README.md
├── CONTRIBUTING.md
└── LICENSE
```

## Implemented Features
1. **Incoming Message Logging**:
   - Logs exchange, routing key, message properties, and message body for readable formats (JSON, XML, text)
   - Configurable via `amqpex.logging.incoming.enabled` and `amqpex.logging.incoming.maxBodySize`
   - Runs with highest precedence to capture original message state
   - Handles character encoding and body truncation to prevent log flooding

2. **Outgoing Message Logging**:
   - Logs exchange, routing key, message properties, and message body for readable formats (JSON, XML, text)
   - Configurable via `amqpex.logging.outgoing.enabled` and `amqpex.logging.outgoing.maxBodySize`
   - Runs with lowest precedence to capture final message state before sending
   - Handles character encoding and body truncation to prevent log flooding

## Configuration Properties
- `amqpex.logging.incoming.enabled` (default: true) - Enable/disable incoming message logging
- `amqpex.logging.incoming.maxBodySize` (default: 1000) - Maximum body size to log for incoming messages
- `amqpex.logging.outgoing.enabled` (default: true) - Enable/disable outgoing message logging 
- `amqpex.logging.outgoing.maxBodySize` (default: 1000) - Maximum body size to log for outgoing messages

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
- `AmqpexProperties` - Configuration properties class with nested structure for type-safe property binding

### Logging Package (`dev.alubenets.amqpex.logging`)
- `LoggingMessagePostProcessor` (sealed abstract class) - Provides common functionality for message logging with sealed class pattern for controlled extensibility
- `IncomingMessageLogger` - Concrete implementation for incoming message logging that extends the abstract base class
- `OutgoingMessageLogger` - Concrete implementation for outgoing message logging that extends the abstract base class  
- `LoggingAutoConfiguration` - Configures the logging feature based on properties

### Configuration Files
- `META-INF/spring/org.springframework.boot.autoconfigure.AutoConfiguration.imports` - Registers auto-configurations

## Dependencies
- Spring Boot 3.5.7
- Spring AMQP
- Spring Rabbit
- SLF4J API
- JUnit 5, Mockito, AssertJ for testing

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

5. **LoggingAutoConfigurationIntegrationTest** - Integration test for auto-configuration
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