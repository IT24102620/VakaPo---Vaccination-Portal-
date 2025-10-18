package lk.vakapo.vakapo.UserManagement.service;

import lk.vakapo.vakapo.UserManagement.model.UserNotification;
import lk.vakapo.vakapo.UserManagement.model.UserAccount;
import lk.vakapo.vakapo.UserManagement.model.Staff;
import lk.vakapo.vakapo.UserManagement.repository.UserNotificationRepository;
import lk.vakapo.vakapo.UserManagement.repository.UserRepository;
import lk.vakapo.vakapo.UserManagement.repository.StaffRepository;
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
public class UserNotificationService {

    private final UserNotificationRepository userNotificationRepository;
    private final UserRepository userRepository;
    private final StaffRepository staffRepository;

    /**
     * Create user notifications from admin notification
     */
    @Transactional
    public void createUserNotifications(Long notificationId, String title, String message, String targetAudience) {
        try {
            log.info("Creating user notifications for notification ID: {} with target: {}", notificationId, targetAudience);
            
            String[] targetAudiences = targetAudience.split(",");
            int notificationsCreated = 0;
            
            for (String target : targetAudiences) {
                target = target.trim();
                
                try {
                    if ("ALL".equals(target)) {
                        notificationsCreated += createNotificationsForAllUsers(notificationId, title, message);
                    } else if ("DOCTORS".equals(target)) {
                        notificationsCreated += createNotificationsForDoctors(notificationId, title, message);
                    } else if ("NURSES".equals(target)) {
                        notificationsCreated += createNotificationsForNurses(notificationId, title, message);
                    } else if ("HOSPITALS".equals(target)) {
                        notificationsCreated += createNotificationsForHospitals(notificationId, title, message);
                    } else if ("CLINICS".equals(target)) {
                        notificationsCreated += createNotificationsForClinics(notificationId, title, message);
                    } else if ("PATIENTS".equals(target)) {
                        notificationsCreated += createNotificationsForPatients(notificationId, title, message);
                    }
                } catch (Exception e) {
                    log.error("Error creating notifications for target {}: {}", target, e.getMessage(), e);
                    // Continue with other targets even if one fails
                }
            }
            
            log.info("Created {} user notifications for notification ID: {}", notificationsCreated, notificationId);
            
        } catch (Exception e) {
            log.error("Error creating user notifications for notification ID {}: {}", notificationId, e.getMessage(), e);
            throw e; // Re-throw to be caught by the calling method
        }
    }

    /**
     * Get notifications for a user
     */
    public List<UserNotification> getUserNotifications(String userId) {
        try {
            return userNotificationRepository.findByUserIdOrderByCreatedAtDesc(userId);
        } catch (Exception e) {
            log.error("Error fetching notifications for user {}: {}", userId, e.getMessage(), e);
            return List.of();
        }
    }

    /**
     * Get notifications for a user by email
     */
    public List<UserNotification> getUserNotificationsByEmail(String userEmail) {
        try {
            return userNotificationRepository.findByUserEmailOrderByCreatedAtDesc(userEmail);
        } catch (Exception e) {
            log.error("Error fetching notifications for user email {}: {}", userEmail, e.getMessage(), e);
            return List.of();
        }
    }

    /**
     * Get unread notifications for a user
     */
    public List<UserNotification> getUnreadNotifications(String userId) {
        try {
            return userNotificationRepository.findByUserIdAndIsReadFalseOrderByCreatedAtDesc(userId);
        } catch (Exception e) {
            log.error("Error fetching unread notifications for user {}: {}", userId, e.getMessage(), e);
            return List.of();
        }
    }

    /**
     * Get unread notifications for a user by email
     */
    public List<UserNotification> getUnreadNotificationsByEmail(String userEmail) {
        try {
            return userNotificationRepository.findByUserEmailAndIsReadFalseOrderByCreatedAtDesc(userEmail);
        } catch (Exception e) {
            log.error("Error fetching unread notifications for user email {}: {}", userEmail, e.getMessage(), e);
            return List.of();
        }
    }

    /**
     * Count unread notifications for a user
     */
    public long getUnreadCount(String userId) {
        try {
            return userNotificationRepository.countByUserIdAndIsReadFalse(userId);
        } catch (Exception e) {
            log.error("Error counting unread notifications for user {}: {}", userId, e.getMessage(), e);
            return 0;
        }
    }

    /**
     * Count unread notifications for a user by email
     */
    public long getUnreadCountByEmail(String userEmail) {
        try {
            return userNotificationRepository.countByUserEmailAndIsReadFalse(userEmail);
        } catch (Exception e) {
            log.error("Error counting unread notifications for user email {}: {}", userEmail, e.getMessage(), e);
            return 0;
        }
    }

    /**
     * Mark notification as read
     */
    @Transactional
    public void markAsRead(Long notificationId) {
        try {
            userNotificationRepository.markAsRead(notificationId);
            log.info("Marked notification {} as read", notificationId);
        } catch (Exception e) {
            log.error("Error marking notification {} as read: {}", notificationId, e.getMessage(), e);
        }
    }

    /**
     * Mark all notifications as read for a user
     */
    @Transactional
    public void markAllAsRead(String userId) {
        try {
            userNotificationRepository.markAllAsReadForUser(userId);
            log.info("Marked all notifications as read for user {}", userId);
        } catch (Exception e) {
            log.error("Error marking all notifications as read for user {}: {}", userId, e.getMessage(), e);
        }
    }

    /**
     * Mark all notifications as read for a user by email
     */
    @Transactional
    public void markAllAsReadByEmail(String userEmail) {
        try {
            userNotificationRepository.markAllAsReadForUserByEmail(userEmail);
            log.info("Marked all notifications as read for user email {}", userEmail);
        } catch (Exception e) {
            log.error("Error marking all notifications as read for user email {}: {}", userEmail, e.getMessage(), e);
        }
    }

    // Private helper methods for creating notifications for different user types

    private int createNotificationsForAllUsers(Long notificationId, String title, String message) {
        try {
            List<UserAccount> allUsers = userRepository.findAll();
            int created = 0;
            
            for (UserAccount user : allUsers) {
                UserNotification userNotification = createUserNotification(notificationId, title, message, user);
                userNotificationRepository.save(userNotification);
                created++;
            }
            
            log.info("Created notifications for {} users", created);
            return created;
            
        } catch (Exception e) {
            log.error("Error creating notifications for all users: {}", e.getMessage(), e);
            return 0;
        }
    }

    private int createNotificationsForDoctors(Long notificationId, String title, String message) {
        try {
            List<Staff> doctors = staffRepository.findByRole("Doctor");
            int created = 0;
            
            for (Staff doctor : doctors) {
                // Find the corresponding user account for this doctor
                Optional<UserAccount> userOpt = userRepository.findByEmail(doctor.getEmail());
                if (userOpt.isEmpty()) {
                    log.warn("No user account found for doctor email: {}", doctor.getEmail());
                    continue;
                }
                
                UserAccount user = userOpt.get();
                UserNotification userNotification = new UserNotification();
                userNotification.setUserId(user.getId()); // Use actual user ID from Users table
                userNotification.setUserEmail(doctor.getEmail());
                userNotification.setUserRole("Doctor");
                userNotification.setNotificationId(notificationId);
                userNotification.setTitle(title);
                userNotification.setMessage(message);
                userNotification.setIsRead(false);
                userNotification.setCreatedAt(LocalDateTime.now());
                
                userNotificationRepository.save(userNotification);
                created++;
            }
            
            log.info("Created notifications for {} doctors", created);
            return created;
            
        } catch (Exception e) {
            log.error("Error creating notifications for doctors: {}", e.getMessage(), e);
            return 0;
        }
    }

    private int createNotificationsForNurses(Long notificationId, String title, String message) {
        try {
            List<Staff> nurses = staffRepository.findByRole("Nurse");
            int created = 0;
            
            for (Staff nurse : nurses) {
                // Find the corresponding user account for this nurse
                Optional<UserAccount> userOpt = userRepository.findByEmail(nurse.getEmail());
                if (userOpt.isEmpty()) {
                    log.warn("No user account found for nurse email: {}", nurse.getEmail());
                    continue;
                }
                
                UserAccount user = userOpt.get();
                UserNotification userNotification = new UserNotification();
                userNotification.setUserId(user.getId()); // Use actual user ID from Users table
                userNotification.setUserEmail(nurse.getEmail());
                userNotification.setUserRole("Nurse");
                userNotification.setNotificationId(notificationId);
                userNotification.setTitle(title);
                userNotification.setMessage(message);
                userNotification.setIsRead(false);
                userNotification.setCreatedAt(LocalDateTime.now());
                
                userNotificationRepository.save(userNotification);
                created++;
            }
            
            log.info("Created notifications for {} nurses", created);
            return created;
            
        } catch (Exception e) {
            log.error("Error creating notifications for nurses: {}", e.getMessage(), e);
            return 0;
        }
    }

    private int createNotificationsForHospitals(Long notificationId, String title, String message) {
        try {
            List<UserAccount> hospitals = userRepository.findByRole("HOSPITAL");
            int created = 0;
            
            for (UserAccount hospital : hospitals) {
                UserNotification userNotification = createUserNotification(notificationId, title, message, hospital);
                userNotificationRepository.save(userNotification);
                created++;
            }
            
            log.info("Created notifications for {} hospitals", created);
            return created;
            
        } catch (Exception e) {
            log.error("Error creating notifications for hospitals: {}", e.getMessage(), e);
            return 0;
        }
    }

    private int createNotificationsForClinics(Long notificationId, String title, String message) {
        try {
            List<UserAccount> clinics = userRepository.findByRole("CLINIC");
            int created = 0;
            
            for (UserAccount clinic : clinics) {
                UserNotification userNotification = createUserNotification(notificationId, title, message, clinic);
                userNotificationRepository.save(userNotification);
                created++;
            }
            
            log.info("Created notifications for {} clinics", created);
            return created;
            
        } catch (Exception e) {
            log.error("Error creating notifications for clinics: {}", e.getMessage(), e);
            return 0;
        }
    }

    private int createNotificationsForPatients(Long notificationId, String title, String message) {
        try {
            List<UserAccount> patients = userRepository.findByRole("PATIENT");
            int created = 0;
            
            for (UserAccount patient : patients) {
                UserNotification userNotification = createUserNotification(notificationId, title, message, patient);
                userNotificationRepository.save(userNotification);
                created++;
            }
            
            log.info("Created notifications for {} patients", created);
            return created;
            
        } catch (Exception e) {
            log.error("Error creating notifications for patients: {}", e.getMessage(), e);
            return 0;
        }
    }

    private UserNotification createUserNotification(Long notificationId, String title, String message, UserAccount user) {
        UserNotification userNotification = new UserNotification();
        userNotification.setUserId(user.getId());
        userNotification.setUserEmail(user.getEmail());
        userNotification.setUserRole(user.getRole());
        userNotification.setNotificationId(notificationId);
        userNotification.setTitle(title);
        userNotification.setMessage(message);
        userNotification.setIsRead(false);
        userNotification.setCreatedAt(LocalDateTime.now());
        return userNotification;
    }
}
