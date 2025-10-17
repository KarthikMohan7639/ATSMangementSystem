# Quick Reference Guide - File Processing Architecture

## Quick Start

### 1. Basic File Processing

```java
// Create and configure pipeline
FileProcessingPipeline pipeline = new FileProcessingPipeline();
FileProcessorFactory factory = FileProcessorFactory.getInstance();

// Register all default processors
for (FileProcessor processor : factory.getAllProcessors()) {
    pipeline.registerProcessor(processor);
}

// Process a file
File file = new File("resume.xlsx");
Set<String> keywords = Set.of("java", "developer");
ProcessingResult result = pipeline.processSingleFile(file, keywords);

// Get results
List<DataRow> data = result.getData();
System.out.println("Extracted " + data.size() + " rows");

// Cleanup
pipeline.shutdown();
```

### 2. Process Multiple Files Concurrently

```java
// Create pipeline with 8 threads
FileProcessingPipeline pipeline = new FileProcessingPipeline(8);

// ... register processors ...

// Process multiple files
List<File> files = Arrays.asList(file1, file2, file3);
List<ProcessingResult> results = pipeline.processFiles(files, keywords);

pipeline.shutdown();
```

### 3. Track Progress

```java
pipeline.registerListener(new ProcessingProgressListener() {
    @Override
    public void onProcessingStarted(ProcessingContext context) {
        System.out.println("Started: " + context.getSourceFile().getName());
    }
    
    @Override
    public void onProcessingCompleted(ProcessingContext context) {
        System.out.println("Done in " + context.getProcessingTimeMs() + "ms");
    }
});
```

## Component Quick Reference

### FileProcessor Interface
**Purpose**: Common interface for all processors  
**Key Methods**:
- `processFile(File, Set<String>)` - Process file
- `supports(File)` - Check if can handle file
- `getProcessorName()` - Get processor name

### FileProcessingPipeline
**Purpose**: Orchestrate processing workflow  
**Key Methods**:
- `processSingleFile(File, Set<String>)` - Process one file
- `processFiles(List<File>, Set<String>)` - Process multiple
- `registerProcessor(FileProcessor)` - Add processor
- `registerListener(ProcessingProgressListener)` - Add listener
- `shutdown()` - Cleanup resources

### ProcessingContext
**Purpose**: Track processing state  
**Key Properties**:
- `status` - Current status (PENDING, PROCESSING, COMPLETED, FAILED)
- `errors` - List of errors
- `metadata` - Custom metadata
- `processingTimeMs` - Processing duration

### FileTypeDetector
**Purpose**: Detect file types  
**Key Methods**:
- `detectFileType(File)` - Detect type
- `isSpreadsheet(File)` - Check if spreadsheet
- `isPDF(File)` - Check if PDF
- `isWordDocument(File)` - Check if Word doc

### FileMetadataExtractor
**Purpose**: Extract file metadata  
**Key Methods**:
- `extractMetadata(File)` - Get all metadata
- `validateFile(File)` - Validate file
- `summarizeFiles(List<File>)` - Batch summary
- `formatFileSize(long)` - Human-readable size

### FileProcessorFactory
**Purpose**: Manage processors (Singleton)  
**Key Methods**:
- `getInstance()` - Get factory instance
- `getProcessor(File)` - Get processor for file
- `getAllProcessors()` - Get all processors
- `canProcess(File)` - Check if supported

## Common Patterns

### Pattern 1: Simple Processing
```java
FileProcessingPipeline pipeline = new FileProcessingPipeline();
// ... setup ...
ProcessingResult result = pipeline.processSingleFile(file, keywords);
if (result.isSuccess()) {
    // Use result.getData()
}
pipeline.shutdown();
```

### Pattern 2: Batch Processing with Progress
```java
FileProcessingPipeline pipeline = new FileProcessingPipeline(4);
// ... register processors ...

// Simple listener using factory method (only supports onCompleted)
pipeline.registerListener(ProcessingProgressListener.create(context -> 
    System.out.println("Completed: " + context.getSourceFile().getName())
));

// OR use full implementation for all callbacks
pipeline.registerListener(new ProcessingProgressListener() {
    @Override
    public void onProcessingStarted(ProcessingContext context) {
        System.out.println("Started: " + context.getSourceFile().getName());
    }
    
    @Override
    public void onProcessingCompleted(ProcessingContext context) {
        System.out.println("Completed: " + context.getSourceFile().getName());
    }
});

List<ProcessingResult> results = pipeline.processFiles(files, keywords);
pipeline.shutdown();
```

### Pattern 3: Error Handling
```java
pipeline.registerListener(new ProcessingProgressListener() {
    @Override
    public void onProcessingFailed(ProcessingContext context, Throwable error) {
        System.err.println("Failed: " + context.getSourceFile().getName());
        for (ProcessingContext.ProcessingError processingError : context.getErrors()) {
            System.err.println("  Error: " + processingError);
        }
    }
});
```

### Pattern 4: Custom Metadata
```java
ProcessingResult result = pipeline.processSingleFile(file, keywords);
ProcessingContext context = result.getContext();
context.getMetadata("processor"); // Get processor name
context.getMetadata("fileType");  // Get file type
context.getMetadata("fileSize");  // Get file size
```

### Pattern 5: File Validation Before Processing
```java
List<String> errors = FileMetadataExtractor.validateFile(file);
if (errors.isEmpty()) {
    // File is valid, process it
    ProcessingResult result = pipeline.processSingleFile(file, keywords);
} else {
    // Handle validation errors
    System.err.println("Validation failed: " + errors);
}
```

## Processor Status Values

- `PENDING` - Not started
- `VALIDATING` - Validation in progress
- `PROCESSING` - Processing in progress
- `COMPLETED` - Successfully completed
- `FAILED` - Processing failed
- `CANCELLED` - Processing cancelled

## Error Recovery Strategies

Available in `ProcessingErrorHandler`:
- `FAIL_FAST` - Stop immediately on error
- `SKIP_AND_CONTINUE` - Skip failed file, continue
- `RETRY` - Retry operation (future)
- `LOG_AND_CONTINUE` - Log and continue

## Best Practices

1. **Always shutdown the pipeline** when done to release resources
2. **Use progress listeners** for long-running operations
3. **Validate files** before processing when possible
4. **Set appropriate thread count** for concurrent processing
5. **Handle errors gracefully** with listeners
6. **Check result.isSuccess()** before using data
7. **Use try-with-resources** for automatic cleanup where possible

## File Type Support

| Type | Extensions | Processor |
|------|------------|-----------|
| Excel | .xlsx, .xls | SpreadsheetProcessor |
| PDF | .pdf | PDFProcessor |
| Word | .docx, .doc | WordProcessor |

**Note**: ZIP file handling is done at a higher level (FileProcessingService extracts and processes contents)

## Performance Tips

1. **Concurrent Processing**: Use `FileProcessingPipeline(n)` with n = number of CPU cores
2. **Batch Processing**: Process multiple files together with `processFiles()`
3. **Early Validation**: Validate files before processing to avoid wasted work
4. **Appropriate Thread Count**: Don't use too many threads (2-8 is usually optimal)

## Troubleshooting

### Problem: "No processor found for file"
**Solution**: Check if file type is supported with `FileTypeDetector.detectFileType()`

### Problem: "File validation failed"
**Solution**: Use `FileMetadataExtractor.validateFile()` to see specific errors

### Problem: "Processing hangs"
**Solution**: Ensure you call `pipeline.shutdown()` when done

### Problem: "Out of memory with many files"
**Solution**: Reduce thread count or process files in smaller batches

## See Also

- [FILE_PROCESSING_ARCHITECTURE.md](FILE_PROCESSING_ARCHITECTURE.md) - Detailed architecture
- [FileProcessingExample.java](src/main/java/com/ats/examples/FileProcessingExample.java) - Complete examples
- [ARCHITECTURE_DIAGRAM.md](ARCHITECTURE_DIAGRAM.md) - Visual diagrams
- [IMPLEMENTATION_SUMMARY.md](IMPLEMENTATION_SUMMARY.md) - Implementation details
