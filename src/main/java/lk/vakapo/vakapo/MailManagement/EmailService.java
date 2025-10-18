package lk.vakapo.vakapo.MailManagement;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import org.springframework.core.io.ByteArrayResource;

@Service
@RequiredArgsConstructor
@Slf4j
public class EmailService {

    private final JavaMailSender mailSender;

    @Value("${spring.mail.username}")
    private String fromEmail;

    @Value("${app.email.from-name:VakaPo Admin}")
    private String fromName;

    /**
     * Generic method to send HTML email
     */
    public void sendEmail(String toEmail, String subject, String htmlContent) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
            
            helper.setFrom(fromEmail);
            helper.setTo(toEmail);
            helper.setSubject(subject);
            helper.setText(htmlContent, true); // true indicates HTML content
            
            mailSender.send(message);
            log.info("Email sent successfully to: {}", toEmail);
        } catch (MessagingException e) {
            log.error("Failed to send email to: {}", toEmail, e);
            throw new RuntimeException("Failed to send email", e);
        }
    }

    /**
     * Send approval notification email to hospital or clinic
     */
    public void sendApprovalNotification(String toEmail, String institutionName, String institutionType) {
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(fromEmail);
            message.setTo(toEmail);
            message.setSubject("🎉 Your " + institutionType + " Registration Has Been Approved!");
            
            String emailBody = buildApprovalEmailBody(institutionName, institutionType);
            message.setText(emailBody);
            
            mailSender.send(message);
            log.info("Approval email sent successfully to: {}", toEmail);
        } catch (Exception e) {
            log.error("Failed to send approval email to: {}", toEmail, e);
            throw new RuntimeException("Failed to send approval email", e);
        }
    }

    /**
     * Send rejection notification email to hospital or clinic
     */
    public void sendRejectionNotification(String toEmail, String institutionName, String institutionType, String reason) {
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(fromEmail);
            message.setTo(toEmail);
            message.setSubject("❌ Your " + institutionType + " Registration Has Been Rejected");
            
            String emailBody = buildRejectionEmailBody(institutionName, institutionType, reason);
            message.setText(emailBody);
            
            mailSender.send(message);
            log.info("Rejection email sent successfully to: {}", toEmail);
        } catch (Exception e) {
            log.error("Failed to send rejection email to: {}", toEmail, e);
            throw new RuntimeException("Failed to send rejection email", e);
        }
    }

    /**
     * Send unapproval notification email to hospital or clinic
     */
    public void sendUnapprovalNotification(String toEmail, String institutionName, String institutionType) {
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(fromEmail);
            message.setTo(toEmail);
            message.setSubject("⚠️ Your " + institutionType + " Account Has Been Unapproved");
            
            String emailBody = buildUnapprovalEmailBody(institutionName, institutionType);
            message.setText(emailBody);
            
            mailSender.send(message);
            log.info("Unapproval email sent successfully to: {}", toEmail);
        } catch (Exception e) {
            log.error("Failed to send unapproval email to: {}", toEmail, e);
            throw new RuntimeException("Failed to send unapproval email", e);
        }
    }

    /**
     * Build the approval email body
     */
    private String buildApprovalEmailBody(String institutionName, String institutionType) {
        return String.format("""
            Dear %s Team,
            
            Congratulations! 🎉
            
            We are pleased to inform you that your %s registration for "%s" has been approved by our administration team.
            
            Your account is now active and you can:
            • Log in to your dashboard
            • Manage your vaccination services
            • Access all platform features
            
            Next Steps:
            1. Log in to your account using your registered credentials
            2. Complete your profile setup
            3. Start managing your vaccination services
            
            If you have any questions or need assistance, please don't hesitate to contact our support team.
            
            Welcome to VakaPo!
            
            Best regards,
            VakaPo Administration Team
            
            ---
            This is an automated message. Please do not reply to this email.
            """, institutionName, institutionType, institutionName);
    }

    /**
     * Build the rejection email body
     */
    private String buildRejectionEmailBody(String institutionName, String institutionType, String reason) {
        return String.format("""
            Dear %s Team,
            
            We regret to inform you that your %s registration for "%s" has been rejected by our administration team.
            
            Reason for rejection: %s
            
            If you believe this decision was made in error or if you have additional documentation to provide, 
            please contact our support team for further assistance.
            
            You may reapply with the necessary corrections and additional documentation.
            
            Best regards,
            VakaPo Administration Team
            
            ---
            This is an automated message. Please do not reply to this email.
            """, institutionName, institutionType, institutionName, reason);
    }

    /**
     * Build the unapproval email body
     */
    private String buildUnapprovalEmailBody(String institutionName, String institutionType) {
        return String.format("""
            Dear %s Team,
            
            We regret to inform you that your %s account for "%s" has been unapproved by our administration team.
            
            This means your account access has been temporarily suspended and you will no longer be able to:
            • Access your dashboard
            • Manage vaccination services
            • Use platform features
            
            If you believe this action was taken in error or if you have questions about this decision, 
            please contact our VakaPo support team immediately.
            
            Contact Information:
            • Email: support@vakapo.com
            • Phone: +94 11 234 5678
            • Office Hours: Monday - Friday, 9:00 AM - 5:00 PM
            
            We apologize for any inconvenience this may cause and appreciate your understanding.
            
            Best regards,
            VakaPo Administration Team
            
            ---
            This is an automated message. Please do not reply to this email.
            """, institutionName, institutionType, institutionName);
    }

    /**
     * Send staff invitation email
     */
    public void sendStaffInvitation(String toEmail, String staffName, String institutionName, 
                                   String institutionType, String invitationToken) {
        try {
            log.info("EmailService: Starting to send staff invitation email to: {}", toEmail);
            log.info("EmailService: From email: {}, Staff name: {}, Institution: {}", fromEmail, staffName, institutionName);
            
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(fromEmail);
            message.setTo(toEmail);
            message.setSubject("🏥 You're Invited to Join " + institutionName + " on VakaPo!");
            
            log.info("EmailService: Building email body...");
            String emailBody = buildStaffInvitationEmailBody(staffName, institutionName, institutionType, invitationToken, toEmail);
            message.setText(emailBody);
            
            log.info("EmailService: Sending email via JavaMailSender...");
            mailSender.send(message);
            log.info("EmailService: Staff invitation email sent successfully to: {}", toEmail);
        } catch (Exception e) {
            log.error("EmailService: Failed to send staff invitation email to: {}", toEmail, e);
            throw new RuntimeException("Failed to send staff invitation email", e);
        }
    }

    /**
     * Send doctor confirmation link for additional hospital
     */
    public void sendDoctorConfirmationLink(String toEmail, String doctorName, String institutionName, 
                                          String institutionType, String confirmationToken) {
        try {
            log.info("EmailService: Starting to send doctor confirmation link to: {}", toEmail);
            
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(fromEmail);
            message.setTo(toEmail);
            message.setSubject("🏥 New Hospital Invitation - " + institutionName);
            
            String emailBody = buildDoctorConfirmationEmailBody(doctorName, institutionName, institutionType, confirmationToken, toEmail);
            message.setText(emailBody);
            
            mailSender.send(message);
            log.info("EmailService: Doctor confirmation link sent successfully to: {}", toEmail);
        } catch (Exception e) {
            log.error("EmailService: Failed to send doctor confirmation link to: {}", toEmail, e);
            throw new RuntimeException("Failed to send doctor confirmation link", e);
        }
    }

    /**
     * Send staff acceptance notification to institution
     */
    public void sendStaffAcceptanceNotification(String staffEmail, String staffName, 
                                               String institutionName, String institutionType) {
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(fromEmail);
            message.setTo(fromEmail); // Send to admin email for now, can be changed to institution email
            message.setSubject("✅ Staff Member Accepted Invitation - " + institutionName);
            
            String emailBody = buildStaffAcceptanceEmailBody(staffName, staffEmail, institutionName, institutionType);
            message.setText(emailBody);
            
            mailSender.send(message);
            log.info("Staff acceptance notification sent successfully for: {}", staffName);
        } catch (Exception e) {
            log.error("Failed to send staff acceptance notification: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to send staff acceptance notification", e);
        }
    }

    /**
     * Send staff removal notification to staff member
     */
    public void sendStaffRemovalNotification(String staffEmail, String staffName, 
                                           String institutionName, String institutionType, String role) {
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(fromEmail);
            message.setTo(staffEmail);
            message.setSubject("📋 Staff Removal Notification - " + institutionName);
            
            String emailBody = buildStaffRemovalEmailBody(staffName, institutionName, institutionType, role);
            message.setText(emailBody);
            
            mailSender.send(message);
            log.info("Staff removal notification sent successfully to: {}", staffEmail);
        } catch (Exception e) {
            log.error("Failed to send staff removal notification to: {}", staffEmail, e);
            throw new RuntimeException("Failed to send staff removal notification", e);
        }
    }

    /**
     * Send staff cancellation notification to staff member
     * This is different from removal - it's a cancellation of the connection
     */
    public void sendStaffCancellationNotification(String staffEmail, String staffName, 
                                                String institutionName, String institutionType, String role) {
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(fromEmail);
            message.setTo(staffEmail);
            message.setSubject("🚫 Staff Connection Cancelled - " + institutionName);
            
            String emailBody = buildStaffCancellationEmailBody(staffName, institutionName, institutionType, role);
            message.setText(emailBody);
            
            mailSender.send(message);
            log.info("Staff cancellation notification sent successfully to: {}", staffEmail);
        } catch (Exception e) {
            log.error("Failed to send staff cancellation notification to: {}", staffEmail, e);
            throw new RuntimeException("Failed to send staff cancellation notification", e);
        }
    }

    /**
     * Send vaccination schedule notification to doctor
     */
    public void sendVaccinationScheduleNotification(String doctorEmail, String doctorName, 
                                                   String institutionName, String institutionType,
                                                   String vaccineName, String timeFrom, String timeTo, 
                                                   String days, String notes) {
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(fromEmail);
            message.setTo(doctorEmail);
            message.setSubject("💉 New Vaccination Schedule Assigned - " + institutionName);
            
            String emailBody = buildVaccinationScheduleEmailBody(doctorName, institutionName, institutionType,
                                                               vaccineName, timeFrom, timeTo, days, notes);
            message.setText(emailBody);
            
            mailSender.send(message);
            log.info("Vaccination schedule notification sent successfully to: {}", doctorEmail);
        } catch (Exception e) {
            log.error("Failed to send vaccination schedule notification to: {}", doctorEmail, e);
            throw new RuntimeException("Failed to send vaccination schedule notification", e);
        }
    }

    /**
     * Send vaccination schedule cancellation notification to doctor
     */
    public void sendVaccinationScheduleCancellationNotification(String doctorEmail, String doctorName, 
                                                               String institutionName, String institutionType,
                                                               String vaccineName, String timeFrom, String timeTo, 
                                                               String days, String notes) {
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(fromEmail);
            message.setTo(doctorEmail);
            message.setSubject("❌ Vaccination Schedule Cancelled - " + institutionName);
            
            String emailBody = buildVaccinationScheduleCancellationEmailBody(doctorName, institutionName, institutionType,
                                                                           vaccineName, timeFrom, timeTo, days, notes);
            message.setText(emailBody);
            
            mailSender.send(message);
            log.info("Vaccination schedule cancellation notification sent successfully to: {}", doctorEmail);
        } catch (Exception e) {
            log.error("Failed to send vaccination schedule cancellation notification to: {}", doctorEmail, e);
            throw new RuntimeException("Failed to send vaccination schedule cancellation notification", e);
        }
    }

    /**
     * Build staff invitation email body
     */
    private String buildStaffInvitationEmailBody(String staffName, String institutionName, 
                                                 String institutionType, String invitationToken, String staffEmail) {
        String baseUrl = "http://localhost:8080"; // You can make this configurable
        String acceptLink = baseUrl + "/staff/invitation/accept?token=" + invitationToken;
        String rejectLink = baseUrl + "/staff/invitation/reject?token=" + invitationToken;
        String loginUrl = baseUrl + "/login";
        
        return String.format("""
            Dear %s,
            
            You have been invited to join "%s" as a staff member on the VakaPo vaccination management platform.
            
            Institution Details:
            • Name: %s
            • Type: %s
            • Platform: VakaPo - Sri Lanka's Vaccination Portal
            
            🔐 TEMPORARY LOGIN CREDENTIALS:
            • Email: %s
            • Password: 1234567890
            • Login URL: %s
            
            IMPORTANT: You MUST change your password on your first login for security reasons.
            
            What this means:
            • You'll be able to manage vaccination services
            • Access patient records and appointments
            • Contribute to public health initiatives
            • Work with a modern, secure platform
            
            To accept this invitation, please click the link below:
            %s
            
            If you wish to decline this invitation, you can click here:
            %s
            
            First Login Steps:
            1. Go to the login page: %s
            2. Use your temporary credentials above
            3. You will be prompted to change your password
            4. Complete your profile setup
            
            Important Notes:
            • This invitation is valid for 7 days
            • You must change your password on first login
            • Contact the institution directly if you have questions
            
            If you have any questions about this invitation, please contact:
            • Institution: %s
            • VakaPo Support: support@vakapo.com
            
            Welcome to the VakaPo team!
            
            Best regards,
            VakaPo Administration Team
            
            ---
            This is an automated message. Please do not reply to this email.
            """, staffName, institutionName, institutionName, institutionType, 
            staffEmail, loginUrl, acceptLink, rejectLink, loginUrl, institutionName);
    }

    /**
     * Build staff acceptance email body
     */
    private String buildStaffAcceptanceEmailBody(String staffName, String staffEmail, 
                                                String institutionName, String institutionType) {
        return String.format("""
            Staff Member Acceptance Notification
            
            A staff member has accepted your invitation to join your %s.
            
            Staff Details:
            • Name: %s
            • Email: %s
            • Institution: %s
            
            Next Steps:
            • The staff member can now access your dashboard
            • They will appear in your staff management section
            • You can assign roles and permissions as needed
            
            To manage your staff, please log in to your VakaPo dashboard.
            
            Best regards,
            VakaPo Administration Team
            
            ---
            This is an automated message. Please do not reply to this email.
            """, institutionType, staffName, staffEmail, institutionName);
    }

    /**
     * Build doctor confirmation email body
     */
    private String buildDoctorConfirmationEmailBody(String doctorName, String institutionName, 
                                                   String institutionType, String confirmationToken, String doctorEmail) {
        String baseUrl = "http://localhost:8080"; // You can make this configurable
        String confirmLink = baseUrl + "/staff/doctor/confirm?token=" + confirmationToken;
        String rejectLink = baseUrl + "/staff/doctor/reject?token=" + confirmationToken;
        
        return String.format("""
            Dear Dr. %s,
            
            You have been invited to join an additional hospital on the VakaPo platform.
            
            New Hospital Details:
            • Name: %s
            • Type: %s
            • Platform: VakaPo - Sri Lanka's Vaccination Portal
            
            As an existing doctor on VakaPo, you can work at multiple hospitals.
            This invitation allows you to expand your practice to include this new hospital.
            
            🔗 CONFIRMATION REQUIRED:
            To accept this invitation and join this hospital, please click the link below:
            %s
            
            If you wish to decline this invitation, you can click here:
            %s
            
            What this means:
            • You'll be able to manage patients at this additional hospital
            • Access patient records and appointments for this hospital
            • Contribute to vaccination services at this location
            • Use your existing VakaPo login credentials
            
            Important Notes:
            • This invitation is valid for 7 days
            • You can work at multiple hospitals simultaneously
            • Your existing login credentials will work for all hospitals
            • You can manage your hospital associations in your profile
            
            If you have any questions about this invitation, please contact:
            • Hospital: %s
            • VakaPo Support: support@vakapo.com
            
            Best regards,
            VakaPo Administration Team
            
            ---
            This is an automated message. Please do not reply to this email.
            """, doctorName, institutionName, institutionType, confirmLink, rejectLink, institutionName);
    }

    /**
     * Build staff removal email body
     */
    private String buildStaffRemovalEmailBody(String staffName, String institutionName, 
                                            String institutionType, String role) {
        return String.format("""
            Dear %s,
            
            We hope this message finds you well.
            
            This is to inform you that your association with %s (%s) has been terminated.
            
            Details:
            • Your Role: %s
            • Institution: %s
            • Institution Type: %s
            • Effective Date: %s
            
            What this means:
            • You will no longer have access to %s's patient records or systems
            • Your login credentials for this institution have been deactivated
            • You may still have access to other institutions if you are associated with them
            
            If you believe this removal was made in error, please contact:
            • Institution: %s
            • VakaPo Support: support@vakapo.com
            
            We thank you for your service and wish you the best in your future endeavors.
            
            Best regards,
            VakaPo Administration Team
            
            ---
            This is an automated message. Please do not reply to this email.
            """, staffName, institutionName, institutionType, role, institutionName, institutionType, 
            java.time.LocalDate.now().toString(), institutionName, institutionName);
    }

    /**
     * Build staff cancellation email body
     */
    private String buildStaffCancellationEmailBody(String staffName, String institutionName, 
                                                 String institutionType, String role) {
        return String.format("""
            Dear %s,
            
            We hope this message finds you well.
            
            This is to inform you that your connection with %s (%s) has been cancelled.
            
            Details:
            • Your Role: %s
            • Institution: %s
            • Institution Type: %s
            • Cancellation Date: %s
            
            What this means:
            • Your connection to %s has been cancelled, but your account remains active
            • You will no longer have access to %s's patient records or systems
            • Your login credentials for this institution have been deactivated
            • You can still be invited to work with other institutions in the future
            • Your professional profile and qualifications remain intact
            
            If you believe this cancellation was made in error, please contact:
            • Institution: %s
            • VakaPo Support: support@vakapo.com
            
            We appreciate your understanding and wish you continued success in your medical career.
            
            Best regards,
            VakaPo Administration Team
            
            ---
            This is an automated message. Please do not reply to this email.
            """, staffName, institutionName, institutionType, role, institutionName, institutionType, 
            java.time.LocalDate.now().toString(), institutionName, institutionName, institutionName);
    }

    /**
     * Build vaccination schedule notification email body
     */
    private String buildVaccinationScheduleEmailBody(String doctorName, String institutionName, 
                                                   String institutionType, String vaccineName, 
                                                   String timeFrom, String timeTo, String days, String notes) {
        return String.format("""
            Dear Dr. %s,
            
            A new vaccination schedule has been assigned to you at %s.
            
            📋 SCHEDULE DETAILS:
            • Institution: %s (%s)
            • Vaccine: %s
            • Time: %s - %s
            • Days: %s
            %s
            
            🎯 YOUR RESPONSIBILITIES:
            • Review the vaccination schedule details
            • Ensure you're available during the specified times
            • Prepare necessary equipment and supplies
            • Coordinate with the institution if any changes are needed
            
            📅 IMPORTANT NOTES:
            • Please confirm your availability for the scheduled times
            • Contact the institution directly if you need to make any adjustments
            • Ensure all required certifications are up to date
            • Follow all safety protocols and guidelines
            
            %s
            
            🔗 NEXT STEPS:
            1. Log in to your VakaPo dashboard
            2. Review the complete schedule details
            3. Confirm your availability
            4. Contact the institution if needed
            
            If you have any questions about this schedule or need to make changes, 
            please contact:
            • Institution: %s
            • VakaPo Support: support@vakapo.com
            
            Thank you for your commitment to public health and vaccination services!
            
            Best regards,
            VakaPo Administration Team
            
            ---
            This is an automated message. Please do not reply to this email.
            """, doctorName, institutionName, institutionName, institutionType, vaccineName, 
            timeFrom, timeTo, days, 
            notes != null && !notes.trim().isEmpty() ? "• Notes: " + notes : "",
            notes != null && !notes.trim().isEmpty() ? "\n📝 ADDITIONAL INFORMATION:\n" + notes : "",
            institutionName);
    }

    /**
     * Build vaccination schedule cancellation email body
     */
    private String buildVaccinationScheduleCancellationEmailBody(String doctorName, String institutionName, 
                                                               String institutionType, String vaccineName, 
                                                               String timeFrom, String timeTo, String days, String notes) {
        return String.format("""
            Dear Dr. %s,
            
            This is to inform you that your vaccination schedule at %s has been cancelled.
            
            📋 CANCELLED SCHEDULE DETAILS:
            • Institution: %s (%s)
            • Vaccine: %s
            • Time: %s - %s
            • Days: %s
            %s
            
            ℹ️ IMPORTANT INFORMATION:
            • This schedule has been permanently removed from the system
            • You are no longer required to be available during these times
            • Any patients who may have been scheduled for these slots will be notified separately
            • You can check your updated schedule in your VakaPo dashboard
            
            🔄 NEXT STEPS:
            1. Log in to your VakaPo dashboard to view your updated schedule
            2. Check for any new schedules that may have been assigned
            3. Contact the institution if you have any questions about this cancellation
            
            %s
            
            If you have any questions about this cancellation or need clarification, 
            please contact:
            • Institution: %s
            • VakaPo Support: support@vakapo.com
            
            We apologize for any inconvenience this may cause and appreciate your understanding.
            
            Best regards,
            VakaPo Administration Team
            
            ---
            This is an automated message. Please do not reply to this email.
            """, doctorName, institutionName, institutionName, institutionType, vaccineName, 
            timeFrom, timeTo, days, 
            notes != null && !notes.trim().isEmpty() ? "• Notes: " + notes : "",
            notes != null && !notes.trim().isEmpty() ? "\n📝 ORIGINAL NOTES:\n" + notes : "",
            institutionName);
    }

    /**
     * Send appointment booking confirmation email to patient
     */
    public void sendAppointmentBookingConfirmation(String patientEmail, String patientName, 
                                                  String vaccineName, String institutionName, 
                                                  String institutionType, String doctorName,
                                                  String appointmentDate, String timeSlot, 
                                                  Long appointmentId) {
        try {
            log.info("EmailService: Starting to send appointment booking confirmation to: {}", patientEmail);
            
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(fromEmail);
            message.setTo(patientEmail);
            message.setSubject("✅ Vaccination Appointment Confirmed - " + vaccineName);
            
            String emailBody = buildAppointmentBookingConfirmationEmailBody(patientName, vaccineName, 
                                                                          institutionName, institutionType, 
                                                                          doctorName, appointmentDate, timeSlot, 
                                                                          appointmentId);
            message.setText(emailBody);
            
            mailSender.send(message);
            log.info("EmailService: Appointment booking confirmation sent successfully to: {}", patientEmail);
        } catch (Exception e) {
            log.error("EmailService: Failed to send appointment booking confirmation to: {}", patientEmail, e);
            throw new RuntimeException("Failed to send appointment booking confirmation", e);
        }
    }

    /**
     * Build appointment booking confirmation email body
     */
    private String buildAppointmentBookingConfirmationEmailBody(String patientName, String vaccineName, 
                                                              String institutionName, String institutionType, 
                                                              String doctorName, String appointmentDate, 
                                                              String timeSlot, Long appointmentId) {
        String baseUrl = "http://localhost:8080"; // You can make this configurable
        String loginUrl = baseUrl + "/login";
        String appointmentsUrl = baseUrl + "/patient/appointments";
        
        return String.format("""
            Dear %s,
            
            🎉 Your vaccination appointment has been successfully booked!
            
            📋 APPOINTMENT DETAILS:
            • Appointment ID: #%d
            • Vaccine: %s
            • Date: %s
            • Time: %s
            • Location: %s (%s)
            • Doctor: %s
            
            📅 IMPORTANT REMINDERS:
            • Please arrive 15 minutes before your scheduled time
            • Bring a valid ID and any relevant medical documents
            • If you need to reschedule or cancel, please do so at least 24 hours in advance
            • Contact the institution directly if you have any questions about the appointment
            
            🔗 MANAGE YOUR APPOINTMENT:
            • View all your appointments: %s
            • Log in to your account: %s
            
            📞 CONTACT INFORMATION:
            • Institution: %s
            • VakaPo Support: support@vakapo.com
            • Phone: +94 11 234 5678
            
            💡 HEALTH TIPS:
            • Get a good night's sleep before your appointment
            • Stay hydrated and eat a light meal
            • Wear comfortable clothing with easy access to your arm
            • Bring your vaccination record if you have one
            
            Thank you for choosing VakaPo for your vaccination needs. We're committed to making your vaccination experience safe, convenient, and efficient.
            
            Best regards,
            VakaPo Team
            
            ---
            This is an automated confirmation message. Please do not reply to this email.
            """, patientName, appointmentId, vaccineName, appointmentDate, timeSlot, 
            institutionName, institutionType, doctorName, appointmentsUrl, loginUrl, institutionName);
    }

    /**
     * Send appointment cancellation confirmation email to patient
     */
    public void sendAppointmentCancellationConfirmation(String patientEmail, String patientName, 
                                                       String vaccineName, String institutionName, 
                                                       String institutionType, String doctorName,
                                                       String appointmentDate, String timeSlot, 
                                                       Long appointmentId) {
        try {
            log.info("EmailService: Starting to send appointment cancellation confirmation to: {}", patientEmail);
            
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(fromEmail);
            message.setTo(patientEmail);
            message.setSubject("❌ Vaccination Appointment Cancelled - " + vaccineName);
            
            String emailBody = buildAppointmentCancellationConfirmationEmailBody(patientName, vaccineName, 
                                                                               institutionName, institutionType, 
                                                                               doctorName, appointmentDate, timeSlot, 
                                                                               appointmentId);
            message.setText(emailBody);
            
            mailSender.send(message);
            log.info("EmailService: Appointment cancellation confirmation sent successfully to: {}", patientEmail);
        } catch (Exception e) {
            log.error("EmailService: Failed to send appointment cancellation confirmation to: {}", patientEmail, e);
            throw new RuntimeException("Failed to send appointment cancellation confirmation", e);
        }
    }

    /**
     * Build appointment cancellation confirmation email body
     */
    private String buildAppointmentCancellationConfirmationEmailBody(String patientName, String vaccineName, 
                                                                   String institutionName, String institutionType, 
                                                                   String doctorName, String appointmentDate, 
                                                                   String timeSlot, Long appointmentId) {
        String baseUrl = "http://localhost:8080"; // You can make this configurable
        String loginUrl = baseUrl + "/login";
        String appointmentsUrl = baseUrl + "/patient/appointments";
        
        return String.format("""
            Dear %s,
            
            📋 Your vaccination appointment has been successfully cancelled.
            
            📋 CANCELLED APPOINTMENT DETAILS:
            • Appointment ID: #%d
            • Vaccine: %s
            • Date: %s
            • Time: %s
            • Location: %s (%s)
            • Doctor: %s
            
            ℹ️ IMPORTANT INFORMATION:
            • Your appointment has been permanently cancelled
            • The time slot is now available for other patients
            • You can book a new appointment at any time
            • No further action is required from your side
            
            🔄 NEXT STEPS:
            • If you need to reschedule, you can book a new appointment anytime
            • Check our available time slots for the same or different dates
            • Contact the institution directly if you have any questions
            
            🔗 MANAGE YOUR APPOINTMENTS:
            • View all your appointments: %s
            • Log in to your account: %s
            • Book a new appointment anytime
            
            📞 CONTACT INFORMATION:
            • Institution: %s
            • VakaPo Support: support@vakapo.com
            • Phone: +94 11 234 5678
            
            💡 REMINDER:
            • You can book a new appointment at any time
            • We recommend booking well in advance for popular time slots
            • Contact us if you need assistance with rescheduling
            
            Thank you for using VakaPo for your vaccination needs. We hope to serve you again soon.
            
            Best regards,
            VakaPo Team
            
            ---
            This is an automated cancellation confirmation message. Please do not reply to this email.
            """, patientName, appointmentId, vaccineName, appointmentDate, timeSlot, 
            institutionName, institutionType, doctorName, appointmentsUrl, loginUrl, institutionName);
    }

    /**
     * Send hospital-initiated appointment cancellation confirmation email to patient
     */
    public void sendHospitalCancellationConfirmation(String patientEmail, String patientName, 
                                                   String vaccineName, String institutionName, 
                                                   String institutionType, String doctorName,
                                                   String appointmentDate, String timeSlot, 
                                                   Long appointmentId) {
        try {
            log.info("EmailService: Starting to send hospital-initiated appointment cancellation confirmation to: {}", patientEmail);
            
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(fromEmail);
            message.setTo(patientEmail);
            message.setSubject("⚠️ Appointment Cancelled by Hospital - " + vaccineName);
            
            String emailBody = buildHospitalCancellationConfirmationEmailBody(patientName, vaccineName, 
                                                                           institutionName, institutionType, 
                                                                           doctorName, appointmentDate, timeSlot, 
                                                                           appointmentId);
            message.setText(emailBody);
            
            mailSender.send(message);
            log.info("EmailService: Hospital-initiated appointment cancellation confirmation sent successfully to: {}", patientEmail);
        } catch (Exception e) {
            log.error("EmailService: Failed to send hospital-initiated appointment cancellation confirmation to: {}", patientEmail, e);
            throw new RuntimeException("Failed to send hospital-initiated appointment cancellation confirmation", e);
        }
    }

    /**
     * Build hospital-initiated appointment cancellation confirmation email body
     */
    private String buildHospitalCancellationConfirmationEmailBody(String patientName, String vaccineName, 
                                                               String institutionName, String institutionType, 
                                                               String doctorName, String appointmentDate, 
                                                               String timeSlot, Long appointmentId) {
        String baseUrl = "http://localhost:8080"; // You can make this configurable
        String loginUrl = baseUrl + "/login";
        String appointmentsUrl = baseUrl + "/patient/appointments";
        
        return String.format("""
            Dear %s,
            
            ⚠️ We regret to inform you that your vaccination appointment has been cancelled by the hospital due to some issues.
            
            📋 CANCELLED APPOINTMENT DETAILS:
            • Appointment ID: #%d
            • Vaccine: %s
            • Date: %s
            • Time: %s
            • Location: %s (%s)
            • Doctor: %s
            
            ⚠️ IMPORTANT NOTICE:
            • Your appointment has been cancelled by the hospital due to some issues
            • Please contact the hospital directly for more information about the cancellation
            • The hospital will provide you with details about rescheduling or alternative arrangements
            • We apologize for any inconvenience this may cause
            
            🔄 NEXT STEPS:
            • Contact the hospital directly to understand the reason for cancellation
            • Inquire about rescheduling options or alternative time slots
            • Ask about any compensation or alternative arrangements if applicable
            • You can also book a new appointment with a different institution if needed
            
            🔗 MANAGE YOUR APPOINTMENTS:
            • View all your appointments: %s
            • Log in to your account: %s
            • Book a new appointment with any available institution
            
            📞 CONTACT INFORMATION:
            • Hospital: %s
            • VakaPo Support: support@vakapo.com
            • Phone: +94 11 234 5678
            
            💡 IMPORTANT REMINDER:
            • Please contact the hospital directly for more information about this cancellation
            • The hospital will provide you with specific details about the issues and next steps
            • You can book a new appointment with any available institution at any time
            • We are here to help you find alternative vaccination options if needed
            
            We sincerely apologize for this inconvenience and appreciate your understanding.
            
            Best regards,
            VakaPo Team
            
            ---
            This is an automated notification message. Please do not reply to this email.
            """, patientName, appointmentId, vaccineName, appointmentDate, timeSlot, 
            institutionName, institutionType, doctorName, appointmentsUrl, loginUrl, institutionName);
    }

    /**
     * Send appointment reversal confirmation email to patient
     */
    public void sendAppointmentReversalConfirmation(String patientEmail, String patientName, 
                                                  String vaccineName, String institutionName, 
                                                  String institutionType, String doctorName,
                                                  String appointmentDate, String timeSlot, 
                                                  Long appointmentId) {
        try {
            log.info("EmailService: Starting to send appointment reversal confirmation to: {}", patientEmail);
            
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(fromEmail);
            message.setTo(patientEmail);
            message.setSubject("✅ Appointment Rescheduled - " + vaccineName);
            
            String emailBody = buildAppointmentReversalConfirmationEmailBody(patientName, vaccineName, 
                                                                           institutionName, institutionType, 
                                                                           doctorName, appointmentDate, timeSlot, 
                                                                           appointmentId);
            message.setText(emailBody);
            
            mailSender.send(message);
            log.info("EmailService: Appointment reversal confirmation sent successfully to: {}", patientEmail);
        } catch (Exception e) {
            log.error("EmailService: Failed to send appointment reversal confirmation to: {}", patientEmail, e);
            throw new RuntimeException("Failed to send appointment reversal confirmation", e);
        }
    }

    /**
     * Build appointment reversal confirmation email body
     */
    private String buildAppointmentReversalConfirmationEmailBody(String patientName, String vaccineName, 
                                                              String institutionName, String institutionType, 
                                                              String doctorName, String appointmentDate, 
                                                              String timeSlot, Long appointmentId) {
        String baseUrl = "http://localhost:8080"; // You can make this configurable
        String loginUrl = baseUrl + "/login";
        String appointmentsUrl = baseUrl + "/patient/appointments";
        
        return String.format("""
            Dear %s,
            
            ✅ Great news! Your previously cancelled vaccination appointment has been rescheduled.
            
            📋 RESCHEDULED APPOINTMENT DETAILS:
            • Appointment ID: #%d
            • Vaccine: %s
            • Date: %s
            • Time: %s
            • Location: %s (%s)
            • Doctor: %s
            
            ✅ IMPORTANT INFORMATION:
            • Your appointment has been successfully rescheduled
            • The new time slot has been automatically assigned
            • Please note the updated time for your appointment
            • You will receive a reminder before your appointment
            
            🔄 WHAT HAPPENED:
            • Your previous appointment was cancelled due to some issues
            • The hospital has now resolved those issues
            • We have automatically found the next available time slot for you
            • Your appointment is now confirmed and ready
            
            📅 IMPORTANT REMINDERS:
            • Please arrive 15 minutes before your scheduled time
            • Bring a valid ID and any relevant medical documents
            • If you need to reschedule or cancel, please do so at least 24 hours in advance
            • Contact the institution directly if you have any questions about the appointment
            
            🔗 MANAGE YOUR APPOINTMENTS:
            • View all your appointments: %s
            • Log in to your account: %s
            • Book additional appointments if needed
            
            📞 CONTACT INFORMATION:
            • Institution: %s
            • VakaPo Support: support@vakapo.com
            • Phone: +94 11 234 5678
            
            💡 HEALTH TIPS:
            • Get a good night's sleep before your appointment
            • Stay hydrated and eat a light meal
            • Wear comfortable clothing with easy access to your arm
            • Bring your vaccination record if you have one
            
            Thank you for your patience and understanding. We're excited to help you with your vaccination!
            
            Best regards,
            VakaPo Team
            
            ---
            This is an automated rescheduling confirmation message. Please do not reply to this email.
            """, patientName, appointmentId, vaccineName, appointmentDate, timeSlot, 
            institutionName, institutionType, doctorName, appointmentsUrl, loginUrl, institutionName);
    }

    /**
     * Send feedback approval notification email
     */
    public void sendFeedbackApprovalNotification(String toEmail, String userName, String userType, String feedbackMessage) {
        try {
            String subject = "🎉 Your Feedback Has Been Approved!";
            String emailBody = buildFeedbackApprovalEmailBody(userName, userType, feedbackMessage);
            
            sendSimpleEmail(toEmail, subject, emailBody);
            log.info("Feedback approval email sent successfully to: {}", toEmail);
        } catch (Exception e) {
            log.error("Failed to send feedback approval email to: {}", toEmail, e);
            throw new RuntimeException("Failed to send feedback approval email", e);
        }
    }
    
    /**
     * Build feedback approval email body
     */
    private String buildFeedbackApprovalEmailBody(String userName, String userType, String feedbackMessage) {
        return String.format("""
            Dear %s,
            
            🎉 Great news! Your feedback has been approved and is now visible on our platform.
            
            📝 YOUR FEEDBACK:
            "%s"
            
            ✅ WHAT THIS MEANS:
            • Your feedback has been reviewed by our admin team
            • It is now displayed on our landing pages for other users to see
            • Your valuable input helps improve our vaccination platform
            • Thank you for taking the time to share your experience
            
            🌟 YOUR IMPACT:
            • Your feedback helps other %s users make informed decisions
            • It contributes to the continuous improvement of VakaPo
            • Your voice matters in shaping the future of vaccination management
            
            🔗 STAY CONNECTED:
            • Continue using VakaPo for your vaccination needs
            • Share your experience with others
            • Keep providing feedback to help us improve
            
            📞 SUPPORT:
            • If you have any questions, contact us at support@vakapo.com
            • Visit our help center for more information
            • We're here to help with any vaccination-related needs
            
            Thank you for being part of the VakaPo community!
            
            Best regards,
            VakaPo Administration Team
            
            ---
            This is an automated notification. Please do not reply to this email.
            """, userName, feedbackMessage, userType);
    }
    
    /**
     * Send feedback rejection notification to user
     */
    public void sendFeedbackRejectionNotification(String toEmail, String userName, String userType, 
                                                 String feedbackMessage, String adminResponse) {
        try {
            String subject = "📝 Feedback Review Update - VakaPo";
            String emailBody = buildFeedbackRejectionEmailBody(userName, userType, feedbackMessage, adminResponse);
            
            sendSimpleEmail(toEmail, subject, emailBody);
            log.info("Feedback rejection email sent successfully to: {}", toEmail);
        } catch (Exception e) {
            log.error("Failed to send feedback rejection email to: {}", toEmail, e);
            throw new RuntimeException("Failed to send feedback rejection email", e);
        }
    }
    
    /**
     * Build feedback rejection email body
     */
    private String buildFeedbackRejectionEmailBody(String userName, String userType, String feedbackMessage, String adminResponse) {
        return String.format("""
            Dear %s,
            
            Thank you for taking the time to provide feedback to VakaPo. We have reviewed your submission and unfortunately, it does not meet our current guidelines for publication.
            
            📝 YOUR FEEDBACK:
            "%s"
            
            💬 ADMIN RESPONSE:
            %s
            
            🔍 WHY FEEDBACK MIGHT BE REJECTED:
            • Content that violates our community guidelines
            • Inappropriate language or tone
            • Spam or promotional content
            • Personal information that should remain private
            • Feedback that doesn't relate to vaccination services
            
            💡 HOW TO IMPROVE:
            • Keep feedback constructive and professional
            • Focus on your experience with vaccination services
            • Avoid personal attacks or inappropriate language
            • Ensure your feedback is relevant to VakaPo services
            
            🔄 SUBMIT NEW FEEDBACK:
            • You can always submit new feedback that meets our guidelines
            • We value your input and want to hear about your experiences
            • Constructive feedback helps us improve our services
            
            📞 NEED HELP?
            • If you have questions about this decision, contact us at support@vakapo.com
            • We're here to help with any vaccination-related needs
            • Visit our help center for more information
            
            Thank you for your understanding and continued support of VakaPo.
            
            Best regards,
            VakaPo Administration Team
            
            ---
            This is an automated notification. Please do not reply to this email.
            """, userName, feedbackMessage, adminResponse);
    }

    /**
     * Send account deletion confirmation email
     */
    public void sendAccountDeletionEmail(String toEmail, String userName, String userType) {
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(fromEmail);
            message.setTo(toEmail);
            message.setSubject("🔒 Your VakaPo Account Has Been Deleted");
            
            String emailBody = buildAccountDeletionEmailBody(userName, userType);
            message.setText(emailBody);
            
            mailSender.send(message);
            log.info("Account deletion email sent successfully to: {}", toEmail);
        } catch (Exception e) {
            log.error("Failed to send account deletion email to: {}", toEmail, e);
            throw new RuntimeException("Failed to send account deletion email", e);
        }
    }

    /**
     * Build account deletion email body
     */
    private String buildAccountDeletionEmailBody(String userName, String userType) {
        return String.format("""
            Dear %s,
            
            🔒 ACCOUNT DELETION CONFIRMATION
            
            We are writing to confirm that your VakaPo %s account has been successfully deleted as requested.
            
            📋 WHAT WAS DELETED:
            • Your %s account and all associated data
            • Your login credentials and access to the VakaPo platform
            • All personal information stored in our system
            • Your vaccination history and appointment records
            • Any feedback or reviews you submitted
            
            ⚠️ IMPORTANT INFORMATION:
            • This action is permanent and cannot be undone
            • You will no longer be able to access the VakaPo platform with this account
            • If you had any pending appointments, they have been cancelled
            • Your vaccination records are no longer accessible through our system
            
            🔄 FUTURE ACCESS:
            • If you wish to use VakaPo services again, you will need to create a new account
            • You can register again at any time using the same or different email address
            • All previous data will not be recoverable
            
            📞 NEED ASSISTANCE?
            • If you have any questions about this deletion, contact us at support@vakapo.com
            • If you believe this deletion was made in error, please contact us immediately
            • For general inquiries, visit our help center
            
            💙 THANK YOU:
            We appreciate the time you spent with VakaPo and hope our vaccination services were helpful to you.
            
            Best regards,
            VakaPo Administration Team
            
            ---
            This is an automated confirmation email. Please do not reply to this email.
            """, userName, userType, userType);
    }

    /**
     * Send appointment cancellation email to patient
     */
    public void sendAppointmentCancellationEmail(String patientEmail, String patientName, 
                                                String doctorOrInstitutionName, 
                                                java.time.LocalDate appointmentDate, 
                                                String timeSlot, String reason) {
        try {
            log.info("EmailService: Starting to send appointment cancellation email to: {}", patientEmail);
            
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(fromEmail);
            message.setTo(patientEmail);
            message.setSubject("❌ Your Vaccination Appointment Has Been Cancelled");
            
            String emailBody = buildAppointmentCancellationEmailBody(
                patientName, doctorOrInstitutionName, appointmentDate, timeSlot, reason);
            message.setText(emailBody);
            
            mailSender.send(message);
            log.info("EmailService: Appointment cancellation email sent successfully to: {}", patientEmail);
        } catch (Exception e) {
            log.error("EmailService: Failed to send appointment cancellation email to: {}", patientEmail, e);
            throw new RuntimeException("Failed to send appointment cancellation email", e);
        }
    }

    /**
     * Build appointment cancellation email body
     */
    private String buildAppointmentCancellationEmailBody(String patientName, String doctorOrInstitutionName, 
                                                        java.time.LocalDate appointmentDate, 
                                                        String timeSlot, String reason) {
        return String.format("""
            Dear %s,
            
            ❌ APPOINTMENT CANCELLATION NOTICE
            
            We regret to inform you that your vaccination appointment has been cancelled.
            
            📅 APPOINTMENT DETAILS:
            • Date: %s
            • Time: %s
            • Doctor/Institution: %s
            • Reason: %s
            
            🔄 NEXT STEPS:
            • Please book a new appointment as soon as possible
            • You can reschedule through the VakaPo platform
            • Contact the institution directly if you have questions
            • Your vaccination schedule may need to be adjusted
            
            📞 NEED HELP?
            • Visit our help center for booking assistance
            • Contact us at support@vakapo.com
            • Call the institution directly for urgent matters
            
            💙 WE APOLOGIZE:
            We understand this cancellation may cause inconvenience. We're working to ensure 
            minimal disruption to your vaccination schedule.
            
            Best regards,
            VakaPo Team
            
            ---
            This is an automated notification. Please do not reply to this email.
            """, patientName, appointmentDate, timeSlot, doctorOrInstitutionName, reason);
    }

    /**
     * Send a simple email with subject and body
     */
    public void sendSimpleEmail(String toEmail, String subject, String body) {
        try {
            log.info("EmailService: Starting to send email to: {}", toEmail);

            // Check if body contains HTML content
            boolean isHtml = body.contains("<!DOCTYPE html>") || body.contains("<html>") || body.contains("<div");
            
            if (isHtml) {
                // Send HTML email
                MimeMessage mimeMessage = mailSender.createMimeMessage();
                MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, true, "UTF-8");
                
                helper.setFrom(fromEmail);
                helper.setTo(toEmail);
                helper.setSubject(subject);
                helper.setText(body, true); // true indicates HTML content
                
                mailSender.send(mimeMessage);
                log.info("EmailService: HTML email sent successfully to: {}", toEmail);
            } else {
                // Send plain text email
                SimpleMailMessage message = new SimpleMailMessage();
                message.setFrom(fromEmail);
                message.setTo(toEmail);
                message.setSubject(subject);
                message.setText(body);

                mailSender.send(message);
                log.info("EmailService: Plain text email sent successfully to: {}", toEmail);
            }
        } catch (MessagingException e) {
            log.error("EmailService: Failed to send email to: {}", toEmail, e);
            throw new RuntimeException("Failed to send email", e);
        } catch (Exception e) {
            log.error("EmailService: Failed to send email to: {}", toEmail, e);
            throw new RuntimeException("Failed to send email", e);
        }
    }

    /**
     * Send email with PDF attachment
     */
    public void sendEmailWithAttachment(String toEmail, String subject, String body, 
                                      byte[] pdfAttachment, String attachmentFileName) {
        try {
            log.info("EmailService: Starting to send email with PDF attachment to: {}", toEmail);

            MimeMessage mimeMessage = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, true, "UTF-8");
            
            helper.setFrom(fromEmail);
            helper.setTo(toEmail);
            helper.setSubject(subject);
            helper.setText(body, true); // true indicates HTML content
            
            // Add PDF attachment
            helper.addAttachment(attachmentFileName, new ByteArrayResource(pdfAttachment), "application/pdf");
            
            mailSender.send(mimeMessage);
            log.info("EmailService: Email with PDF attachment sent successfully to: {}", toEmail);
            
        } catch (MessagingException e) {
            log.error("EmailService: Failed to send email with attachment to: {}", toEmail, e);
            throw new RuntimeException("Failed to send email with attachment", e);
        } catch (Exception e) {
            log.error("EmailService: Failed to send email with attachment to: {}", toEmail, e);
            throw new RuntimeException("Failed to send email with attachment", e);
        }
    }

    /**
     * Send vaccination confirmation email with vaccination record card PDF
     */
    public void sendVaccinationConfirmationWithCard(String patientEmail, String patientName, 
                                                   String vaccineName, String vaccinationDate, 
                                                   String location, String doctorName, 
                                                   String nurseName, byte[] vaccinationCardPdf) {
        try {
            String subject = "🎉 Vaccination Confirmed - Your Official Vaccination Record";
            String attachmentFileName = "Vaccination_Record_" + patientName.replaceAll("\\s+", "_") + "_" + 
                                      vaccinationDate.replaceAll("-", "") + ".pdf";
            
            StringBuilder emailBody = new StringBuilder();
            
            // HTML-styled email body
            emailBody.append("<!DOCTYPE html>");
            emailBody.append("<html><head><meta charset='UTF-8'>");
            emailBody.append("<style>");
            emailBody.append("body { font-family: 'Segoe UI', Tahoma, Geneva, Verdana, sans-serif; line-height: 1.6; color: #333; margin: 0; padding: 20px; background-color: #f8f9fa; }");
            emailBody.append(".container { max-width: 600px; margin: 0 auto; background: white; border-radius: 10px; box-shadow: 0 4px 6px rgba(0,0,0,0.1); overflow: hidden; }");
            emailBody.append(".header { background: linear-gradient(135deg, #28a745 0%, #20c997 100%); color: white; padding: 30px; text-align: center; }");
            emailBody.append(".header h1 { margin: 0; font-size: 28px; font-weight: 300; }");
            emailBody.append(".header p { margin: 10px 0 0 0; opacity: 0.9; font-size: 16px; }");
            emailBody.append(".content { padding: 30px; }");
            emailBody.append(".greeting { font-size: 18px; margin-bottom: 20px; color: #2c3e50; }");
            emailBody.append(".success-message { background: #d4edda; border: 1px solid #c3e6cb; color: #155724; padding: 15px; border-radius: 5px; margin-bottom: 25px; }");
            emailBody.append(".details-card { background: #f8f9fa; border: 1px solid #e9ecef; border-radius: 8px; padding: 20px; margin: 20px 0; }");
            emailBody.append(".details-title { font-size: 20px; font-weight: 600; color: #495057; margin-bottom: 15px; border-bottom: 2px solid #28a745; padding-bottom: 8px; }");
            emailBody.append(".detail-row { display: flex; margin-bottom: 12px; padding: 8px 0; border-bottom: 1px solid #e9ecef; }");
            emailBody.append(".detail-row:last-child { border-bottom: none; }");
            emailBody.append(".detail-label { font-weight: 600; color: #495057; min-width: 120px; }");
            emailBody.append(".detail-value { color: #6c757d; flex: 1; }");
            emailBody.append(".pdf-notice { background: #e7f3ff; border: 1px solid #b3d9ff; color: #004085; padding: 15px; border-radius: 5px; margin: 20px 0; }");
            emailBody.append(".pdf-notice h3 { margin: 0 0 10px 0; color: #004085; }");
            emailBody.append(".footer { background: #f8f9fa; padding: 20px; text-align: center; border-top: 1px solid #e9ecef; }");
            emailBody.append(".footer p { margin: 5px 0; color: #6c757d; }");
            emailBody.append(".logo { font-size: 24px; font-weight: bold; color: #28a745; }");
            emailBody.append("</style></head><body>");
            
            emailBody.append("<div class='container'>");
            emailBody.append("<div class='header'>");
            emailBody.append("<h1>🎉 Vaccination Confirmed!</h1>");
            emailBody.append("<p>Your vaccination has been successfully processed</p>");
            emailBody.append("</div>");
            
            emailBody.append("<div class='content'>");
            emailBody.append("<div class='greeting'>Dear ").append(patientName).append(",</div>");
            
            emailBody.append("<div class='success-message'>");
            emailBody.append("✅ <strong>Congratulations!</strong> Your vaccination has been successfully confirmed by our nursing staff and is now part of your official medical records.");
            emailBody.append("</div>");
            
            emailBody.append("<div class='details-card'>");
            emailBody.append("<div class='details-title'>📋 Vaccination Details</div>");
            
            emailBody.append("<div class='detail-row'>");
            emailBody.append("<div class='detail-label'>💉 Vaccine:</div>");
            emailBody.append("<div class='detail-value'>").append(vaccineName).append("</div>");
            emailBody.append("</div>");
            
            emailBody.append("<div class='detail-row'>");
            emailBody.append("<div class='detail-label'>📅 Date:</div>");
            emailBody.append("<div class='detail-value'>").append(vaccinationDate).append("</div>");
            emailBody.append("</div>");
            
            emailBody.append("<div class='detail-row'>");
            emailBody.append("<div class='detail-label'>🏥 Location:</div>");
            emailBody.append("<div class='detail-value'>").append(location).append("</div>");
            emailBody.append("</div>");
            
            emailBody.append("<div class='detail-row'>");
            emailBody.append("<div class='detail-label'>👨‍⚕️ Doctor:</div>");
            emailBody.append("<div class='detail-value'>").append(doctorName).append("</div>");
            emailBody.append("</div>");
            
            emailBody.append("<div class='detail-row'>");
            emailBody.append("<div class='detail-label'>👩‍⚕️ Nurse:</div>");
            emailBody.append("<div class='detail-value'>").append(nurseName).append("</div>");
            emailBody.append("</div>");
            
            emailBody.append("</div>");
            
            emailBody.append("<div class='pdf-notice'>");
            emailBody.append("<h3>📄 Official Vaccination Record</h3>");
            emailBody.append("<p><strong>Your official vaccination record card is attached to this email.</strong></p>");
            emailBody.append("<p>This document contains:</p>");
            emailBody.append("<ul>");
            emailBody.append("<li>Your personal information</li>");
            emailBody.append("<li>Complete vaccination details</li>");
            emailBody.append("<li>Healthcare provider signatures</li>");
            emailBody.append("<li>Official verification stamp</li>");
            emailBody.append("</ul>");
            emailBody.append("<p><strong>Important:</strong> Please save this PDF file and keep it safe. You may need it for travel, work, or future medical appointments.</p>");
            emailBody.append("</div>");
            
            emailBody.append("<div style='margin: 25px 0; padding: 20px; background: #f8f9fa; border-radius: 8px; text-align: center;'>");
            emailBody.append("<h3 style='color: #495057; margin-top: 0;'>Thank you for choosing VakaPo!</h3>");
            emailBody.append("<p style='color: #6c757d; margin-bottom: 0;'>We're committed to keeping you healthy and safe. Your vaccination record has been updated in our system.</p>");
            emailBody.append("</div>");
            
            emailBody.append("</div>");
            
            emailBody.append("<div class='footer'>");
            emailBody.append("<div class='logo'>VakaPo</div>");
            emailBody.append("<p>Making Vaccination Simple & Secure</p>");
            emailBody.append("<p>Best regards,<br><strong>VakaPo Healthcare Team</strong></p>");
            emailBody.append("<p style='font-size: 12px; color: #adb5bd;'>This is an automated message. Please do not reply to this email.</p>");
            emailBody.append("</div>");
            
            emailBody.append("</div></body></html>");
            
            sendEmailWithAttachment(patientEmail, subject, emailBody.toString(), vaccinationCardPdf, attachmentFileName);
            log.info("Vaccination confirmation email with PDF card sent to: {}", patientEmail);
            
        } catch (Exception e) {
            log.error("Error sending vaccination confirmation email with PDF card: {}", e.getMessage(), e);
            throw e;
        }
    }

    /**
     * Send vaccine added notification to all hospitals and clinics
     */
    public void sendVaccineAddedNotification(String vaccineName) {
        try {
            log.info("EmailService: Starting to send vaccine added notification for: {}", vaccineName);
            
            // Note: This method will be called from the service layer with the list of recipients
            log.info("EmailService: Vaccine added notification prepared for: {}", vaccineName);
            
        } catch (Exception e) {
            log.error("EmailService: Failed to prepare vaccine added notification: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to prepare vaccine added notification", e);
        }
    }

    /**
     * Send vaccine deleted notification to all hospitals and clinics
     */
    public void sendVaccineDeletedNotification(String vaccineName) {
        try {
            log.info("EmailService: Starting to send vaccine deleted notification for: {}", vaccineName);
            
            // Note: This method will be called from the service layer with the list of recipients
            log.info("EmailService: Vaccine deleted notification prepared for: {}", vaccineName);
            
        } catch (Exception e) {
            log.error("EmailService: Failed to prepare vaccine deleted notification: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to prepare vaccine deleted notification", e);
        }
    }

    /**
     * Send vaccine status changed notification to all hospitals and clinics
     */
    public void sendVaccineStatusChangedNotification(String vaccineName, boolean isActive) {
        try {
            log.info("EmailService: Starting to send vaccine status changed notification for: {}", vaccineName);
            
            String status = isActive ? "activated" : "deactivated";
            
            // Note: This method will be called from the service layer with the list of recipients
            log.info("EmailService: Vaccine status changed notification prepared for: {} - {}", vaccineName, status);
            
        } catch (Exception e) {
            log.error("EmailService: Failed to prepare vaccine status changed notification: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to prepare vaccine status changed notification", e);
        }
    }

    /**
     * Send vaccine notification to a specific recipient
     */
    public void sendVaccineNotificationToRecipient(String recipientEmail, String subject, String body) {
        try {
            log.info("EmailService: Sending vaccine notification to: {}", recipientEmail);
            
            sendSimpleEmail(recipientEmail, subject, body);
            log.info("EmailService: Vaccine notification sent successfully to: {}", recipientEmail);
            
        } catch (Exception e) {
            log.error("EmailService: Failed to send vaccine notification to: {}", recipientEmail, e);
            throw new RuntimeException("Failed to send vaccine notification", e);
        }
    }

    /**
     * Build vaccine added email body
     */
    private String buildVaccineAddedEmailBody(String vaccineName) {
        return String.format("""
            Dear Healthcare Provider,
            
            🆕 NEW VACCINE ADDED TO VAKAPO SYSTEM
            
            We are pleased to inform you that a new vaccine has been added to the VakaPo vaccination management system.
            
            📋 VACCINE DETAILS:
            • Vaccine Name: %s
            • Status: Active
            • Added Date: %s
            • Available For: All healthcare institutions
            
            🎯 WHAT THIS MEANS FOR YOU:
            • You can now offer this vaccine to your patients
            • The vaccine is available for appointment booking
            • It will appear in your vaccine selection dropdown
            • You can create vaccination schedules for this vaccine
            
            🔗 NEXT STEPS:
            1. Log in to your VakaPo dashboard
            2. Go to your appointment management section
            3. The new vaccine will be available in the vaccine dropdown
            4. You can start booking appointments for this vaccine immediately
            
            📞 SUPPORT:
            • If you have any questions about this new vaccine, contact us at support@vakapo.com
            • For technical assistance, visit our help center
            • We're here to help you provide the best vaccination services
            
            Thank you for using VakaPo for your vaccination management needs!
            
            Best regards,
            VakaPo Administration Team
            
            ---
            This is an automated notification. Please do not reply to this email.
            """, vaccineName, java.time.LocalDate.now().toString());
    }

    /**
     * Build vaccine deleted email body
     */
    private String buildVaccineDeletedEmailBody(String vaccineName) {
        return String.format("""
            Dear Healthcare Provider,
            
            🗑️ VACCINE REMOVED FROM VAKAPO SYSTEM
            
            This is to inform you that a vaccine has been removed from the VakaPo vaccination management system.
            
            📋 VACCINE DETAILS:
            • Vaccine Name: %s
            • Status: Removed
            • Removal Date: %s
            • Effective Immediately
            
            ⚠️ IMPORTANT NOTICE:
            • This vaccine is no longer available for new appointments
            • Existing appointments for this vaccine may need to be rescheduled
            • Please review your upcoming appointments and make necessary changes
            • Contact patients if their appointments are affected
            
            🔄 RECOMMENDED ACTIONS:
            1. Log in to your VakaPo dashboard
            2. Review your upcoming appointments
            3. Reschedule any appointments for the removed vaccine
            4. Inform affected patients about the change
            5. Consider alternative vaccines if available
            
            📞 SUPPORT:
            • If you need assistance with rescheduling appointments, contact us at support@vakapo.com
            • For questions about alternative vaccines, consult with your medical team
            • We're here to help you manage this transition smoothly
            
            We apologize for any inconvenience this may cause and appreciate your understanding.
            
            Best regards,
            VakaPo Administration Team
            
            ---
            This is an automated notification. Please do not reply to this email.
            """, vaccineName, java.time.LocalDate.now().toString());
    }

    /**
     * Build vaccine status changed email body
     */
    private String buildVaccineStatusChangedEmailBody(String vaccineName, boolean isActive) {
        String status = isActive ? "activated" : "deactivated";
        String statusEmoji = isActive ? "✅" : "❌";
        
        return String.format("""
            Dear Healthcare Provider,
            
            %s VACCINE STATUS CHANGED - %s
            
            This is to inform you that the status of a vaccine in the VakaPo system has been updated.
            
            📋 VACCINE DETAILS:
            • Vaccine Name: %s
            • New Status: %s
            • Change Date: %s
            • Effective: Immediately
            
            %s WHAT THIS MEANS:
            %s
            
            🔗 NEXT STEPS:
            1. Log in to your VakaPo dashboard
            2. Review your vaccination schedules
            3. Update your appointment availability if needed
            4. Inform your staff about the status change
            
            📞 SUPPORT:
            • If you have questions about this status change, contact us at support@vakapo.com
            • For technical assistance, visit our help center
            • We're here to help you manage your vaccination services effectively
            
            Thank you for using VakaPo for your vaccination management needs!
            
            Best regards,
            VakaPo Administration Team
            
            ---
            This is an automated notification. Please do not reply to this email.
            """, 
            statusEmoji, vaccineName, vaccineName, status, java.time.LocalDate.now().toString(),
            isActive ? 
                "• The vaccine is now available for new appointments\n• You can create vaccination schedules for this vaccine\n• Patients can book appointments for this vaccine" :
                "• The vaccine is no longer available for new appointments\n• Existing appointments may need to be reviewed\n• Consider alternative vaccines if needed");
    }
}
