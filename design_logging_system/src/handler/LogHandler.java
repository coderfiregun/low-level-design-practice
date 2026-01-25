package design_logging_system.src.handler;


import design_logging_system.src.appender.LogAppender;
import design_logging_system.src.enums.LogLevel;

public abstract class LogHandler {
    protected final LogLevel logLevel;
    protected final LogAppender logAppender;
    protected volatile LogHandler nextHandler;

    public LogHandler(LogLevel logLevel, LogAppender logAppender) {
        this.logLevel = logLevel;
        this.logAppender = logAppender;
    }

    public void setNext(LogHandler nextHandler) {
        this.nextHandler = nextHandler;
    }

    public void handleNext(LogLevel messageLevel, String message) {
        if (this.logLevel == messageLevel) {
            // Handle and log - pass raw data to appender (formatter is in appender)
            long timestamp = System.currentTimeMillis();
            logAppender.append(messageLevel, message, timestamp);
            return; // Handle and drop
        }

        // Pass to next handler
        if (nextHandler != null) {
            nextHandler.handleNext(messageLevel, message);
        }
    }
}
