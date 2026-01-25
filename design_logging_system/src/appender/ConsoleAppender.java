package design_logging_system.src.appender;

import design_logging_system.src.config.LogConfig;
import design_logging_system.src.enums.LogLevel;
import design_logging_system.src.formatter.SimpleFormatter;

/**
 * ConsoleAppender - Writes formatted messages to console
 * Receives LogConfig and extracts formatter from it
 */
public class ConsoleAppender implements LogAppender {
    private final SimpleFormatter formatter;

    public ConsoleAppender(LogConfig logConfig) {
        this.formatter = logConfig.getFormatter();
    }

    @Override
    public void append(LogLevel level, String message, long timestamp) {
        String formattedMessage = formatter.format(level, message, timestamp);
        System.out.println(formattedMessage);
    }
}
