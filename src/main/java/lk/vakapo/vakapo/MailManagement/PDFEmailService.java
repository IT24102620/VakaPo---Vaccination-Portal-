package lk.vakapo.vakapo.MailManagement;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import java.io.UnsupportedEncodingException;

@Service
@RequiredArgsConstructor
@Slf4j
public class PDFEmailService {

    private final JavaMailSender mailSender;

    @Value("${spring.mail.username}")
    private String fromEmail;

    @Value("${app.email.from-name:VakaPo Admin}")
    private String fromName;

    /**
     * Send patient registration card PDF via email
     */
    public void sendPatientRegistrationCard(String patientEmail, String patientName, String patientId, 
                                          byte[] pdfBytes) {
        try {
            log.info("PDFEmailService: Starting to send patient registration card to: {}", patientEmail);
            
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
            
            try {
                helper.setFrom(fromEmail, fromName);
            } catch (UnsupportedEncodingException e) {
                helper.setFrom(fromEmail);
                log.warn("Failed to set sender name, using email only: {}", e.getMessage());
            }
            
            helper.setTo(patientEmail);
            helper.setSubject("🎉 Welcome to VakaPo - Your Registration Card");
            
            // Create HTML email body
            String htmlContent = buildRegistrationEmailHTML(patientName, patientId);
            helper.setText(htmlContent, true);
            
            // Attach the PDF
            helper.addAttachment("VakaPo_Registration_Card_" + patientId + ".pdf", 
                               new org.springframework.core.io.ByteArrayResource(pdfBytes), 
                               "application/pdf");
            
            mailSender.send(message);
            log.info("PDFEmailService: Patient registration card sent successfully to: {}", patientEmail);
            
        } catch (MessagingException e) {
            log.error("PDFEmailService: Failed to send patient registration card to: {}", patientEmail, e);
            throw new RuntimeException("Failed to send patient registration card", e);
        }
    }

    /**
     * Build HTML email content for registration card
     */
    private String buildRegistrationEmailHTML(String patientName, String patientId) {
        return String.format("""
            <!DOCTYPE html>
            <html>
            <head>
                <meta charset="UTF-8">
                <meta name="viewport" content="width=device-width, initial-scale=1.0">
                <title>Welcome to VakaPo</title>
                <style>
                    body { font-family: Arial, sans-serif; line-height: 1.6; color: #333; margin: 0; padding: 0; }
                    .container { max-width: 600px; margin: 0 auto; padding: 20px; }
                    .header { background: linear-gradient(135deg, #2c5aa0, #1e3d72); color: white; padding: 30px; text-align: center; border-radius: 10px 10px 0 0; }
                    .content { background: #f9f9f9; padding: 30px; border-radius: 0 0 10px 10px; }
                    .welcome-icon { font-size: 48px; margin-bottom: 20px; }
                    .patient-id-box { background: #e8f4fd; border: 2px solid #2c5aa0; border-radius: 10px; padding: 20px; text-align: center; margin: 20px 0; }
                    .patient-id-label { font-size: 14px; color: #666; margin-bottom: 5px; }
                    .patient-id-value { font-size: 24px; font-weight: bold; color: #2c5aa0; }
                    .info-section { background: #d4edda; padding: 20px; border-radius: 8px; margin: 20px 0; border-left: 4px solid #28a745; }
                    .info-section h3 { color: #155724; margin-top: 0; }
                    .next-steps { background: #e7f3ff; padding: 20px; border-radius: 8px; margin: 20px 0; border-left: 4px solid #007bff; }
                    .next-steps h3 { color: #007bff; margin-top: 0; }
                    .button { display: inline-block; background: #28a745; color: white; padding: 12px 30px; text-decoration: none; border-radius: 5px; margin: 10px 5px; }
                    .button:hover { background: #218838; }
                    .footer { text-align: center; margin-top: 30px; padding-top: 20px; border-top: 1px solid #ddd; color: #666; font-size: 12px; }
                    .contact-info { background: #f8f9fa; padding: 20px; border-radius: 8px; margin: 20px 0; }
                    .contact-info h3 { color: #495057; margin-top: 0; }
                    ul { padding-left: 20px; }
                    li { margin: 8px 0; }
                </style>
            </head>
            <body>
                <div class="container">
                    <div class="header">
                        <div class="welcome-icon">🎉</div>
                        <h1>Welcome to VakaPo!</h1>
                        <p>Your registration has been completed successfully</p>
                    </div>
                    <div class="content">
                        <h2>Dear %s,</h2>
                        <p>Congratulations! Your registration with VakaPo has been completed successfully. We're excited to help you with your vaccination needs.</p>
                        
                        <div class="patient-id-box">
                            <div class="patient-id-label">Your Patient ID</div>
                            <div class="patient-id-value">%s</div>
                        </div>
                        
                        <div class="info-section">
                            <h3>📋 What's Included</h3>
                            <ul>
                                <li><strong>Your Registration Card PDF</strong> - Attached to this email</li>
                                <li><strong>Patient ID</strong> - Use this for all future appointments</li>
                                <li><strong>Access to VakaPo Portal</strong> - Book appointments online</li>
                                <li><strong>Email Notifications</strong> - Stay updated on your appointments</li>
                            </ul>
                        </div>
                        
                        <div class="next-steps">
                            <h3>🚀 Next Steps</h3>
                            <ul>
                                <li><strong>Download your registration card</strong> from the PDF attachment</li>
                                <li><strong>Keep your Patient ID safe</strong> - you'll need it for appointments</li>
                                <li><strong>Log in to your account</strong> to start booking vaccinations</li>
                                <li><strong>Browse available vaccines</strong> and schedule your appointments</li>
                            </ul>
                        </div>
                        
                        <div style="text-align: center; margin: 30px 0;">
                            <a href="http://localhost:8080/login" class="button">Log In to Your Account</a>
                            <a href="http://localhost:8080/patient/appointments" class="button">Book Appointment</a>
                        </div>
                        
                        <div class="contact-info">
                            <h3>📞 Need Help?</h3>
                            <p><strong>VakaPo Support:</strong> support@vakapo.com</p>
                            <p><strong>Phone:</strong> +94 11 234 5678</p>
                            <p><strong>Website:</strong> http://localhost:8080</p>
                        </div>
                        
                        <p>Thank you for choosing VakaPo for your vaccination needs. We're committed to making your vaccination experience safe, convenient, and efficient.</p>
                    </div>
                    <div class="footer">
                        <p>Best regards,<br><strong>VakaPo Team</strong></p>
                        <p>This is an automated welcome message. Please do not reply to this email.</p>
                    </div>
                </div>
            </body>
            </html>
            """, patientName, patientId);
    }
}
