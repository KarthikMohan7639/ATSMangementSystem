package com.ats.service;

import com.ats.model.DataRow;

import java.io.*;
import java.util.*;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

/**
 * Service for coordinating file processing across different file types
 */
public class FileProcessingService {
    
    private final SpreadsheetProcessor spreadsheetProcessor;
    private final PDFProcessor pdfProcessor;
    private final WordProcessor wordProcessor;
    
    public FileProcessingService() {
        this.spreadsheetProcessor = new SpreadsheetProcessor();
        this.pdfProcessor = new PDFProcessor();
        this.wordProcessor = new WordProcessor();
    }

    /**
     * Process files or folders based on selected file types and keywords
     */
    public List<DataRow> processFiles(List<File> files, Set<String> fileTypes, 
                                     Set<String> keywords) throws IOException {
        List<DataRow> allResults = new ArrayList<>();
        
        for (File file : files) {
            if (file.isDirectory()) {
                allResults.addAll(processDirectory(file, fileTypes, keywords));
            } else if (file.getName().endsWith(".zip")) {
                allResults.addAll(processZipFile(file, fileTypes, keywords));
            } else {
                allResults.addAll(processSingleFile(file, fileTypes, keywords));
            }
        }
        
        // Remove duplicates based on unique identifiers
        return removeDuplicates(allResults);
    }

    /**
     * Process a directory recursively
     */
    private List<DataRow> processDirectory(File directory, Set<String> fileTypes, 
                                          Set<String> keywords) throws IOException {
        List<DataRow> results = new ArrayList<>();
        
        File[] files = directory.listFiles();
        if (files != null) {
            for (File file : files) {
                if (file.isDirectory()) {
                    results.addAll(processDirectory(file, fileTypes, keywords));
                } else {
                    results.addAll(processSingleFile(file, fileTypes, keywords));
                }
            }
        }
        
        return results;
    }

    /**
     * Process a ZIP file
     */
    private List<DataRow> processZipFile(File zipFile, Set<String> fileTypes, 
                                        Set<String> keywords) throws IOException {
        List<DataRow> results = new ArrayList<>();
        
        try (ZipInputStream zis = new ZipInputStream(new FileInputStream(zipFile))) {
            ZipEntry entry;
            
            while ((entry = zis.getNextEntry()) != null) {
                if (!entry.isDirectory()) {
                    // Create temporary file
                    File tempFile = File.createTempFile("ats_", "_" + entry.getName());
                    tempFile.deleteOnExit();
                    
                    try (FileOutputStream fos = new FileOutputStream(tempFile)) {
                        byte[] buffer = new byte[1024];
                        int len;
                        while ((len = zis.read(buffer)) > 0) {
                            fos.write(buffer, 0, len);
                        }
                    }
                    
                    results.addAll(processSingleFile(tempFile, fileTypes, keywords));
                    tempFile.delete();
                }
                zis.closeEntry();
            }
        }
        
        return results;
    }

    /**
     * Process a single file based on its type
     */
    private List<DataRow> processSingleFile(File file, Set<String> fileTypes, 
                                          Set<String> keywords) throws IOException {
        List<DataRow> results = new ArrayList<>();
        
        String fileName = file.getName().toLowerCase();
        
        // Process spreadsheets
        if (fileTypes.contains("Spreadsheet") && 
            (fileName.endsWith(".xlsx") || fileName.endsWith(".xls"))) {
            results.addAll(spreadsheetProcessor.processFile(file, keywords));
        }
        
        // Process PDFs
        if (fileTypes.contains("PDF") && fileName.endsWith(".pdf")) {
            results.addAll(pdfProcessor.processFile(file, keywords));
        }
        
        // Process Word documents
        if (fileTypes.contains("Word") && 
            (fileName.endsWith(".docx") || fileName.endsWith(".doc"))) {
            results.addAll(wordProcessor.processFile(file, keywords));
        }
        
        return results;
    }

    /**
     * Remove duplicate rows based on unique identifiers
     */
    private List<DataRow> removeDuplicates(List<DataRow> rows) {
        List<DataRow> uniqueRows = new ArrayList<>();
        
        for (DataRow row : rows) {
            boolean isDuplicate = false;
            
            for (DataRow existing : uniqueRows) {
                if (row.isDuplicateOf(existing)) {
                    isDuplicate = true;
                    break;
                }
            }
            
            if (!isDuplicate) {
                uniqueRows.add(row);
            }
        }
        
        return uniqueRows;
    }
}
