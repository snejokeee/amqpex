# Changelog

All notable changes to this project will be documented in this file.

The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.0.0/),
and this project adheres to [Semantic Versioning](https://semver.org/spec/v2.0.0.html).

## [0.0.2] - 2025-11-14

### Added
- Header logging functionality for both incoming and outgoing messages
- New configuration options: `amqpex.logging.incoming.log-headers` and `amqpex.logging.outgoing.log-headers` to control header logging
- Comprehensive array formatting tests for all primitive types in header formatter
- Deep nesting tests with proper truncation when exceeding max depth
- Javadoc documentation for all test methods
- Updated documentation to reflect new header logging functionality

### Changed
- Reduced max recursion depth from 10 to 5 in header formatter for better safety
- Removed escaping functionality from header formatter for simplicity (will add back in future release if needed)
- Removed all inline comments inside method bodies for cleaner code
- Added proper Javadoc to all test method

## [0.0.1] - 2025-11-10

### Added
- Initial release of Amqpex library
- Incoming message logging with options to enable/disable and set max body size
- Outgoing message logging with options to enable/disable and set max body size
- Support for logging message exchange, routing key, content type and body for readable formats (JSON, XML, text)
- Configuration via amqpex.logging.incoming and amqpex.logging.outgoing properties