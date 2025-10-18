package lk.vakapo.vakapo.UserManagement.controller;

import lk.vakapo.vakapo.UserManagement.model.UserNotification;
import lk.vakapo.vakapo.UserManagement.service.UserNotificationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Controller
@RequestMapping("/api/notifications")
@RequiredArgsConstructor
@Slf4j
public class UserNotificationController {

    private final UserNotificationService userNotificationService;

    /**
     * Get notifications for current user
     */
    @GetMapping("/user")
    @ResponseBody
    public ResponseEntity<?> getUserNotifications() {
        try {
            String userEmail = getCurrentUserEmail();
            if (userEmail == null) {
                return ResponseEntity.badRequest().body("{\"error\": \"User not authenticated\"}");
            }

            List<UserNotification> notifications = userNotificationService.getUserNotificationsByEmail(userEmail);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("notifications", notifications);
            response.put("count", notifications.size());
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            log.error("Error fetching user notifications: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError().body("{\"error\": \"Failed to fetch notifications\"}");
        }
    }

    /**
     * Get unread notifications for current user
     */
    @GetMapping("/user/unread")
    @ResponseBody
    public ResponseEntity<?> getUnreadNotifications() {
        try {
            String userEmail = getCurrentUserEmail();
            if (userEmail == null) {
                return ResponseEntity.badRequest().body("{\"error\": \"User not authenticated\"}");
            }

            List<UserNotification> unreadNotifications = userNotificationService.getUnreadNotificationsByEmail(userEmail);
            long unreadCount = userNotificationService.getUnreadCountByEmail(userEmail);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("notifications", unreadNotifications);
            response.put("unreadCount", unreadCount);
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            log.error("Error fetching unread notifications: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError().body("{\"error\": \"Failed to fetch unread notifications\"}");
        }
    }

    /**
     * Get unread count for current user
     */
    @GetMapping("/user/unread-count")
    @ResponseBody
    public ResponseEntity<?> getUnreadCount() {
        try {
            String userEmail = getCurrentUserEmail();
            if (userEmail == null) {
                return ResponseEntity.badRequest().body("{\"error\": \"User not authenticated\"}");
            }

            long unreadCount = userNotificationService.getUnreadCountByEmail(userEmail);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("unreadCount", unreadCount);
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            log.error("Error fetching unread count: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError().body("{\"error\": \"Failed to fetch unread count\"}");
        }
    }

    /**
     * Mark notification as read
     */
    @PostMapping("/mark-read/{id}")
    @ResponseBody
    public ResponseEntity<?> markAsRead(@PathVariable Long id) {
        try {
            userNotificationService.markAsRead(id);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Notification marked as read");
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            log.error("Error marking notification as read: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError().body("{\"error\": \"Failed to mark notification as read\"}");
        }
    }

    /**
     * Mark all notifications as read for current user
     */
    @PostMapping("/mark-all-read")
    @ResponseBody
    public ResponseEntity<?> markAllAsRead() {
        try {
            String userEmail = getCurrentUserEmail();
            if (userEmail == null) {
                return ResponseEntity.badRequest().body("{\"error\": \"User not authenticated\"}");
            }

            userNotificationService.markAllAsReadByEmail(userEmail);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "All notifications marked as read");
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            log.error("Error marking all notifications as read: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError().body("{\"error\": \"Failed to mark all notifications as read\"}");
        }
    }

    /**
     * Display all notifications page for current user
     */
    @GetMapping("/page")
    public String notificationsPage(Model model) {
        try {
            String userEmail = getCurrentUserEmail();
            if (userEmail == null) {
                log.warn("User not authenticated when accessing notifications page");
                return "error/403";
            }

            log.info("Loading notifications page for user: {}", userEmail);
            
            List<UserNotification> notifications = userNotificationService.getUserNotificationsByEmail(userEmail);
            long unreadCount = userNotificationService.getUnreadCountByEmail(userEmail);
            
            log.info("Found {} notifications for user {}, {} unread", notifications.size(), userEmail, unreadCount);
            
            model.addAttribute("notifications", notifications);
            model.addAttribute("unreadCount", unreadCount);
            model.addAttribute("userEmail", userEmail);
            
            return "shared/notifications/AllNotificationsPage";
            
        } catch (Exception e) {
            log.error("Error loading notifications page: {}", e.getMessage(), e);
            model.addAttribute("error", "Failed to load notifications. Please check if the database tables are properly created.");
            return "error/500";
        }
    }

    /**
     * Get current user email from security context
     */
    private String getCurrentUserEmail() {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            if (authentication != null && authentication.isAuthenticated()) {
                return authentication.getName(); // This should be the email
            }
            return null;
        } catch (Exception e) {
            log.error("Error getting current user email: {}", e.getMessage(), e);
            return null;
        }
    }
}
