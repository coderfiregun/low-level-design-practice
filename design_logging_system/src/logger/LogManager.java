package design_logging_system.src.logger;

import design_logging_system.src.appender.ConsoleAppender;
import design_logging_system.src.appender.FileAppender;
import design_logging_system.src.appender.LogAppender;
import design_logging_system.src.config.LogConfig;
import design_logging_system.src.enums.LogLevel;
import design_logging_system.src.handler.*;

/**
 * LogManager - Manages logging with Chain of Responsibility pattern
 * 
 * Factory Methods for creating different types of loggers:
 * - createConsole(): Console logger
 * - createFile(): File logger
 */
public class LogManager {
    private final LogConfig logConfig;
    private final LogAppender logAppender;
    private final LogHandler logHandlerChain;

    // Private constructor - use factory methods
    private LogManager(LogConfig logConfig, LogAppender logAppender) {
        this.logConfig = logConfig;
        this.logAppender = logAppender;
        this.logHandlerChain = buildChain();
    }

    /**
     * Factory Method: Create console logger
     * Passes LogConfig to appender (appender extracts formatter from config)
     */
    public static LogManager createConsole(LogConfig logConfig) {
        LogAppender appender = new ConsoleAppender(logConfig);
        return new LogManager(logConfig, appender);
    }

    /**
     * Factory Method: Create file logger
     * Passes LogConfig to appender (appender extracts formatter from config)
     */
    public static LogManager createFile(LogConfig logConfig, String filePath) {
        LogAppender appender = new FileAppender(filePath, logConfig);
        return new LogManager(logConfig, appender);
    }

    private LogHandler buildChain() {
        // Build chain: ERROR -> WARN -> INFO -> DEBUG (most severe first)
        ErrorLogger errorLogger = new ErrorLogger(logAppender);
        WarnLogger warnLogger = new WarnLogger(logAppender);
        InfoLogger infoLogger = new InfoLogger(logAppender);
        DebugLogger debugLogger = new DebugLogger(logAppender);

        // Chain them: ERROR -> WARN -> INFO -> DEBUG
        errorLogger.setNext(warnLogger);
        warnLogger.setNext(infoLogger);
        infoLogger.setNext(debugLogger);

        // Return head of chain (ERROR)
        return errorLogger;
    }

    private void log(LogLevel level, String message) {
        // Filter: only log if level >= minLevel
        if (level.getValue() < logConfig.getLogLevel().getValue()) {
            return;
        }

        logHandlerChain.handleNext(level, message);
    }

    public void debug(String message) {
        log(LogLevel.DEBUG, message);
    }

    public void info(String message) {
        log(LogLevel.INFO, message);
    }

    public void warn(String message) {
        log(LogLevel.WARN, message);
    }

    public void error(String message) {
        log(LogLevel.ERROR, message);
    }
}
