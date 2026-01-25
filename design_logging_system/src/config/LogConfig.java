package design_logging_system.src.config;

import design_logging_system.src.enums.LogLevel;
import design_logging_system.src.formatter.SimpleFormatter;

/**
 * LogConfig - Configuration for logging system
 * Contains log level and formatter
 */
public class LogConfig {
    private final LogLevel logLevel;
    private final SimpleFormatter formatter;

    public LogConfig(LogLevel logLevel) {
        this.logLevel = logLevel;
        this.formatter = new SimpleFormatter();
    }

    public LogLevel getLogLevel() {
        return logLevel;
    }

    public SimpleFormatter getFormatter() {
        return formatter;
    }
}
