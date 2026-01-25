# Logger System - Low Level Design (LLD)

## Table of Contents
1. [Problem Statement](#problem-statement)
2. [Requirements](#requirements)
3. [Class Diagram](#class-diagram)
4. [Component Diagram](#component-diagram)
5. [Architecture Diagram](#architecture-diagram)
6. [Sequence Diagrams](#sequence-diagrams)
7. [Design Patterns Used](#design-patterns-used)
8. [Component Details](#component-details)
9. [Use Cases](#use-cases)
10. [Interview Discussion Points](#interview-discussion-points)

---

## Problem Statement

Design a logging system that:
- Supports multiple log levels (DEBUG, INFO, ERROR)
- Can output logs to different destinations (Console, File, etc.)
- Filters logs based on minimum log level
- Is extensible for future log destinations

---

## Requirements

### Functional Requirements
1. Support multiple log levels: DEBUG, INFO, ERROR
2. Filter logs based on minimum log level threshold
3. Support multiple output destinations (Console, File)
4. Provide convenient methods: `debug()`, `info()`, `error()`
5. Include timestamp in log messages

### Non-Functional Requirements
1. Thread-safe (if using singleton)
2. Extensible (easy to add new appenders)
3. Simple and maintainable code
4. Good separation of concerns

---

## Class Diagram

### Box Diagram - Class Structure

```
┌─────────────────────────────────────────────────────────────────────────┐
│                           LOGGER SYSTEM                                  │
│                    (Chain of Responsibility Pattern)                    │
└─────────────────────────────────────────────────────────────────────────┘

┌─────────────────────────────────────────────────────────────────────────┐
│                              LogLevel                                    │
│                          <<enumeration>>                                 │
├─────────────────────────────────────────────────────────────────────────┤
│ + DEBUG : LogLevel                                                       │
│ + INFO : LogLevel                                                        │
│ + ERROR : LogLevel                                                       │
├─────────────────────────────────────────────────────────────────────────┤
│ - value : int                                                            │
├─────────────────────────────────────────────────────────────────────────┤
│ + getValue() : int                                                       │
│ + isGreaterOrEqual(LogLevel) : boolean                                   │
└─────────────────────────────────────────────────────────────────────────┘
                                      ▲
                                      │ uses
                                      │
┌─────────────────────────────────────────────────────────────────────────┐
│                            LogMessage                                    │
│                      <<Immutable Value Object>>                          │
├─────────────────────────────────────────────────────────────────────────┤
│ - level : LogLevel                                                       │
│ - message : String                                                       │
│ - timestamp : long                                                       │
├─────────────────────────────────────────────────────────────────────────┤
│ + LogMessage(LogLevel, String)                                          │
│ + getLevel() : LogLevel                                                  │
│ + getMessage() : String                                                  │
│ + getTimestamp() : long                                                  │
│ + toString() : String                                                    │
└─────────────────────────────────────────────────────────────────────────┘
                                      ▲
                                      │ creates/uses
                                      │
┌─────────────────────────────────────────────────────────────────────────┐
│                              Logger                                      │
│              <<Main Controller with COR Pattern>>                        │
├─────────────────────────────────────────────────────────────────────────┤
│ - minLevel : LogLevel                                                    │
│ - chainHead : LogHandler                                                 │
├─────────────────────────────────────────────────────────────────────────┤
│ - Logger(LogLevel, LogAppender) {private}                                │
│ - buildChain(LogLevel, LogAppender) : LogHandler                         │
│ + getInstance(LogLevel, LogAppender) : Logger {static}                   │
│ + create(LogLevel, LogAppender) : Logger {static}                        │
│ + log(LogLevel, String) : void                                           │
│ + debug(String) : void                                                    │
│ + info(String) : void                                                     │
│ + error(String) : void                                                   │
└─────────────────────────────────────────────────────────────────────────┘
                                      │ uses
                                      │ builds chain
                                      ▼
┌─────────────────────────────────────────────────────────────────────────┐
│                           LogHandler                                     │
│                    <<Abstract Handler (COR)>>                            │
├─────────────────────────────────────────────────────────────────────────┤
│ # level : LogLevel                                                       │
│ # nextHandler : LogHandler                                               │
│ # appender : LogAppender                                                 │
├─────────────────────────────────────────────────────────────────────────┤
│ + LogHandler(LogLevel, LogAppender)                                      │
│ + setNext(LogHandler) : void                                             │
│ + handle(LogLevel, String) : void                                       │
└─────────────────────────────────────────────────────────────────────────┘
                                      ▲
                                      │ extends
                    ┌─────────────────┼─────────────────┐
                    │                 │                 │
                    ▼                 ▼                 ▼
┌──────────────────────────┐  ┌──────────────────┐  ┌──────────────────┐
│      DebugLogger         │  │   InfoLogger     │  │   ErrorLogger    │
│  <<Concrete Handler>>    │  │ <<Concrete Handler>>│  │<<Concrete Handler>>│
├──────────────────────────┤  ├──────────────────┤  ├──────────────────┤
│ + DebugLogger(Appender)  │  │ + InfoLogger(Appender)││ + ErrorLogger(Appender)│
└──────────────────────────┘  └──────────────────┘  └──────────────────┘
                                      │
                                      │ uses (Strategy Pattern)
                                      ▼
┌─────────────────────────────────────────────────────────────────────────┐
│                           LogAppender                                    │
│                            <<interface>>                                 │
├─────────────────────────────────────────────────────────────────────────┤
│ + append(LogMessage) : void                                             │
└─────────────────────────────────────────────────────────────────────────┘
                                      ▲
                                      │ implements
                    ┌─────────────────┴─────────────────┐
                    │                                     │
┌───────────────────┴───────────────────┐  ┌────────────┴──────────────────┐
│        ConsoleAppender                │  │        FileAppender              │
│    <<Concrete Strategy>>              │  │    <<Concrete Strategy>>        │
├───────────────────────────────────────┤  ├─────────────────────────────────┤
│ + append(LogMessage) : void           │  │ - filePath : String             │
│                                       │  │                                 │
│                                       │  │ + FileAppender(String)          │
│                                       │  │ + append(LogMessage) : void     │
└───────────────────────────────────────┘  └─────────────────────────────────┘

Chain Example (minLevel = INFO):
┌──────────┐    ┌──────────┐
│  INFO    │───>│  ERROR   │
│ Handler  │    │ Handler  │
└──────────┘    └──────────┘
   (skips DEBUG)
```

┌─────────────────────────────────────────────────────────────────────────┐
│                              LogLevel                                    │
│                          <<enumeration>>                                 │
├─────────────────────────────────────────────────────────────────────────┤
│ + DEBUG : LogLevel                                                       │
│ + INFO : LogLevel                                                        │
│ + ERROR : LogLevel                                                       │
├─────────────────────────────────────────────────────────────────────────┤
│ - value : int                                                            │
├─────────────────────────────────────────────────────────────────────────┤
│ + getValue() : int                                                       │
│ + isGreaterOrEqual(LogLevel) : boolean                                   │
└─────────────────────────────────────────────────────────────────────────┘
                                      ▲
                                      │ uses
                                      │
┌─────────────────────────────────────────────────────────────────────────┐
│                            LogMessage                                    │
│                      <<Immutable Value Object>>                          │
├─────────────────────────────────────────────────────────────────────────┤
│ - level : LogLevel                                                       │
│ - message : String                                                       │
│ - timestamp : long                                                       │
├─────────────────────────────────────────────────────────────────────────┤
│ + LogMessage(LogLevel, String)                                          │
│ + getLevel() : LogLevel                                                  │
│ + getMessage() : String                                                  │
│ + getTimestamp() : long                                                  │
│ + toString() : String                                                    │
└─────────────────────────────────────────────────────────────────────────┘
                                      ▲
                                      │ creates/uses
                                      │
┌─────────────────────────────────────────────────────────────────────────┐
│                              Logger                                      │
│                    <<Main Controller Class>>                             │
├─────────────────────────────────────────────────────────────────────────┤
│ - minLevel : LogLevel                                                    │
│ - appender : LogAppender                                                 │
│ - instance : Logger {static}                                             │
├─────────────────────────────────────────────────────────────────────────┤
│ - Logger(LogLevel, LogAppender) {private}                               │
│ + getInstance(LogLevel, LogAppender) : Logger {static}                   │
│ + create(LogLevel, LogAppender) : Logger {static}                        │
│ + log(LogLevel, String) : void                                          │
│ + debug(String) : void                                                    │
│ + info(String) : void                                                     │
│ + error(String) : void                                                   │
└─────────────────────────────────────────────────────────────────────────┘
                                      │ uses (Strategy Pattern)
                                      │
                                      ▼
┌─────────────────────────────────────────────────────────────────────────┐
│                           LogAppender                                    │
│                            <<interface>>                                 │
├─────────────────────────────────────────────────────────────────────────┤
│ + append(LogMessage) : void                                             │
└─────────────────────────────────────────────────────────────────────────┘
                                      ▲
                                      │ implements
                    ┌─────────────────┴─────────────────┐
                    │                                     │
┌───────────────────┴───────────────────┐  ┌────────────┴──────────────────┐
│        ConsoleAppender                │  │        FileAppender              │
│    <<Concrete Strategy>>              │  │    <<Concrete Strategy>>        │
├───────────────────────────────────────┤  ├─────────────────────────────────┤
│ + append(LogMessage) : void           │  │ - filePath : String             │
│                                       │  │                                 │
│                                       │  │ + FileAppender(String)          │
│                                       │  │ + append(LogMessage) : void     │
└───────────────────────────────────────┘  └─────────────────────────────────┘
```

### Relationship Legend:
- `uses` → Dependency/Composition
- `implements` → Interface Realization
- `creates` → Object Creation

---

## Component Diagram

### Box Diagram - System Architecture

```
┌──────────────────────────────────────────────────────────────────────────────┐
│                           CLIENT APPLICATION                                 │
├──────────────────────────────────────────────────────────────────────────────┤
│                                                                              │
│  ┌────────────────────────────────────────────────────────────────────┐     │
│  │                          Client Code                               │     │
│  │                                                                     │     │
│  │  logger.info("User logged in");                                    │     │
│  │  logger.error("Database error");                                    │     │
│  └────────────────────────────────────────────────────────────────────┘     │
│                                                                              │
└──────────────────────────────────────────────────────────────────────────────┘
                                    │
                                    │ calls
                                    ▼
┌──────────────────────────────────────────────────────────────────────────────┐
│                            LOGGER SYSTEM                                     │
├──────────────────────────────────────────────────────────────────────────────┤
│                                                                              │
│  ┌────────────────────────────────────────────────────────────────────┐     │
│  │                            Logger                                  │     │
│  │                    <<Core Controller>>                             │     │
│  │                                                                     │     │
│  │  • Level Filtering                                                 │     │
│  │  • Message Creation                                                │     │
│  │  • Appender Delegation                                             │     │
│  └────────────────────────────────────────────────────────────────────┘     │
│                                    │                                         │
│                    ┌───────────────┼───────────────┐                        │
│                    │               │               │                         │
│                    ▼               ▼               ▼                         │
│  ┌──────────────────┐  ┌──────────────────┐  ┌──────────────────┐         │
│  │   LogLevel       │  │   LogMessage     │  │  LogAppender     │         │
│  │   <<Enum>>       │  │   <<Value Obj>>  │  │  <<Interface>>   │         │
│  │                  │  │                  │  │                  │         │
│  │  DEBUG = 1       │  │  • level         │  │  + append()     │         │
│  │  INFO = 2        │  │  • message       │  └──────────────────┘         │
│  │  ERROR = 3       │  │  • timestamp     │           ▲                    │
│  └──────────────────┘  └──────────────────┘           │                    │
│                                                         │                    │
│                                              ┌──────────┴──────────┐        │
│                                              │                     │        │
│                                              ▼                     ▼        │
│                              ┌──────────────────┐  ┌──────────────────┐     │
│                              │ ConsoleAppender  │  │  FileAppender   │     │
│                              │                  │  │                  │     │
│                              │  • Writes to     │  │  • filePath     │     │
│                              │    System.out    │  │  • Writes to    │     │
│                              └──────────────────┘  │    file         │     │
│                                                     └──────────────────┘     │
│                                                                              │
└──────────────────────────────────────────────────────────────────────────────┘
                                    │
                                    │ writes to
                    ┌───────────────┴───────────────┐
                    │                               │
                    ▼                               ▼
┌───────────────────────────────┐  ┌───────────────────────────────┐
│        CONSOLE/STDOUT         │  │       FILE SYSTEM             │
│                               │  │                               │
│  [INFO] 1234567890 - Message  │  │  logs.txt                     │
│  [ERROR] 1234567891 - Error   │  │  app.log                      │
└───────────────────────────────┘  └───────────────────────────────┘
```

---

## Architecture Diagram

### Box Diagram - High-Level Architecture

```
┌────────────────────────────────────────────────────────────────────────────┐
│                         LOGGER SYSTEM ARCHITECTURE                          │
└────────────────────────────────────────────────────────────────────────────┘

    ┌──────────────────────────────────────────────────────────────┐
    │                      APPLICATION LAYER                        │
    │                                                               │
    │  ┌──────────────┐  ┌──────────────┐  ┌──────────────┐      │
    │  │   Service    │  │  Controller │  │   Utility    │      │
    │  │    Class     │  │    Class    │  │    Class     │      │
    │  └──────┬───────┘  └──────┬───────┘  └──────┬───────┘      │
    │         │                 │                  │               │
    │         └─────────────────┼──────────────────┘               │
    │                           │                                   │
    │                           ▼                                   │
    │                  ┌──────────────────┐                        │
    │                  │     Logger       │                        │
    │                  │  <<Facade>>      │                        │
    │                  └────────┬─────────┘                        │
    └───────────────────────────┼───────────────────────────────────┘
                                │
                ┌───────────────┼───────────────┐
                │               │               │
                ▼               ▼               ▼
    ┌──────────────────┐  ┌──────────────┐  ┌──────────────────┐
    │   LogLevel       │  │  LogMessage  │  │  LogAppender    │
    │   Manager        │  │   Builder    │  │   Strategy      │
    └──────────────────┘  └──────────────┘  └────────┬─────────┘
                                                      │
                                      ┌───────────────┼───────────────┐
                                      │               │               │
                                      ▼               ▼               ▼
                          ┌──────────────┐  ┌──────────────┐  ┌──────────────┐
                          │  Console    │  │    File      │  │  Database    │
                          │  Appender   │  │   Appender   │  │   Appender   │
                          │             │  │              │  │  (Future)    │
                          └──────┬──────┘  └──────┬───────┘  └──────┬───────┘
                                 │                │                  │
                                 └────────────────┼──────────────────┘
                                                   │
                                                   ▼
                          ┌──────────────────────────────────┐
                          │      OUTPUT DESTINATIONS         │
                          │                                  │
                          │  • Console/Stdout                │
                          │  • File System                   │
                          │  • Database (Future)             │
                          │  • Network (Future)              │
                          └──────────────────────────────────┘
```

### Data Flow Box Diagram

```
┌────────────────────────────────────────────────────────────────────────────┐
│                          LOGGING DATA FLOW                                 │
└────────────────────────────────────────────────────────────────────────────┘

    Client Code
         │
         │ 1. logger.info("message")
         ▼
    ┌─────────────┐
    │   Logger   │
    │            │
    │  ┌───────┐ │
    │  │Filter │ │ 2. Check: level >= minLevel?
    │  └───┬───┘ │
    │      │     │
    │      │ Yes │
    │      ▼     │
    │  ┌─────────┐│
    │  │ Create ││ 3. new LogMessage(level, message, timestamp)
    │  │Message ││
    │  └───┬────┘│
    │      │     │
    │      │     │
    └──────┼─────┘
           │
           │ 4. appender.append(logMessage)
           ▼
    ┌─────────────┐
    │ LogAppender │
    │  Interface  │
    └──────┬──────┘
           │
    ┌──────┴──────┐
    │             │
    ▼             ▼
┌─────────┐  ┌─────────┐
│Console  │  │  File  │
│Appender │  │Appender│
└────┬────┘  └────┬────┘
     │           │
     │           │
     ▼           ▼
┌─────────┐  ┌─────────┐
│ Console │  │  File   │
│ Output  │  │ System  │
└─────────┘  └─────────┘
```

---

## Sequence Diagrams

### Sequence Diagram 1: Successful Logging (Box Format)

```
┌────────────────────────────────────────────────────────────────────────────┐
│                    SEQUENCE: Logging INFO Message                           │
└────────────────────────────────────────────────────────────────────────────┘

Client          Logger          LogMessage      LogAppender    ConsoleAppender
  │               │                │                │                │
  │               │                │                │                │
  │─info("msg")──>│                │                │                │
  │               │                │                │                │
  │               │─Check Level───>│                │                │
  │               │  INFO >= INFO │                │                │
  │               │  (2 >= 2) ✓   │                │                │
  │               │                │                │                │
  │               │─new LogMessage─>│                │                │
  │               │                │─Set timestamp─>│                │
  │               │                │<─return───────│                │
  │               │<─logMessage────│                │                │
  │               │                │                │                │
  │               │─append(msg)───>│                │                │
  │               │                │                │                │
  │               │                │                │─append(msg)───>│
  │               │                │                │                │
  │               │                │                │                │─println()
  │               │                │                │                │
  │               │                │                │<─return───────│
  │               │                │                │                │
  │               │<─return────────│                │                │
  │               │                │                │                │
  │<─return───────│                │                │                │
  │               │                │                │                │
```

### Sequence Diagram 2: Filtered Message (Box Format)

```
┌────────────────────────────────────────────────────────────────────────────┐
│                    SEQUENCE: Filtered DEBUG Message                         │
└────────────────────────────────────────────────────────────────────────────┘

Client          Logger
  │               │
  │               │  minLevel = INFO (value = 2)
  │               │
  │─debug("msg")─>│
  │               │
  │               │─Check Level───>│
  │               │  DEBUG >= INFO │
  │               │  (1 >= 2) ✗    │
  │               │                │
  │               │  [Message Filtered]
  │               │  [No Output]
  │               │                │
  │<─return───────│                │
  │               │                │
```

### Sequence Diagram 3: Logger Creation (Box Format)

```
┌────────────────────────────────────────────────────────────────────────────┐
│                    SEQUENCE: Creating Logger with File Appender             │
└────────────────────────────────────────────────────────────────────────────┘

Client          Logger          FileAppender      FileSystem
  │               │                │                │
  │─create()──────>│                │                │
  │  (INFO,        │                │                │
  │   fileApp)     │                │                │
  │               │                │                │
  │               │─new Logger()──>│                │
  │               │                │                │
  │<─logger───────│                │                │
  │               │                │                │
  │─error("msg")──>│                │                │
  │               │                │                │
  │               │─Check Level───>│                │
  │               │  ERROR >= INFO │                │
  │               │  (3 >= 2) ✓    │                │
  │               │                │                │
  │               │─Create Msg─────>│                │
  │               │                │                │
  │               │─append(msg)───>│                │
  │               │                │                │
  │               │                │─Write to file─>│
  │               │                │                │
  │               │                │<─Success───────│
  │               │                │                │
  │               │<─return────────│                │
  │               │                │                │
  │<─return───────│                │                │
  │               │                │                │
```

### Sequence Diagram 4: Singleton Pattern (Box Format)

```
┌────────────────────────────────────────────────────────────────────────────┐
│                    SEQUENCE: Singleton Logger Creation                     │
└────────────────────────────────────────────────────────────────────────────┘

Thread1         Thread2         Logger          Synchronized Lock
  │               │               │                    │
  │─getInstance()─>│               │                    │
  │               │               │                    │
  │               │               │─Check: instance    │
  │               │               │  == null?          │
  │               │               │  Yes               │
  │               │               │                    │
  │               │               │─Acquire Lock──────>│
  │               │               │                    │
  │               │               │─Double-check:      │
  │               │               │  instance == null? │
  │               │               │  Yes               │
  │               │               │                    │
  │               │               │─Create Instance───>│
  │               │               │                    │
  │               │               │─Release Lock───────>│
  │               │               │                    │
  │<─instance─────│               │                    │
  │               │               │                    │
  │               │─getInstance()─>│                    │
  │               │               │                    │
  │               │               │─Check: instance    │
  │               │               │  == null?          │
  │               │               │  No (exists)       │
  │               │               │                    │
  │               │<─instance─────│                    │
  │               │               │                    │
```

---

## Design Patterns Used

### 1. Chain of Responsibility Pattern (Simple Implementation)

```
┌────────────────────────────────────────────────────────────────────────────┐
│                    CHAIN OF RESPONSIBILITY PATTERN                        │
└────────────────────────────────────────────────────────────────────────────┘

How it works:
┌──────────────────────────────────────────────────────────────────────────┐
│  1. Build chain starting from minLevel                                    │
│     Example: minLevel = INFO → Chain: INFO -> ERROR                       │
│                                                                           │
│  2. Each handler checks if message level matches its level                │
│     • If match: Log the message                                           │
│     • Always: Pass to next handler                                        │
│                                                                           │
│  3. Message flows through chain                                           │
│     • INFO message: INFO handler logs, passes to ERROR (no log)          │
│     • ERROR message: INFO handler (no log), ERROR handler logs            │
└──────────────────────────────────────────────────────────────────────────┘

Chain Flow Example (minLevel = INFO):
┌──────────────────────────────────────────────────────────────────────────┐
│                                                                           │
│  Message: INFO                                                            │
│      │                                                                    │
│      ▼                                                                    │
│  ┌──────────┐                                                            │
│  │  INFO    │───> Logs message (INFO == INFO)                            │
│  │ Handler  │                                                            │
│  └────┬─────┘                                                            │
│       │ Pass to next                                                     │
│       ▼                                                                    │
│  ┌──────────┐                                                            │
│  │  ERROR   │───> No log (INFO != ERROR)                                │
│  │ Handler  │                                                            │
│  └──────────┘                                                            │
│                                                                           │
│  Message: ERROR                                                           │
│      │                                                                    │
│      ▼                                                                    │
│  ┌──────────┐                                                            │
│  │  INFO    │───> No log (ERROR != INFO)                                │
│  │ Handler  │                                                            │
│  └────┬─────┘                                                            │
│       │ Pass to next                                                     │
│       ▼                                                                    │
│  ┌──────────┐                                                            │
│  │  ERROR   │───> Logs message (ERROR == ERROR)                         │
│  │ Handler  │                                                            │
│  └──────────┘                                                            │
│                                                                           │
└──────────────────────────────────────────────────────────────────────────┘

Benefits:
┌──────────────────────────────────────────────────────────────────────────┐
│  ✓ Simple and clear chain structure                                     │
│  ✓ Each handler responsible for one level                                │
│  ✓ Easy to extend with new log levels                                    │
│  ✓ Demonstrates COR pattern in simple way                                │
└──────────────────────────────────────────────────────────────────────────┘
```

### 3. Strategy Pattern

```
┌────────────────────────────────────────────────────────────────────────────┐
│                         STRATEGY PATTERN                                   │
└────────────────────────────────────────────────────────────────────────────┘

                    ┌──────────────────────┐
                    │   LogAppender        │
                    │   <<Interface>>      │
                    │                      │
                    │  + append(Message)  │
                    └──────────┬───────────┘
                               │
                               │ implements
                ┌──────────────┼──────────────┐
                │              │              │
                ▼              ▼              ▼
    ┌─────────────────┐  ┌──────────────┐  ┌──────────────┐
    │ ConsoleAppender │  │ FileAppender │  │DatabaseAppend│
    │                 │  │              │  │    er        │
    │  • Writes to    │  │  • Writes to │  │  • Writes to │
    │    stdout       │  │    file      │  │    database  │
    └─────────────────┘  └──────────────┘  └──────────────┘
```

**Benefits:**
- Easy to add new appenders without modifying existing code
- Runtime selection of output strategy
- Follows Open/Closed Principle

### 2. Singleton Pattern

```
┌────────────────────────────────────────────────────────────────────────────┐
│                         SINGLETON PATTERN                                   │
└────────────────────────────────────────────────────────────────────────────┘

                    ┌──────────────────────┐
                    │      Logger          │
                    │                      │
                    │  - instance (static) │
                    │                      │
                    │  + getInstance()     │
                    │  - Logger() private  │
                    └──────────────────────┘
                              │
                              │
                    ┌─────────┴─────────┐
                    │                   │
                    ▼                   ▼
            ┌──────────────┐    ┌──────────────┐
            │  Thread 1    │    │  Thread 2    │
            │              │    │              │
            │ getInstance()│    │ getInstance()│
            └──────┬───────┘    └──────┬───────┘
                   │                  │
                   └────────┬─────────┘
                            │
                            ▼
                    ┌──────────────┐
                    │ Same Instance│
                    └──────────────┘
```

**Benefits:**
- Single instance across application
- Thread-safe with double-checked locking
- Memory efficient

---

## Component Details

### Component Box Diagram

```
┌────────────────────────────────────────────────────────────────────────────┐
│                         COMPONENT BREAKDOWN                                 │
└────────────────────────────────────────────────────────────────────────────┘

┌──────────────────────────────────────────────────────────────────────────┐
│ 1. LogLevel (Enum)                                                        │
├──────────────────────────────────────────────────────────────────────────┤
│                                                                           │
│  ┌──────────┐  ┌──────────┐  ┌──────────┐                               │
│  │  DEBUG   │  │   INFO   │  │  ERROR   │                               │
│  │  (1)     │  │   (2)    │  │   (3)    │                               │
│  └──────────┘  └──────────┘  └──────────┘                               │
│                                                                           │
│  Purpose: Represents log severity levels                                 │
│  Usage: Used for filtering logs                                          │
│                                                                           │
└──────────────────────────────────────────────────────────────────────────┘

┌──────────────────────────────────────────────────────────────────────────┐
│ 2. LogMessage (Value Object)                                             │
├──────────────────────────────────────────────────────────────────────────┤
│                                                                           │
│  ┌────────────────────────────────────────────────────────────┐         │
│  │  Properties:                                                │         │
│  │  • level: LogLevel                                         │         │
│  │  • message: String                                         │         │
│  │  • timestamp: long                                         │         │
│  └────────────────────────────────────────────────────────────┘         │
│                                                                           │
│  Purpose: Immutable container for log data                              │
│  Thread-Safe: Yes (immutable)                                            │
│                                                                           │
└──────────────────────────────────────────────────────────────────────────┘

┌──────────────────────────────────────────────────────────────────────────┐
│ 3. Logger (Main Controller)                                               │
├──────────────────────────────────────────────────────────────────────────┤
│                                                                           │
│  ┌────────────────────────────────────────────────────────────┐         │
│  │  Responsibilities:                                         │         │
│  │  • Level Filtering                                         │         │
│  │  • Message Creation                                        │         │
│  │  • Appender Delegation                                     │         │
│  └────────────────────────────────────────────────────────────┘         │
│                                                                           │
│  ┌────────────────────────────────────────────────────────────┐         │
│  │  Methods:                                                   │         │
│  │  • log(level, message)                                     │         │
│  │  • debug(message)                                          │         │
│  │  • info(message)                                            │         │
│  │  • error(message)                                           │         │
│  └────────────────────────────────────────────────────────────┘         │
│                                                                           │
└──────────────────────────────────────────────────────────────────────────┘

┌──────────────────────────────────────────────────────────────────────────┐
│ 4. LogAppender (Strategy Interface)                                      │
├──────────────────────────────────────────────────────────────────────────┤
│                                                                           │
│  ┌──────────────────┐          ┌──────────────────┐                    │
│  │ ConsoleAppender  │          │  FileAppender     │                    │
│  ├──────────────────┤          ├──────────────────┤                    │
│  │ • Writes to      │          │ • filePath        │                    │
│  │   System.out     │          │ • Writes to file  │                    │
│  └──────────────────┘          └──────────────────┘                    │
│                                                                           │
│  Purpose: Strategy pattern for output destinations                      │
│  Extensibility: Easy to add new appenders                               │
│                                                                           │
└──────────────────────────────────────────────────────────────────────────┘
```

---

## Use Cases

### Use Case 1: Console Logging

```
┌────────────────────────────────────────────────────────────────────────────┐
│  Use Case: Console Logging with INFO Level                                │
└────────────────────────────────────────────────────────────────────────────┘

Client Code:
┌──────────────────────────────────────────────────────────────────────────┐
│ Logger logger = Logger.create(LogLevel.INFO, new ConsoleAppender());     │
│ logger.debug("Debug message");  // ✗ Filtered (1 < 2)                   │
│ logger.info("Info message");    // ✓ Logged (2 >= 2)                    │
│ logger.error("Error message");  // ✓ Logged (3 >= 2)                    │
└──────────────────────────────────────────────────────────────────────────┘

Output:
┌──────────────────────────────────────────────────────────────────────────┐
│ [INFO] 1234567890 - Info message                                        │
│ [ERROR] 1234567891 - Error message                                       │
└──────────────────────────────────────────────────────────────────────────┘
```

### Use Case 2: File Logging

```
┌────────────────────────────────────────────────────────────────────────────┐
│  Use Case: File Logging with DEBUG Level                                  │
└────────────────────────────────────────────────────────────────────────────┘

Client Code:
┌──────────────────────────────────────────────────────────────────────────┐
│ Logger logger = Logger.create(LogLevel.DEBUG,                            │
│                               new FileAppender("app.log"));              │
│ logger.debug("Debug message");  // ✓ Logged (1 >= 1)                     │
│ logger.info("Info message");    // ✓ Logged (2 >= 1)                     │
│ logger.error("Error message");  // ✓ Logged (3 >= 1)                     │
└──────────────────────────────────────────────────────────────────────────┘

File: app.log
┌──────────────────────────────────────────────────────────────────────────┐
│ [DEBUG] 1234567890 - Debug message                                       │
│ [INFO] 1234567891 - Info message                                         │
│ [ERROR] 1234567892 - Error message                                       │
└──────────────────────────────────────────────────────────────────────────┘
```

---

## Interview Discussion Points

### 1. Why Strategy Pattern for Appenders?

```
┌────────────────────────────────────────────────────────────────────────────┐
│                    STRATEGY PATTERN BENEFITS                               │
└────────────────────────────────────────────────────────────────────────────┘

┌──────────────────────────────────────────────────────────────────────────┐
│  Problem: Need different output destinations                             │
│  Solution: Strategy Pattern                                              │
│                                                                           │
│  Benefits:                                                               │
│  ┌────────────────────────────────────────────────────────────┐         │
│  │ ✓ Easy to add new appenders                                │         │
│  │ ✓ No modification to existing code                         │         │
│  │ ✓ Runtime selection of strategy                            │         │
│  │ ✓ Follows Open/Closed Principle                           │         │
│  └────────────────────────────────────────────────────────────┘         │
│                                                                           │
│  Example Extension:                                                       │
│  ┌────────────────────────────────────────────────────────────┐         │
│  │ public class DatabaseAppender implements LogAppender {       │         │
│  │     public void append(LogMessage msg) {                    │         │
│  │         // Write to database                                │         │
│  │     }                                                        │         │
│  │ }                                                            │         │
│  └────────────────────────────────────────────────────────────┘         │
└──────────────────────────────────────────────────────────────────────────┘
```

### 2. Why NOT Chain of Responsibility?

```
┌────────────────────────────────────────────────────────────────────────────┐
│              CHAIN OF RESPONSIBILITY vs SIMPLE IF                          │
└────────────────────────────────────────────────────────────────────────────┘

Chain of Responsibility (Complex):
┌──────────────────────────────────────────────────────────────────────────┐
│  INFO Logger → DEBUG Logger → ERROR Logger                               │
│      │              │              │                                      │
│      └──────────────┴──────────────┘                                      │
│              Chain Traversal                                               │
│                                                                           │
│  Problems:                                                               │
│  • Unnecessary complexity                                                │
│  • Chain setup overhead                                                  │
│  • Harder to understand                                                  │
└──────────────────────────────────────────────────────────────────────────┘

Simple If Statement (Our Approach):
┌──────────────────────────────────────────────────────────────────────────┐
│  if (level >= minLevel) {                                                │
│      // Log message                                                      │
│  }                                                                        │
│                                                                           │
│  Benefits:                                                               │
│  • Simple and clear                                                      │
│  • Easy to understand                                                   │
│  • No overhead                                                           │
└──────────────────────────────────────────────────────────────────────────┘
```

### 3. Thread Safety

```
┌────────────────────────────────────────────────────────────────────────────┐
│                         THREAD SAFETY ANALYSIS                            │
└────────────────────────────────────────────────────────────────────────────┘

┌──────────────────────────────────────────────────────────────────────────┐
│  Component          Thread Safety                                         │
├──────────────────────────────────────────────────────────────────────────┤
│  Logger Singleton   ✓ Double-checked locking                             │
│  LogMessage         ✓ Immutable (thread-safe by design)                  │
│  FileAppender       ⚠ Should use synchronized writes                    │
│  ConsoleAppender    ✓ System.out is thread-safe                          │
└──────────────────────────────────────────────────────────────────────────┘

Enhancement for FileAppender:
┌──────────────────────────────────────────────────────────────────────────┐
│  public synchronized void append(LogMessage msg) {                        │
│      // Thread-safe file writing                                         │
│  }                                                                        │
└──────────────────────────────────────────────────────────────────────────┘
```

---

## Architecture Benefits

```
┌────────────────────────────────────────────────────────────────────────────┐
│                         ARCHITECTURE BENEFITS                              │
└────────────────────────────────────────────────────────────────────────────┘

┌──────────────┐  ┌──────────────┐  ┌──────────────┐  ┌──────────────┐
│   Simple     │  │ Extensible  │  │  Testable   │  │    Clean     │
│              │  │             │  │              │  │              │
│  Easy to     │  │  Easy to    │  │  Clear       │  │  No          │
│  understand  │  │  add new    │  │  separation  │  │  unnecessary │
│  and         │  │  appenders  │  │  of concerns │  │  abstractions│
│  maintain    │  │             │  │              │  │              │
└──────────────┘  └──────────────┘  └──────────────┘  └──────────────┘
```

---

## Summary

```
┌────────────────────────────────────────────────────────────────────────────┐
│                         DESIGN SUMMARY                                    │
└────────────────────────────────────────────────────────────────────────────┘

┌──────────────────────────────────────────────────────────────────────────┐
│  Design Patterns:                                                         │
│  • Strategy Pattern (LogAppender)                                        │
│  • Singleton Pattern (Optional)                                          │
│                                                                           │
│  Key Features:                                                            │
│  • Simple level filtering (no COR complexity)                            │
│  • Extensible appender system                                            │
│  • Clean, maintainable code                                              │
│  • Thread-safe singleton                                                 │
│                                                                           │
│  SOLID Principles:                                                        │
│  • Open/Closed Principle (extensible without modification)               │
│  • Single Responsibility Principle (each class has one job)              │
│                                                                           │
└──────────────────────────────────────────────────────────────────────────┘
```

Perfect for interview discussions on design patterns, extensibility, and clean code principles!
