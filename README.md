# ATS Management System

A comprehensive Java-based application for processing and managing resumes, CVs, and candidate data from various file formats.

## Features

1. **Multi-Format Support**: Process spreadsheets (Excel), PDFs, and Word documents
2. **Flexible File Upload**: Upload folders or ZIP files containing multiple documents
3. **File Type Selection**: Choose which file types to process (Spreadsheet, PDF, Word)
4. **Keyword Search**: Search for specific keywords across all documents
5. **Smart Data Extraction**: 
   - Extracts entire rows from spreadsheets with field information
   - Partitions data by field density (maximum to minimum filled rows)
   - Automatically identifies candidate ID, email, and contact information
6. **Duplicate Detection**: Removes duplicate entries based on candidate ID, email, or contact
7. **Interactive Data Management**: 
   - View extracted data in a user-friendly table
   - Delete individual rows with a delete button
   - Rows displayed with spacing for better readability
8. **Multi-Format Export**: Export results to Excel, PDF, or Word format

## Technology Stack

- **Java 11+**: Core programming language
- **JavaFX 17**: GUI framework
- **Apache POI 5.2.3**: Excel and Word document processing
- **Apache PDFBox 2.0.29**: PDF document processing
- **Maven**: Build and dependency management

## Project Structure

```
src/main/java/com/ats/
├── ATSApplication.java          # Main JavaFX application
├── controller/
│   └── ATSController.java       # GUI controller
├── model/
│   └── DataRow.java            # Data model for extracted rows
└── service/
    ├── FileProcessingService.java    # Main file processing coordinator
    ├── SpreadsheetProcessor.java     # Excel file processor
    ├── PDFProcessor.java             # PDF file processor
    ├── WordProcessor.java            # Word document processor
    └── ExportService.java            # Export functionality
```

## Requirements

- Java Development Kit (JDK) 11 or higher
- Maven 3.6 or higher
- IntelliJ IDEA Community Edition (recommended) or any Java IDE

## Setup and Installation

### 1. Clone the Repository

```bash
git clone https://github.com/KarthikMohan7639/ATSMangementSystem.git
cd ATSMangementSystem
```

### 2. Build the Project

```bash
mvn clean install
```

### 3. Run the Application

Using Maven:
```bash
mvn javafx:run
```

Or run directly from IDE:
- Open the project in IntelliJ IDEA
- Right-click on `ATSApplication.java`
- Select "Run 'ATSApplication.main()'"

## Usage Guide

### 1. Upload Files
- Click **"Upload Folder"** to select a folder containing documents
- Click **"Upload ZIP File"** to select ZIP files
- You can upload multiple folders and ZIP files

### 2. Select File Types
- Check the file types you want to process:
  - **Spreadsheet**: Excel files (.xlsx, .xls)
  - **PDF**: PDF documents (.pdf)
  - **Word**: Word documents (.docx, .doc)

### 3. Enter Keywords
- Enter comma-separated keywords to search for (e.g., "java, developer, senior")
- Leave blank to retrieve all data

### 4. Process Files
- Click **"Process Files"** button
- The application will:
  - Scan all uploaded files
  - Extract rows containing keywords
  - Remove duplicates based on candidate ID, email, or contact
  - Display results in the table

### 5. Review and Edit Data
- Review extracted data in the table
- Click **"Delete"** button on any row to remove it
- Each row shows:
  - File name
  - Sheet/Section name
  - Row number
  - Candidate ID
  - Name
  - Email
  - Contact
  - Full content

### 6. Export Results
- Click **"Export to Excel"** to save as .xlsx file
- Click **"Export to PDF"** to save as .pdf file
- Click **"Export to Word"** to save as .docx file

## How It Works

### Spreadsheet Processing
- Reads Excel files (.xlsx, .xls)
- Identifies headers from the first row
- Extracts entire rows that contain keywords
- Automatically detects candidate ID, name, email, and contact fields
- Sorts rows by field density (most filled fields first)

### PDF Processing
- Extracts text from PDF documents
- Splits content into paragraphs
- Searches for keywords in paragraphs
- Automatically extracts email addresses and phone numbers using regex

### Word Processing
- Reads Word documents (.docx)
- Processes both paragraphs and tables
- Extracts content containing keywords
- Identifies email and phone numbers

### Duplicate Detection
- Compares rows based on:
  - Candidate ID
  - Email address
  - Contact number
- Removes duplicates if any of these fields match

## Configuration

### Maven Dependencies
The project uses the following key dependencies:
- JavaFX Controls and FXML
- Apache POI (for Excel and Word)
- Apache PDFBox (for PDF)
- SLF4J (for logging)

### Building JAR File
To create a standalone JAR:
```bash
mvn clean package
```

The JAR will be created in the `target/` directory.

## Troubleshooting

### JavaFX Module Issues
If you encounter JavaFX module errors:
1. Ensure you have Java 11 or higher
2. Verify JavaFX dependencies in pom.xml
3. Run using `mvn javafx:run` instead of direct Java execution

### Memory Issues with Large Files
For processing large files or many files:
```bash
export MAVEN_OPTS="-Xmx2g"
mvn javafx:run
```

### File Format Issues
- Ensure files are not corrupted
- Verify file extensions match actual file types
- Check that ZIP files are not password-protected

## Future Enhancements

- Apache Spark integration for distributed processing
- Advanced text analytics and matching algorithms
- Database storage for processed data
- Batch processing with progress tracking
- Custom field mapping configuration
- Resume parsing with skills extraction
- Integration with HR systems and APIs

## Contributing

Contributions are welcome! Please feel free to submit a Pull Request.

## License

This project is open source and available under the MIT License.

## Support

For issues, questions, or contributions, please create an issue in the GitHub repository.

## Author

Developed for HR consultancy to streamline resume and CV shortlisting processes.
