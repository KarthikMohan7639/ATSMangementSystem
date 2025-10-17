package com.ats.examples;

import com.ats.model.DataRow;
import com.ats.processor.*;
import com.ats.processor.FileProcessingPipeline.ProcessingResult;

import java.io.File;
import java.util.*;

/**
 * Example demonstrating the usage of the file processing architecture.
 * This class shows various ways to use the new processing pipeline and components.
 */
public class FileProcessingExample {
    
    /**
     * Example 1: Basic file processing
     */
    public static void basicProcessing() throws Exception {
        System.out.println("=== Example 1: Basic File Processing ===");
        
        // Create a pipeline
        FileProcessingPipeline pipeline = new FileProcessingPipeline();
        
        // Register processors from factory
        FileProcessorFactory factory = FileProcessorFactory.getInstance();
        for (FileProcessor processor : factory.getAllProcessors()) {
            pipeline.registerProcessor(processor);
        }
        
        // Process a file
        File file = new File("sample.xlsx");
        Set<String> keywords = new HashSet<>(Arrays.asList("java", "developer"));
        
        if (file.exists()) {
            ProcessingResult result = pipeline.processSingleFile(file, keywords);
            
            System.out.println("Processing completed: " + result.isSuccess());
            System.out.println("Data rows extracted: " + result.getData().size());
            System.out.println("Processing time: " + result.getContext().getProcessingTimeMs() + "ms");
        }
        
        pipeline.shutdown();
    }
    
    /**
     * Example 2: Concurrent processing of multiple files
     */
    public static void concurrentProcessing() throws Exception {
        System.out.println("\n=== Example 2: Concurrent File Processing ===");
        
        // Create pipeline with 8 concurrent threads
        FileProcessingPipeline pipeline = new FileProcessingPipeline(8);
        
        // Register processors
        FileProcessorFactory factory = FileProcessorFactory.getInstance();
        for (FileProcessor processor : factory.getAllProcessors()) {
            pipeline.registerProcessor(processor);
        }
        
        // Prepare list of files
        List<File> files = Arrays.asList(
            new File("file1.xlsx"),
            new File("file2.pdf"),
            new File("file3.docx")
        );
        
        // Filter to only existing files
        List<File> existingFiles = new ArrayList<>();
        for (File file : files) {
            if (file.exists()) {
                existingFiles.add(file);
            }
        }
        
        if (!existingFiles.isEmpty()) {
            // Process all files concurrently
            Set<String> keywords = new HashSet<>(Arrays.asList("java", "python", "developer"));
            List<ProcessingResult> results = pipeline.processFiles(existingFiles, keywords);
            
            // Print results
            System.out.println("Total files processed: " + results.size());
            for (ProcessingResult result : results) {
                System.out.println("  - " + result.getContext().getSourceFile().getName() + 
                                 ": " + result.getData().size() + " rows, " +
                                 result.getContext().getProcessingTimeMs() + "ms");
            }
        }
        
        pipeline.shutdown();
    }
    
    /**
     * Example 3: Processing with progress tracking
     */
    public static void processingWithProgress() throws Exception {
        System.out.println("\n=== Example 3: Progress Tracking ===");
        
        FileProcessingPipeline pipeline = new FileProcessingPipeline();
        
        // Register processors
        FileProcessorFactory factory = FileProcessorFactory.getInstance();
        for (FileProcessor processor : factory.getAllProcessors()) {
            pipeline.registerProcessor(processor);
        }
        
        // Register progress listener
        pipeline.registerListener(new ProcessingProgressListener() {
            @Override
            public void onProcessingStarted(ProcessingContext context) {
                System.out.println("Started: " + context.getSourceFile().getName());
            }
            
            @Override
            public void onProcessingCompleted(ProcessingContext context) {
                System.out.println("Completed: " + context.getSourceFile().getName() + 
                                 " in " + context.getProcessingTimeMs() + "ms");
            }
            
            @Override
            public void onProcessingFailed(ProcessingContext context, Throwable error) {
                System.err.println("Failed: " + context.getSourceFile().getName() + 
                                 " - " + error.getMessage());
            }
        });
        
        // Process file
        File file = new File("sample.xlsx");
        if (file.exists()) {
            Set<String> keywords = Collections.emptySet();
            pipeline.processSingleFile(file, keywords);
        }
        
        pipeline.shutdown();
    }
    
    /**
     * Example 4: File type detection and metadata extraction
     */
    public static void fileTypeDetectionAndMetadata() {
        System.out.println("\n=== Example 4: File Type Detection & Metadata ===");
        
        File file = new File("sample.xlsx");
        
        if (file.exists()) {
            // Detect file type
            FileTypeDetector.FileType type = FileTypeDetector.detectFileType(file);
            System.out.println("File type: " + type.getDescription());
            System.out.println("MIME type: " + type.getMimeType());
            
            // Extract metadata
            Map<String, Object> metadata = FileMetadataExtractor.extractMetadata(file);
            System.out.println("\nFile Metadata:");
            System.out.println("  Name: " + metadata.get("name"));
            System.out.println("  Size: " + metadata.get("sizeHuman"));
            System.out.println("  Type: " + metadata.get("typeDescription"));
            System.out.println("  Extension: " + metadata.get("extension"));
            
            // Validate file
            List<String> errors = FileMetadataExtractor.validateFile(file);
            if (errors.isEmpty()) {
                System.out.println("\nFile is valid ✓");
            } else {
                System.out.println("\nValidation errors:");
                for (String error : errors) {
                    System.out.println("  - " + error);
                }
            }
        }
    }
    
    /**
     * Example 5: Using the processor factory
     */
    public static void processorFactoryUsage() {
        System.out.println("\n=== Example 5: Processor Factory ===");
        
        FileProcessorFactory factory = FileProcessorFactory.getInstance();
        
        // List all registered processors
        System.out.println("Registered processors:");
        for (FileProcessor processor : factory.getAllProcessors()) {
            System.out.println("  - " + processor.getProcessorName());
        }
        
        // Get processor for a specific file
        File file = new File("sample.xlsx");
        if (file.exists()) {
            FileProcessor processor = factory.getProcessor(file);
            if (processor != null) {
                System.out.println("\nProcessor for " + file.getName() + ": " + 
                                 processor.getProcessorName());
            }
            
            // Check if file can be processed
            boolean canProcess = factory.canProcess(file);
            System.out.println("Can process: " + canProcess);
        }
    }
    
    /**
     * Example 6: Summarizing multiple files
     */
    public static void fileSummary() {
        System.out.println("\n=== Example 6: File Summary ===");
        
        List<File> files = Arrays.asList(
            new File("file1.xlsx"),
            new File("file2.pdf"),
            new File("file3.docx")
        );
        
        // Filter existing files
        List<File> existingFiles = new ArrayList<>();
        for (File file : files) {
            if (file.exists()) {
                existingFiles.add(file);
            }
        }
        
        if (!existingFiles.isEmpty()) {
            Map<String, Object> summary = FileMetadataExtractor.summarizeFiles(existingFiles);
            
            System.out.println("File Summary:");
            System.out.println("  Total files: " + summary.get("totalFiles"));
            System.out.println("  Processable files: " + summary.get("processableFiles"));
            System.out.println("  Total size: " + summary.get("totalSizeHuman"));
            System.out.println("  Type distribution: " + summary.get("typeDistribution"));
        }
    }
    
    /**
     * Example 7: Error handling
     */
    public static void errorHandling() throws Exception {
        System.out.println("\n=== Example 7: Error Handling ===");
        
        FileProcessingPipeline pipeline = new FileProcessingPipeline();
        
        // Register processors
        FileProcessorFactory factory = FileProcessorFactory.getInstance();
        for (FileProcessor processor : factory.getAllProcessors()) {
            pipeline.registerProcessor(processor);
        }
        
        // Register error listener
        pipeline.registerListener(new ProcessingProgressListener() {
            @Override
            public void onProcessingFailed(ProcessingContext context, Throwable error) {
                System.err.println("Processing failed for: " + 
                                 context.getSourceFile().getName());
                System.err.println("Error: " + error.getMessage());
                System.err.println("Total errors: " + context.getErrorCount());
                
                for (ProcessingContext.ProcessingError err : context.getErrors()) {
                    System.err.println("  - " + err);
                }
            }
        });
        
        // Try to process non-existent file (will fail)
        File file = new File("nonexistent.xlsx");
        try {
            pipeline.processSingleFile(file, Collections.emptySet());
        } catch (Exception e) {
            System.out.println("Expected error caught: " + e.getMessage());
        }
        
        pipeline.shutdown();
    }
    
    /**
     * Main method to run all examples
     */
    public static void main(String[] args) {
        try {
            // Note: These examples assume sample files exist
            // Comment out examples as needed based on available files
            
            basicProcessing();
            concurrentProcessing();
            processingWithProgress();
            fileTypeDetectionAndMetadata();
            processorFactoryUsage();
            fileSummary();
            errorHandling();
            
        } catch (Exception e) {
            System.err.println("Error running examples: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
