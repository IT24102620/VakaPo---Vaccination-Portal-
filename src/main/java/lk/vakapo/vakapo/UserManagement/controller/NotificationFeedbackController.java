package lk.vakapo.vakapo.UserManagement.controller;

import lk.vakapo.vakapo.UserManagement.model.NotificationFeedback;
import lk.vakapo.vakapo.UserManagement.service.NotificationFeedbackService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Controller
@RequestMapping("/admin/delegate/notify")
@RequiredArgsConstructor
@Slf4j
public class NotificationFeedbackController {

    private final NotificationFeedbackService notificationFeedbackService;

    /**
     * Display notifications page
     */
    @GetMapping("/page")
    public String notificationsPage(Model model) {
        try {
            List<NotificationFeedback> notifications = notificationFeedbackService.getAllActiveNotifications();
            model.addAttribute("notifications", notifications);
            return "admin/messages/Admin_Notification";
        } catch (Exception e) {
            log.error("Error loading notifications page: {}", e.getMessage(), e);
            model.addAttribute("error", "Failed to load notifications");
            return "admin/messages/Admin_Notification";
        }
    }

    /**
     * Create new notification
     */
    @PostMapping("/custom")
    public String createNotification(@RequestParam String title,
                                   @RequestParam String type,
                                   @RequestParam String message,
                                   @RequestParam String[] target,
                                   Model model) {
        try {
            log.info("Creating notification: {} for targets: {}", title, String.join(",", target));
            
            // Convert target array to comma-separated string
            String targetAudience = String.join(",", target);
            
            // For emergency notifications, automatically set target audience to ALL
            if ("emergency".equalsIgnoreCase(type)) {
                targetAudience = "ALL";
                log.info("Emergency notification detected - automatically setting target audience to ALL");
            }
            
            // Create notification
            NotificationFeedback notification = notificationFeedbackService.createNotification(
                title, type, message, targetAudience, "ADMIN"
            );
            
            log.info("Notification created successfully with ID: {}", notification.getId());
            
            // Reload notifications for display
            List<NotificationFeedback> notifications = notificationFeedbackService.getAllActiveNotifications();
            model.addAttribute("notifications", notifications);
            model.addAttribute("success", "Notification sent successfully! Emails have been sent to all target recipients.");
            
            return "admin/messages/Admin_Notification";
            
        } catch (Exception e) {
            log.error("Error creating notification: {}", e.getMessage(), e);
            model.addAttribute("error", "Failed to create notification: " + e.getMessage());
            
            // Still load existing notifications
            try {
                List<NotificationFeedback> notifications = notificationFeedbackService.getAllActiveNotifications();
                model.addAttribute("notifications", notifications);
            } catch (Exception ex) {
                log.error("Error loading notifications after creation failure: {}", ex.getMessage(), ex);
            }
            
            return "admin/messages/Admin_Notification";
        }
    }

    /**
     * Get notification for editing (AJAX)
     */
    @GetMapping("/{id}")
    @ResponseBody
    public ResponseEntity<?> getNotification(@PathVariable Long id) {
        try {
            Optional<NotificationFeedback> notificationOpt = notificationFeedbackService.getNotificationById(id);
            if (notificationOpt.isEmpty()) {
                return ResponseEntity.notFound().build();
            }
            
            NotificationFeedback notification = notificationOpt.get();
            Map<String, Object> response = new HashMap<>();
            response.put("id", notification.getId());
            response.put("title", notification.getTitle());
            response.put("type", notification.getType());
            response.put("message", notification.getMessage());
            response.put("targetAudience", notification.getTargetAudience());
            response.put("createdAt", notification.getCreatedAt());
            response.put("createdBy", notification.getCreatedBy());
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            log.error("Error fetching notification with ID {}: {}", id, e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("{\"error\": \"Failed to fetch notification\"}");
        }
    }

    /**
     * Update notification (AJAX)
     */
    @PutMapping("/{id}")
    @ResponseBody
    public ResponseEntity<?> updateNotification(@PathVariable Long id,
                                              @RequestParam String title,
                                              @RequestParam String type,
                                              @RequestParam String message,
                                              @RequestParam String targetAudience) {
        try {
            log.info("Updating notification with ID: {}", id);
            
            // For emergency notifications, automatically set target audience to ALL
            if ("emergency".equalsIgnoreCase(type)) {
                targetAudience = "ALL";
                log.info("Emergency notification update detected - automatically setting target audience to ALL");
            }
            
            NotificationFeedback updatedNotification = notificationFeedbackService.updateNotification(
                id, title, type, message, targetAudience
            );
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Notification updated successfully! Updated emails have been sent to all target recipients.");
            response.put("notification", updatedNotification);
            
            return ResponseEntity.ok(response);
            
        } catch (IllegalArgumentException e) {
            log.error("Invalid request for updating notification {}: {}", id, e.getMessage());
            return ResponseEntity.badRequest()
                    .body("{\"error\": \"" + e.getMessage() + "\"}");
        } catch (Exception e) {
            log.error("Error updating notification with ID {}: {}", id, e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("{\"error\": \"Failed to update notification\"}");
        }
    }

    /**
     * Delete notification (AJAX)
     */
    @DeleteMapping("/{id}")
    @ResponseBody
    public ResponseEntity<?> deleteNotification(@PathVariable Long id) {
        try {
            log.info("Deleting notification with ID: {}", id);
            
            notificationFeedbackService.deleteNotification(id);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Notification deleted successfully");
            
            return ResponseEntity.ok(response);
            
        } catch (IllegalArgumentException e) {
            log.error("Invalid request for deleting notification {}: {}", id, e.getMessage());
            return ResponseEntity.badRequest()
                    .body("{\"error\": \"" + e.getMessage() + "\"}");
        } catch (Exception e) {
            log.error("Error deleting notification with ID {}: {}", id, e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("{\"error\": \"Failed to delete notification\"}");
        }
    }

    /**
     * Get notifications for specific audience (API endpoint)
     */
    @GetMapping("/audience/{audience}")
    @ResponseBody
    public ResponseEntity<?> getNotificationsForAudience(@PathVariable String audience) {
        try {
            List<NotificationFeedback> notifications = notificationFeedbackService.getNotificationsForAudience(audience);
            return ResponseEntity.ok(notifications);
        } catch (Exception e) {
            log.error("Error fetching notifications for audience {}: {}", audience, e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("{\"error\": \"Failed to fetch notifications\"}");
        }
    }

    /**
     * Get latest emergency notification for announcement display (Admin only)
     */
    @GetMapping("/emergency/latest")
    @ResponseBody
    public ResponseEntity<?> getLatestEmergencyNotification() {
        try {
            Optional<NotificationFeedback> emergencyNotification = notificationFeedbackService.getLatestEmergencyNotification();
            if (emergencyNotification.isPresent()) {
                Map<String, Object> response = new HashMap<>();
                response.put("id", emergencyNotification.get().getId());
                response.put("title", emergencyNotification.get().getTitle());
                response.put("message", emergencyNotification.get().getMessage());
                response.put("createdAt", emergencyNotification.get().getCreatedAt());
                return ResponseEntity.ok(response);
            } else {
                return ResponseEntity.ok(null);
            }
        } catch (Exception e) {
            log.error("Error fetching latest emergency notification: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("{\"error\": \"Failed to fetch emergency notification\"}");
        }
    }

    /**
     * Get latest emergency notification for announcement display (Public - accessible to all authenticated users)
     */
    @GetMapping("/public/emergency/latest")
    @ResponseBody
    public ResponseEntity<?> getLatestEmergencyNotificationPublic() {
        try {
            Optional<NotificationFeedback> emergencyNotification = notificationFeedbackService.getLatestEmergencyNotification();
            if (emergencyNotification.isPresent()) {
                Map<String, Object> response = new HashMap<>();
                response.put("id", emergencyNotification.get().getId());
                response.put("title", emergencyNotification.get().getTitle());
                response.put("message", emergencyNotification.get().getMessage());
                response.put("createdAt", emergencyNotification.get().getCreatedAt());
                return ResponseEntity.ok(response);
            } else {
                return ResponseEntity.ok(null);
            }
        } catch (Exception e) {
            log.error("Error fetching latest emergency notification: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("{\"error\": \"Failed to fetch emergency notification\"}");
        }
    }
}
