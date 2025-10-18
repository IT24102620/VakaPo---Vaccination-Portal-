package lk.vakapo.vakapo.UserManagement.controller;

import lk.vakapo.vakapo.UserManagement.model.UserAccount;
import lk.vakapo.vakapo.UserManagement.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;

@RestController
@RequestMapping("/nurse")
@RequiredArgsConstructor
@Slf4j
public class NursePasswordController {

    private final UserRepository userRepository;

    /**
     * Change password for nurse
     */
    @PostMapping("/change-password")
    public ResponseEntity<?> changePassword(@RequestBody PasswordChangeRequest request) {
        try {
            // Get current authenticated user
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            String userEmail = authentication.getName();
            log.info("Nurse password change request from: {}", userEmail);

            // Find user account
            Optional<UserAccount> userOpt = userRepository.findByEmail(userEmail);
            if (userOpt.isEmpty()) {
                log.error("User not found for email: {}", userEmail);
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body("{\"success\": false, \"error\": \"User not found\"}");
            }

            // Check if user is a nurse
            UserAccount user = userOpt.get();
            if (!"Nurse".equalsIgnoreCase(user.getRole())) {
                log.error("Access denied - user is not a nurse: {}", userEmail);
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                        .body("{\"success\": false, \"error\": \"Access denied - nurses only\"}");
            }

            // Debug logging
            log.info("Password change request - New Password: {}, Confirm Password: {}", 
                    request.getNewPassword(), request.getConfirmPassword());
            log.info("Passwords match: {}", request.getNewPassword().equals(request.getConfirmPassword()));

            // Validate new password
            if (request.getNewPassword() == null || request.getNewPassword().length() < 8) {
                return ResponseEntity.badRequest()
                        .body("{\"success\": false, \"error\": \"Password must be at least 8 characters long\"}");
            }

            if (!request.getNewPassword().equals(request.getConfirmPassword())) {
                log.error("Password mismatch - New: '{}', Confirm: '{}'", 
                         request.getNewPassword(), request.getConfirmPassword());
                return ResponseEntity.badRequest()
                        .body("{\"success\": false, \"error\": \"Passwords do not match\"}");
            }

            // Update the user's password
            user.setPassword(request.getNewPassword());
            userRepository.save(user);
            
            log.info("Nurse {} password changed successfully", userEmail);

            return ResponseEntity.ok().body("{\"success\": true, \"message\": \"Password changed successfully\"}");

        } catch (Exception e) {
            log.error("Error changing nurse password: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("{\"success\": false, \"error\": \"Failed to change password\"}");
        }
    }

    /**
     * Password change request DTO
     */
    public static class PasswordChangeRequest {
        private String currentPassword;
        private String newPassword;
        private String confirmPassword;

        // Getters and setters
        public String getCurrentPassword() { return currentPassword; }
        public void setCurrentPassword(String currentPassword) { this.currentPassword = currentPassword; }
        
        public String getNewPassword() { return newPassword; }
        public void setNewPassword(String newPassword) { this.newPassword = newPassword; }
        
        public String getConfirmPassword() { return confirmPassword; }
        public void setConfirmPassword(String confirmPassword) { this.confirmPassword = confirmPassword; }
    }
}
