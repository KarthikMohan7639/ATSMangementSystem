package com.ats.processor;

import com.ats.model.DataRow;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Set;

/**
 * Common interface for all file processors in the ATS system.
 * This interface defines the contract for processing different file types.
 */
public interface FileProcessor {
    
    /**
     * Process a file and extract data based on keywords
     * 
     * @param file File to process
     * @param keywords Keywords to search for (empty set means extract all data)
     * @return List of extracted data rows
     * @throws IOException if file processing fails
     */
    List<DataRow> processFile(File file, Set<String> keywords) throws IOException;
    
    /**
     * Check if this processor supports the given file
     * 
     * @param file File to check
     * @return true if this processor can handle the file
     */
    boolean supports(File file);
    
    /**
     * Get the processor name for logging and identification
     * 
     * @return Processor name
     */
    String getProcessorName();
    
    /**
     * Validate the file before processing
     * 
     * @param file File to validate
     * @throws IllegalArgumentException if file is invalid
     */
    default void validateFile(File file) {
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
    }
}
