# File Processing Architecture

## Overview

This document describes the file processing architecture implemented in the ATS Management System. The architecture provides a robust, extensible, and maintainable framework for processing various file types (Excel, PDF, Word documents).

## Architecture Components

### 1. Core Interfaces and Abstractions

#### FileProcessor Interface
The `FileProcessor` interface is the foundation of the architecture. All file processors must implement this interface.

**Key Methods:**
- `processFile(File, Set<String>)` - Process a file and extract data
- `supports(File)` - Check if processor can handle a file
- `getProcessorName()` - Get processor identifier
- `validateFile(File)` - Validate file before processing

**Location:** `com.ats.processor.FileProcessor`

#### Concrete Implementations
- **SpreadsheetProcessor** - Handles Excel files (.xlsx, .xls)
- **PDFProcessor** - Handles PDF documents (.pdf)
- **WordProcessor** - Handles Word documents (.docx, .doc)

All processors implement the `FileProcessor` interface and are located in `com.ats.service` package.

### 2. File Type Detection

#### FileTypeDetector
Robust file type detection using:
1. **Magic Numbers (File Signatures)** - Reads first bytes to identify format
2. **Extension Fallback** - Uses file extension if signature detection fails

**Supported Types:**
- XLSX, XLS (Excel)
- PDF
- DOCX, DOC (Word)
- ZIP archives

**Key Methods:**
- `detectFileType(File)` - Detect file type
- `isSpreadsheet(File)` - Check if file is spreadsheet
- `isPDF(File)` - Check if file is PDF
- `isWordDocument(File)` - Check if file is Word doc

**Location:** `com.ats.processor.FileTypeDetector`

### 3. Processing Pipeline

#### FileProcessingPipeline
Orchestrates the complete file processing workflow with support for:
- Multi-stage processing (Validation → Processing → Completion)
- Concurrent file processing
- Progress tracking
- Error handling
- Event notifications

**Processing Stages:**
1. **PENDING** - Initial state
2. **VALIDATING** - File validation
3. **PROCESSING** - Active processing
4. **COMPLETED** - Successfully finished
5. **FAILED** - Processing failed
6. **CANCELLED** - Processing cancelled

**Key Methods:**
- `processSingleFile(File, Set<String>)` - Process one file
- `processFiles(List<File>, Set<String>)` - Process multiple files concurrently
- `registerProcessor(FileProcessor)` - Register a processor
- `registerListener(ProcessingProgressListener)` - Add progress listener

**Location:** `com.ats.processor.FileProcessingPipeline`

### 4. Processing Context

#### ProcessingContext
Carries state and metadata throughout the processing pipeline.

**Features:**
- Status tracking
- Error collection
- Metadata storage
- Progress calculation
- Timing information

**Key Properties:**
- `sourceFile` - File being processed
- `keywords` - Search keywords
- `status` - Current processing status
- `errors` - List of errors encountered
- `metadata` - Custom metadata map
- `processedItems` / `totalItems` - Progress tracking

**Location:** `com.ats.processor.ProcessingContext`

### 5. Progress Tracking

#### ProcessingProgressListener
Interface for tracking processing events.

**Events:**
- `onProcessingStarted(ProcessingContext)` - Processing begins
- `onProgressUpdate(ProcessingContext)` - Progress update
- `onProcessingCompleted(ProcessingContext)` - Processing succeeds
- `onProcessingFailed(ProcessingContext, Throwable)` - Processing fails

**Usage Example:**
```java
pipeline.registerListener(new ProcessingProgressListener() {
    @Override
    public void onProcessingCompleted(ProcessingContext context) {
        System.out.println("Processed: " + context.getSourceFile().getName());
    }
});
```

**Location:** `com.ats.processor.ProcessingProgressListener`

### 6. Error Handling

#### ProcessingErrorHandler
Centralized error handling with configurable recovery strategies.

**Recovery Strategies:**
- **FAIL_FAST** - Stop immediately on error
- **SKIP_AND_CONTINUE** - Skip failed file, continue with others
- **RETRY** - Retry failed operation (future enhancement)
- **LOG_AND_CONTINUE** - Log error and continue

**Features:**
- Error listener support
- Recoverable error detection
- Validation error handling

**Location:** `com.ats.processor.ProcessingErrorHandler`

### 7. File Metadata Extraction

#### FileMetadataExtractor
Utility for extracting comprehensive file metadata.

**Extracted Metadata:**
- File name, path, size
- Last modified date
- Permissions (read/write)
- File type and MIME type
- Extension
- Parent directory info

**Key Methods:**
- `extractMetadata(File)` - Extract all metadata
- `formatFileSize(long)` - Human-readable size
- `isProcessable(File)` - Check if file can be processed
- `summarizeFiles(List<File>)` - Summarize multiple files
- `validateFile(File)` - Validate file and return errors

**Location:** `com.ats.processor.FileMetadataExtractor`

### 8. Processor Factory

#### FileProcessorFactory
Singleton factory for managing processors.

**Features:**
- Automatic processor registration
- Processor selection by file type
- Custom processor support

**Key Methods:**
- `getInstance()` - Get factory instance
- `getProcessor(File)` - Get appropriate processor for file
- `registerProcessor(FileProcessor)` - Register custom processor
- `canProcess(File)` - Check if file is supported

**Location:** `com.ats.processor.FileProcessorFactory`

## Usage Guide

### Basic Usage

```java
// Create pipeline
FileProcessingPipeline pipeline = new FileProcessingPipeline();

// Register processors
FileProcessorFactory factory = FileProcessorFactory.getInstance();
for (FileProcessor processor : factory.getAllProcessors()) {
    pipeline.registerProcessor(processor);
}

// Process a file
File file = new File("resume.xlsx");
Set<String> keywords = Set.of("java", "developer");
ProcessingResult result = pipeline.processSingleFile(file, keywords);

// Access results
List<DataRow> data = result.getData();
ProcessingContext context = result.getContext();
```

### Concurrent Processing

```java
FileProcessingPipeline pipeline = new FileProcessingPipeline(8); // 8 threads
List<File> files = Arrays.asList(file1, file2, file3);
List<ProcessingResult> results = pipeline.processFiles(files, keywords);
```

### With Progress Tracking

```java
pipeline.registerListener(new ProcessingProgressListener() {
    @Override
    public void onProcessingStarted(ProcessingContext context) {
        System.out.println("Starting: " + context.getSourceFile().getName());
    }
    
    @Override
    public void onProcessingCompleted(ProcessingContext context) {
        System.out.println("Completed in " + context.getProcessingTimeMs() + "ms");
    }
});
```

### File Type Detection

```java
FileTypeDetector.FileType type = FileTypeDetector.detectFileType(file);
System.out.println("Type: " + type.getDescription());

if (FileTypeDetector.isSpreadsheet(file)) {
    // Handle spreadsheet
}
```

### Metadata Extraction

```java
Map<String, Object> metadata = FileMetadataExtractor.extractMetadata(file);
System.out.println("Size: " + metadata.get("sizeHuman"));
System.out.println("Type: " + metadata.get("typeDescription"));

// Validate file
List<String> errors = FileMetadataExtractor.validateFile(file);
if (!errors.isEmpty()) {
    System.err.println("Validation errors: " + errors);
}
```

## Architecture Benefits

1. **Separation of Concerns** - Each component has a single responsibility
2. **Extensibility** - Easy to add new file processors
3. **Testability** - Components can be tested independently
4. **Maintainability** - Clear structure and responsibilities
5. **Robustness** - Comprehensive error handling
6. **Performance** - Concurrent processing support
7. **Observability** - Progress tracking and error reporting
8. **Reliability** - File validation and type detection

## Design Patterns Used

1. **Interface Segregation** - FileProcessor interface
2. **Strategy Pattern** - Error recovery strategies
3. **Factory Pattern** - FileProcessorFactory
4. **Observer Pattern** - Progress listeners
5. **Pipeline Pattern** - FileProcessingPipeline
6. **Singleton Pattern** - FileProcessorFactory

## Extension Points

### Adding a New File Processor

```java
public class CustomProcessor implements FileProcessor {
    @Override
    public List<DataRow> processFile(File file, Set<String> keywords) {
        // Implementation
    }
    
    @Override
    public boolean supports(File file) {
        return file.getName().endsWith(".custom");
    }
    
    @Override
    public String getProcessorName() {
        return "CustomProcessor";
    }
}

// Register
FileProcessorFactory.getInstance().registerProcessor(new CustomProcessor());
```

### Custom Error Handling

```java
ProcessingErrorHandler errorHandler = new ProcessingErrorHandler();
errorHandler.setRecoveryStrategy(ErrorRecoveryStrategy.RETRY);
errorHandler.addErrorListener((context, error, file) -> {
    // Custom error handling
});
```

## Integration with Existing Code

The architecture integrates seamlessly with existing code:
- Existing processors (SpreadsheetProcessor, PDFProcessor, WordProcessor) now implement FileProcessor
- FileProcessingService can use the new pipeline for enhanced functionality
- All existing functionality is preserved
- New features are additive, not breaking

## Future Enhancements

1. **Retry Logic** - Implement automatic retry for transient failures
2. **Batch Processing** - Enhanced batch processing with checkpointing
3. **Distributed Processing** - Support for distributed file processing
4. **Caching** - Cache processed results for repeated queries
5. **Streaming** - Support for streaming large files
6. **Plugins** - Plugin architecture for third-party processors
7. **Metrics** - Detailed processing metrics and analytics
8. **Configuration** - External configuration for processing parameters

## Conclusion

This file processing architecture provides a solid foundation for the ATS Management System. It offers flexibility, reliability, and performance while maintaining clean code structure and testability.
