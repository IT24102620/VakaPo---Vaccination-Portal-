package lk.vakapo.vakapo.PDFManagement;

import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.element.Paragraph;
import com.itextpdf.layout.element.Table;
import com.itextpdf.layout.element.Cell;
import com.itextpdf.layout.properties.TextAlignment;
import com.itextpdf.kernel.colors.ColorConstants;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

@Service
@RequiredArgsConstructor
@Slf4j
public class PDFGenerationService {

    /**
     * Generate a patient registration card PDF
     */
    public byte[] generatePatientRegistrationCard(String patientId, String patientName, String email, 
                                                String contact, String dateOfBirth, String gender, 
                                                String address) {
        try {
            log.info("Generating patient registration card PDF for patient: {}", patientId);
            
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            PdfWriter writer = new PdfWriter(outputStream);
            PdfDocument pdf = new PdfDocument(writer);
            Document document = new Document(pdf);

            // Add title
            Paragraph title = new Paragraph("VakaPo - Patient Registration Card")
                    .setTextAlignment(TextAlignment.CENTER)
                    .setFontSize(24)
                    .setBold()
                    .setMarginBottom(20);
            document.add(title);

            // Add VakaPo logo section
            Paragraph logoSection = new Paragraph()
                    .add("🏥 ")
                    .add("VakaPo")
                    .setTextAlignment(TextAlignment.CENTER)
                    .setFontSize(28)
                    .setBold()
                    .setMarginBottom(10);
            document.add(logoSection);
            
            // Add tagline
            Paragraph tagline = new Paragraph("Vaccination Portal")
                    .setTextAlignment(TextAlignment.CENTER)
                    .setFontSize(14)
                    .setMarginBottom(30);
            document.add(tagline);

            // Create patient information table
            Table table = new Table(2).useAllAvailableWidth();
            
            // Add patient details
            addTableRow(table, "Patient ID", patientId, true);
            addTableRow(table, "Full Name", patientName, false);
            addTableRow(table, "Email", email, false);
            addTableRow(table, "Contact Number", contact, false);
            addTableRow(table, "Date of Birth", dateOfBirth, false);
            addTableRow(table, "Gender", gender, false);
            addTableRow(table, "Address", address, false);
            addTableRow(table, "Registration Date", LocalDate.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy")), false);
            addTableRow(table, "Status", "Active", false);

            document.add(table);

            // Add footer information
            Paragraph footer = new Paragraph()
                    .add("This is your official VakaPo Patient Registration Card. ")
                    .add("Please keep this card safe and bring it with you for all vaccination appointments. ")
                    .add("For any queries, contact us at support@vakapo.com or call +94 11 234 5678.")
                    .setTextAlignment(TextAlignment.CENTER)
                    .setFontSize(10)
                    .setMarginTop(30);
            document.add(footer);

            // Add terms and conditions
            Paragraph terms = new Paragraph()
                    .add("Terms & Conditions: This card is valid for all VakaPo services. ")
                    .add("Please inform us of any changes to your personal information. ")
                    .add("Keep your login credentials secure and do not share them with others.")
                    .setTextAlignment(TextAlignment.CENTER)
                    .setFontSize(8)
                    .setMarginTop(10);
            document.add(terms);

            document.close();
            
            byte[] pdfBytes = outputStream.toByteArray();
            log.info("Patient registration card PDF generated successfully for patient: {}", patientId);
            return pdfBytes;
            
        } catch (Exception e) {
            log.error("Error generating patient registration card PDF for patient: {}. Error: {}", 
                     patientId, e.getMessage(), e);
            throw new RuntimeException("Failed to generate patient registration card PDF", e);
        }
    }

    /**
     * Add a row to the table with proper styling
     */
    private void addTableRow(Table table, String label, String value, boolean highlight) {
        Cell labelCell = new Cell()
                .add(new Paragraph(label).setBold())
                .setPadding(8)
                .setBackgroundColor(highlight ? ColorConstants.LIGHT_GRAY : ColorConstants.WHITE);
        
        Cell valueCell = new Cell()
                .add(new Paragraph(value != null ? value : "N/A"))
                .setPadding(8)
                .setBackgroundColor(highlight ? ColorConstants.LIGHT_GRAY : ColorConstants.WHITE);
        
        table.addCell(labelCell);
        table.addCell(valueCell);
    }

    /**
     * Add a styled row to the table with better formatting
     */
    private void addStyledTableRow(Table table, String label, String value, boolean highlight) {
        Cell labelCell = new Cell()
                .add(new Paragraph(label).setBold().setFontSize(12))
                .setPadding(10)
                .setBackgroundColor(highlight ? ColorConstants.CYAN : ColorConstants.LIGHT_GRAY);
        
        Cell valueCell = new Cell()
                .add(new Paragraph(value != null ? value : "N/A").setFontSize(11))
                .setPadding(10)
                .setBackgroundColor(highlight ? ColorConstants.CYAN : ColorConstants.WHITE);
        
        table.addCell(labelCell);
        table.addCell(valueCell);
    }

    /**
     * Generate a styled PDF registration card (simplified method without HTML)
     */
    public byte[] generatePatientRegistrationCardHTML(String patientId, String patientName, String email, 
                                                    String contact, String dateOfBirth, String gender, 
                                                    String address) {
        try {
            log.info("Generating patient registration card PDF for patient: {}", patientId);
            
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            PdfWriter writer = new PdfWriter(outputStream);
            PdfDocument pdf = new PdfDocument(writer);
            Document document = new Document(pdf);

            // Add title with styling
            Paragraph title = new Paragraph("VakaPo - Patient Registration Card")
                    .setTextAlignment(TextAlignment.CENTER)
                    .setFontSize(24)
                    .setBold()
                    .setMarginBottom(20);
            document.add(title);

            // Add VakaPo logo section
            Paragraph logoSection = new Paragraph()
                    .add("🏥 ")
                    .add("VakaPo")
                    .setTextAlignment(TextAlignment.CENTER)
                    .setFontSize(28)
                    .setBold()
                    .setMarginBottom(10);
            document.add(logoSection);
            
            // Add tagline
            Paragraph tagline = new Paragraph("Vaccination Portal")
                    .setTextAlignment(TextAlignment.CENTER)
                    .setFontSize(14)
                    .setMarginBottom(30);
            document.add(tagline);

            // Create patient information table with better styling
            Table table = new Table(2).useAllAvailableWidth();
            
            // Add patient details with improved styling
            addStyledTableRow(table, "Patient ID", patientId, true);
            addStyledTableRow(table, "Full Name", patientName, false);
            addStyledTableRow(table, "Email", email, false);
            addStyledTableRow(table, "Contact Number", contact, false);
            addStyledTableRow(table, "Date of Birth", dateOfBirth, false);
            addStyledTableRow(table, "Gender", gender, false);
            addStyledTableRow(table, "Address", address, false);
            addStyledTableRow(table, "Registration Date", LocalDate.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy")), false);
            addStyledTableRow(table, "Status", "Active", false);

            document.add(table);

            // Add footer information
            Paragraph footer = new Paragraph()
                    .add("This is your official VakaPo Patient Registration Card. ")
                    .add("Please keep this card safe and bring it with you for all vaccination appointments. ")
                    .add("For any queries, contact us at support@vakapo.com or call +94 11 234 5678.")
                    .setTextAlignment(TextAlignment.CENTER)
                    .setFontSize(10)
                    .setMarginTop(30);
            document.add(footer);

            // Add terms and conditions
            Paragraph terms = new Paragraph()
                    .add("Terms & Conditions: This card is valid for all VakaPo services. ")
                    .add("Please inform us of any changes to your personal information. ")
                    .add("Keep your login credentials secure and do not share them with others.")
                    .setTextAlignment(TextAlignment.CENTER)
                    .setFontSize(8)
                    .setMarginTop(10);
            document.add(terms);

            document.close();
            
            byte[] pdfBytes = outputStream.toByteArray();
            log.info("Patient registration card PDF generated successfully for patient: {}", patientId);
            return pdfBytes;
            
        } catch (Exception e) {
            log.error("Error generating patient registration card PDF for patient: {}. Error: {}", 
                     patientId, e.getMessage(), e);
            throw new RuntimeException("Failed to generate patient registration card PDF", e);
        }
    }

    /**
     * Generate an official vaccination record card PDF based on the provided template
     */
    public byte[] generateVaccinationRecordCard(String patientId, String patientName, String dateOfBirth, 
                                              String healthId, String vaccineName, String dateReceived, 
                                              String location, String batchLotNo, String doctorName, 
                                              String nurseName) {
        try {
            log.info("Generating vaccination record card PDF for patient: {}", patientId);
            
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            PdfWriter writer = new PdfWriter(outputStream);
            PdfDocument pdf = new PdfDocument(writer);
            Document document = new Document(pdf);

            // Fonts will use default system fonts

            // Add official header with shield logo
            Paragraph header = new Paragraph("OFFICIAL VACCINATION RECORD")
                    .setTextAlignment(TextAlignment.CENTER)
                    .setFontSize(20)
                    .setBold()
                    .setMarginBottom(5);
            document.add(header);

            Paragraph issuer = new Paragraph("Issued by Ministry of Health, Sri Lanka")
                    .setTextAlignment(TextAlignment.CENTER)
                    .setFontSize(12)
                    .setMarginBottom(30);
            document.add(issuer);

            // Patient Information Section
            Paragraph patientInfoTitle = new Paragraph("PATIENT INFORMATION")
                    .setFontSize(14)
                    .setBold()
                    .setMarginBottom(15);
            document.add(patientInfoTitle);

            // Create patient info table
            Table patientTable = new Table(2).useAllAvailableWidth();
            addVaccinationTableRow(patientTable, "PATIENT NAME:", patientName, true);
            addVaccinationTableRow(patientTable, "DATE OF BIRTH:", dateOfBirth, false);
            addVaccinationTableRow(patientTable, "HEALTH ID:", healthId, false);

            document.add(patientTable);

            // Add spacing
            document.add(new Paragraph().setMarginBottom(20));

            // Vaccination Record Section
            Paragraph vaccinationTitle = new Paragraph("VACCINATION RECORD")
                    .setFontSize(14)
                    .setBold()
                    .setMarginBottom(15);
            document.add(vaccinationTitle);

            // Create vaccination record table
            Table vaccinationTable = new Table(4).useAllAvailableWidth();
            
            // Table headers
            Cell header1 = new Cell().add(new Paragraph("VACCINE/PRODUCT").setBold())
                    .setPadding(8)
                    .setBackgroundColor(ColorConstants.LIGHT_GRAY);
            Cell header2 = new Cell().add(new Paragraph("DATE RECEIVED").setBold())
                    .setPadding(8)
                    .setBackgroundColor(ColorConstants.LIGHT_GRAY);
            Cell header3 = new Cell().add(new Paragraph("LOCATION").setBold())
                    .setPadding(8)
                    .setBackgroundColor(ColorConstants.LIGHT_GRAY);
            Cell header4 = new Cell().add(new Paragraph("BATCH/LOT NO.").setBold())
                    .setPadding(8)
                    .setBackgroundColor(ColorConstants.LIGHT_GRAY);

            vaccinationTable.addCell(header1);
            vaccinationTable.addCell(header2);
            vaccinationTable.addCell(header3);
            vaccinationTable.addCell(header4);

            // Add vaccination data
            Cell vaccineCell = new Cell().add(new Paragraph(vaccineName))
                    .setPadding(8);
            Cell dateCell = new Cell().add(new Paragraph(dateReceived))
                    .setPadding(8);
            Cell locationCell = new Cell().add(new Paragraph(location))
                    .setPadding(8);
            Cell batchCell = new Cell().add(new Paragraph(batchLotNo))
                    .setPadding(8);

            vaccinationTable.addCell(vaccineCell);
            vaccinationTable.addCell(dateCell);
            vaccinationTable.addCell(locationCell);
            vaccinationTable.addCell(batchCell);

            document.add(vaccinationTable);

            // Add spacing
            document.add(new Paragraph().setMarginBottom(30));

            // Authorization Section
            Table authTable = new Table(2).useAllAvailableWidth();
            
            // Left side - QR Code placeholder and signature
            Cell leftCell = new Cell();
            leftCell.add(new Paragraph("AUTHORIZED HEALTHCARE PROVIDER SIGNATURE")
                    .setFontSize(10)
                    .setBold());
            leftCell.add(new Paragraph("Nurse: " + nurseName)
                    .setFontSize(10)
                    .setMarginTop(20));
            leftCell.add(new Paragraph("Doctor: " + doctorName)
                    .setFontSize(10)
                    .setMarginTop(5));
            leftCell.add(new Paragraph("Date: " + LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd")))
                    .setFontSize(10)
                    .setMarginTop(5));
            leftCell.setPadding(10);

            // Right side - Verification stamp
            Cell rightCell = new Cell();
            rightCell.add(new Paragraph("VERIFIED")
                    .setTextAlignment(TextAlignment.CENTER)
                    .setFontSize(12)
                    .setBold()
                    .setMarginTop(20));
            rightCell.add(new Paragraph("✓")
                    .setTextAlignment(TextAlignment.CENTER)
                    .setFontSize(24)
                    .setMarginTop(10));
            rightCell.add(new Paragraph("Ministry of Health")
                    .setTextAlignment(TextAlignment.CENTER)
                    .setFontSize(8)
                    .setMarginTop(10));
            rightCell.setPadding(10);

            authTable.addCell(leftCell);
            authTable.addCell(rightCell);

            document.add(authTable);

            // Add footer
            Paragraph footer = new Paragraph()
                    .add("This is an official vaccination record issued by the Ministry of Health, Sri Lanka. ")
                    .add("Please keep this record safe and present it when required for travel or medical purposes. ")
                    .add("For verification, contact: health@vakapo.com")
                    .setTextAlignment(TextAlignment.CENTER)
                    .setFontSize(8)
                    .setMarginTop(30);
            document.add(footer);

            document.close();
            
            byte[] pdfBytes = outputStream.toByteArray();
            log.info("Vaccination record card PDF generated successfully for patient: {}", patientId);
            return pdfBytes;
            
        } catch (Exception e) {
            log.error("Error generating vaccination record card PDF for patient: {}. Error: {}", 
                     patientId, e.getMessage(), e);
            throw new RuntimeException("Failed to generate vaccination record card PDF", e);
        }
    }

    /**
     * Add a row to the vaccination table with proper styling
     */
    private void addVaccinationTableRow(Table table, String label, String value, boolean highlight) {
        Cell labelCell = new Cell()
                .add(new Paragraph(label).setBold())
                .setPadding(8)
                .setBackgroundColor(highlight ? ColorConstants.LIGHT_GRAY : ColorConstants.WHITE);
        
        Cell valueCell = new Cell()
                .add(new Paragraph(value != null ? value : "N/A"))
                .setPadding(8)
                .setBackgroundColor(highlight ? ColorConstants.LIGHT_GRAY : ColorConstants.WHITE);
        
        table.addCell(labelCell);
        table.addCell(valueCell);
    }

    /**
     * Build HTML content for the registration card
     */
    private String buildRegistrationCardHTML(String patientId, String patientName, String email, 
                                           String contact, String dateOfBirth, String gender, 
                                           String address, String emergencyContact) {
        String currentDate = LocalDate.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy"));
        
        return String.format("""
            <!DOCTYPE html>
            <html>
            <head>
                <meta charset="UTF-8">
                <style>
                    body { 
                        font-family: Arial, sans-serif; 
                        margin: 0; 
                        padding: 20px; 
                        background-color: #f5f5f5;
                    }
                    .card { 
                        max-width: 600px; 
                        margin: 0 auto; 
                        background: white; 
                        border-radius: 15px; 
                        box-shadow: 0 4px 20px rgba(0,0,0,0.1);
                        overflow: hidden;
                    }
                    .header { 
                        background: linear-gradient(135deg, #2c5aa0, #1e3d72); 
                        color: white; 
                        padding: 30px; 
                        text-align: center; 
                    }
                    .logo { 
                        font-size: 28px; 
                        font-weight: bold; 
                        margin-bottom: 10px;
                    }
                    .title { 
                        font-size: 20px; 
                        margin: 0;
                    }
                    .content { 
                        padding: 30px; 
                    }
                    .patient-id { 
                        background: #e8f4fd; 
                        border: 2px solid #2c5aa0; 
                        border-radius: 10px; 
                        padding: 20px; 
                        text-align: center; 
                        margin-bottom: 25px;
                    }
                    .patient-id-label { 
                        font-size: 14px; 
                        color: #666; 
                        margin-bottom: 5px;
                    }
                    .patient-id-value { 
                        font-size: 24px; 
                        font-weight: bold; 
                        color: #2c5aa0;
                    }
                    .info-table { 
                        width: 100%%; 
                        border-collapse: collapse; 
                        margin-bottom: 25px;
                    }
                    .info-table th, .info-table td { 
                        padding: 12px; 
                        text-align: left; 
                        border-bottom: 1px solid #eee;
                    }
                    .info-table th { 
                        background-color: #f8f9fa; 
                        font-weight: bold; 
                        color: #333;
                        width: 30%%;
                    }
                    .info-table td { 
                        color: #555;
                    }
                    .footer { 
                        background: #f8f9fa; 
                        padding: 20px; 
                        text-align: center; 
                        border-top: 1px solid #eee;
                    }
                    .footer-text { 
                        font-size: 12px; 
                        color: #666; 
                        line-height: 1.5;
                    }
                    .terms { 
                        font-size: 10px; 
                        color: #888; 
                        margin-top: 15px;
                        line-height: 1.4;
                    }
                </style>
            </head>
            <body>
                <div class="card">
                    <div class="header">
                        <div class="logo">🏥 VakaPo</div>
                        <div class="title">Patient Registration Card</div>
                    </div>
                    
                    <div class="content">
                        <div class="patient-id">
                            <div class="patient-id-label">Your Patient ID</div>
                            <div class="patient-id-value">%s</div>
                        </div>
                        
                        <table class="info-table">
                            <tr>
                                <th>Full Name</th>
                                <td>%s</td>
                            </tr>
                            <tr>
                                <th>Email Address</th>
                                <td>%s</td>
                            </tr>
                            <tr>
                                <th>Contact Number</th>
                                <td>%s</td>
                            </tr>
                            <tr>
                                <th>Date of Birth</th>
                                <td>%s</td>
                            </tr>
                            <tr>
                                <th>Gender</th>
                                <td>%s</td>
                            </tr>
                            <tr>
                                <th>Address</th>
                                <td>%s</td>
                            </tr>
                            <tr>
                                <th>Emergency Contact</th>
                                <td>%s</td>
                            </tr>
                            <tr>
                                <th>Registration Date</th>
                                <td>%s</td>
                            </tr>
                            <tr>
                                <th>Status</th>
                                <td><strong style="color: #28a745;">Active</strong></td>
                            </tr>
                        </table>
                    </div>
                    
                    <div class="footer">
                        <div class="footer-text">
                            <strong>This is your official VakaPo Patient Registration Card.</strong><br>
                            Please keep this card safe and bring it with you for all vaccination appointments.<br>
                            For any queries, contact us at <strong>support@vakapo.com</strong> or call <strong>+94 11 234 5678</strong>.
                        </div>
                        <div class="terms">
                            <strong>Terms & Conditions:</strong> This card is valid for all VakaPo services. 
                            Please inform us of any changes to your personal information. 
                            Keep your login credentials secure and do not share them with others.
                        </div>
                    </div>
                </div>
            </body>
            </html>
            """, patientId, patientName, email, contact, dateOfBirth, gender, address, emergencyContact, currentDate);
    }
}
