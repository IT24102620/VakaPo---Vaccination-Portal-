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
public class EmailTemplateService {

    private final JavaMailSender mailSender;

    @Value("${spring.mail.username}")
    private String fromEmail;

    @Value("${app.email.from-name:VakaPo Admin}")
    private String fromName;

    /**
     * Send HTML approval notification email
     */
    public void sendHtmlApprovalNotification(String toEmail, String institutionName, String institutionType) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
            
            try {
                helper.setFrom(fromEmail, fromName);
            } catch (UnsupportedEncodingException e) {
                helper.setFrom(fromEmail);
                log.warn("Failed to set sender name, using email only: {}", e.getMessage());
            }
            helper.setTo(toEmail);
            helper.setSubject("🎉 Your " + institutionType + " Registration Has Been Approved!");
            
            String htmlContent = buildHtmlApprovalEmail(institutionName, institutionType);
            helper.setText(htmlContent, true);
            
            mailSender.send(message);
            log.info("HTML approval email sent successfully to: {}", toEmail);
        } catch (MessagingException e) {
            log.error("Failed to send HTML approval email to: {}", toEmail, e);
            throw new RuntimeException("Failed to send HTML approval email", e);
        }
    }

    /**
     * Build HTML approval email template
     */
    private String buildHtmlApprovalEmail(String institutionName, String institutionType) {
        return String.format("""
            <!DOCTYPE html>
            <html>
            <head>
                <meta charset="UTF-8">
                <meta name="viewport" content="width=device-width, initial-scale=1.0">
                <title>Registration Approved</title>
                <style>
                    body { font-family: Arial, sans-serif; line-height: 1.6; color: #333; }
                    .container { max-width: 600px; margin: 0 auto; padding: 20px; }
                    .header { background: linear-gradient(135deg, #1E489E, #2c5aa0); color: white; padding: 30px; text-align: center; border-radius: 10px 10px 0 0; }
                    .content { background: #f9f9f9; padding: 30px; border-radius: 0 0 10px 10px; }
                    .success-icon { font-size: 48px; margin-bottom: 20px; }
                    .button { display: inline-block; background: #1E489E; color: white; padding: 12px 30px; text-decoration: none; border-radius: 5px; margin: 20px 0; }
                    .footer { text-align: center; margin-top: 30px; padding-top: 20px; border-top: 1px solid #ddd; color: #666; font-size: 12px; }
                </style>
            </head>
            <body>
                <div class="container">
                    <div class="header">
                        <div class="success-icon">🎉</div>
                        <h1>Registration Approved!</h1>
                        <p>Welcome to VakaPo</p>
                    </div>
                    <div class="content">
                        <h2>Congratulations, %s Team!</h2>
                        <p>We are pleased to inform you that your <strong>%s</strong> registration for "<strong>%s</strong>" has been approved by our administration team.</p>
                        
                        <h3>Your account is now active and you can:</h3>
                        <ul>
                            <li>✅ Log in to your dashboard</li>
                            <li>✅ Manage your vaccination services</li>
                            <li>✅ Access all platform features</li>
                            <li>✅ Start serving patients</li>
                        </ul>
                        
                        <h3>Next Steps:</h3>
                        <ol>
                            <li>Log in to your account using your registered credentials</li>
                            <li>Complete your profile setup</li>
                            <li>Start managing your vaccination services</li>
                        </ol>
                        
                        <p>If you have any questions or need assistance, please don't hesitate to contact our support team.</p>
                        
                        <p><strong>Welcome to VakaPo!</strong></p>
                    </div>
                    <div class="footer">
                        <p>Best regards,<br>VakaPo Administration Team</p>
                        <p>This is an automated message. Please do not reply to this email.</p>
                    </div>
                </div>
            </body>
            </html>
            """, institutionName, institutionType, institutionName);
    }

    /**
     * Send HTML unapproval notification email
     */
    public void sendHtmlUnapprovalNotification(String toEmail, String institutionName, String institutionType) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
            
            try {
                helper.setFrom(fromEmail, fromName);
            } catch (UnsupportedEncodingException e) {
                helper.setFrom(fromEmail);
                log.warn("Failed to set sender name, using email only: {}", e.getMessage());
            }
            helper.setTo(toEmail);
            helper.setSubject("⚠️ Your " + institutionType + " Account Has Been Unapproved");
            
            String htmlContent = buildHtmlUnapprovalEmail(institutionName, institutionType);
            helper.setText(htmlContent, true);
            
            mailSender.send(message);
            log.info("HTML unapproval email sent successfully to: {}", toEmail);
        } catch (MessagingException e) {
            log.error("Failed to send HTML unapproval email to: {}", toEmail, e);
            throw new RuntimeException("Failed to send HTML unapproval email", e);
        }
    }

    /**
     * Build HTML unapproval email template
     */
    private String buildHtmlUnapprovalEmail(String institutionName, String institutionType) {
        return String.format("""
            <!DOCTYPE html>
            <html>
            <head>
                <meta charset="UTF-8">
                <meta name="viewport" content="width=device-width, initial-scale=1.0">
                <title>Account Unapproved</title>
                <style>
                    body { font-family: Arial, sans-serif; line-height: 1.6; color: #333; }
                    .container { max-width: 600px; margin: 0 auto; padding: 20px; }
                    .header { background: linear-gradient(135deg, #dc3545, #c82333); color: white; padding: 30px; text-align: center; border-radius: 10px 10px 0 0; }
                    .content { background: #f9f9f9; padding: 30px; border-radius: 0 0 10px 10px; }
                    .warning-icon { font-size: 48px; margin-bottom: 20px; }
                    .button { display: inline-block; background: #dc3545; color: white; padding: 12px 30px; text-decoration: none; border-radius: 5px; margin: 20px 0; }
                    .footer { text-align: center; margin-top: 30px; padding-top: 20px; border-top: 1px solid #ddd; color: #666; font-size: 12px; }
                    .contact-info { background: #e9ecef; padding: 20px; border-radius: 5px; margin: 20px 0; }
                </style>
            </head>
            <body>
                <div class="container">
                    <div class="header">
                        <div class="warning-icon">⚠️</div>
                        <h1>Account Unapproved</h1>
                        <p>Important Notice from VakaPo</p>
                    </div>
                    <div class="content">
                        <h2>Dear %s Team,</h2>
                        <p>We regret to inform you that your <strong>%s</strong> account for "<strong>%s</strong>" has been unapproved by our administration team.</p>
                        
                        <h3>What this means:</h3>
                        <ul>
                            <li>❌ Your account access has been temporarily suspended</li>
                            <li>❌ You can no longer access your dashboard</li>
                            <li>❌ You cannot manage vaccination services</li>
                            <li>❌ Platform features are no longer available</li>
                        </ul>
                        
                        <div class="contact-info">
                            <h3>Need Help?</h3>
                            <p>If you believe this action was taken in error or have questions about this decision, please contact our VakaPo support team immediately:</p>
                            <p><strong>Email:</strong> support@vakapo.com</p>
                            <p><strong>Phone:</strong> +94 11 234 5678</p>
                            <p><strong>Office Hours:</strong> Monday - Friday, 9:00 AM - 5:00 PM</p>
                        </div>
                        
                        <p>We apologize for any inconvenience this may cause and appreciate your understanding.</p>
                    </div>
                    <div class="footer">
                        <p>Best regards,<br>VakaPo Administration Team</p>
                        <p>This is an automated message. Please do not reply to this email.</p>
                    </div>
                </div>
            </body>
            </html>
            """, institutionName, institutionType, institutionName);
    }

    /**
     * Send HTML appointment booking confirmation email to patient
     */
    public void sendHtmlAppointmentBookingConfirmation(String patientEmail, String patientName, 
                                                      String vaccineName, String institutionName, 
                                                      String institutionType, String doctorName,
                                                      String appointmentDate, String timeSlot, 
                                                      Long appointmentId) {
        try {
            log.info("EmailTemplateService: Starting to send HTML appointment booking confirmation to: {}", patientEmail);
            
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
            
            try {
                helper.setFrom(fromEmail, fromName);
            } catch (UnsupportedEncodingException e) {
                helper.setFrom(fromEmail);
                log.warn("Failed to set sender name, using email only: {}", e.getMessage());
            }
            helper.setTo(patientEmail);
            helper.setSubject("✅ Vaccination Appointment Confirmed - " + vaccineName);
            
            String htmlContent = buildHtmlAppointmentBookingConfirmationEmail(patientName, vaccineName, 
                                                                            institutionName, institutionType, 
                                                                            doctorName, appointmentDate, timeSlot, 
                                                                            appointmentId);
            helper.setText(htmlContent, true);
            
            mailSender.send(message);
            log.info("EmailTemplateService: HTML appointment booking confirmation sent successfully to: {}", patientEmail);
        } catch (MessagingException e) {
            log.error("EmailTemplateService: Failed to send HTML appointment booking confirmation to: {}", patientEmail, e);
            throw new RuntimeException("Failed to send HTML appointment booking confirmation", e);
        }
    }

    /**
     * Build HTML appointment booking confirmation email template
     */
    private String buildHtmlAppointmentBookingConfirmationEmail(String patientName, String vaccineName, 
                                                              String institutionName, String institutionType, 
                                                              String doctorName, String appointmentDate, 
                                                              String timeSlot, Long appointmentId) {
        String baseUrl = "http://localhost:8080"; // You can make this configurable
        String loginUrl = baseUrl + "/login";
        String appointmentsUrl = baseUrl + "/patient/appointments";
        
        return String.format("""
            <!DOCTYPE html>
            <html>
            <head>
                <meta charset="UTF-8">
                <meta name="viewport" content="width=device-width, initial-scale=1.0">
                <title>Appointment Confirmed</title>
                <style>
                    body { font-family: Arial, sans-serif; line-height: 1.6; color: #333; margin: 0; padding: 0; }
                    .container { max-width: 600px; margin: 0 auto; padding: 20px; }
                    .header { background: linear-gradient(135deg, #28a745, #20c997); color: white; padding: 30px; text-align: center; border-radius: 10px 10px 0 0; }
                    .content { background: #f9f9f9; padding: 30px; border-radius: 0 0 10px 10px; }
                    .success-icon { font-size: 48px; margin-bottom: 20px; }
                    .appointment-details { background: white; padding: 20px; border-radius: 8px; margin: 20px 0; border-left: 4px solid #28a745; }
                    .appointment-details h3 { color: #28a745; margin-top: 0; }
                    .detail-row { display: flex; justify-content: space-between; margin: 10px 0; padding: 8px 0; border-bottom: 1px solid #eee; }
                    .detail-label { font-weight: bold; color: #555; }
                    .detail-value { color: #333; }
                    .reminders { background: #e7f3ff; padding: 20px; border-radius: 8px; margin: 20px 0; border-left: 4px solid #007bff; }
                    .reminders h3 { color: #007bff; margin-top: 0; }
                    .health-tips { background: #fff3cd; padding: 20px; border-radius: 8px; margin: 20px 0; border-left: 4px solid #ffc107; }
                    .health-tips h3 { color: #856404; margin-top: 0; }
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
                        <div class="success-icon">🎉</div>
                        <h1>Appointment Confirmed!</h1>
                        <p>Your vaccination appointment has been successfully booked</p>
                    </div>
                    <div class="content">
                        <h2>Dear %s,</h2>
                        <p>Thank you for choosing VakaPo for your vaccination needs. Your appointment has been confirmed and you will receive a reminder closer to your appointment date.</p>
                        
                        <div class="appointment-details">
                            <h3>📋 Appointment Details</h3>
                            <div class="detail-row">
                                <span class="detail-label">Appointment ID:</span>
                                <span class="detail-value">#%d</span>
                            </div>
                            <div class="detail-row">
                                <span class="detail-label">Vaccine:</span>
                                <span class="detail-value">%s</span>
                            </div>
                            <div class="detail-row">
                                <span class="detail-label">Date:</span>
                                <span class="detail-value">%s</span>
                            </div>
                            <div class="detail-row">
                                <span class="detail-label">Time:</span>
                                <span class="detail-value">%s</span>
                            </div>
                            <div class="detail-row">
                                <span class="detail-label">Location:</span>
                                <span class="detail-value">%s (%s)</span>
                            </div>
                            <div class="detail-row">
                                <span class="detail-label">Doctor:</span>
                                <span class="detail-value">%s</span>
                            </div>
                        </div>
                        
                        <div class="reminders">
                            <h3>📅 Important Reminders</h3>
                            <ul>
                                <li>Please arrive <strong>15 minutes before</strong> your scheduled time</li>
                                <li>Bring a <strong>valid ID</strong> and any relevant medical documents</li>
                                <li>If you need to reschedule or cancel, please do so at least <strong>24 hours in advance</strong></li>
                                <li>Contact the institution directly if you have any questions about the appointment</li>
                            </ul>
                        </div>
                        
                        <div class="health-tips">
                            <h3>💡 Health Tips for Your Appointment</h3>
                            <ul>
                                <li>Get a good night's sleep before your appointment</li>
                                <li>Stay hydrated and eat a light meal</li>
                                <li>Wear comfortable clothing with easy access to your arm</li>
                                <li>Bring your vaccination record if you have one</li>
                            </ul>
                        </div>
                        
                        <div style="text-align: center; margin: 30px 0;">
                            <a href="%s" class="button">View All Appointments</a>
                            <a href="%s" class="button">Log In to Account</a>
                        </div>
                        
                        <div class="contact-info">
                            <h3>📞 Contact Information</h3>
                            <p><strong>Institution:</strong> %s</p>
                            <p><strong>VakaPo Support:</strong> support@vakapo.com</p>
                            <p><strong>Phone:</strong> +94 11 234 5678</p>
                        </div>
                        
                        <p>We're committed to making your vaccination experience safe, convenient, and efficient. If you have any questions or concerns, please don't hesitate to contact us.</p>
                    </div>
                    <div class="footer">
                        <p>Best regards,<br><strong>VakaPo Team</strong></p>
                        <p>This is an automated confirmation message. Please do not reply to this email.</p>
                    </div>
                </div>
            </body>
            </html>
            """, patientName, appointmentId, vaccineName, appointmentDate, timeSlot, 
            institutionName, institutionType, doctorName, appointmentsUrl, loginUrl, institutionName);
    }

    /**
     * Send HTML appointment cancellation confirmation email to patient
     */
    public void sendHtmlAppointmentCancellationConfirmation(String patientEmail, String patientName, 
                                                          String vaccineName, String institutionName, 
                                                          String institutionType, String doctorName,
                                                          String appointmentDate, String timeSlot, 
                                                          Long appointmentId) {
        try {
            log.info("EmailTemplateService: Starting to send HTML appointment cancellation confirmation to: {}", patientEmail);
            
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
            
            try {
                helper.setFrom(fromEmail, fromName);
            } catch (UnsupportedEncodingException e) {
                helper.setFrom(fromEmail);
                log.warn("Failed to set sender name, using email only: {}", e.getMessage());
            }
            helper.setTo(patientEmail);
            helper.setSubject("❌ Vaccination Appointment Cancelled - " + vaccineName);
            
            String htmlContent = buildHtmlAppointmentCancellationConfirmationEmail(patientName, vaccineName, 
                                                                                 institutionName, institutionType, 
                                                                                 doctorName, appointmentDate, timeSlot, 
                                                                                 appointmentId);
            helper.setText(htmlContent, true);
            
            mailSender.send(message);
            log.info("EmailTemplateService: HTML appointment cancellation confirmation sent successfully to: {}", patientEmail);
        } catch (MessagingException e) {
            log.error("EmailTemplateService: Failed to send HTML appointment cancellation confirmation to: {}", patientEmail, e);
            throw new RuntimeException("Failed to send HTML appointment cancellation confirmation", e);
        }
    }

    /**
     * Build HTML appointment cancellation confirmation email template
     */
    private String buildHtmlAppointmentCancellationConfirmationEmail(String patientName, String vaccineName, 
                                                                   String institutionName, String institutionType, 
                                                                   String doctorName, String appointmentDate, 
                                                                   String timeSlot, Long appointmentId) {
        String baseUrl = "http://localhost:8080"; // You can make this configurable
        String appointmentsUrl = baseUrl + "/patient/appointments";
        
        return String.format("""
            <!DOCTYPE html>
            <html>
            <head>
                <meta charset="UTF-8">
                <meta name="viewport" content="width=device-width, initial-scale=1.0">
                <title>Appointment Cancelled</title>
                <style>
                    body { font-family: Arial, sans-serif; line-height: 1.6; color: #333; margin: 0; padding: 0; }
                    .container { max-width: 600px; margin: 0 auto; padding: 20px; }
                    .header { background: linear-gradient(135deg, #dc3545, #c82333); color: white; padding: 30px; text-align: center; border-radius: 10px 10px 0 0; }
                    .content { background: #f9f9f9; padding: 30px; border-radius: 0 0 10px 10px; }
                    .cancelled-icon { font-size: 48px; margin-bottom: 20px; }
                    .appointment-details { background: white; padding: 20px; border-radius: 8px; margin: 20px 0; border-left: 4px solid #dc3545; }
                    .appointment-details h3 { color: #dc3545; margin-top: 0; }
                    .detail-row { display: flex; justify-content: space-between; margin: 10px 0; padding: 8px 0; border-bottom: 1px solid #eee; }
                    .detail-label { font-weight: bold; color: #555; }
                    .detail-value { color: #333; }
                    .info-section { background: #e7f3ff; padding: 20px; border-radius: 8px; margin: 20px 0; border-left: 4px solid #007bff; }
                    .info-section h3 { color: #007bff; margin-top: 0; }
                    .next-steps { background: #fff3cd; padding: 20px; border-radius: 8px; margin: 20px 0; border-left: 4px solid #ffc107; }
                    .next-steps h3 { color: #856404; margin-top: 0; }
                    .button { display: inline-block; background: #28a745; color: white; padding: 12px 30px; text-decoration: none; border-radius: 5px; margin: 10px 5px; }
                    .button:hover { background: #218838; }
                    .button-secondary { display: inline-block; background: #6c757d; color: white; padding: 12px 30px; text-decoration: none; border-radius: 5px; margin: 10px 5px; }
                    .button-secondary:hover { background: #5a6268; }
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
                        <div class="cancelled-icon">❌</div>
                        <h1>Appointment Cancelled</h1>
                        <p>Your vaccination appointment has been successfully cancelled</p>
                    </div>
                    <div class="content">
                        <h2>Dear %s,</h2>
                        <p>This email confirms that your vaccination appointment has been successfully cancelled. We understand that plans can change, and we're here to help you reschedule when you're ready.</p>
                        
                        <div class="appointment-details">
                            <h3>📋 Cancelled Appointment Details</h3>
                            <div class="detail-row">
                                <span class="detail-label">Appointment ID:</span>
                                <span class="detail-value">#%d</span>
                            </div>
                            <div class="detail-row">
                                <span class="detail-label">Vaccine:</span>
                                <span class="detail-value">%s</span>
                            </div>
                            <div class="detail-row">
                                <span class="detail-label">Date:</span>
                                <span class="detail-value">%s</span>
                            </div>
                            <div class="detail-row">
                                <span class="detail-label">Time:</span>
                                <span class="detail-value">%s</span>
                            </div>
                            <div class="detail-row">
                                <span class="detail-label">Location:</span>
                                <span class="detail-value">%s (%s)</span>
                            </div>
                            <div class="detail-row">
                                <span class="detail-label">Doctor:</span>
                                <span class="detail-value">%s</span>
                            </div>
                        </div>
                        
                        <div class="info-section">
                            <h3>ℹ️ Important Information</h3>
                            <ul>
                                <li>Your appointment has been <strong>permanently cancelled</strong></li>
                                <li>The time slot is now <strong>available for other patients</strong></li>
                                <li>You can <strong>book a new appointment at any time</strong></li>
                                <li><strong>No further action is required</strong> from your side</li>
                            </ul>
                        </div>
                        
                        <div class="next-steps">
                            <h3>🔄 Next Steps</h3>
                            <ul>
                                <li>If you need to reschedule, you can book a new appointment anytime</li>
                                <li>Check our available time slots for the same or different dates</li>
                                <li>Contact the institution directly if you have any questions</li>
                                <li>We recommend booking well in advance for popular time slots</li>
                            </ul>
                        </div>
                        
                        <div style="text-align: center; margin: 30px 0;">
                            <a href="%s" class="button">Book New Appointment</a>
                            <a href="%s" class="button-secondary">View All Appointments</a>
                        </div>
                        
                        <div class="contact-info">
                            <h3>📞 Contact Information</h3>
                            <p><strong>Institution:</strong> %s</p>
                            <p><strong>VakaPo Support:</strong> support@vakapo.com</p>
                            <p><strong>Phone:</strong> +94 11 234 5678</p>
                        </div>
                        
                        <p>Thank you for using VakaPo for your vaccination needs. We hope to serve you again soon and are here to help whenever you're ready to reschedule.</p>
                    </div>
                    <div class="footer">
                        <p>Best regards,<br><strong>VakaPo Team</strong></p>
                        <p>This is an automated cancellation confirmation message. Please do not reply to this email.</p>
                    </div>
                </div>
            </body>
            </html>
            """, patientName, appointmentId, vaccineName, appointmentDate, timeSlot, 
            institutionName, institutionType, doctorName, appointmentsUrl, appointmentsUrl, institutionName);
    }

    /**
     * Send HTML hospital-initiated appointment cancellation confirmation email to patient
     */
    public void sendHtmlHospitalCancellationConfirmation(String patientEmail, String patientName, 
                                                      String vaccineName, String institutionName, 
                                                      String institutionType, String doctorName,
                                                      String appointmentDate, String timeSlot, 
                                                      Long appointmentId) {
        try {
            log.info("EmailTemplateService: Starting to send HTML hospital-initiated appointment cancellation confirmation to: {}", patientEmail);
            
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
            
            try {
                helper.setFrom(fromEmail, fromName);
            } catch (UnsupportedEncodingException e) {
                helper.setFrom(fromEmail);
                log.warn("Failed to set sender name, using email only: {}", e.getMessage());
            }
            helper.setTo(patientEmail);
            helper.setSubject("⚠️ Appointment Cancelled by Hospital - " + vaccineName);
            
            String htmlContent = buildHtmlHospitalCancellationConfirmationEmail(patientName, vaccineName, 
                                                                             institutionName, institutionType, 
                                                                             doctorName, appointmentDate, timeSlot, 
                                                                             appointmentId);
            helper.setText(htmlContent, true);
            
            mailSender.send(message);
            log.info("EmailTemplateService: HTML hospital-initiated appointment cancellation confirmation sent successfully to: {}", patientEmail);
        } catch (MessagingException e) {
            log.error("EmailTemplateService: Failed to send HTML hospital-initiated appointment cancellation confirmation to: {}", patientEmail, e);
            throw new RuntimeException("Failed to send HTML hospital-initiated appointment cancellation confirmation", e);
        }
    }

    /**
     * Build HTML hospital-initiated appointment cancellation confirmation email template
     */
    private String buildHtmlHospitalCancellationConfirmationEmail(String patientName, String vaccineName, 
                                                               String institutionName, String institutionType, 
                                                               String doctorName, String appointmentDate, 
                                                               String timeSlot, Long appointmentId) {
        String baseUrl = "http://localhost:8080"; // You can make this configurable
        String appointmentsUrl = baseUrl + "/patient/appointments";
        
        return String.format("""
            <!DOCTYPE html>
            <html>
            <head>
                <meta charset="UTF-8">
                <meta name="viewport" content="width=device-width, initial-scale=1.0">
                <title>Appointment Cancelled by Hospital</title>
                <style>
                    body { font-family: Arial, sans-serif; line-height: 1.6; color: #333; margin: 0; padding: 0; }
                    .container { max-width: 600px; margin: 0 auto; padding: 20px; }
                    .header { background: linear-gradient(135deg, #dc3545, #c82333); color: white; padding: 30px; text-align: center; border-radius: 10px 10px 0 0; }
                    .content { background: #f9f9f9; padding: 30px; border-radius: 0 0 10px 10px; }
                    .warning-icon { font-size: 48px; margin-bottom: 20px; }
                    .appointment-details { background: white; padding: 20px; border-radius: 8px; margin: 20px 0; border-left: 4px solid #dc3545; }
                    .appointment-details h3 { color: #dc3545; margin-top: 0; }
                    .detail-row { display: flex; justify-content: space-between; margin: 10px 0; padding: 8px 0; border-bottom: 1px solid #eee; }
                    .detail-label { font-weight: bold; color: #555; }
                    .detail-value { color: #333; }
                    .warning-section { background: #fff3cd; padding: 20px; border-radius: 8px; margin: 20px 0; border-left: 4px solid #ffc107; }
                    .warning-section h3 { color: #856404; margin-top: 0; }
                    .next-steps { background: #e7f3ff; padding: 20px; border-radius: 8px; margin: 20px 0; border-left: 4px solid #007bff; }
                    .next-steps h3 { color: #007bff; margin-top: 0; }
                    .button { display: inline-block; background: #28a745; color: white; padding: 12px 30px; text-decoration: none; border-radius: 5px; margin: 10px 5px; }
                    .button:hover { background: #218838; }
                    .button-secondary { display: inline-block; background: #6c757d; color: white; padding: 12px 30px; text-decoration: none; border-radius: 5px; margin: 10px 5px; }
                    .button-secondary:hover { background: #5a6268; }
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
                        <div class="warning-icon">⚠️</div>
                        <h1>Appointment Cancelled by Hospital</h1>
                        <p>We regret to inform you that your appointment has been cancelled due to some issues</p>
                    </div>
                    <div class="content">
                        <h2>Dear %s,</h2>
                        <p>We regret to inform you that your vaccination appointment has been cancelled by the hospital due to some issues. Please contact the hospital directly for more information about the cancellation.</p>
                        
                        <div class="appointment-details">
                            <h3>📋 Cancelled Appointment Details</h3>
                            <div class="detail-row">
                                <span class="detail-label">Appointment ID:</span>
                                <span class="detail-value">#%d</span>
                            </div>
                            <div class="detail-row">
                                <span class="detail-label">Vaccine:</span>
                                <span class="detail-value">%s</span>
                            </div>
                            <div class="detail-row">
                                <span class="detail-label">Date:</span>
                                <span class="detail-value">%s</span>
                            </div>
                            <div class="detail-row">
                                <span class="detail-label">Time:</span>
                                <span class="detail-value">%s</span>
                            </div>
                            <div class="detail-row">
                                <span class="detail-label">Location:</span>
                                <span class="detail-value">%s (%s)</span>
                            </div>
                            <div class="detail-row">
                                <span class="detail-label">Doctor:</span>
                                <span class="detail-value">%s</span>
                            </div>
                        </div>
                        
                        <div class="warning-section">
                            <h3>⚠️ Important Notice</h3>
                            <ul>
                                <li>Your appointment has been <strong>cancelled by the hospital due to some issues</strong></li>
                                <li>Please <strong>contact the hospital directly</strong> for more information about the cancellation</li>
                                <li>The hospital will provide you with details about rescheduling or alternative arrangements</li>
                                <li>We <strong>apologize for any inconvenience</strong> this may cause</li>
                            </ul>
                        </div>
                        
                        <div class="next-steps">
                            <h3>🔄 Next Steps</h3>
                            <ul>
                                <li><strong>Contact the hospital directly</strong> to understand the reason for cancellation</li>
                                <li>Inquire about <strong>rescheduling options</strong> or alternative time slots</li>
                                <li>Ask about any <strong>compensation or alternative arrangements</strong> if applicable</li>
                                <li>You can also <strong>book a new appointment with a different institution</strong> if needed</li>
                            </ul>
                        </div>
                        
                        <div style="text-align: center; margin: 30px 0;">
                            <a href="%s" class="button">Book New Appointment</a>
                            <a href="%s" class="button-secondary">View All Appointments</a>
                        </div>
                        
                        <div class="contact-info">
                            <h3>📞 Contact Information</h3>
                            <p><strong>Hospital:</strong> %s</p>
                            <p><strong>VakaPo Support:</strong> support@vakapo.com</p>
                            <p><strong>Phone:</strong> +94 11 234 5678</p>
                        </div>
                        
                        <p>We sincerely apologize for this inconvenience and appreciate your understanding. Please contact the hospital directly for more information about this cancellation.</p>
                    </div>
                    <div class="footer">
                        <p>Best regards,<br><strong>VakaPo Team</strong></p>
                        <p>This is an automated notification message. Please do not reply to this email.</p>
                    </div>
                </div>
            </body>
            </html>
            """, patientName, appointmentId, vaccineName, appointmentDate, timeSlot, 
            institutionName, institutionType, doctorName, appointmentsUrl, appointmentsUrl, institutionName);
    }

    /**
     * Send HTML appointment reversal confirmation email to patient
     */
    public void sendHtmlAppointmentReversalConfirmation(String patientEmail, String patientName, 
                                                      String vaccineName, String institutionName, 
                                                      String institutionType, String doctorName,
                                                      String appointmentDate, String timeSlot, 
                                                      Long appointmentId) {
        try {
            log.info("EmailTemplateService: Starting to send HTML appointment reversal confirmation to: {}", patientEmail);
            
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
            
            try {
                helper.setFrom(fromEmail, fromName);
            } catch (UnsupportedEncodingException e) {
                helper.setFrom(fromEmail);
                log.warn("Failed to set sender name, using email only: {}", e.getMessage());
            }
            helper.setTo(patientEmail);
            helper.setSubject("✅ Appointment Rescheduled - " + vaccineName);
            
            String htmlContent = buildHtmlAppointmentReversalConfirmationEmail(patientName, vaccineName, 
                                                                             institutionName, institutionType, 
                                                                             doctorName, appointmentDate, timeSlot, 
                                                                             appointmentId);
            helper.setText(htmlContent, true);
            
            mailSender.send(message);
            log.info("EmailTemplateService: HTML appointment reversal confirmation sent successfully to: {}", patientEmail);
        } catch (MessagingException e) {
            log.error("EmailTemplateService: Failed to send HTML appointment reversal confirmation to: {}", patientEmail, e);
            throw new RuntimeException("Failed to send HTML appointment reversal confirmation", e);
        }
    }

    /**
     * Send HTML appointment reschedule confirmation email to patient
     */
    public void sendHtmlAppointmentRescheduleConfirmation(String patientEmail, String patientName, 
                                                      String vaccineName, String institutionName, 
                                                      String institutionType, String doctorName,
                                                      String appointmentDate, String timeSlot, 
                                                      Long appointmentId) {
        try {
            log.info("EmailTemplateService: Starting to send HTML appointment reschedule confirmation to: {}", patientEmail);
            
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
            
            try {
                helper.setFrom(fromEmail, fromName);
            } catch (UnsupportedEncodingException e) {
                helper.setFrom(fromEmail);
                log.warn("Failed to set sender name, using email only: {}", e.getMessage());
            }
            helper.setTo(patientEmail);
            helper.setSubject("🔄 Vaccination Appointment Rescheduled - " + vaccineName);
            
            String htmlContent = buildHtmlAppointmentRescheduleConfirmationEmail(patientName, vaccineName, 
                                                                            institutionName, institutionType, 
                                                                            doctorName, appointmentDate, timeSlot, 
                                                                            appointmentId);
            helper.setText(htmlContent, true);
            
            mailSender.send(message);
            log.info("EmailTemplateService: HTML appointment reschedule confirmation sent successfully to: {}", patientEmail);
        } catch (MessagingException e) {
            log.error("EmailTemplateService: Failed to send HTML appointment reschedule confirmation to: {}", patientEmail, e);
            throw new RuntimeException("Failed to send HTML appointment reschedule confirmation", e);
        }
    }

    /**
     * Build HTML appointment reversal confirmation email template
     */
    private String buildHtmlAppointmentReversalConfirmationEmail(String patientName, String vaccineName, 
                                                               String institutionName, String institutionType, 
                                                               String doctorName, String appointmentDate, 
                                                               String timeSlot, Long appointmentId) {
        String baseUrl = "http://localhost:8080"; // You can make this configurable
        String appointmentsUrl = baseUrl + "/patient/appointments";
        
        return String.format("""
            <!DOCTYPE html>
            <html>
            <head>
                <meta charset="UTF-8">
                <meta name="viewport" content="width=device-width, initial-scale=1.0">
                <title>Appointment Rescheduled</title>
                <style>
                    body { font-family: Arial, sans-serif; line-height: 1.6; color: #333; margin: 0; padding: 0; }
                    .container { max-width: 600px; margin: 0 auto; padding: 20px; }
                    .header { background: linear-gradient(135deg, #28a745, #20c997); color: white; padding: 30px; text-align: center; border-radius: 10px 10px 0 0; }
                    .content { background: #f9f9f9; padding: 30px; border-radius: 0 0 10px 10px; }
                    .success-icon { font-size: 48px; margin-bottom: 20px; }
                    .appointment-details { background: white; padding: 20px; border-radius: 8px; margin: 20px 0; border-left: 4px solid #28a745; }
                    .appointment-details h3 { color: #28a745; margin-top: 0; }
                    .detail-row { display: flex; justify-content: space-between; margin: 10px 0; padding: 8px 0; border-bottom: 1px solid #eee; }
                    .detail-label { font-weight: bold; color: #555; }
                    .detail-value { color: #333; }
                    .info-section { background: #d4edda; padding: 20px; border-radius: 8px; margin: 20px 0; border-left: 4px solid #28a745; }
                    .info-section h3 { color: #155724; margin-top: 0; }
                    .what-happened { background: #e7f3ff; padding: 20px; border-radius: 8px; margin: 20px 0; border-left: 4px solid #007bff; }
                    .what-happened h3 { color: #007bff; margin-top: 0; }
                    .reminders { background: #fff3cd; padding: 20px; border-radius: 8px; margin: 20px 0; border-left: 4px solid #ffc107; }
                    .reminders h3 { color: #856404; margin-top: 0; }
                    .button { display: inline-block; background: #28a745; color: white; padding: 12px 30px; text-decoration: none; border-radius: 5px; margin: 10px 5px; }
                    .button:hover { background: #218838; }
                    .button-secondary { display: inline-block; background: #6c757d; color: white; padding: 12px 30px; text-decoration: none; border-radius: 5px; margin: 10px 5px; }
                    .button-secondary:hover { background: #5a6268; }
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
                        <div class="success-icon">✅</div>
                        <h1>Appointment Rescheduled</h1>
                        <p>Great news! Your appointment has been successfully rescheduled</p>
                    </div>
                    <div class="content">
                        <h2>Dear %s,</h2>
                        <p>Great news! Your previously cancelled vaccination appointment has been rescheduled. We have automatically found the next available time slot for you.</p>
                        
                        <div class="appointment-details">
                            <h3>📋 Rescheduled Appointment Details</h3>
                            <div class="detail-row">
                                <span class="detail-label">Appointment ID:</span>
                                <span class="detail-value">#%d</span>
                            </div>
                            <div class="detail-row">
                                <span class="detail-label">Vaccine:</span>
                                <span class="detail-value">%s</span>
                            </div>
                            <div class="detail-row">
                                <span class="detail-label">Date:</span>
                                <span class="detail-value">%s</span>
                            </div>
                            <div class="detail-row">
                                <span class="detail-label">Time:</span>
                                <span class="detail-value">%s</span>
                            </div>
                            <div class="detail-row">
                                <span class="detail-label">Location:</span>
                                <span class="detail-value">%s (%s)</span>
                            </div>
                            <div class="detail-row">
                                <span class="detail-label">Doctor:</span>
                                <span class="detail-value">%s</span>
                            </div>
                        </div>
                        
                        <div class="info-section">
                            <h3>✅ Important Information</h3>
                            <ul>
                                <li>Your appointment has been <strong>successfully rescheduled</strong></li>
                                <li>The new time slot has been <strong>automatically assigned</strong></li>
                                <li>Please note the <strong>updated time</strong> for your appointment</li>
                                <li>You will receive a <strong>reminder before your appointment</strong></li>
                            </ul>
                        </div>
                        
                        <div class="what-happened">
                            <h3>🔄 What Happened</h3>
                            <ul>
                                <li>Your previous appointment was <strong>cancelled due to some issues</strong></li>
                                <li>The hospital has now <strong>resolved those issues</strong></li>
                                <li>We have automatically found the <strong>next available time slot</strong> for you</li>
                                <li>Your appointment is now <strong>confirmed and ready</strong></li>
                            </ul>
                        </div>
                        
                        <div class="reminders">
                            <h3>📅 Important Reminders</h3>
                            <ul>
                                <li>Please arrive <strong>15 minutes before</strong> your scheduled time</li>
                                <li>Bring a <strong>valid ID</strong> and any relevant medical documents</li>
                                <li>If you need to reschedule or cancel, please do so at least <strong>24 hours in advance</strong></li>
                                <li>Contact the institution directly if you have any questions about the appointment</li>
                            </ul>
                        </div>
                        
                        <div style="text-align: center; margin: 30px 0;">
                            <a href="%s" class="button">View All Appointments</a>
                            <a href="%s" class="button-secondary">Book New Appointment</a>
                        </div>
                        
                        <div class="contact-info">
                            <h3>📞 Contact Information</h3>
                            <p><strong>Institution:</strong> %s</p>
                            <p><strong>VakaPo Support:</strong> support@vakapo.com</p>
                            <p><strong>Phone:</strong> +94 11 234 5678</p>
                        </div>
                        
                        <p>Thank you for your patience and understanding. We're excited to help you with your vaccination!</p>
                    </div>
                    <div class="footer">
                        <p>Best regards,<br><strong>VakaPo Team</strong></p>
                        <p>This is an automated rescheduling confirmation message. Please do not reply to this email.</p>
                    </div>
                </div>
            </body>
            </html>
            """, patientName, appointmentId, vaccineName, appointmentDate, timeSlot, 
            institutionName, institutionType, doctorName, appointmentsUrl, appointmentsUrl, institutionName);
    }

    /**
     * Build HTML appointment reschedule confirmation email template
     */
    private String buildHtmlAppointmentRescheduleConfirmationEmail(String patientName, String vaccineName, 
                                                               String institutionName, String institutionType, 
                                                               String doctorName, String appointmentDate, 
                                                               String timeSlot, Long appointmentId) {
        String baseUrl = "http://localhost:8080"; // You can make this configurable
        String appointmentsUrl = baseUrl + "/patient/appointments";
        
        return String.format("""
            <!DOCTYPE html>
            <html>
            <head>
                <meta charset="UTF-8">
                <meta name="viewport" content="width=device-width, initial-scale=1.0">
                <title>Appointment Rescheduled</title>
                <style>
                    body { font-family: Arial, sans-serif; line-height: 1.6; color: #333; margin: 0; padding: 0; background-color: #f4f4f4; }
                    .container { max-width: 600px; margin: 0 auto; background-color: #ffffff; }
                    .header { background: linear-gradient(135deg, #667eea 0%%, #764ba2 100%%); color: white; padding: 30px; text-align: center; }
                    .header h1 { margin: 0; font-size: 28px; }
                    .content { padding: 30px; }
                    .appointment-details { background-color: #f8f9fa; border-left: 4px solid #28a745; padding: 20px; margin: 20px 0; border-radius: 5px; }
                    .detail-row { display: flex; justify-content: space-between; margin: 10px 0; padding: 8px 0; border-bottom: 1px solid #e9ecef; }
                    .detail-label { font-weight: bold; color: #495057; }
                    .detail-value { color: #212529; }
                    .info-section { background-color: #e3f2fd; border-left: 4px solid #2196f3; padding: 20px; margin: 20px 0; border-radius: 5px; }
                    .what-happened { background-color: #fff3e0; border-left: 4px solid #ff9800; padding: 20px; margin: 20px 0; border-radius: 5px; }
                    .reminders { background-color: #f3e5f5; border-left: 4px solid #9c27b0; padding: 20px; margin: 20px 0; border-radius: 5px; }
                    .info-section h3, .what-happened h3, .reminders h3 { margin-top: 0; color: #333; }
                    .info-section ul, .what-happened ul, .reminders ul { margin: 10px 0; padding-left: 20px; }
                    .info-section li, .what-happened li, .reminders li { margin: 8px 0; }
                    .button { display: inline-block; background-color: #28a745; color: white; padding: 12px 25px; text-decoration: none; border-radius: 5px; margin: 10px; font-weight: bold; }
                    .button-secondary { display: inline-block; background-color: #6c757d; color: white; padding: 12px 25px; text-decoration: none; border-radius: 5px; margin: 10px; font-weight: bold; }
                    .contact-info { background-color: #f8f9fa; padding: 20px; margin: 20px 0; border-radius: 5px; border: 1px solid #dee2e6; }
                    .footer { background-color: #343a40; color: white; padding: 20px; text-align: center; }
                    .footer p { margin: 5px 0; }
                </style>
            </head>
            <body>
                <div class="container">
                    <div class="header">
                        <h1>🔄 Appointment Rescheduled</h1>
                        <p>Your vaccination appointment has been successfully rescheduled</p>
                    </div>
                    <div class="content">
                        <p>Dear <strong>%s</strong>,</p>
                        
                        <p>We're writing to confirm that your vaccination appointment has been <strong>successfully rescheduled</strong>. Please find the updated details below:</p>
                        
                        <div class="appointment-details">
                            <h3>📅 Updated Appointment Details</h3>
                            <div class="detail-row">
                                <span class="detail-label">Appointment ID:</span>
                                <span class="detail-value">#%s</span>
                            </div>
                            <div class="detail-row">
                                <span class="detail-label">Vaccine:</span>
                                <span class="detail-value">%s</span>
                            </div>
                            <div class="detail-row">
                                <span class="detail-label">New Date:</span>
                                <span class="detail-value">%s</span>
                            </div>
                            <div class="detail-row">
                                <span class="detail-label">New Time:</span>
                                <span class="detail-value">%s</span>
                            </div>
                            <div class="detail-row">
                                <span class="detail-label">Location:</span>
                                <span class="detail-value">%s (%s)</span>
                            </div>
                            <div class="detail-row">
                                <span class="detail-label">Doctor:</span>
                                <span class="detail-value">%s</span>
                            </div>
                        </div>
                        
                        <div class="info-section">
                            <h3>✅ Important Information</h3>
                            <ul>
                                <li>Your appointment has been <strong>successfully rescheduled</strong></li>
                                <li>The new time slot has been <strong>automatically assigned</strong> based on availability</li>
                                <li>Please note the <strong>updated date and time</strong> for your appointment</li>
                                <li>You will receive a <strong>reminder before your appointment</strong></li>
                            </ul>
                        </div>
                        
                        <div class="what-happened">
                            <h3>🔄 What Happened</h3>
                            <ul>
                                <li>You requested to <strong>reschedule your appointment</strong></li>
                                <li>We found the <strong>next available time slot</strong> for the same vaccine and location</li>
                                <li>Your appointment has been <strong>updated with the new date and time</strong></li>
                                <li>All other details (vaccine, location, doctor) remain the same</li>
                            </ul>
                        </div>
                        
                        <div class="reminders">
                            <h3>📅 Important Reminders</h3>
                            <ul>
                                <li>Please arrive <strong>15 minutes before</strong> your scheduled time</li>
                                <li>Bring a <strong>valid ID</strong> and any relevant medical documents</li>
                                <li>If you need to reschedule again, please do so at least <strong>24 hours in advance</strong></li>
                                <li>Contact the institution directly if you have any questions about the appointment</li>
                            </ul>
                        </div>
                        
                        <div style="text-align: center; margin: 30px 0;">
                            <a href="%s" class="button">View All Appointments</a>
                            <a href="%s" class="button-secondary">Book New Appointment</a>
                        </div>
                        
                        <div class="contact-info">
                            <h3>📞 Contact Information</h3>
                            <p><strong>Institution:</strong> %s</p>
                            <p><strong>VakaPo Support:</strong> support@vakapo.com</p>
                            <p><strong>Phone:</strong> +94 11 234 5678</p>
                        </div>
                        
                        <p>Thank you for using VakaPo for your vaccination needs. We look forward to seeing you at your rescheduled appointment!</p>
                    </div>
                    <div class="footer">
                        <p>Best regards,<br><strong>VakaPo Team</strong></p>
                        <p>This is an automated rescheduling confirmation message. Please do not reply to this email.</p>
                    </div>
                </div>
            </body>
            </html>
            """, patientName, appointmentId, vaccineName, appointmentDate, timeSlot, 
            institutionName, institutionType, doctorName, appointmentsUrl, appointmentsUrl, institutionName);
    }
}
