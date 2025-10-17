package com.ats.processor;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Arrays;

/**
 * Utility class for detecting file types based on file signatures (magic numbers)
 * and extensions. Provides robust file type detection for the ATS system.
 */
public class FileTypeDetector {
    
    public enum FileType {
        XLSX("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet", "Excel Spreadsheet"),
        XLS("application/vnd.ms-excel", "Excel 97-2003 Spreadsheet"),
        PDF("application/pdf", "PDF Document"),
        DOCX("application/vnd.openxmlformats-officedocument.wordprocessingml.document", "Word Document"),
        DOC("application/msword", "Word 97-2003 Document"),
        ZIP("application/zip", "ZIP Archive"),
        UNKNOWN("application/octet-stream", "Unknown");
        
        private final String mimeType;
        private final String description;
        
        FileType(String mimeType, String description) {
            this.mimeType = mimeType;
            this.description = description;
        }
        
        public String getMimeType() {
            return mimeType;
        }
        
        public String getDescription() {
            return description;
        }
    }
    
    // File signatures (magic numbers)
    private static final byte[] PDF_SIGNATURE = {0x25, 0x50, 0x44, 0x46}; // %PDF
    private static final byte[] ZIP_SIGNATURE = {0x50, 0x4B, 0x03, 0x04}; // PK..
    private static final byte[] XLS_SIGNATURE = {(byte) 0xD0, (byte) 0xCF, 0x11, (byte) 0xE0}; // OLE2 format
    
    /**
     * Detect the file type based on both magic number and extension
     * 
     * @param file File to detect
     * @return Detected file type
     */
    public static FileType detectFileType(File file) {
        if (file == null || !file.exists() || !file.isFile()) {
            return FileType.UNKNOWN;
        }
        
        String fileName = file.getName().toLowerCase();
        
        // Check by magic number first (more reliable)
        try {
            FileType typeBySignature = detectBySignature(file);
            if (typeBySignature != FileType.UNKNOWN) {
                // For ZIP-based formats (XLSX, DOCX), check extension to distinguish
                if (typeBySignature == FileType.ZIP) {
                    if (fileName.endsWith(".xlsx")) {
                        return FileType.XLSX;
                    } else if (fileName.endsWith(".docx")) {
                        return FileType.DOCX;
                    } else if (fileName.endsWith(".zip")) {
                        return FileType.ZIP;
                    }
                }
                return typeBySignature;
            }
        } catch (IOException e) {
            // Fall back to extension-based detection
        }
        
        // Fall back to extension-based detection
        return detectByExtension(fileName);
    }
    
    /**
     * Detect file type by reading file signature (magic numbers)
     * 
     * @param file File to check
     * @return Detected file type
     * @throws IOException if reading fails
     */
    private static FileType detectBySignature(File file) throws IOException {
        byte[] signature = new byte[4];
        
        try (FileInputStream fis = new FileInputStream(file)) {
            int bytesRead = fis.read(signature);
            if (bytesRead < 4) {
                return FileType.UNKNOWN;
            }
        }
        
        // Check for PDF
        if (Arrays.equals(signature, PDF_SIGNATURE)) {
            return FileType.PDF;
        }
        
        // Check for ZIP-based formats (XLSX, DOCX, ZIP)
        if (Arrays.equals(signature, ZIP_SIGNATURE)) {
            return FileType.ZIP; // Need to check extension to distinguish
        }
        
        // Check for XLS (OLE2 format)
        if (Arrays.equals(signature, XLS_SIGNATURE)) {
            return FileType.XLS;
        }
        
        return FileType.UNKNOWN;
    }
    
    /**
     * Detect file type by extension
     * 
     * @param fileName File name to check
     * @return Detected file type
     */
    private static FileType detectByExtension(String fileName) {
        if (fileName.endsWith(".xlsx")) {
            return FileType.XLSX;
        } else if (fileName.endsWith(".xls")) {
            return FileType.XLS;
        } else if (fileName.endsWith(".pdf")) {
            return FileType.PDF;
        } else if (fileName.endsWith(".docx")) {
            return FileType.DOCX;
        } else if (fileName.endsWith(".doc")) {
            return FileType.DOC;
        } else if (fileName.endsWith(".zip")) {
            return FileType.ZIP;
        }
        
        return FileType.UNKNOWN;
    }
    
    /**
     * Check if a file is a spreadsheet
     * 
     * @param file File to check
     * @return true if file is a spreadsheet
     */
    public static boolean isSpreadsheet(File file) {
        FileType type = detectFileType(file);
        return type == FileType.XLSX || type == FileType.XLS;
    }
    
    /**
     * Check if a file is a PDF
     * 
     * @param file File to check
     * @return true if file is a PDF
     */
    public static boolean isPDF(File file) {
        return detectFileType(file) == FileType.PDF;
    }
    
    /**
     * Check if a file is a Word document
     * 
     * @param file File to check
     * @return true if file is a Word document
     */
    public static boolean isWordDocument(File file) {
        FileType type = detectFileType(file);
        return type == FileType.DOCX || type == FileType.DOC;
    }
    
    /**
     * Check if a file is a ZIP archive
     * 
     * @param file File to check
     * @return true if file is a ZIP
     */
    public static boolean isZipArchive(File file) {
        return detectFileType(file) == FileType.ZIP;
    }
}
