package com.ats.processor;

import java.io.File;
import java.time.LocalDateTime;
import java.util.*;

/**
 * Context object that carries state and metadata throughout the file processing pipeline.
 * Provides a way to track processing status, errors, and metadata.
 */
public class ProcessingContext {
    
    private final File sourceFile;
    private final Set<String> keywords;
    private final Map<String, Object> metadata;
    private final List<ProcessingError> errors;
    private final LocalDateTime startTime;
    
    private ProcessingStatus status;
    private int processedItems;
    private int totalItems;
    private LocalDateTime endTime;
    
    public enum ProcessingStatus {
        PENDING,
        VALIDATING,
        PROCESSING,
        COMPLETED,
        FAILED,
        CANCELLED
    }
    
    public ProcessingContext(File sourceFile, Set<String> keywords) {
        this.sourceFile = sourceFile;
        this.keywords = keywords != null ? new HashSet<>(keywords) : new HashSet<>();
        this.metadata = new HashMap<>();
        this.errors = new ArrayList<>();
        this.startTime = LocalDateTime.now();
        this.status = ProcessingStatus.PENDING;
        this.processedItems = 0;
        this.totalItems = 0;
    }
    
    // Getters
    public File getSourceFile() {
        return sourceFile;
    }
    
    public Set<String> getKeywords() {
        return Collections.unmodifiableSet(keywords);
    }
    
    public ProcessingStatus getStatus() {
        return status;
    }
    
    public void setStatus(ProcessingStatus status) {
        this.status = status;
        if (status == ProcessingStatus.COMPLETED || status == ProcessingStatus.FAILED || status == ProcessingStatus.CANCELLED) {
            this.endTime = LocalDateTime.now();
        }
    }
    
    public int getProcessedItems() {
        return processedItems;
    }
    
    public void setProcessedItems(int processedItems) {
        this.processedItems = processedItems;
    }
    
    public void incrementProcessedItems() {
        this.processedItems++;
    }
    
    public int getTotalItems() {
        return totalItems;
    }
    
    public void setTotalItems(int totalItems) {
        this.totalItems = totalItems;
    }
    
    public LocalDateTime getStartTime() {
        return startTime;
    }
    
    public LocalDateTime getEndTime() {
        return endTime;
    }
    
    public long getProcessingTimeMs() {
        if (endTime != null) {
            return java.time.Duration.between(startTime, endTime).toMillis();
        }
        return java.time.Duration.between(startTime, LocalDateTime.now()).toMillis();
    }
    
    // Metadata management
    public void setMetadata(String key, Object value) {
        metadata.put(key, value);
    }
    
    public Object getMetadata(String key) {
        return metadata.get(key);
    }
    
    public <T> T getMetadata(String key, Class<T> type) {
        Object value = metadata.get(key);
        if (value != null && type.isInstance(value)) {
            return type.cast(value);
        }
        return null;
    }
    
    public Map<String, Object> getAllMetadata() {
        return Collections.unmodifiableMap(metadata);
    }
    
    // Error management
    public void addError(String message, Throwable cause) {
        errors.add(new ProcessingError(message, cause, LocalDateTime.now()));
    }
    
    public void addError(String message) {
        addError(message, null);
    }
    
    public List<ProcessingError> getErrors() {
        return Collections.unmodifiableList(errors);
    }
    
    public boolean hasErrors() {
        return !errors.isEmpty();
    }
    
    public int getErrorCount() {
        return errors.size();
    }
    
    // Processing progress
    public double getProgress() {
        if (totalItems == 0) {
            return 0.0;
        }
        return (double) processedItems / totalItems * 100.0;
    }
    
    public boolean isComplete() {
        return status == ProcessingStatus.COMPLETED;
    }
    
    public boolean isFailed() {
        return status == ProcessingStatus.FAILED;
    }
    
    /**
     * Represents a processing error with timestamp
     */
    public static class ProcessingError {
        private final String message;
        private final Throwable cause;
        private final LocalDateTime timestamp;
        
        public ProcessingError(String message, Throwable cause, LocalDateTime timestamp) {
            this.message = message;
            this.cause = cause;
            this.timestamp = timestamp;
        }
        
        public String getMessage() {
            return message;
        }
        
        public Throwable getCause() {
            return cause;
        }
        
        public LocalDateTime getTimestamp() {
            return timestamp;
        }
        
        @Override
        public String toString() {
            return String.format("[%s] %s%s", 
                timestamp, 
                message, 
                cause != null ? " - " + cause.getMessage() : "");
        }
    }
    
    @Override
    public String toString() {
        return String.format("ProcessingContext{file=%s, status=%s, progress=%.1f%%, errors=%d, time=%dms}",
            sourceFile.getName(),
            status,
            getProgress(),
            errors.size(),
            getProcessingTimeMs());
    }
}
