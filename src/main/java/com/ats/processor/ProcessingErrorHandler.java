package com.ats.processor;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Error handler for file processing operations.
 * Provides strategies for handling and recovering from errors during processing.
 */
public class ProcessingErrorHandler {
    
    private static final Logger LOGGER = Logger.getLogger(ProcessingErrorHandler.class.getName());
    
    private final List<ErrorListener> errorListeners;
    private ErrorRecoveryStrategy recoveryStrategy;
    
    public enum ErrorRecoveryStrategy {
        /**
         * Stop processing immediately when an error occurs
         */
        FAIL_FAST,
        
        /**
         * Skip the failed file and continue with others
         */
        SKIP_AND_CONTINUE,
        
        /**
         * Retry the operation a limited number of times
         */
        RETRY,
        
        /**
         * Log the error and continue (for non-critical errors)
         */
        LOG_AND_CONTINUE
    }
    
    public ProcessingErrorHandler() {
        this.errorListeners = new ArrayList<>();
        this.recoveryStrategy = ErrorRecoveryStrategy.SKIP_AND_CONTINUE;
    }
    
    public ProcessingErrorHandler(ErrorRecoveryStrategy strategy) {
        this.errorListeners = new ArrayList<>();
        this.recoveryStrategy = strategy;
    }
    
    /**
     * Add an error listener
     * 
     * @param listener Listener to add
     */
    public void addErrorListener(ErrorListener listener) {
        errorListeners.add(listener);
    }
    
    /**
     * Set the error recovery strategy
     * 
     * @param strategy Strategy to use
     */
    public void setRecoveryStrategy(ErrorRecoveryStrategy strategy) {
        this.recoveryStrategy = strategy;
    }
    
    /**
     * Handle an error that occurred during processing
     * 
     * @param context Processing context
     * @param error The error that occurred
     * @param file File being processed
     * @throws IOException if error should propagate
     */
    public void handleError(ProcessingContext context, Throwable error, File file) throws IOException {
        // Log the error
        LOGGER.log(Level.WARNING, "Error processing file: " + file.getName(), error);
        
        // Add to context
        context.addError("Error processing file: " + file.getName(), error);
        
        // Notify listeners
        notifyListeners(context, error, file);
        
        // Apply recovery strategy
        switch (recoveryStrategy) {
            case FAIL_FAST:
                context.setStatus(ProcessingContext.ProcessingStatus.FAILED);
                throw new IOException("Processing failed: " + error.getMessage(), error);
                
            case SKIP_AND_CONTINUE:
                LOGGER.info("Skipping file after error: " + file.getName());
                break;
                
            case RETRY:
                // Retry logic would go here
                LOGGER.info("Retry not implemented, skipping file: " + file.getName());
                break;
                
            case LOG_AND_CONTINUE:
                LOGGER.info("Continuing after error in file: " + file.getName());
                break;
        }
    }
    
    /**
     * Handle a validation error
     * 
     * @param context Processing context
     * @param message Error message
     * @param file File being validated
     */
    public void handleValidationError(ProcessingContext context, String message, File file) {
        LOGGER.warning("Validation error for file " + file.getName() + ": " + message);
        context.addError("Validation failed: " + message);
        notifyListeners(context, new IllegalArgumentException(message), file);
    }
    
    /**
     * Notify all error listeners
     */
    private void notifyListeners(ProcessingContext context, Throwable error, File file) {
        for (ErrorListener listener : errorListeners) {
            try {
                listener.onError(context, error, file);
            } catch (Exception e) {
                LOGGER.log(Level.SEVERE, "Error notifying listener", e);
            }
        }
    }
    
    /**
     * Check if an error is recoverable
     * 
     * @param error Error to check
     * @return true if error might be recoverable
     */
    public static boolean isRecoverable(Throwable error) {
        // File not found or permission errors are not recoverable
        if (error instanceof java.io.FileNotFoundException ||
            error instanceof java.nio.file.AccessDeniedException) {
            return false;
        }
        
        // IO errors might be recoverable with retry
        if (error instanceof IOException) {
            return true;
        }
        
        // Parse errors are typically not recoverable
        if (error.getMessage() != null && 
            error.getMessage().toLowerCase().contains("parse")) {
            return false;
        }
        
        return true;
    }
    
    /**
     * Interface for error listeners
     */
    public interface ErrorListener {
        void onError(ProcessingContext context, Throwable error, File file);
    }
}
