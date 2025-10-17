package com.ats.service;

import com.ats.model.DataRow;
import com.ats.processor.FileProcessor;
import com.ats.processor.FileTypeDetector;
import org.apache.poi.xwpf.usermodel.*;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Service for processing Word document files
 */
public class WordProcessor implements FileProcessor {
    
    private static final Pattern EMAIL_PATTERN = Pattern.compile(
        "[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}"
    );
    
    private static final Pattern PHONE_PATTERN = Pattern.compile(
        "\\+?\\d{1,4}?[-.\\s]?\\(?\\d{1,3}?\\)?[-.\\s]?\\d{1,4}[-.\\s]?\\d{1,4}[-.\\s]?\\d{1,9}"
    );
    
    @Override
    public boolean supports(File file) {
        return FileTypeDetector.isWordDocument(file);
    }
    
    @Override
    public String getProcessorName() {
        return "WordProcessor";
    }

    /**
     * Process Word document and extract content containing keywords
     */
    @Override
    public List<DataRow> processFile(File file, Set<String> keywords) throws IOException {
        validateFile(file);
        List<DataRow> results = new ArrayList<>();
        
        try (FileInputStream fis = new FileInputStream(file);
             XWPFDocument document = new XWPFDocument(fis)) {
            
            List<XWPFParagraph> paragraphs = document.getParagraphs();
            
            for (int i = 0; i < paragraphs.size(); i++) {
                XWPFParagraph paragraph = paragraphs.get(i);
                String text = paragraph.getText();
                
                if (text != null && !text.trim().isEmpty() && containsKeyword(text, keywords)) {
                    DataRow dataRow = new DataRow();
                    dataRow.setFileName(file.getName());
                    dataRow.setRowNumber(i + 1);
                    dataRow.setContent(text.trim());
                    
                    // Try to extract email
                    Matcher emailMatcher = EMAIL_PATTERN.matcher(text);
                    if (emailMatcher.find()) {
                        dataRow.setEmail(emailMatcher.group());
                    }
                    
                    // Try to extract phone
                    Matcher phoneMatcher = PHONE_PATTERN.matcher(text);
                    if (phoneMatcher.find()) {
                        dataRow.setContact(phoneMatcher.group());
                    }
                    
                    // Count non-empty fields
                    int fieldCount = 0;
                    if (dataRow.getEmail() != null) fieldCount++;
                    if (dataRow.getContact() != null) fieldCount++;
                    if (text.trim().length() > 0) fieldCount++;
                    dataRow.setFieldCount(fieldCount);
                    
                    results.add(dataRow);
                }
            }
            
            // Also process tables
            List<XWPFTable> tables = document.getTables();
            for (XWPFTable table : tables) {
                results.addAll(processTable(table, file.getName(), keywords));
            }
        }
        
        return results;
    }

    /**
     * Process table in Word document
     */
    private List<DataRow> processTable(XWPFTable table, String fileName, Set<String> keywords) {
        List<DataRow> results = new ArrayList<>();
        
        List<XWPFTableRow> rows = table.getRows();
        if (rows.isEmpty()) return results;
        
        // Get headers from first row
        XWPFTableRow headerRow = rows.get(0);
        List<String> headers = new ArrayList<>();
        for (XWPFTableCell cell : headerRow.getTableCells()) {
            headers.add(cell.getText());
        }
        
        // Process data rows
        for (int i = 1; i < rows.size(); i++) {
            XWPFTableRow row = rows.get(i);
            StringBuilder content = new StringBuilder();
            
            boolean hasKeyword = false;
            for (XWPFTableCell cell : row.getTableCells()) {
                String cellText = cell.getText();
                if (containsKeyword(cellText, keywords)) {
                    hasKeyword = true;
                }
                content.append(cellText).append(" | ");
            }
            
            if (hasKeyword) {
                DataRow dataRow = new DataRow();
                dataRow.setFileName(fileName);
                dataRow.setRowNumber(i);
                dataRow.setContent(content.toString());
                
                // Try to extract email and phone from content
                String fullContent = content.toString();
                Matcher emailMatcher = EMAIL_PATTERN.matcher(fullContent);
                if (emailMatcher.find()) {
                    dataRow.setEmail(emailMatcher.group());
                }
                
                Matcher phoneMatcher = PHONE_PATTERN.matcher(fullContent);
                if (phoneMatcher.find()) {
                    dataRow.setContact(phoneMatcher.group());
                }
                
                dataRow.setFieldCount(row.getTableCells().size());
                results.add(dataRow);
            }
        }
        
        return results;
    }

    /**
     * Check if text contains any of the keywords
     */
    private boolean containsKeyword(String text, Set<String> keywords) {
        if (keywords.isEmpty()) return true;
        
        String lowerText = text.toLowerCase();
        for (String keyword : keywords) {
            if (lowerText.contains(keyword.toLowerCase())) {
                return true;
            }
        }
        return false;
    }
}
