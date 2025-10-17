package com.ats.service;

import com.ats.model.DataRow;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.apache.poi.xwpf.usermodel.*;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.font.PDType1Font;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.List;

/**
 * Service for exporting data to various file formats
 */
public class ExportService {

    /**
     * Export data to specified format
     */
    public void exportData(List<DataRow> data, File outputFile, String format) throws IOException {
        switch (format.toLowerCase()) {
            case "spreadsheet":
            case "excel":
                exportToExcel(data, outputFile);
                break;
            case "pdf":
                exportToPDF(data, outputFile);
                break;
            case "word":
                exportToWord(data, outputFile);
                break;
            default:
                throw new IllegalArgumentException("Unsupported export format: " + format);
        }
    }

    /**
     * Export data to Excel spreadsheet
     */
    private void exportToExcel(List<DataRow> data, File outputFile) throws IOException {
        try (Workbook workbook = new XSSFWorkbook();
             FileOutputStream fos = new FileOutputStream(outputFile)) {
            
            Sheet sheet = workbook.createSheet("ATS Data");
            
            // Create header row
            Row headerRow = sheet.createRow(0);
            String[] headers = {"File Name", "Sheet/Section", "Row Number", 
                              "Candidate ID", "Name", "Email", "Contact", "Content"};
            for (int i = 0; i < headers.length; i++) {
                Cell cell = headerRow.createCell(i);
                cell.setCellValue(headers[i]);
                
                CellStyle style = workbook.createCellStyle();
                Font font = workbook.createFont();
                font.setBold(true);
                style.setFont(font);
                cell.setCellStyle(style);
            }
            
            // Add data rows with spacing
            int rowIndex = 1;
            for (DataRow dataRow : data) {
                Row row = sheet.createRow(rowIndex++);
                
                row.createCell(0).setCellValue(dataRow.getFileName() != null ? dataRow.getFileName() : "");
                row.createCell(1).setCellValue(dataRow.getSheetName() != null ? dataRow.getSheetName() : "");
                row.createCell(2).setCellValue(dataRow.getRowNumber());
                row.createCell(3).setCellValue(dataRow.getCandidateId() != null ? dataRow.getCandidateId() : "");
                row.createCell(4).setCellValue(dataRow.getName() != null ? dataRow.getName() : "");
                row.createCell(5).setCellValue(dataRow.getEmail() != null ? dataRow.getEmail() : "");
                row.createCell(6).setCellValue(dataRow.getContact() != null ? dataRow.getContact() : "");
                row.createCell(7).setCellValue(dataRow.getContent() != null ? dataRow.getContent() : "");
                
                // Add empty row for spacing
                rowIndex++;
            }
            
            // Auto-size columns
            for (int i = 0; i < headers.length; i++) {
                sheet.autoSizeColumn(i);
            }
            
            workbook.write(fos);
        }
    }

    /**
     * Export data to PDF
     */
    private void exportToPDF(List<DataRow> data, File outputFile) throws IOException {
        try (PDDocument document = new PDDocument()) {
            PDPage page = new PDPage();
            document.addPage(page);
            
            PDPageContentStream contentStream = new PDPageContentStream(document, page);
            
            contentStream.setFont(PDType1Font.HELVETICA_BOLD, 12);
            contentStream.beginText();
            contentStream.newLineAtOffset(50, 750);
            contentStream.showText("ATS Management System - Exported Data");
            contentStream.endText();
            
            float yPosition = 720;
            int pageCount = 0;
            
            for (DataRow dataRow : data) {
                // Check if we need a new page
                if (yPosition < 100) {
                    contentStream.close();
                    page = new PDPage();
                    document.addPage(page);
                    contentStream = new PDPageContentStream(document, page);
                    yPosition = 750;
                    pageCount++;
                }
                
                // Write data row
                contentStream.setFont(PDType1Font.HELVETICA, 10);
                contentStream.beginText();
                contentStream.newLineAtOffset(50, yPosition);
                
                String line = String.format("File: %s | Row: %d | %s",
                    dataRow.getFileName() != null ? dataRow.getFileName() : "N/A",
                    dataRow.getRowNumber(),
                    dataRow.getContent() != null ? 
                        (dataRow.getContent().length() > 60 ? 
                            dataRow.getContent().substring(0, 60) + "..." : 
                            dataRow.getContent()) : "");
                
                contentStream.showText(line);
                contentStream.endText();
                
                yPosition -= 30; // Move to next row with spacing
            }
            
            contentStream.close();
            document.save(outputFile);
        }
    }

    /**
     * Export data to Word document
     */
    private void exportToWord(List<DataRow> data, File outputFile) throws IOException {
        try (XWPFDocument document = new XWPFDocument();
             FileOutputStream fos = new FileOutputStream(outputFile)) {
            
            // Add title
            XWPFParagraph title = document.createParagraph();
            title.setAlignment(ParagraphAlignment.CENTER);
            XWPFRun titleRun = title.createRun();
            titleRun.setText("ATS Management System - Exported Data");
            titleRun.setBold(true);
            titleRun.setFontSize(16);
            
            // Add empty line
            document.createParagraph();
            
            // Create table
            XWPFTable table = document.createTable();
            
            // Create header row
            XWPFTableRow headerRow = table.getRow(0);
            headerRow.getCell(0).setText("File Name");
            headerRow.addNewTableCell().setText("Row Number");
            headerRow.addNewTableCell().setText("Candidate ID");
            headerRow.addNewTableCell().setText("Name");
            headerRow.addNewTableCell().setText("Email");
            headerRow.addNewTableCell().setText("Contact");
            headerRow.addNewTableCell().setText("Content");
            
            // Add data rows
            for (DataRow dataRow : data) {
                XWPFTableRow row = table.createRow();
                row.getCell(0).setText(dataRow.getFileName() != null ? dataRow.getFileName() : "");
                row.getCell(1).setText(String.valueOf(dataRow.getRowNumber()));
                row.getCell(2).setText(dataRow.getCandidateId() != null ? dataRow.getCandidateId() : "");
                row.getCell(3).setText(dataRow.getName() != null ? dataRow.getName() : "");
                row.getCell(4).setText(dataRow.getEmail() != null ? dataRow.getEmail() : "");
                row.getCell(5).setText(dataRow.getContact() != null ? dataRow.getContact() : "");
                row.getCell(6).setText(dataRow.getContent() != null ? dataRow.getContent() : "");
            }
            
            document.write(fos);
        }
    }
}
