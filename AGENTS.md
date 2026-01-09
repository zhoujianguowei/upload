# Agent Guidelines for This Repository

## Build Commands

This is a Maven-based Java 8 project.

```bash
# Compile the project
mvn compile

# Build JAR with dependencies
mvn package

# Run the main application (Async class is entry point)
mvn exec:java -Dexec.mainClass=Async

# Clean build artifacts
mvn clean
```

## Testing

```bash
# Run all tests
mvn test

# Run a single test class
mvn test -Dtest=ClassNameTest

# Run a single test method
mvn test -Dtest=ClassNameTest#methodName

# Run tests with verbose output
mvn test -X
```

## Code Style Guidelines

### Imports
- Group imports in this order: java.*, javax.*, org.*, com.*, static imports at the end
- Use static imports for constants and enum values (e.g., `import static rpc.thrift.file.transfer.ResResult.FILE_END;`)
- Avoid wildcard imports except for static constants

### Formatting
- Indent with 4 spaces (no tabs)
- Line length: 120 characters max
- Place opening brace `{` on same line as declaration
- Use blank lines to separate logical sections within methods
- No trailing whitespace

### Types
- Use `long` for file offsets and sizes (e.g., `long startPos = 0L;`)
- Use `int` for counts, retry times, and indices
- Prefer primitive types over wrappers except where null is needed
- Use `String` for all text, `boolean` for flags

### Naming Conventions
- **Classes**: PascalCase (e.g., `DefaultClientWorker`, `FileHandlerHelper`)
- **Methods**: camelCase (e.g., `handleUploadFile`, `generateFileToken`)
- **Variables**: camelCase (e.g., `fileUploadRequest`, `maxRetryTimes`)
- **Constants**: UPPER_SNAKE_CASE (e.g., `MAX_PARALLEL_UPLOAD_FILE_NUM`)
- **Packages**: lowercase (e.g., `handler`, `worker`, `common`)
- **Enums**: PascalCase, fields UPPER_SNAKE_CASE

### Error Handling
- Throw `RuntimeException` or subclasses for unrecoverable errors
- Use `try-catch` with specific exception types, not generic `Exception`
- Log errors with context using SLF4J: `LOGGER.error("message||key=value", e)`
- Return status enums (e.g., `ClientUploadStatus.FAIL`) for recoverable failures
- Validate inputs early and throw `IllegalArgumentException` for bad params

### Logging
- Use SLF4J logger: `private static final Logger LOGGER = LoggerFactory.getLogger(ClassName.class);`
- Log at appropriate level: ERROR for failures, WARN for warnings, INFO for significant events
- Include contextual data in log messages using `||key=value` format

### General Patterns
- Use enums for status/strategy values (see `RetryStrategyEnum`, `ClientUploadStatus`)
- Make fields `private` with accessor methods or package-private if in same package
- Use abstract classes for base implementations with shared logic
- Document public APIs with Javadoc; omit for private methods
- Initialize collections inline when possible
