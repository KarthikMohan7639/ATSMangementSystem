package com.ats.processor;

import java.io.File;
import java.util.function.Consumer;

/**
 * Listener interface for tracking file processing progress.
 * Implementations can be registered with the file processing pipeline
 * to receive notifications about processing events.
 */
public interface ProcessingProgressListener {
    
    /**
     * Called when processing of a file starts
     * 
     * @param context Processing context
     */
    default void onProcessingStarted(ProcessingContext context) {
        // Default implementation does nothing
    }
    
    /**
     * Called when progress is made during processing
     * 
     * @param context Processing context
     */
    default void onProgressUpdate(ProcessingContext context) {
        // Default implementation does nothing
    }
    
    /**
     * Called when processing completes successfully
     * 
     * @param context Processing context
     */
    default void onProcessingCompleted(ProcessingContext context) {
        // Default implementation does nothing
    }
    
    /**
     * Called when processing fails
     * 
     * @param context Processing context
     * @param error The error that caused the failure
     */
    default void onProcessingFailed(ProcessingContext context, Throwable error) {
        // Default implementation does nothing
    }
    
    /**
     * Factory method to create a simple listener from lambda expressions
     * 
     * @param onCompleted Callback for completion
     * @return ProcessingProgressListener
     */
    static ProcessingProgressListener create(Consumer<ProcessingContext> onCompleted) {
        return new ProcessingProgressListener() {
            @Override
            public void onProcessingCompleted(ProcessingContext context) {
                onCompleted.accept(context);
            }
        };
    }
}
