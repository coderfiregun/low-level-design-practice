package design_logging_system.src.handler;

import design_logging_system.src.appender.LogAppender;
import design_logging_system.src.enums.LogLevel;

public class DebugLogger extends LogHandler {
    public DebugLogger(LogAppender logAppender) {
        super(LogLevel.DEBUG, logAppender);
    }
}
