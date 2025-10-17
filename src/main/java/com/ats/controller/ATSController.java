package com.ats.controller;

import com.ats.model.DataRow;
import com.ats.service.ExportService;
import com.ats.service.FileProcessingService;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.*;
import javafx.stage.DirectoryChooser;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import java.io.File;
import java.util.*;

/**
 * Main controller for ATS Management System GUI
 */
public class ATSController {
    
    private Stage primaryStage;
    private FileProcessingService fileProcessingService;
    private ExportService exportService;
    
    private List<File> selectedFiles;
    private ObservableList<DataRow> dataRows;
    
    // UI Components
    private TextField keywordField;
    private CheckBox spreadsheetCheckBox;
    private CheckBox pdfCheckBox;
    private CheckBox wordCheckBox;
    private TableView<DataRow> dataTable;
    private Label statusLabel;
    
    public ATSController(Stage primaryStage) {
        this.primaryStage = primaryStage;
        this.fileProcessingService = new FileProcessingService();
        this.exportService = new ExportService();
        this.selectedFiles = new ArrayList<>();
        this.dataRows = FXCollections.observableArrayList();
    }
    
    /**
     * Create and return the main UI scene
     */
    public VBox createUI() {
        VBox root = new VBox(15);
        root.setPadding(new Insets(20));
        root.setStyle("-fx-background-color: #f5f5f5;");
        
        // Title
        Label titleLabel = new Label("ATS Management System");
        titleLabel.setStyle("-fx-font-size: 24px; -fx-font-weight: bold; -fx-text-fill: #2c3e50;");
        
        // File Upload Section
        VBox uploadSection = createUploadSection();
        
        // File Type Selection Section
        HBox fileTypeSection = createFileTypeSection();
        
        // Keyword Search Section
        HBox keywordSection = createKeywordSection();
        
        // Process Button
        Button processButton = new Button("Process Files");
        processButton.setStyle("-fx-background-color: #3498db; -fx-text-fill: white; -fx-font-size: 14px; -fx-padding: 10 20;");
        processButton.setOnAction(e -> processFiles());
        
        // Data Table
        dataTable = createDataTable();
        
        // Export Section
        HBox exportSection = createExportSection();
        
        // Status Label
        statusLabel = new Label("Ready");
        statusLabel.setStyle("-fx-font-size: 12px; -fx-text-fill: #7f8c8d;");
        
        root.getChildren().addAll(
            titleLabel,
            new Separator(),
            uploadSection,
            fileTypeSection,
            keywordSection,
            processButton,
            new Separator(),
            new Label("Search Results:"),
            dataTable,
            exportSection,
            statusLabel
        );
        
        return root;
    }
    
    /**
     * Create file upload section
     */
    private VBox createUploadSection() {
        VBox section = new VBox(10);
        
        Label label = new Label("1. Upload Files/Folders:");
        label.setStyle("-fx-font-weight: bold; -fx-font-size: 14px;");
        
        HBox buttonBox = new HBox(10);
        
        Button uploadFolderButton = new Button("Upload Folder");
        uploadFolderButton.setStyle("-fx-background-color: #27ae60; -fx-text-fill: white;");
        uploadFolderButton.setOnAction(e -> uploadFolder());
        
        Button uploadZipButton = new Button("Upload ZIP File");
        uploadZipButton.setStyle("-fx-background-color: #16a085; -fx-text-fill: white;");
        uploadZipButton.setOnAction(e -> uploadZipFile());
        
        Button clearButton = new Button("Clear All");
        clearButton.setStyle("-fx-background-color: #e74c3c; -fx-text-fill: white;");
        clearButton.setOnAction(e -> clearFiles());
        
        Label fileCountLabel = new Label("No files selected");
        fileCountLabel.setId("fileCountLabel");
        
        buttonBox.getChildren().addAll(uploadFolderButton, uploadZipButton, clearButton, fileCountLabel);
        section.getChildren().addAll(label, buttonBox);
        
        return section;
    }
    
    /**
     * Create file type selection section
     */
    private HBox createFileTypeSection() {
        HBox section = new HBox(15);
        
        Label label = new Label("2. Select File Types:");
        label.setStyle("-fx-font-weight: bold; -fx-font-size: 14px;");
        
        spreadsheetCheckBox = new CheckBox("Spreadsheet");
        spreadsheetCheckBox.setSelected(true);
        
        pdfCheckBox = new CheckBox("PDF");
        pdfCheckBox.setSelected(true);
        
        wordCheckBox = new CheckBox("Word");
        wordCheckBox.setSelected(true);
        
        section.getChildren().addAll(label, spreadsheetCheckBox, pdfCheckBox, wordCheckBox);
        return section;
    }
    
    /**
     * Create keyword search section
     */
    private HBox createKeywordSection() {
        HBox section = new HBox(10);
        
        Label label = new Label("3. Enter Keywords (comma-separated):");
        label.setStyle("-fx-font-weight: bold; -fx-font-size: 14px;");
        
        keywordField = new TextField();
        keywordField.setPromptText("e.g., java, developer, senior");
        keywordField.setPrefWidth(400);
        
        section.getChildren().addAll(label, keywordField);
        return section;
    }
    
    /**
     * Create data table
     */
    private TableView<DataRow> createDataTable() {
        TableView<DataRow> table = new TableView<>();
        table.setItems(dataRows);
        table.setPrefHeight(400);
        
        TableColumn<DataRow, String> fileCol = new TableColumn<>("File Name");
        fileCol.setCellValueFactory(new PropertyValueFactory<>("fileName"));
        fileCol.setPrefWidth(150);
        
        TableColumn<DataRow, String> sheetCol = new TableColumn<>("Sheet/Section");
        sheetCol.setCellValueFactory(new PropertyValueFactory<>("sheetName"));
        sheetCol.setPrefWidth(100);
        
        TableColumn<DataRow, Integer> rowCol = new TableColumn<>("Row #");
        rowCol.setCellValueFactory(new PropertyValueFactory<>("rowNumber"));
        rowCol.setPrefWidth(60);
        
        TableColumn<DataRow, String> candidateCol = new TableColumn<>("Candidate ID");
        candidateCol.setCellValueFactory(new PropertyValueFactory<>("candidateId"));
        candidateCol.setPrefWidth(100);
        
        TableColumn<DataRow, String> nameCol = new TableColumn<>("Name");
        nameCol.setCellValueFactory(new PropertyValueFactory<>("name"));
        nameCol.setPrefWidth(120);
        
        TableColumn<DataRow, String> emailCol = new TableColumn<>("Email");
        emailCol.setCellValueFactory(new PropertyValueFactory<>("email"));
        emailCol.setPrefWidth(150);
        
        TableColumn<DataRow, String> contactCol = new TableColumn<>("Contact");
        contactCol.setCellValueFactory(new PropertyValueFactory<>("contact"));
        contactCol.setPrefWidth(120);
        
        TableColumn<DataRow, String> contentCol = new TableColumn<>("Content");
        contentCol.setCellValueFactory(new PropertyValueFactory<>("content"));
        contentCol.setPrefWidth(250);
        
        TableColumn<DataRow, Void> actionCol = new TableColumn<>("Actions");
        actionCol.setPrefWidth(80);
        actionCol.setCellFactory(param -> new TableCell<>() {
            private final Button deleteButton = new Button("Delete");
            
            {
                deleteButton.setStyle("-fx-background-color: #e74c3c; -fx-text-fill: white; -fx-font-size: 10px;");
                deleteButton.setOnAction(event -> {
                    DataRow row = getTableView().getItems().get(getIndex());
                    dataRows.remove(row);
                });
            }
            
            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setGraphic(null);
                } else {
                    setGraphic(deleteButton);
                }
            }
        });
        
        table.getColumns().addAll(fileCol, sheetCol, rowCol, candidateCol, nameCol, 
                                  emailCol, contactCol, contentCol, actionCol);
        
        return table;
    }
    
    /**
     * Create export section
     */
    private HBox createExportSection() {
        HBox section = new HBox(10);
        section.setAlignment(Pos.CENTER_LEFT);
        
        Label label = new Label("Export Data:");
        label.setStyle("-fx-font-weight: bold; -fx-font-size: 14px;");
        
        Button exportExcelButton = new Button("Export to Excel");
        exportExcelButton.setStyle("-fx-background-color: #27ae60; -fx-text-fill: white;");
        exportExcelButton.setOnAction(e -> exportData("spreadsheet"));
        
        Button exportPdfButton = new Button("Export to PDF");
        exportPdfButton.setStyle("-fx-background-color: #c0392b; -fx-text-fill: white;");
        exportPdfButton.setOnAction(e -> exportData("pdf"));
        
        Button exportWordButton = new Button("Export to Word");
        exportWordButton.setStyle("-fx-background-color: #2980b9; -fx-text-fill: white;");
        exportWordButton.setOnAction(e -> exportData("word"));
        
        section.getChildren().addAll(label, exportExcelButton, exportPdfButton, exportWordButton);
        return section;
    }
    
    /**
     * Upload folder
     */
    private void uploadFolder() {
        DirectoryChooser chooser = new DirectoryChooser();
        chooser.setTitle("Select Folder");
        File folder = chooser.showDialog(primaryStage);
        
        if (folder != null) {
            selectedFiles.add(folder);
            updateFileCount();
        }
    }
    
    /**
     * Upload ZIP file
     */
    private void uploadZipFile() {
        FileChooser chooser = new FileChooser();
        chooser.setTitle("Select ZIP File");
        chooser.getExtensionFilters().add(
            new FileChooser.ExtensionFilter("ZIP Files", "*.zip")
        );
        
        List<File> files = chooser.showOpenMultipleDialog(primaryStage);
        if (files != null) {
            selectedFiles.addAll(files);
            updateFileCount();
        }
    }
    
    /**
     * Clear all selected files
     */
    private void clearFiles() {
        selectedFiles.clear();
        updateFileCount();
    }
    
    /**
     * Update file count label
     */
    private void updateFileCount() {
        Label fileCountLabel = (Label) primaryStage.getScene().lookup("#fileCountLabel");
        if (fileCountLabel != null) {
            fileCountLabel.setText(selectedFiles.size() + " file(s)/folder(s) selected");
        }
    }
    
    /**
     * Process files
     */
    private void processFiles() {
        if (selectedFiles.isEmpty()) {
            showAlert("No Files Selected", "Please upload files or folders first.");
            return;
        }
        
        // Get selected file types
        Set<String> fileTypes = new HashSet<>();
        if (spreadsheetCheckBox.isSelected()) fileTypes.add("Spreadsheet");
        if (pdfCheckBox.isSelected()) fileTypes.add("PDF");
        if (wordCheckBox.isSelected()) fileTypes.add("Word");
        
        if (fileTypes.isEmpty()) {
            showAlert("No File Types Selected", "Please select at least one file type.");
            return;
        }
        
        // Get keywords
        Set<String> keywords = new HashSet<>();
        String keywordText = keywordField.getText();
        if (keywordText != null && !keywordText.trim().isEmpty()) {
            String[] parts = keywordText.split(",");
            for (String part : parts) {
                keywords.add(part.trim());
            }
        }
        
        // Process in background thread
        statusLabel.setText("Processing files...");
        
        new Thread(() -> {
            try {
                List<DataRow> results = fileProcessingService.processFiles(
                    selectedFiles, fileTypes, keywords
                );
                
                Platform.runLater(() -> {
                    dataRows.clear();
                    dataRows.addAll(results);
                    statusLabel.setText("Processing complete. Found " + results.size() + " matching rows.");
                });
            } catch (Exception ex) {
                Platform.runLater(() -> {
                    showAlert("Error", "Error processing files: " + ex.getMessage());
                    statusLabel.setText("Error during processing.");
                });
            }
        }).start();
    }
    
    /**
     * Export data to specified format
     */
    private void exportData(String format) {
        if (dataRows.isEmpty()) {
            showAlert("No Data", "No data to export. Please process files first.");
            return;
        }
        
        FileChooser chooser = new FileChooser();
        chooser.setTitle("Save Export File");
        
        String extension;
        switch (format) {
            case "spreadsheet":
                extension = "*.xlsx";
                chooser.getExtensionFilters().add(
                    new FileChooser.ExtensionFilter("Excel Files", extension)
                );
                break;
            case "pdf":
                extension = "*.pdf";
                chooser.getExtensionFilters().add(
                    new FileChooser.ExtensionFilter("PDF Files", extension)
                );
                break;
            case "word":
                extension = "*.docx";
                chooser.getExtensionFilters().add(
                    new FileChooser.ExtensionFilter("Word Files", extension)
                );
                break;
            default:
                return;
        }
        
        File file = chooser.showSaveDialog(primaryStage);
        if (file != null) {
            try {
                exportService.exportData(new ArrayList<>(dataRows), file, format);
                showAlert("Export Successful", "Data exported to " + file.getName());
                statusLabel.setText("Export completed successfully.");
            } catch (Exception ex) {
                showAlert("Export Error", "Error exporting data: " + ex.getMessage());
                statusLabel.setText("Export failed.");
            }
        }
    }
    
    /**
     * Show alert dialog
     */
    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
