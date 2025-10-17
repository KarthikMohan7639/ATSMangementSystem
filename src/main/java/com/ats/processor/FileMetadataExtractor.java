package com.ats.processor;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Utility class for extracting metadata from files.
 * Provides information about file properties useful for processing and tracking.
 */
public class FileMetadataExtractor {
    
    /**
     * Extract metadata from a file
     * 
     * @param file File to extract metadata from
     * @return Map containing metadata
     */
    public static Map<String, Object> extractMetadata(File file) {
        Map<String, Object> metadata = new HashMap<>();
        
        if (file == null || !file.exists()) {
            return metadata;
        }
        
        // Basic file properties
        metadata.put("name", file.getName());
        metadata.put("path", file.getAbsolutePath());
        metadata.put("size", file.length());
        metadata.put("sizeHuman", formatFileSize(file.length()));
        metadata.put("lastModified", file.lastModified());
        metadata.put("canRead", file.canRead());
        metadata.put("canWrite", file.canWrite());
        metadata.put("isHidden", file.isHidden());
        
        // File extension
        String fileName = file.getName();
        int dotIndex = fileName.lastIndexOf('.');
        if (dotIndex > 0 && dotIndex < fileName.length() - 1) {
            metadata.put("extension", fileName.substring(dotIndex + 1).toLowerCase());
        } else {
            metadata.put("extension", "");
        }
        
        // File type detection
        FileTypeDetector.FileType fileType = FileTypeDetector.detectFileType(file);
        metadata.put("detectedType", fileType.name());
        metadata.put("mimeType", fileType.getMimeType());
        metadata.put("typeDescription", fileType.getDescription());
        
        // Parent directory info
        File parent = file.getParentFile();
        if (parent != null) {
            metadata.put("parentDir", parent.getName());
            metadata.put("parentPath", parent.getAbsolutePath());
        }
        
        return metadata;
    }
    
    /**
     * Format file size in human-readable format
     * 
     * @param size Size in bytes
     * @return Formatted string
     */
    public static String formatFileSize(long size) {
        if (size < 1024) {
            return size + " B";
        }
        
        int exp = (int) (Math.log(size) / Math.log(1024));
        String pre = "KMGTPE".charAt(exp - 1) + "";
        return String.format("%.1f %sB", size / Math.pow(1024, exp), pre);
    }
    
    /**
     * Check if file is likely to be processable
     * 
     * @param file File to check
     * @return true if file appears processable
     */
    public static boolean isProcessable(File file) {
        if (file == null || !file.exists() || !file.isFile() || !file.canRead()) {
            return false;
        }
        
        // Check if file has supported type
        FileTypeDetector.FileType type = FileTypeDetector.detectFileType(file);
        return type != FileTypeDetector.FileType.UNKNOWN;
    }
    
    /**
     * Get a summary of metadata for a list of files
     * 
     * @param files Files to summarize
     * @return Summary information
     */
    public static Map<String, Object> summarizeFiles(List<File> files) {
        Map<String, Object> summary = new HashMap<>();
        
        if (files == null || files.isEmpty()) {
            summary.put("totalFiles", 0);
            summary.put("totalSize", 0L);
            return summary;
        }
        
        long totalSize = 0;
        int processableCount = 0;
        Map<String, Integer> typeCount = new HashMap<>();
        
        for (File file : files) {
            if (file.exists() && file.isFile()) {
                totalSize += file.length();
                
                if (isProcessable(file)) {
                    processableCount++;
                }
                
                FileTypeDetector.FileType type = FileTypeDetector.detectFileType(file);
                typeCount.put(type.name(), typeCount.getOrDefault(type.name(), 0) + 1);
            }
        }
        
        summary.put("totalFiles", files.size());
        summary.put("processableFiles", processableCount);
        summary.put("totalSize", totalSize);
        summary.put("totalSizeHuman", formatFileSize(totalSize));
        summary.put("typeDistribution", typeCount);
        
        return summary;
    }
    
    /**
     * Validate that a file meets minimum requirements
     * 
     * @param file File to validate
     * @return List of validation errors (empty if valid)
     */
    public static List<String> validateFile(File file) {
        List<String> errors = new ArrayList<>();
        
        if (file == null) {
            errors.add("File is null");
            return errors;
        }
        
        if (!file.exists()) {
            errors.add("File does not exist: " + file.getPath());
        }
        
        if (!file.isFile()) {
            errors.add("Not a regular file: " + file.getPath());
        }
        
        if (!file.canRead()) {
            errors.add("Cannot read file: " + file.getPath());
        }
        
        if (file.length() == 0) {
            errors.add("File is empty: " + file.getName());
        }
        
        if (file.length() > 100 * 1024 * 1024) { // 100 MB
            errors.add("File is too large (> 100MB): " + file.getName());
        }
        
        FileTypeDetector.FileType type = FileTypeDetector.detectFileType(file);
        if (type == FileTypeDetector.FileType.UNKNOWN) {
            errors.add("Unknown or unsupported file type: " + file.getName());
        }
        
        return errors;
    }
}
