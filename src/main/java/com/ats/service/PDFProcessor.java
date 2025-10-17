package com.ats.service;

import com.ats.model.DataRow;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;

import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Service for processing PDF files
 */
public class PDFProcessor {
    
    private static final Pattern EMAIL_PATTERN = Pattern.compile(
        "[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}"
    );
    
    private static final Pattern PHONE_PATTERN = Pattern.compile(
        "\\+?\\d{1,4}?[-.\\s]?\\(?\\d{1,3}?\\)?[-.\\s]?\\d{1,4}[-.\\s]?\\d{1,4}[-.\\s]?\\d{1,9}"
    );

    /**
     * Process PDF file and extract content containing keywords
     */
    public List<DataRow> processFile(File file, Set<String> keywords) throws IOException {
        List<DataRow> results = new ArrayList<>();
        
        try (PDDocument document = PDDocument.load(file)) {
            PDFTextStripper stripper = new PDFTextStripper();
            String text = stripper.getText(document);
            
            // Split text into paragraphs
            String[] paragraphs = text.split("\n\n+");
            
            for (int i = 0; i < paragraphs.length; i++) {
                String paragraph = paragraphs[i];
                
                if (containsKeyword(paragraph, keywords)) {
                    DataRow dataRow = new DataRow();
                    dataRow.setFileName(file.getName());
                    dataRow.setRowNumber(i + 1);
                    dataRow.setContent(paragraph.trim());
                    
                    // Try to extract email
                    Matcher emailMatcher = EMAIL_PATTERN.matcher(paragraph);
                    if (emailMatcher.find()) {
                        dataRow.setEmail(emailMatcher.group());
                    }
                    
                    // Try to extract phone
                    Matcher phoneMatcher = PHONE_PATTERN.matcher(paragraph);
                    if (phoneMatcher.find()) {
                        dataRow.setContact(phoneMatcher.group());
                    }
                    
                    // Count non-empty fields
                    int fieldCount = 0;
                    if (dataRow.getEmail() != null) fieldCount++;
                    if (dataRow.getContact() != null) fieldCount++;
                    if (paragraph.trim().length() > 0) fieldCount++;
                    dataRow.setFieldCount(fieldCount);
                    
                    results.add(dataRow);
                }
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
