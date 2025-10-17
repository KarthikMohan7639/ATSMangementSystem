package com.ats.model;

import java.util.Objects;

/**
 * Represents a row of data extracted from documents
 */
public class DataRow {
    private String fileName;
    private String sheetName;
    private int rowNumber;
    private String candidateId;
    private String name;
    private String email;
    private String contact;
    private String content;
    private int fieldCount;

    public DataRow() {
        this.fieldCount = 0;
    }

    public DataRow(String fileName, String content) {
        this.fileName = fileName;
        this.content = content;
        this.fieldCount = 0;
    }

    // Getters and Setters
    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public String getSheetName() {
        return sheetName;
    }

    public void setSheetName(String sheetName) {
        this.sheetName = sheetName;
    }

    public int getRowNumber() {
        return rowNumber;
    }

    public void setRowNumber(int rowNumber) {
        this.rowNumber = rowNumber;
    }

    public String getCandidateId() {
        return candidateId;
    }

    public void setCandidateId(String candidateId) {
        this.candidateId = candidateId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getContact() {
        return contact;
    }

    public void setContact(String contact) {
        this.contact = contact;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public int getFieldCount() {
        return fieldCount;
    }

    public void setFieldCount(int fieldCount) {
        this.fieldCount = fieldCount;
    }

    /**
     * Check if this row is a duplicate of another based on unique identifiers
     */
    public boolean isDuplicateOf(DataRow other) {
        if (other == null) return false;
        
        // Check if either candidateId, email, or contact matches
        boolean candidateIdMatch = candidateId != null && other.candidateId != null 
                                   && candidateId.equals(other.candidateId);
        boolean emailMatch = email != null && other.email != null 
                            && email.equalsIgnoreCase(other.email);
        boolean contactMatch = contact != null && other.contact != null 
                              && contact.equals(other.contact);
        
        return candidateIdMatch || emailMatch || contactMatch;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        DataRow dataRow = (DataRow) o;
        return Objects.equals(candidateId, dataRow.candidateId) ||
               Objects.equals(email, dataRow.email) ||
               Objects.equals(contact, dataRow.contact);
    }

    @Override
    public int hashCode() {
        return Objects.hash(candidateId, email, contact);
    }

    @Override
    public String toString() {
        return "DataRow{" +
                "fileName='" + fileName + '\'' +
                ", sheetName='" + sheetName + '\'' +
                ", rowNumber=" + rowNumber +
                ", candidateId='" + candidateId + '\'' +
                ", name='" + name + '\'' +
                ", email='" + email + '\'' +
                ", contact='" + contact + '\'' +
                ", fieldCount=" + fieldCount +
                '}';
    }
}
