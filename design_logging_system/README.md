# Logger System - Low Level Design

A simple, clean, and extensible logging system implementation using Chain of Responsibility pattern - perfect for interview discussions.

## 📁 Project Structure

```
src/
├── CommonEnum/
│   └── LogLevel.java              # Log level enumeration (DEBUG, INFO, ERROR)
├── UtilityClasses/
│   └── LogMessage.java            # Immutable log message object
├── CORPattern/
│   ├── LogHandler.java            # Abstract handler for Chain of Responsibility
│   └── ConcreteLogHandlers/
│       ├── DebugLogger.java       # Handles DEBUG level
│       ├── InfoLogger.java        # Handles INFO level
│       └── ErrorLogger.java       # Handles ERROR level
├── LogAppenderStrategies/
│   ├── LogAppender.java           # Strategy interface for output destinations
│   └── ConcreteLogAppenders/
│       ├── ConsoleAppender.java   # Console output implementation
│       └── FileAppender.java      # File output implementation
├── LoggerControllers/
│   └── Logger.java                # Main logger class (uses COR pattern)
└── Main.java                      # Demo usage
```

## 🎯 Key Features

- ✅ **Multiple Log Levels**: DEBUG, INFO, ERROR
- ✅ **Chain of Responsibility**: Simple COR pattern for level handling
- ✅ **Level Filtering**: Only logs messages >= minimum level
- ✅ **Multiple Output Destinations**: Console, File (easily extensible)
- ✅ **Strategy Pattern**: Clean separation for different appenders
- ✅ **Thread-Safe Singleton**: Optional singleton pattern
- ✅ **Simple & Maintainable**: Clean COR implementation

## 🚀 Quick Start

### Example 1: Console Logging with INFO Level
```java
Logger logger = Logger.create(LogLevel.INFO, new ConsoleAppender());
// Chain: INFO -> ERROR (skips DEBUG)
logger.debug("This will be filtered");  // Not logged (DEBUG < INFO)
logger.info("This will be logged");      // Logged by InfoLogger
logger.error("This will be logged");     // Logged by ErrorLogger
```

### Example 2: File Logging with DEBUG Level
```java
Logger logger = Logger.create(LogLevel.DEBUG, new FileAppender("app.log"));
// Chain: DEBUG -> INFO -> ERROR
logger.debug("All messages logged to file");
logger.info("All messages logged to file");
logger.error("All messages logged to file");
```

### Example 3: Singleton Logger
```java
Logger logger = Logger.getInstance(LogLevel.INFO, new ConsoleAppender());
logger.info("Using singleton logger");
```

## 📊 Design Patterns

### 1. Chain of Responsibility Pattern
- **Abstract Handler**: `LogHandler`
- **Concrete Handlers**: `DebugLogger`, `InfoLogger`, `ErrorLogger`
- **How it works**: 
  - Chain is built starting from `minLevel` to ERROR
  - Each handler logs if message level matches its level
  - Message flows through the chain
- **Example**: If `minLevel = INFO`, chain is `INFO -> ERROR`

### 2. Strategy Pattern
- **Interface**: `LogAppender`
- **Implementations**: `ConsoleAppender`, `FileAppender`
- **Benefit**: Easy to add new appenders (Database, Network, etc.) without modifying existing code

### 3. Singleton Pattern (Optional)
- **Method**: `Logger.getInstance()`
- **Benefit**: Single logger instance across application
- **Note**: Also provides `create()` for multiple instances

## 🔍 How Chain of Responsibility Works

```
minLevel = INFO
Chain: INFO -> ERROR

When INFO message comes:
  INFO Handler: Logs (INFO == INFO) → Passes to ERROR
  ERROR Handler: No log (INFO != ERROR)

When ERROR message comes:
  INFO Handler: No log (ERROR != INFO) → Passes to ERROR
  ERROR Handler: Logs (ERROR == ERROR)
```

## 📖 Documentation

- **[LLD_DIAGRAM.md](./LLD_DIAGRAM.md)**: Comprehensive Low Level Design document with:
  - Class diagrams (Box diagrams)
  - Sequence diagrams
  - Chain of Responsibility flow
  - Interview discussion points
  - Use cases
  - Future enhancements

## 🧪 Running the Code

```bash
# Compile
javac -d out src/**/*.java src/Main.java

# Run
java -cp out Main
```

## 💡 Interview Points

1. **Design Patterns**: Chain of Responsibility, Strategy Pattern, optional Singleton
2. **COR Implementation**: Simple and clean chain building
3. **Extensibility**: Easy to add new log levels and appenders
4. **Thread Safety**: Singleton uses double-checked locking
5. **SOLID Principles**: Open/Closed Principle (extensible without modification)

## 🔮 Future Enhancements

- Async logging with thread pool
- Log formatting interface
- Multiple appenders per logger
- Log rotation for FileAppender
- Structured logging (JSON/XML)
- More complex filtering rules

---

**Note**: This is a simplified, interview-ready implementation using Chain of Responsibility pattern. For production use, consider libraries like Log4j, SLF4J, or Logback.
