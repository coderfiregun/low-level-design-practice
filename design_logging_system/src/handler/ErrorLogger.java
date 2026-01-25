package design_logging_system.src.handler;

import design_logging_system.src.appender.LogAppender;
import design_logging_system.src.enums.LogLevel;

public class ErrorLogger extends LogHandler {
    public ErrorLogger(LogAppender logAppender) {
        super(LogLevel.ERROR, logAppender);
    }
}
