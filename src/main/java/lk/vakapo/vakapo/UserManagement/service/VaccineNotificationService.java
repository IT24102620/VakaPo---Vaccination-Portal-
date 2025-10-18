package lk.vakapo.vakapo.UserManagement.service;

import lk.vakapo.vakapo.MailManagement.EmailService;
import lk.vakapo.vakapo.UserManagement.model.UserAccount;
import lk.vakapo.vakapo.UserManagement.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.concurrent.CompletableFuture;

@Service
@RequiredArgsConstructor
@Slf4j
public class VaccineNotificationService {

    private final EmailService emailService;
    private final UserRepository userRepository;

    /**
     * Send vaccine added notification to all hospitals and clinics
     */
    @Transactional
    public void notifyVaccineAdded(String vaccineName) {
        try {
            log.info("VaccineNotificationService: Starting to notify about vaccine added: {}", vaccineName);
            
            // Get all hospitals and clinics
            List<UserAccount> hospitals = userRepository.findByRole("HOSPITAL");
            List<UserAccount> clinics = userRepository.findByRole("CLINIC");
            
            String subject = "🆕 New Vaccine Added to VakaPo System";
            String emailBody = buildVaccineAddedEmailBody(vaccineName);
            
            // Send notifications asynchronously to avoid blocking
            CompletableFuture.runAsync(() -> {
                sendNotificationsToInstitutions(hospitals, subject, emailBody, "hospitals");
                sendNotificationsToInstitutions(clinics, subject, emailBody, "clinics");
            });
            
            log.info("VaccineNotificationService: Vaccine added notification initiated for {} hospitals and {} clinics", 
                    hospitals.size(), clinics.size());
            
        } catch (Exception e) {
            log.error("VaccineNotificationService: Error notifying about vaccine added: {}", e.getMessage(), e);
        }
    }

    /**
     * Send vaccine deleted notification to all hospitals and clinics
     */
    @Transactional
    public void notifyVaccineDeleted(String vaccineName) {
        try {
            log.info("VaccineNotificationService: Starting to notify about vaccine deleted: {}", vaccineName);
            
            // Get all hospitals and clinics
            List<UserAccount> hospitals = userRepository.findByRole("HOSPITAL");
            List<UserAccount> clinics = userRepository.findByRole("CLINIC");
            
            String subject = "🗑️ Vaccine Removed from VakaPo System";
            String emailBody = buildVaccineDeletedEmailBody(vaccineName);
            
            // Send notifications asynchronously to avoid blocking
            CompletableFuture.runAsync(() -> {
                sendNotificationsToInstitutions(hospitals, subject, emailBody, "hospitals");
                sendNotificationsToInstitutions(clinics, subject, emailBody, "clinics");
            });
            
            log.info("VaccineNotificationService: Vaccine deleted notification initiated for {} hospitals and {} clinics", 
                    hospitals.size(), clinics.size());
            
        } catch (Exception e) {
            log.error("VaccineNotificationService: Error notifying about vaccine deleted: {}", e.getMessage(), e);
        }
    }

    /**
     * Send vaccine status changed notification to all hospitals and clinics
     */
    @Transactional
    public void notifyVaccineStatusChanged(String vaccineName, boolean isActive) {
        try {
            log.info("VaccineNotificationService: Starting to notify about vaccine status changed: {} - {}", 
                    vaccineName, isActive ? "activated" : "deactivated");
            
            // Get all hospitals and clinics
            List<UserAccount> hospitals = userRepository.findByRole("HOSPITAL");
            List<UserAccount> clinics = userRepository.findByRole("CLINIC");
            
            String subject = "🔄 Vaccine Status Changed - " + vaccineName;
            String emailBody = buildVaccineStatusChangedEmailBody(vaccineName, isActive);
            
            // Send notifications asynchronously to avoid blocking
            CompletableFuture.runAsync(() -> {
                sendNotificationsToInstitutions(hospitals, subject, emailBody, "hospitals");
                sendNotificationsToInstitutions(clinics, subject, emailBody, "clinics");
            });
            
            log.info("VaccineNotificationService: Vaccine status changed notification initiated for {} hospitals and {} clinics", 
                    hospitals.size(), clinics.size());
            
        } catch (Exception e) {
            log.error("VaccineNotificationService: Error notifying about vaccine status changed: {}", e.getMessage(), e);
        }
    }

    /**
     * Send notifications to a list of institutions
     */
    private void sendNotificationsToInstitutions(List<UserAccount> institutions, String subject, String emailBody, String institutionType) {
        try {
            int successCount = 0;
            int failureCount = 0;
            
            for (UserAccount institution : institutions) {
                try {
                    emailService.sendVaccineNotificationToRecipient(institution.getEmail(), subject, emailBody);
                    successCount++;
                    log.debug("VaccineNotificationService: Notification sent successfully to {}: {}", 
                            institutionType, institution.getEmail());
                } catch (Exception e) {
                    failureCount++;
                    log.error("VaccineNotificationService: Failed to send notification to {}: {} - Error: {}", 
                            institutionType, institution.getEmail(), e.getMessage());
                }
            }
            
            log.info("VaccineNotificationService: Notification results for {} - Success: {}, Failed: {}", 
                    institutionType, successCount, failureCount);
            
        } catch (Exception e) {
            log.error("VaccineNotificationService: Error sending notifications to {}: {}", institutionType, e.getMessage(), e);
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
