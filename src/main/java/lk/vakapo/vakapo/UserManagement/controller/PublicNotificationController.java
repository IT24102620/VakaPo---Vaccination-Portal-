package lk.vakapo.vakapo.UserManagement.controller;

import lk.vakapo.vakapo.UserManagement.model.NotificationFeedback;
import lk.vakapo.vakapo.UserManagement.service.NotificationFeedbackService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/public")
@RequiredArgsConstructor
@Slf4j
public class PublicNotificationController {

    private final NotificationFeedbackService notificationFeedbackService;

    /**
     * Get latest emergency notification for announcement display (Public - accessible to all authenticated users)
     */
    @GetMapping("/emergency/latest")
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
}
