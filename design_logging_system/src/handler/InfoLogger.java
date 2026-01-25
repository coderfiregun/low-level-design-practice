package design_logging_system.src.handler;

import design_logging_system.src.appender.LogAppender;
import design_logging_system.src.enums.LogLevel;

public class InfoLogger extends LogHandler {
    public InfoLogger(LogAppender logAppender) {
        super(LogLevel.INFO, logAppender);
    }
}
