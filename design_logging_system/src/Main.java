package design_logging_system.src;


import design_logging_system.src.config.LogConfig;
import design_logging_system.src.enums.LogLevel;
import design_logging_system.src.logger.LogManager;

public class Main {
    public static void main(String[] args) {
        // Create log config with INFO as minimum level
        LogConfig logConfig = new LogConfig(LogLevel.INFO);
        
        // Create console logger using factory method
        LogManager logger = LogManager.createConsole(logConfig);

        System.out.println("=== Logger with INFO as minimum level ===");
        System.out.println("Chain: ERROR -> WARN -> INFO -> DEBUG\n");
        
        logger.debug("This debug message will be filtered (DEBUG < INFO)");
        logger.info("This info message will be logged");
        logger.warn("This warn message will be logged");
        logger.error("This error message will be logged");

        System.out.println("\n=== Logger with DEBUG as minimum level ===");
        System.out.println("Chain: ERROR -> WARN -> INFO -> DEBUG\n");
        
        LogConfig debugConfig = new LogConfig(LogLevel.DEBUG);
        LogManager debugLogger = LogManager.createConsole(debugConfig);
        
        debugLogger.debug("This debug message will be logged");
        debugLogger.info("This info message will be logged");
        debugLogger.warn("This warn message will be logged");
        debugLogger.error("This error message will be logged");

        System.out.println("\n=== LogManager with File Appender (Queue + Batching) ===");
        System.out.println("Features: Bounded Queue, Batching, No Backpressure\n");
        System.out.println("Chain: ERROR -> WARN -> INFO -> DEBUG\n");
        
        // Create file logger using factory method
        LogManager fileLogger = LogManager.createFile(logConfig, "logs.txt");
        
        // Multiple threads can log simultaneously without blocking
        fileLogger.info("This will be written to file with detailed format");
        fileLogger.warn("This warning will be written to file with detailed format");
        fileLogger.error("This error will be written to file with detailed format");
        
        // Give some time for background thread to process and batch
        try {
            Thread.sleep(500);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        
        // Note: In real implementation, would need to expose shutdown method
        System.out.println("File logger created with detailed formatting");
    }
}
