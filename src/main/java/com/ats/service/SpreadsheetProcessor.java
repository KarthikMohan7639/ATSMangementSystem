package com.ats.service;

import com.ats.model.DataRow;
import com.ats.processor.FileProcessor;
import com.ats.processor.FileTypeDetector;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.*;

/**
 * Service for processing Excel spreadsheet files
 */
public class SpreadsheetProcessor implements FileProcessor {
    
    @Override
    public boolean supports(File file) {
        return FileTypeDetector.isSpreadsheet(file);
    }
    
    @Override
    public String getProcessorName() {
        return "SpreadsheetProcessor";
    }

    /**
     * Process Excel file and extract rows containing keywords
     */
    @Override
    public List<DataRow> processFile(File file, Set<String> keywords) throws IOException {
        validateFile(file);
        List<DataRow> results = new ArrayList<>();
        
        try (FileInputStream fis = new FileInputStream(file)) {
            Workbook workbook = null;
            
            // Determine workbook type based on file extension
            if (file.getName().endsWith(".xlsx")) {
                workbook = new XSSFWorkbook(fis);
            } else if (file.getName().endsWith(".xls")) {
                workbook = new HSSFWorkbook(fis);
            } else {
                return results; // Not a supported spreadsheet file
            }
            
            // Process each sheet
            for (int i = 0; i < workbook.getNumberOfSheets(); i++) {
                Sheet sheet = workbook.getSheetAt(i);
                results.addAll(processSheet(sheet, file.getName(), keywords));
            }
            
            workbook.close();
        }
        
        // Sort by field count (maximum filled rows first, then minimum)
        results.sort((r1, r2) -> Integer.compare(r2.getFieldCount(), r1.getFieldCount()));
        
        return results;
    }

    /**
     * Process a single sheet and extract matching rows
     */
    private List<DataRow> processSheet(Sheet sheet, String fileName, Set<String> keywords) {
        List<DataRow> results = new ArrayList<>();
        
        // Get header row (assumed to be first row)
        Row headerRow = sheet.getRow(0);
        if (headerRow == null) return results;
        
        List<String> headers = new ArrayList<>();
        for (Cell cell : headerRow) {
            headers.add(getCellValueAsString(cell));
        }
        
        // Process data rows
        for (int i = 1; i <= sheet.getLastRowNum(); i++) {
            Row row = sheet.getRow(i);
            if (row == null) continue;
            
            // Check if row contains any keyword
            if (rowContainsKeyword(row, keywords)) {
                DataRow dataRow = extractDataRow(row, headers, fileName, sheet.getSheetName(), i);
                results.add(dataRow);
            }
        }
        
        return results;
    }

    /**
     * Check if a row contains any of the keywords
     */
    private boolean rowContainsKeyword(Row row, Set<String> keywords) {
        if (keywords.isEmpty()) return true; // If no keywords, return all rows
        
        for (Cell cell : row) {
            String cellValue = getCellValueAsString(cell).toLowerCase();
            for (String keyword : keywords) {
                if (cellValue.contains(keyword.toLowerCase())) {
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * Extract data row with all field information
     */
    private DataRow extractDataRow(Row row, List<String> headers, String fileName, 
                                   String sheetName, int rowNumber) {
        DataRow dataRow = new DataRow();
        dataRow.setFileName(fileName);
        dataRow.setSheetName(sheetName);
        dataRow.setRowNumber(rowNumber);
        
        StringBuilder contentBuilder = new StringBuilder();
        int fieldCount = 0;
        
        for (int i = 0; i < headers.size(); i++) {
            Cell cell = row.getCell(i);
            String value = getCellValueAsString(cell);
            
            if (value != null && !value.trim().isEmpty()) {
                fieldCount++;
                
                String header = headers.get(i).toLowerCase();
                
                // Try to identify special fields
                if (header.contains("candidate") && header.contains("id")) {
                    dataRow.setCandidateId(value);
                } else if (header.contains("name") && !header.contains("file")) {
                    dataRow.setName(value);
                } else if (header.contains("email") || header.contains("e-mail")) {
                    dataRow.setEmail(value);
                } else if (header.contains("contact") || header.contains("phone") || header.contains("mobile")) {
                    dataRow.setContact(value);
                }
                
                contentBuilder.append(headers.get(i)).append(": ").append(value).append(" | ");
            }
        }
        
        dataRow.setFieldCount(fieldCount);
        dataRow.setContent(contentBuilder.toString());
        
        return dataRow;
    }

    /**
     * Get cell value as string
     */
    private String getCellValueAsString(Cell cell) {
        if (cell == null) return "";
        
        switch (cell.getCellType()) {
            case STRING:
                return cell.getStringCellValue();
            case NUMERIC:
                if (DateUtil.isCellDateFormatted(cell)) {
                    return cell.getDateCellValue().toString();
                } else {
                    return String.valueOf(cell.getNumericCellValue());
                }
            case BOOLEAN:
                return String.valueOf(cell.getBooleanCellValue());
            case FORMULA:
                try {
                    return cell.getStringCellValue();
                } catch (Exception e) {
                    return String.valueOf(cell.getNumericCellValue());
                }
            case BLANK:
                return "";
            default:
                return "";
        }
    }
}
