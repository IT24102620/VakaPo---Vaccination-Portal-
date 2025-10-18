package lk.vakapo.vakapo.UserManagement.controller;

import lk.vakapo.vakapo.UserManagement.model.UserAccount;
import lk.vakapo.vakapo.UserManagement.repository.UserRepository;
import lk.vakapo.vakapo.MailManagement.EmailService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

@Controller
@RequiredArgsConstructor
@Slf4j
public class ForgotPasswordController {

    private final UserRepository userRepository;
    private final EmailService emailService;
    
    // In-memory storage for reset tokens (in production, use Redis or database)
    private final Map<String, PasswordResetToken> resetTokens = new HashMap<>();

    @GetMapping("/forgot-password")
    public String forgotPasswordPage() {
        return "ForgotPassword";
    }

    @PostMapping("/forgot-password")
    public String processForgotPassword(@RequestParam String email, 
                                      RedirectAttributes redirectAttributes) {
        try {
            // Check if email exists in the database
            Optional<UserAccount> userOpt = userRepository.findByEmail(email);
            
            if (userOpt.isPresent()) {
                // Generate reset token
                String resetToken = UUID.randomUUID().toString();
                LocalDateTime expiryTime = LocalDateTime.now().plusHours(1); // Token expires in 1 hour
                
                // Store token with user info
                resetTokens.put(resetToken, new PasswordResetToken(
                    userOpt.get().getEmail(), 
                    userOpt.get().getId(), 
                    expiryTime
                ));
                
                // Send reset email
                sendPasswordResetEmail(userOpt.get().getEmail(), resetToken);
                
                log.info("Password reset email sent to: {}", email);
                redirectAttributes.addFlashAttribute("successMessage", 
                    "Password reset link has been sent to your email address. Please check your inbox and follow the instructions.");
                
            } else {
                // Email not found - show error message
                log.warn("Password reset attempted for non-existent email: {}", email);
                redirectAttributes.addFlashAttribute("errorMessage", 
                    "The email address you entered is not registered with us. Please check your email address or sign up for a new account.");
            }
            
        } catch (Exception e) {
            log.error("Error processing forgot password request for email: {}", email, e);
            redirectAttributes.addFlashAttribute("errorMessage", 
                "An error occurred while processing your request. Please try again later.");
        }
        
        return "redirect:/forgot-password";
    }

    @GetMapping("/reset-password")
    public String resetPasswordPage(@RequestParam String token, Model model) {
        try {
            // Validate token
            PasswordResetToken resetToken = resetTokens.get(token);
            
            if (resetToken == null) {
                model.addAttribute("errorMessage", "Invalid or expired reset token. Please request a new password reset.");
                return "ResetPassword";
            }
            
            if (resetToken.getExpiryTime().isBefore(LocalDateTime.now())) {
                // Token expired
                resetTokens.remove(token);
                model.addAttribute("errorMessage", "Reset token has expired. Please request a new password reset.");
                return "ResetPassword";
            }
            
            // Token is valid
            model.addAttribute("token", token);
            return "ResetPassword";
            
        } catch (Exception e) {
            log.error("Error validating reset token: {}", token, e);
            model.addAttribute("errorMessage", "Invalid reset token. Please request a new password reset.");
            return "ResetPassword";
        }
    }

    @PostMapping("/reset-password")
    public String processResetPassword(@RequestParam String token,
                                     @RequestParam String newPassword,
                                     @RequestParam String confirmPassword,
                                     RedirectAttributes redirectAttributes) {
        try {
            // Validate token
            PasswordResetToken resetToken = resetTokens.get(token);
            
            if (resetToken == null) {
                redirectAttributes.addFlashAttribute("errorMessage", "Invalid or expired reset token.");
                return "redirect:/forgot-password";
            }
            
            if (resetToken.getExpiryTime().isBefore(LocalDateTime.now())) {
                resetTokens.remove(token);
                redirectAttributes.addFlashAttribute("errorMessage", "Reset token has expired. Please request a new password reset.");
                return "redirect:/forgot-password";
            }
            
            // Validate passwords match
            if (!newPassword.equals(confirmPassword)) {
                redirectAttributes.addFlashAttribute("errorMessage", "Passwords do not match.");
                return "redirect:/reset-password?token=" + token;
            }
            
            // Validate password strength
            if (newPassword.length() < 8) {
                redirectAttributes.addFlashAttribute("errorMessage", "Password must be at least 8 characters long.");
                return "redirect:/reset-password?token=" + token;
            }
            
            // Update password in database
            Optional<UserAccount> userOpt = userRepository.findByEmail(resetToken.getEmail());
            if (userOpt.isPresent()) {
                UserAccount user = userOpt.get();
                user.setPassword(newPassword); // Note: In production, hash the password
                userRepository.save(user);
                
                // Remove used token
                resetTokens.remove(token);
                
                // Send password change confirmation email
                sendPasswordChangeConfirmationEmail(resetToken.getEmail());
                
                log.info("Password successfully reset for user: {}", resetToken.getEmail());
                redirectAttributes.addFlashAttribute("successMessage", 
                    "Your password has been successfully reset. You can now login with your new password.");
                
                return "redirect:/login";
            } else {
                redirectAttributes.addFlashAttribute("errorMessage", "User not found. Please request a new password reset.");
                return "redirect:/forgot-password";
            }
            
        } catch (Exception e) {
            log.error("Error resetting password for token: {}", token, e);
            redirectAttributes.addFlashAttribute("errorMessage", 
                "An error occurred while resetting your password. Please try again.");
            return "redirect:/reset-password?token=" + token;
        }
    }

    private void sendPasswordResetEmail(String email, String resetToken) {
        try {
            String resetLink = "http://localhost:8080/reset-password?token=" + resetToken;
            
            String subject = "VakaPo - Password Reset Request";
            String body = buildPasswordResetEmailBody(email, resetLink);
            
            emailService.sendEmail(email, subject, body);
            
        } catch (Exception e) {
            log.error("Error sending password reset email to: {}", email, e);
            throw new RuntimeException("Failed to send password reset email", e);
        }
    }

    private void sendPasswordChangeConfirmationEmail(String email) {
        try {
            String subject = "VakaPo - Password Successfully Changed";
            String body = buildPasswordChangeConfirmationEmailBody(email);
            
            emailService.sendEmail(email, subject, body);
            log.info("Password change confirmation email sent to: {}", email);
            
        } catch (Exception e) {
            log.error("Error sending password change confirmation email to: {}", email, e);
            // Don't throw exception here as password was already changed successfully
        }
    }

    private String buildPasswordResetEmailBody(String email, String resetLink) {
        StringBuilder emailBody = new StringBuilder();
        emailBody.append("<!DOCTYPE html>");
        emailBody.append("<html>");
        emailBody.append("<head>");
        emailBody.append("<meta charset=\"UTF-8\">");
        emailBody.append("<meta name=\"viewport\" content=\"width=device-width, initial-scale=1.0\">");
        emailBody.append("</head>");
        emailBody.append("<body style=\"font-family: Arial, sans-serif; line-height: 1.6; color: #333; margin: 0; padding: 0; background-color: #f4f4f4;\">");
        emailBody.append("<div style=\"max-width: 600px; margin: 0 auto; padding: 20px;\">");
        
        // Header
        emailBody.append("<div style=\"background: linear-gradient(135deg, #528CD7 0%, #037 100%); color: white; padding: 30px; text-align: center; border-radius: 8px 8px 0 0;\">");
        emailBody.append("<h1 style=\"margin: 0; font-size: 24px; font-weight: bold;\">🔒 VakaPo Password Reset</h1>");
        emailBody.append("</div>");
        
        // Content
        emailBody.append("<div style=\"background: white; padding: 30px; border-radius: 0 0 8px 8px; box-shadow: 0 2px 10px rgba(0,0,0,0.1);\">");
        emailBody.append("<h2 style=\"color: #333; margin-top: 0;\">Hello!</h2>");
        emailBody.append("<p style=\"font-size: 16px; margin-bottom: 20px;\">We received a request to reset your password for your VakaPo account associated with <strong>").append(email).append("</strong>.</p>");
        emailBody.append("<p style=\"font-size: 16px; margin-bottom: 30px;\">Click the button below to reset your password:</p>");
        
        // Button
        emailBody.append("<div style=\"text-align: center; margin: 30px 0;\">");
        emailBody.append("<a href=\"").append(resetLink).append("\" style=\"display: inline-block; background: #528CD7; color: white; padding: 15px 30px; text-decoration: none; border-radius: 8px; font-weight: bold; font-size: 18px; box-shadow: 0 4px 8px rgba(82, 140, 215, 0.3); transition: all 0.3s ease;\">Reset My Password</a>");
        emailBody.append("</div>");
        
        // Important info
        emailBody.append("<div style=\"background: #f8f9fa; padding: 20px; border-radius: 8px; margin: 30px 0;\">");
        emailBody.append("<p style=\"font-weight: bold; margin-top: 0; color: #333;\">Important:</p>");
        emailBody.append("<ul style=\"margin: 0; padding-left: 20px;\">");
        emailBody.append("<li style=\"margin-bottom: 8px;\">This link will expire in 1 hour for security reasons</li>");
        emailBody.append("<li style=\"margin-bottom: 8px;\">If you didn't request this password reset, please ignore this email</li>");
        emailBody.append("<li style=\"margin-bottom: 0;\">Your password will remain unchanged until you create a new one</li>");
        emailBody.append("</ul>");
        emailBody.append("</div>");
        
        // Fallback link
        emailBody.append("<p style=\"font-size: 14px; color: #666; margin-bottom: 10px;\">If the button doesn't work, you can copy and paste this link into your browser:</p>");
        emailBody.append("<p style=\"word-break: break-all; background: #e9ecef; padding: 15px; border-radius: 6px; font-family: monospace; font-size: 12px; color: #333; border: 1px solid #dee2e6;\">").append(resetLink).append("</p>");
        emailBody.append("</div>");
        
        // Footer
        emailBody.append("<div style=\"text-align: center; margin-top: 30px; padding: 20px; background: #f8f9fa; border-radius: 8px;\">");
        emailBody.append("<p style=\"margin: 0; font-size: 12px; color: #666;\">This email was sent from VakaPo - Vaccination Portal</p>");
        emailBody.append("<p style=\"margin: 5px 0 0 0; font-size: 12px; color: #666;\">If you have any questions, please contact our support team.</p>");
        emailBody.append("</div>");
        emailBody.append("</div>");
        emailBody.append("</body>");
        emailBody.append("</html>");
        
        return emailBody.toString();
    }

    private String buildPasswordChangeConfirmationEmailBody(String email) {
        StringBuilder emailBody = new StringBuilder();
        emailBody.append("<!DOCTYPE html>");
        emailBody.append("<html>");
        emailBody.append("<head>");
        emailBody.append("<meta charset=\"UTF-8\">");
        emailBody.append("<meta name=\"viewport\" content=\"width=device-width, initial-scale=1.0\">");
        emailBody.append("</head>");
        emailBody.append("<body style=\"font-family: Arial, sans-serif; line-height: 1.6; color: #333; margin: 0; padding: 0; background-color: #f4f4f4;\">");
        emailBody.append("<div style=\"max-width: 600px; margin: 0 auto; padding: 20px;\">");
        
        // Header
        emailBody.append("<div style=\"background: linear-gradient(135deg, #28a745 0%, #20c997 100%); color: white; padding: 30px; text-align: center; border-radius: 8px 8px 0 0;\">");
        emailBody.append("<h1 style=\"margin: 0; font-size: 24px; font-weight: bold;\">✅ Password Successfully Changed</h1>");
        emailBody.append("</div>");
        
        // Content
        emailBody.append("<div style=\"background: white; padding: 30px; border-radius: 0 0 8px 8px; box-shadow: 0 2px 10px rgba(0,0,0,0.1);\">");
        emailBody.append("<h2 style=\"color: #333; margin-top: 0;\">Hello!</h2>");
        emailBody.append("<p style=\"font-size: 16px; margin-bottom: 20px;\">Your password for your VakaPo account associated with <strong>").append(email).append("</strong> has been successfully changed.</p>");
        
        // Success message
        emailBody.append("<div style=\"background: #d4edda; border: 1px solid #c3e6cb; color: #155724; padding: 20px; border-radius: 8px; margin: 20px 0;\">");
        emailBody.append("<p style=\"margin: 0; font-weight: bold;\">✅ Password Change Confirmed</p>");
        emailBody.append("<p style=\"margin: 10px 0 0 0;\">Your account is now secure with your new password.</p>");
        emailBody.append("</div>");
        
        // Security info
        emailBody.append("<div style=\"background: #f8f9fa; padding: 20px; border-radius: 8px; margin: 30px 0;\">");
        emailBody.append("<p style=\"font-weight: bold; margin-top: 0; color: #333;\">Security Information:</p>");
        emailBody.append("<ul style=\"margin: 0; padding-left: 20px;\">");
        emailBody.append("<li style=\"margin-bottom: 8px;\">Your password was changed on ").append(LocalDateTime.now().format(java.time.format.DateTimeFormatter.ofPattern("MMMM dd, yyyy 'at' h:mm a"))).append("</li>");
        emailBody.append("<li style=\"margin-bottom: 8px;\">If you did not make this change, please contact our support team immediately</li>");
        emailBody.append("<li style=\"margin-bottom: 0;\">For security reasons, we recommend using a strong, unique password</li>");
        emailBody.append("</ul>");
        emailBody.append("</div>");
        
        // Login link
        emailBody.append("<div style=\"text-align: center; margin: 30px 0;\">");
        emailBody.append("<a href=\"http://localhost:8080/login\" style=\"display: inline-block; background: #528CD7; color: white; padding: 15px 30px; text-decoration: none; border-radius: 8px; font-weight: bold; font-size: 18px; box-shadow: 0 4px 8px rgba(82, 140, 215, 0.3);\">Login to Your Account</a>");
        emailBody.append("</div>");
        
        // Footer
        emailBody.append("<div style=\"text-align: center; margin-top: 30px; padding: 20px; background: #f8f9fa; border-radius: 8px;\">");
        emailBody.append("<p style=\"margin: 0; font-size: 12px; color: #666;\">This email was sent from VakaPo - Vaccination Portal</p>");
        emailBody.append("<p style=\"margin: 5px 0 0 0; font-size: 12px; color: #666;\">If you have any questions, please contact our support team.</p>");
        emailBody.append("</div>");
        emailBody.append("</div>");
        emailBody.append("</body>");
        emailBody.append("</html>");
        
        return emailBody.toString();
    }

    // Inner class to store reset token information
    private static class PasswordResetToken {
        private final String email;
        private final String userId;
        private final LocalDateTime expiryTime;

        public PasswordResetToken(String email, String userId, LocalDateTime expiryTime) {
            this.email = email;
            this.userId = userId;
            this.expiryTime = expiryTime;
        }

        public String getEmail() { return email; }
        public String getUserId() { return userId; }
        public LocalDateTime getExpiryTime() { return expiryTime; }
    }
}
