package design_logging_system.src.formatter;


import design_logging_system.src.enums.LogLevel;

/**
 * SimpleFormatter - Formats log messages
 * Format: [LEVEL] timestamp - message
 */
public class SimpleFormatter {
    public String format(LogLevel level, String message, long timestamp) {
        return "[" + level + "] " + timestamp + " - " + message;
    }
}
