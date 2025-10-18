package lk.vakapo.vakapo.UserManagement.service;

import lk.vakapo.vakapo.UserManagement.model.NotificationFeedback;
import lk.vakapo.vakapo.UserManagement.model.UserAccount;
import lk.vakapo.vakapo.UserManagement.model.Staff;
import lk.vakapo.vakapo.UserManagement.repository.NotificationFeedbackRepository;
import lk.vakapo.vakapo.UserManagement.repository.UserRepository;
import lk.vakapo.vakapo.UserManagement.repository.StaffRepository;
import lk.vakapo.vakapo.MailManagement.EmailService;
import lk.vakapo.vakapo.UserManagement.service.UserNotificationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class NotificationFeedbackService {

    private final NotificationFeedbackRepository notificationFeedbackRepository;
    private final UserRepository userRepository;
    private final StaffRepository staffRepository;
    private final EmailService emailService;
    private final UserNotificationService userNotificationService;

    /**
     * Create a new notification and send it to target audience
     */
    @Transactional
    public NotificationFeedback createNotification(String title, String type, String message, String targetAudience, String createdBy) {
        try {
            log.info("Creating notification: {} for audience: {}", title, targetAudience);
            
            // If this is an emergency notification, deactivate all previous emergency notifications
            if ("emergency".equalsIgnoreCase(type)) {
                deactivateOldEmergencyNotifications();
            }
            
            NotificationFeedback notification = new NotificationFeedback();
            notification.setTitle(title);
            notification.setType(type);
            notification.setMessage(message);
            notification.setTargetAudience(targetAudience);
            notification.setCreatedBy(createdBy);
            notification.setIsActive(true);
            notification.setCreatedAt(LocalDateTime.now());
            notification.setUpdatedAt(LocalDateTime.now());
            
            NotificationFeedback savedNotification = notificationFeedbackRepository.save(notification);
            log.info("Notification created successfully with ID: {}", savedNotification.getId());
            
            // Send notification to target audience (non-transactional)
            try {
                sendNotificationToAudience(savedNotification, false);
            } catch (Exception e) {
                log.error("Failed to send email notifications, but notification was created: {}", e.getMessage(), e);
                // Don't fail the entire operation if email sending fails
            }
            
            // Create user notifications for in-app display (non-transactional)
            try {
                userNotificationService.createUserNotifications(savedNotification.getId(), title, message, targetAudience);
            } catch (Exception e) {
                log.error("Failed to create user notifications, but notification was created: {}", e.getMessage(), e);
                // Don't fail the entire operation if user notification creation fails
            }
            
            return savedNotification;
            
        } catch (Exception e) {
            log.error("Error creating notification: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to create notification: " + e.getMessage(), e);
        }
    }

    /**
     * Get all active notifications
     */
    public List<NotificationFeedback> getAllActiveNotifications() {
        try {
            return notificationFeedbackRepository.findByIsActiveTrueOrderByCreatedAtDesc();
        } catch (Exception e) {
            log.error("Error fetching active notifications: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to fetch notifications: " + e.getMessage(), e);
        }
    }

    /**
     * Get notifications for specific audience
     */
    public List<NotificationFeedback> getNotificationsForAudience(String audience) {
        try {
            return notificationFeedbackRepository.findActiveNotificationsForAudience(audience);
        } catch (Exception e) {
            log.error("Error fetching notifications for audience {}: {}", audience, e.getMessage(), e);
            throw new RuntimeException("Failed to fetch notifications for audience: " + e.getMessage(), e);
        }
    }

    /**
     * Get notification by ID
     */
    public Optional<NotificationFeedback> getNotificationById(Long id) {
        try {
            return notificationFeedbackRepository.findById(id);
        } catch (Exception e) {
            log.error("Error fetching notification with ID {}: {}", id, e.getMessage(), e);
            throw new RuntimeException("Failed to fetch notification: " + e.getMessage(), e);
        }
    }

    /**
     * Update notification and resend emails
     */
    @Transactional
    public NotificationFeedback updateNotification(Long id, String title, String type, String message, String targetAudience) {
        try {
            log.info("Updating notification with ID: {}", id);
            
            Optional<NotificationFeedback> notificationOpt = notificationFeedbackRepository.findById(id);
            if (notificationOpt.isEmpty()) {
                throw new IllegalArgumentException("Notification not found with ID: " + id);
            }
            
            NotificationFeedback notification = notificationOpt.get();
            notification.setTitle(title);
            notification.setType(type);
            notification.setMessage(message);
            notification.setTargetAudience(targetAudience);
            notification.setUpdatedAt(LocalDateTime.now());
            
            NotificationFeedback updatedNotification = notificationFeedbackRepository.save(notification);
            log.info("Notification updated successfully with ID: {}", updatedNotification.getId());
            
            // Resend notification to target audience (non-transactional)
            try {
                log.info("Resending notification emails after update for ID: {}", id);
                sendNotificationToAudience(updatedNotification, true);
            } catch (Exception e) {
                log.error("Failed to resend email notifications, but notification was updated: {}", e.getMessage(), e);
                // Don't fail the entire operation if email sending fails
            }
            
            // Update user notifications for in-app display (non-transactional)
            try {
                userNotificationService.createUserNotifications(updatedNotification.getId(), title, message, targetAudience);
            } catch (Exception e) {
                log.error("Failed to update user notifications, but notification was updated: {}", e.getMessage(), e);
                // Don't fail the entire operation if user notification creation fails
            }
            
            return updatedNotification;
            
        } catch (IllegalArgumentException e) {
            throw e;
        } catch (Exception e) {
            log.error("Error updating notification with ID {}: {}", id, e.getMessage(), e);
            throw new RuntimeException("Failed to update notification: " + e.getMessage(), e);
        }
    }

    /**
     * Delete notification (soft delete by setting isActive to false)
     */
    @Transactional
    public void deleteNotification(Long id) {
        try {
            log.info("Deleting notification with ID: {}", id);
            
            Optional<NotificationFeedback> notificationOpt = notificationFeedbackRepository.findById(id);
            if (notificationOpt.isEmpty()) {
                throw new IllegalArgumentException("Notification not found with ID: " + id);
            }
            
            NotificationFeedback notification = notificationOpt.get();
            notification.setIsActive(false);
            notification.setUpdatedAt(LocalDateTime.now());
            
            notificationFeedbackRepository.save(notification);
            log.info("Notification deleted successfully with ID: {}", id);
            
        } catch (IllegalArgumentException e) {
            throw e;
        } catch (Exception e) {
            log.error("Error deleting notification with ID {}: {}", id, e.getMessage(), e);
            throw new RuntimeException("Failed to delete notification: " + e.getMessage(), e);
        }
    }

    /**
     * Get notification count
     */
    public long getNotificationCount() {
        try {
            return notificationFeedbackRepository.countByIsActiveTrue();
        } catch (Exception e) {
            log.error("Error getting notification count: {}", e.getMessage(), e);
            return 0;
        }
    }

    /**
     * Get latest emergency notification for announcement display
     */
    public Optional<NotificationFeedback> getLatestEmergencyNotification() {
        try {
            List<NotificationFeedback> emergencyNotifications = notificationFeedbackRepository.findEmergencyNotifications();
            if (emergencyNotifications.isEmpty()) {
                return Optional.empty();
            }
            // Return the first (most recent) emergency notification
            return Optional.of(emergencyNotifications.get(0));
        } catch (Exception e) {
            log.error("Error fetching latest emergency notification: {}", e.getMessage(), e);
            return Optional.empty();
        }
    }

    /**
     * Deactivate all old emergency notifications when a new one is created
     */
    @Transactional
    private void deactivateOldEmergencyNotifications() {
        try {
            List<NotificationFeedback> oldEmergencyNotifications = notificationFeedbackRepository.findAllEmergencyNotifications();
            for (NotificationFeedback notification : oldEmergencyNotifications) {
                notification.setIsActive(false);
                notification.setUpdatedAt(LocalDateTime.now());
                notificationFeedbackRepository.save(notification);
                log.info("Deactivated old emergency notification with ID: {}", notification.getId());
            }
            if (!oldEmergencyNotifications.isEmpty()) {
                log.info("Deactivated {} old emergency notifications", oldEmergencyNotifications.size());
            }
        } catch (Exception e) {
            log.error("Error deactivating old emergency notifications: {}", e.getMessage(), e);
            // Don't throw exception as this shouldn't prevent creating new notification
        }
    }

    /**
     * Send notification to target audience via email
     */
    private void sendNotificationToAudience(NotificationFeedback notification, boolean isUpdate) {
        try {
            String action = isUpdate ? "Resending updated notification" : "Sending notification";
            log.info("{} to audience: {}", action, notification.getTargetAudience());
            
            String[] targetAudiences = notification.getTargetAudience().split(",");
            int emailsSent = 0;
            
            for (String target : targetAudiences) {
                target = target.trim();
                
                if ("ALL".equals(target)) {
                    // Send to all users
                    emailsSent += sendToAllUsers(notification, isUpdate);
                } else if ("DOCTORS".equals(target)) {
                    // Send to all doctors
                    emailsSent += sendToDoctors(notification, isUpdate);
                } else if ("NURSES".equals(target)) {
                    // Send to all nurses
                    emailsSent += sendToNurses(notification, isUpdate);
                } else if ("HOSPITALS".equals(target)) {
                    // Send to all hospitals
                    emailsSent += sendToHospitals(notification, isUpdate);
                } else if ("CLINICS".equals(target)) {
                    // Send to all clinics
                    emailsSent += sendToClinics(notification, isUpdate);
                } else if ("PATIENTS".equals(target)) {
                    // Send to all patients
                    emailsSent += sendToPatients(notification, isUpdate);
                }
            }
            
            String successMessage = isUpdate ? "Updated notification sent successfully to {} recipients" : "Notification sent successfully to {} recipients";
            log.info(successMessage, emailsSent);
            
        } catch (Exception e) {
            log.error("Error sending notification to audience: {}", e.getMessage(), e);
            // Don't throw exception - email failure shouldn't prevent notification creation
        }
    }

    /**
     * Send notification to all users
     */
    private int sendToAllUsers(NotificationFeedback notification, boolean isUpdate) {
        try {
            List<UserAccount> allUsers = userRepository.findAll();
            int sentCount = 0;
            
            for (UserAccount user : allUsers) {
                try {
                    if (isUpdate) {
                        sendUpdatedNotificationEmail(user.getEmail(), user.getRole(), notification);
                    } else {
                        sendNotificationEmail(user.getEmail(), user.getRole(), notification);
                    }
                    sentCount++;
                } catch (Exception e) {
                    log.warn("Failed to send notification email to user {}: {}", user.getEmail(), e.getMessage());
                }
            }
            
            String action = isUpdate ? "Resent updated notification to" : "Sent notification to";
            log.info("{} {} users", action, sentCount);
            return sentCount;
            
        } catch (Exception e) {
            log.error("Error sending notification to all users: {}", e.getMessage(), e);
            return 0;
        }
    }

    /**
     * Send notification to all doctors
     */
    private int sendToDoctors(NotificationFeedback notification, boolean isUpdate) {
        try {
            List<Staff> doctors = staffRepository.findByRole("Doctor");
            int sentCount = 0;
            
            for (Staff doctor : doctors) {
                try {
                    if (isUpdate) {
                        sendUpdatedNotificationEmail(doctor.getEmail(), "Doctor", notification);
                    } else {
                        sendNotificationEmail(doctor.getEmail(), "Doctor", notification);
                    }
                    sentCount++;
                } catch (Exception e) {
                    log.warn("Failed to send notification email to doctor {}: {}", doctor.getEmail(), e.getMessage());
                }
            }
            
            String action = isUpdate ? "Resent updated notification to" : "Sent notification to";
            log.info("{} {} doctors", action, sentCount);
            return sentCount;
            
        } catch (Exception e) {
            log.error("Error sending notification to doctors: {}", e.getMessage(), e);
            return 0;
        }
    }

    /**
     * Send notification to all nurses
     */
    private int sendToNurses(NotificationFeedback notification, boolean isUpdate) {
        try {
            List<Staff> nurses = staffRepository.findByRole("Nurse");
            int sentCount = 0;
            
            for (Staff nurse : nurses) {
                try {
                    if (isUpdate) {
                        sendUpdatedNotificationEmail(nurse.getEmail(), "Nurse", notification);
                    } else {
                        sendNotificationEmail(nurse.getEmail(), "Nurse", notification);
                    }
                    sentCount++;
                } catch (Exception e) {
                    log.warn("Failed to send notification email to nurse {}: {}", nurse.getEmail(), e.getMessage());
                }
            }
            
            String action = isUpdate ? "Resent updated notification to" : "Sent notification to";
            log.info("{} {} nurses", action, sentCount);
            return sentCount;
            
        } catch (Exception e) {
            log.error("Error sending notification to nurses: {}", e.getMessage(), e);
            return 0;
        }
    }

    /**
     * Send notification to all hospitals
     */
    private int sendToHospitals(NotificationFeedback notification, boolean isUpdate) {
        try {
            List<UserAccount> hospitals = userRepository.findByRole("HOSPITAL");
            int sentCount = 0;
            
            for (UserAccount hospital : hospitals) {
                try {
                    if (isUpdate) {
                        sendUpdatedNotificationEmail(hospital.getEmail(), "Hospital", notification);
                    } else {
                        sendNotificationEmail(hospital.getEmail(), "Hospital", notification);
                    }
                    sentCount++;
                } catch (Exception e) {
                    log.warn("Failed to send notification email to hospital {}: {}", hospital.getEmail(), e.getMessage());
                }
            }
            
            String action = isUpdate ? "Resent updated notification to" : "Sent notification to";
            log.info("{} {} hospitals", action, sentCount);
            return sentCount;
            
        } catch (Exception e) {
            log.error("Error sending notification to hospitals: {}", e.getMessage(), e);
            return 0;
        }
    }

    /**
     * Send notification to all clinics
     */
    private int sendToClinics(NotificationFeedback notification, boolean isUpdate) {
        try {
            List<UserAccount> clinics = userRepository.findByRole("CLINIC");
            int sentCount = 0;
            
            for (UserAccount clinic : clinics) {
                try {
                    if (isUpdate) {
                        sendUpdatedNotificationEmail(clinic.getEmail(), "Clinic", notification);
                    } else {
                        sendNotificationEmail(clinic.getEmail(), "Clinic", notification);
                    }
                    sentCount++;
                } catch (Exception e) {
                    log.warn("Failed to send notification email to clinic {}: {}", clinic.getEmail(), e.getMessage());
                }
            }
            
            String action = isUpdate ? "Resent updated notification to" : "Sent notification to";
            log.info("{} {} clinics", action, sentCount);
            return sentCount;
            
        } catch (Exception e) {
            log.error("Error sending notification to clinics: {}", e.getMessage(), e);
            return 0;
        }
    }

    /**
     * Send notification to all patients
     */
    private int sendToPatients(NotificationFeedback notification, boolean isUpdate) {
        try {
            List<UserAccount> patients = userRepository.findByRole("PATIENT");
            int sentCount = 0;
            
            for (UserAccount patient : patients) {
                try {
                    if (isUpdate) {
                        sendUpdatedNotificationEmail(patient.getEmail(), "Patient", notification);
                    } else {
                        sendNotificationEmail(patient.getEmail(), "Patient", notification);
                    }
                    sentCount++;
                } catch (Exception e) {
                    log.warn("Failed to send notification email to patient {}: {}", patient.getEmail(), e.getMessage());
                }
            }
            
            String action = isUpdate ? "Resent updated notification to" : "Sent notification to";
            log.info("{} {} patients", action, sentCount);
            return sentCount;
            
        } catch (Exception e) {
            log.error("Error sending notification to patients: {}", e.getMessage(), e);
            return 0;
        }
    }

    /**
     * Send notification email to a specific user
     */
    private void sendNotificationEmail(String email, String userType, NotificationFeedback notification) {
        try {
            String subject = "🔔 VakaPo Notification: " + notification.getTitle();
            String emailBody = buildNotificationEmailBody(notification, userType);
            
            emailService.sendSimpleEmail(email, subject, emailBody);
            log.debug("Notification email sent to {} ({})", email, userType);
            
        } catch (Exception e) {
            log.error("Failed to send notification email to {}: {}", email, e.getMessage(), e);
            throw e;
        }
    }

    /**
     * Send updated notification email to a specific user
     */
    private void sendUpdatedNotificationEmail(String email, String userType, NotificationFeedback notification) {
        try {
            String subject = "🔄 VakaPo Notification Update: " + notification.getTitle();
            String emailBody = buildUpdatedNotificationEmailBody(notification, userType);
            
            emailService.sendSimpleEmail(email, subject, emailBody);
            log.debug("Updated notification email sent to {} ({})", email, userType);
            
        } catch (Exception e) {
            log.error("Failed to send updated notification email to {}: {}", email, e.getMessage(), e);
            throw e;
        }
    }

    /**
     * Build notification email body
     */
    private String buildNotificationEmailBody(NotificationFeedback notification, String userType) {
        StringBuilder body = new StringBuilder();
        
        body.append("Dear ").append(userType).append(",\n\n");
        body.append("You have received a new notification from VakaPo:\n\n");
        body.append("📢 ").append(notification.getTitle()).append("\n\n");
        body.append("Message:\n");
        body.append(notification.getMessage()).append("\n\n");
        body.append("This notification was sent on: ").append(notification.getCreatedAt()).append("\n\n");
        body.append("Please log in to your VakaPo account to view more details.\n\n");
        body.append("Best regards,\n");
        body.append("VakaPo Administration Team\n\n");
        body.append("---\n");
        body.append("This is an automated message. Please do not reply to this email.");
        
        return body.toString();
    }

    /**
     * Build updated notification email body
     */
    private String buildUpdatedNotificationEmailBody(NotificationFeedback notification, String userType) {
        StringBuilder body = new StringBuilder();
        
        body.append("Dear ").append(userType).append(",\n\n");
        body.append("A notification you previously received has been updated:\n\n");
        body.append("🔄 ").append(notification.getTitle()).append("\n\n");
        body.append("Updated Message:\n");
        body.append(notification.getMessage()).append("\n\n");
        body.append("Original notification was sent on: ").append(notification.getCreatedAt()).append("\n");
        body.append("This update was sent on: ").append(notification.getUpdatedAt()).append("\n\n");
        body.append("Please log in to your VakaPo account to view more details.\n\n");
        body.append("Best regards,\n");
        body.append("VakaPo Administration Team\n\n");
        body.append("---\n");
        body.append("This is an automated message. Please do not reply to this email.");
        
        return body.toString();
    }
}
