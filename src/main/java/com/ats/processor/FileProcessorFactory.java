package com.ats.processor;

import com.ats.service.PDFProcessor;
import com.ats.service.SpreadsheetProcessor;
import com.ats.service.WordProcessor;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * Factory for creating and managing file processors.
 * Provides centralized access to all available processors and automatic
 * processor selection based on file type.
 */
public class FileProcessorFactory {
    
    private static FileProcessorFactory instance;
    private final List<FileProcessor> registeredProcessors;
    
    private FileProcessorFactory() {
        this.registeredProcessors = new ArrayList<>();
        registerDefaultProcessors();
    }
    
    /**
     * Get singleton instance of the factory
     * 
     * @return Factory instance
     */
    public static synchronized FileProcessorFactory getInstance() {
        if (instance == null) {
            instance = new FileProcessorFactory();
        }
        return instance;
    }
    
    /**
     * Register default processors (Spreadsheet, PDF, Word)
     */
    private void registerDefaultProcessors() {
        registeredProcessors.add(new SpreadsheetProcessor());
        registeredProcessors.add(new PDFProcessor());
        registeredProcessors.add(new WordProcessor());
    }
    
    /**
     * Register a custom processor
     * 
     * @param processor Processor to register
     */
    public void registerProcessor(FileProcessor processor) {
        if (processor != null && !registeredProcessors.contains(processor)) {
            registeredProcessors.add(processor);
        }
    }
    
    /**
     * Get a processor that can handle the given file
     * 
     * @param file File to process
     * @return Appropriate processor or null if none found
     */
    public FileProcessor getProcessor(File file) {
        for (FileProcessor processor : registeredProcessors) {
            if (processor.supports(file)) {
                return processor;
            }
        }
        return null;
    }
    
    /**
     * Get all registered processors
     * 
     * @return List of processors
     */
    public List<FileProcessor> getAllProcessors() {
        return new ArrayList<>(registeredProcessors);
    }
    
    /**
     * Check if a file can be processed by any registered processor
     * 
     * @param file File to check
     * @return true if file can be processed
     */
    public boolean canProcess(File file) {
        return getProcessor(file) != null;
    }
    
    /**
     * Get processor by name
     * 
     * @param name Processor name
     * @return Processor or null if not found
     */
    public FileProcessor getProcessorByName(String name) {
        for (FileProcessor processor : registeredProcessors) {
            if (processor.getProcessorName().equalsIgnoreCase(name)) {
                return processor;
            }
        }
        return null;
    }
    
    /**
     * Clear all registered processors (useful for testing)
     */
    public void clearProcessors() {
        registeredProcessors.clear();
    }
    
    /**
     * Reset to default processors
     */
    public void resetToDefaults() {
        clearProcessors();
        registerDefaultProcessors();
    }
}
