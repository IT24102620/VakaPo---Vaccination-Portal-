package lk.vakapo.vakapo.UserManagement.controller;

import lk.vakapo.vakapo.UserManagement.model.UserAccount;
import lk.vakapo.vakapo.UserManagement.model.Staff;
import lk.vakapo.vakapo.UserManagement.repository.UserRepository;
import lk.vakapo.vakapo.UserManagement.repository.StaffRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;

@RestController
@RequestMapping("/staff")
@RequiredArgsConstructor
@Slf4j
public class StaffPasswordController {

    private final UserRepository userRepository;
    private final StaffRepository staffRepository;

    /**
     * Change password for staff member
     */
    @PostMapping("/change-password")
    public ResponseEntity<?> changePassword(@RequestBody PasswordChangeRequest request) {
        try {
            // Get current authenticated user
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            String userEmail = authentication.getName();
            log.info("Password change request from: {}", userEmail);

            // Find user account
            Optional<UserAccount> userOpt = userRepository.findByEmail(userEmail);
            if (userOpt.isEmpty()) {
                log.error("User not found for email: {}", userEmail);
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body("{\"success\": false, \"error\": \"User not found\"}");
            }

            // Check if user is a staff member
            UserAccount user = userOpt.get();
            if (!isStaffMember(user.getId())) {
                log.error("Access denied - user is not a staff member: {}", userEmail);
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                        .body("{\"success\": false, \"error\": \"Access denied - staff only\"}");
            }

            // Verify current password
            if (!user.getPassword().equals(request.getCurrentPassword())) {
                log.warn("Invalid current password for user: {}", userEmail);
                return ResponseEntity.badRequest()
                        .body("{\"success\": false, \"error\": \"Current password is incorrect\"}");
            }

            // Validate new password
            if (request.getNewPassword().length() < 8) {
                return ResponseEntity.badRequest()
                        .body("{\"success\": false, \"error\": \"New password must be at least 8 characters long\"}");
            }

            // Update password
            user.setPassword(request.getNewPassword());
            userRepository.save(user);

            log.info("Password changed successfully for user: {}", userEmail);
            return ResponseEntity.ok().body("{\"success\": true, \"message\": \"Password changed successfully\"}");

        } catch (Exception e) {
            log.error("Error changing password: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("{\"success\": false, \"error\": \"Failed to change password\"}");
        }
    }

    /**
     * Check if user needs to change password (for first login)
     */
    @GetMapping("/password-status")
    public ResponseEntity<?> getPasswordStatus() {
        try {
            // Get current authenticated user
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            String userEmail = authentication.getName();

            // Find user account
            Optional<UserAccount> userOpt = userRepository.findByEmail(userEmail);
            if (userOpt.isEmpty()) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body("{\"success\": false, \"error\": \"User not found\"}");
            }

            UserAccount user = userOpt.get();
            
            // Check if user is a staff member
            if (!isStaffMember(user.getId())) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                        .body("{\"success\": false, \"error\": \"Access denied - staff only\"}");
            }
            boolean isDefaultPassword = "1234567890".equals(user.getPassword());

            return ResponseEntity.ok().body("{\"success\": true, \"needsPasswordChange\": " + isDefaultPassword + "}");

        } catch (Exception e) {
            log.error("Error checking password status: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("{\"success\": false, \"error\": \"Failed to check password status\"}");
        }
    }

    /**
     * Password change request DTO
     */
    public static class PasswordChangeRequest {
        private String currentPassword;
        private String newPassword;

        // Getters and setters
        public String getCurrentPassword() {
            return currentPassword;
        }

        public void setCurrentPassword(String currentPassword) {
            this.currentPassword = currentPassword;
        }

        public String getNewPassword() {
            return newPassword;
        }

        public void setNewPassword(String newPassword) {
            this.newPassword = newPassword;
        }
    }

    /**
     * Check if user ID indicates a staff member (Doctor or Nurse only)
     */
    private boolean isStaffMember(String userId) {
        if (userId == null) return false;
        
        // Check for staff ID patterns: Vak D (Doctor), Vak N (Nurse)
        return userId.startsWith("Vak D ") || 
               userId.startsWith("Vak N ");
    }
}
