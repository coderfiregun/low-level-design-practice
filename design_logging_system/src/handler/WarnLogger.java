package design_logging_system.src.handler;


import design_logging_system.src.appender.LogAppender;
import design_logging_system.src.enums.LogLevel;

public class WarnLogger extends LogHandler {
    public WarnLogger(LogAppender logAppender) {
        super(LogLevel.WARN, logAppender);
    }
}
