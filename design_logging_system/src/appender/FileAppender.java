package design_logging_system.src.appender;


import design_logging_system.src.config.LogConfig;
import design_logging_system.src.enums.LogLevel;
import design_logging_system.src.formatter.SimpleFormatter;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * FileAppender - Async file logging with queue and batching
 * 
 * ALGORITHM:
 * ==========
 * 
 * 1. INITIALIZATION:
 *    - Create bounded BlockingQueue (size: 1024)
 *    - Start background writer thread
 *    - Writer thread opens file in append mode
 * 
 * 2. APPEND (Producer - Non-blocking):
 *    - Thread calls append(message)
 *    - Try to enqueue message: queue.offer(message)
 *    - If queue full: Drop message, log to console (overflow policy)
 *    - Return immediately (no blocking, no backpressure)
 * 
 * 3. WRITE MESSAGES (Consumer - Background Thread):
 *    - Continuously poll queue: message = queue.poll()
 *    - Collect messages into batch (up to BATCH_SIZE = 10)
 *    - When batch full OR shutdown with remaining messages:
 *      a. Write entire batch to file
 *      b. Flush to disk
 *      c. Clear batch
 *    - If queue empty: Sleep briefly (avoid busy-waiting)
 *    - Repeat until shutdown and queue empty
 * 
 * 4. BATCHING LOGIC:
 *    - Collect multiple messages before writing
 *    - Reduces I/O operations (better performance)
 *    - Batch size: 10 messages
 *    - Write when: batch.size() >= 10 OR shutdown
 * 
 * 5. SHUTDOWN:
 *    - Set isRunning = false
 *    - Interrupt writer thread
 *    - Wait for thread to finish (flush remaining messages)
 *    - Close file
 * 
 * BENEFITS:
 * - No backpressure: append() never blocks
 * - Better performance: Batching reduces I/O
 * - Memory safe: Bounded queue prevents OOM
 * - Thread-safe: Multiple threads can log simultaneously
 */
public class FileAppender implements LogAppender {
    private final String filePath;
    private final SimpleFormatter formatter;
    private final BlockingQueue<String> messageQueue;
    private final Thread writerThread;
    private final AtomicBoolean isRunning;
    
    private static final int QUEUE_SIZE = 1024;
    private static final int BATCH_SIZE = 10;

    public FileAppender(String filePath, LogConfig logConfig) {
        this.filePath = filePath;
        this.formatter = logConfig.getFormatter();
        // Bounded queue to prevent memory issues
        this.messageQueue = new LinkedBlockingQueue<>(QUEUE_SIZE);
        this.isRunning = new AtomicBoolean(true);
        
        // Start background thread for writing to file
        this.writerThread = new Thread(this::writeMessages, "FileAppender-Writer");
        this.writerThread.setDaemon(true);
        this.writerThread.start();
    }

    @Override
    public void append(LogLevel level, String message, long timestamp) {
        // Format message using formatter
        String formattedMessage = formatter.format(level, message, timestamp);
        
        // Non-blocking enqueue - no backpressure
        if (!messageQueue.offer(formattedMessage)) {
            // Queue full - drop message (overflow policy)
            System.err.println("[QUEUE FULL] Dropped: " + formattedMessage);
        }
    }

    /**
     * Background thread: Consumes from queue, batches messages, writes to file
     */
    private void writeMessages() {
        List<String> batch = new ArrayList<>(BATCH_SIZE);
        
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(filePath, true))) {
            while (isRunning.get() || !messageQueue.isEmpty()) {
                try {
                    // Collect messages into batch
                    String message = messageQueue.poll();
                    
                    if (message != null) {
                        batch.add(message);
                    }
                    
                    // Write batch when full OR when shutting down with remaining messages
                    if (batch.size() >= BATCH_SIZE || 
                        (!isRunning.get() && messageQueue.isEmpty() && !batch.isEmpty())) {
                        writeBatch(writer, batch);
                        batch.clear();
                    }
                    
                    // Small sleep to avoid busy-waiting when queue is empty
                    if (message == null && isRunning.get()) {
                        Thread.sleep(10);
                    }
                    
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    if (!isRunning.get() && messageQueue.isEmpty()) {
                        break;
                    }
                } catch (IOException e) {
                    System.err.println("Write error: " + e.getMessage());
                    batch.clear();
                }
            }
            
            // Flush any remaining messages
            if (!batch.isEmpty()) {
                writeBatch(writer, batch);
            }
            
        } catch (IOException e) {
            System.err.println("Failed to open file: " + filePath);
        }
    }

    /**
     * Write a batch of messages to file (batching improves performance)
     */
    private void writeBatch(BufferedWriter writer, List<String> batch) throws IOException {
        for (String message : batch) {
            writer.write(message);
            writer.newLine();
        }
        writer.flush();
    }

    /**
     * Graceful shutdown - flush remaining messages
     */
    public void shutdown() {
        isRunning.set(false);
        writerThread.interrupt();
        
        try {
            writerThread.join(5000);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
}
