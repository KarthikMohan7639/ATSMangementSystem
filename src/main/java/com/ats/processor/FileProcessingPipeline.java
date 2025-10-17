package com.ats.processor;

import com.ats.model.DataRow;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.*;

/**
 * File processing pipeline that orchestrates the complete processing workflow.
 * Supports validation, processing, error handling, and progress tracking.
 * Also supports concurrent processing of multiple files.
 */
public class FileProcessingPipeline {
    
    private final List<FileProcessor> processors;
    private final List<ProcessingProgressListener> listeners;
    private final ExecutorService executorService;
    private final int maxConcurrentFiles;
    
    /**
     * Create a pipeline with default settings (4 concurrent threads)
     */
    public FileProcessingPipeline() {
        this(4);
    }
    
    /**
     * Create a pipeline with specified concurrency
     * 
     * @param maxConcurrentFiles Maximum number of files to process concurrently
     */
    public FileProcessingPipeline(int maxConcurrentFiles) {
        this.processors = new ArrayList<>();
        this.listeners = new ArrayList<>();
        this.maxConcurrentFiles = maxConcurrentFiles;
        this.executorService = Executors.newFixedThreadPool(maxConcurrentFiles);
    }
    
    /**
     * Register a file processor
     * 
     * @param processor Processor to register
     */
    public void registerProcessor(FileProcessor processor) {
        processors.add(processor);
    }
    
    /**
     * Register a progress listener
     * 
     * @param listener Listener to register
     */
    public void registerListener(ProcessingProgressListener listener) {
        listeners.add(listener);
    }
    
    /**
     * Process a single file through the pipeline
     * 
     * @param file File to process
     * @param keywords Keywords to search for
     * @return Processing result containing extracted data
     * @throws IOException if processing fails
     */
    public ProcessingResult processSingleFile(File file, Set<String> keywords) throws IOException {
        ProcessingContext context = new ProcessingContext(file, keywords);
        
        try {
            // Notify listeners - processing started
            notifyListeners(listener -> listener.onProcessingStarted(context));
            
            // Stage 1: Validation
            context.setStatus(ProcessingContext.ProcessingStatus.VALIDATING);
            validateFile(file, context);
            
            // Stage 2: Find appropriate processor
            FileProcessor processor = findProcessor(file);
            if (processor == null) {
                throw new IOException("No processor found for file: " + file.getName());
            }
            
            context.setMetadata("processor", processor.getProcessorName());
            context.setMetadata("fileType", FileTypeDetector.detectFileType(file).getDescription());
            
            // Stage 3: Process file
            context.setStatus(ProcessingContext.ProcessingStatus.PROCESSING);
            List<DataRow> results = processor.processFile(file, keywords);
            
            // Stage 4: Complete
            context.setStatus(ProcessingContext.ProcessingStatus.COMPLETED);
            notifyListeners(listener -> listener.onProcessingCompleted(context));
            
            return new ProcessingResult(context, results);
            
        } catch (Exception e) {
            context.setStatus(ProcessingContext.ProcessingStatus.FAILED);
            context.addError("Processing failed", e);
            notifyListeners(listener -> listener.onProcessingFailed(context, e));
            throw new IOException("Failed to process file: " + file.getName(), e);
        }
    }
    
    /**
     * Process multiple files concurrently
     * 
     * @param files Files to process
     * @param keywords Keywords to search for
     * @return List of processing results
     */
    public List<ProcessingResult> processFiles(List<File> files, Set<String> keywords) {
        List<ProcessingResult> results = new CopyOnWriteArrayList<>();
        List<Future<ProcessingResult>> futures = new ArrayList<>();
        
        // Submit all files for processing
        for (File file : files) {
            Future<ProcessingResult> future = executorService.submit(() -> {
                try {
                    return processSingleFile(file, keywords);
                } catch (IOException e) {
                    // Return a failed result
                    ProcessingContext context = new ProcessingContext(file, keywords);
                    context.setStatus(ProcessingContext.ProcessingStatus.FAILED);
                    context.addError("Processing failed", e);
                    return new ProcessingResult(context, new ArrayList<>());
                }
            });
            futures.add(future);
        }
        
        // Collect results
        for (Future<ProcessingResult> future : futures) {
            try {
                results.add(future.get());
            } catch (InterruptedException | ExecutionException e) {
                // Log error but continue processing other files
                System.err.println("Error collecting result: " + e.getMessage());
            }
        }
        
        return results;
    }
    
    /**
     * Validate a file before processing
     */
    private void validateFile(File file, ProcessingContext context) {
        if (file == null) {
            throw new IllegalArgumentException("File cannot be null");
        }
        if (!file.exists()) {
            throw new IllegalArgumentException("File does not exist: " + file.getPath());
        }
        if (!file.isFile()) {
            throw new IllegalArgumentException("Not a file: " + file.getPath());
        }
        if (!file.canRead()) {
            throw new IllegalArgumentException("Cannot read file: " + file.getPath());
        }
        if (file.length() == 0) {
            throw new IllegalArgumentException("File is empty: " + file.getPath());
        }
        
        // Add file metadata to context
        context.setMetadata("fileSize", file.length());
        context.setMetadata("fileName", file.getName());
        context.setMetadata("filePath", file.getAbsolutePath());
    }
    
    /**
     * Find a processor that can handle the given file
     */
    private FileProcessor findProcessor(File file) {
        for (FileProcessor processor : processors) {
            if (processor.supports(file)) {
                return processor;
            }
        }
        return null;
    }
    
    /**
     * Notify all listeners
     */
    private void notifyListeners(java.util.function.Consumer<ProcessingProgressListener> action) {
        for (ProcessingProgressListener listener : listeners) {
            try {
                action.accept(listener);
            } catch (Exception e) {
                // Log but don't fail processing
                System.err.println("Error notifying listener: " + e.getMessage());
            }
        }
    }
    
    /**
     * Shutdown the executor service
     */
    public void shutdown() {
        executorService.shutdown();
        try {
            if (!executorService.awaitTermination(60, TimeUnit.SECONDS)) {
                executorService.shutdownNow();
            }
        } catch (InterruptedException e) {
            executorService.shutdownNow();
            Thread.currentThread().interrupt();
        }
    }
    
    /**
     * Result of file processing
     */
    public static class ProcessingResult {
        private final ProcessingContext context;
        private final List<DataRow> data;
        
        public ProcessingResult(ProcessingContext context, List<DataRow> data) {
            this.context = context;
            this.data = data;
        }
        
        public ProcessingContext getContext() {
            return context;
        }
        
        public List<DataRow> getData() {
            return data;
        }
        
        public boolean isSuccess() {
            return context.isComplete() && !context.hasErrors();
        }
        
        public boolean hasErrors() {
            return context.hasErrors();
        }
    }
}
