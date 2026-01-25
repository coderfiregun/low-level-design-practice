package design_logging_system.src.appender;

import design_logging_system.src.enums.LogLevel;

public interface LogAppender {
    void append(LogLevel level, String message, long timestamp);
}
