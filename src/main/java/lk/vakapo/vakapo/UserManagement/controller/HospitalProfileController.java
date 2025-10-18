package lk.vakapo.vakapo.UserManagement.controller;

import jakarta.servlet.http.HttpServletRequest;
import lk.vakapo.vakapo.UserManagement.model.Hospital;
import lk.vakapo.vakapo.UserManagement.model.UserAccount;
import lk.vakapo.vakapo.UserManagement.model.Feedback;
import lk.vakapo.vakapo.UserManagement.repository.HospitalRepository;
import lk.vakapo.vakapo.UserManagement.repository.UserRepository;
import lk.vakapo.vakapo.UserManagement.repository.FeedbackRepository;
import lk.vakapo.vakapo.UserManagement.service.FeedbackService;
import lk.vakapo.vakapo.MailManagement.EmailService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.Optional;
import java.util.List;
import java.util.Map;
import java.util.HashMap;

@Controller
@RequestMapping("/hospital")
@RequiredArgsConstructor
@Slf4j
public class HospitalProfileController {

    private final HospitalRepository hospitalRepository;
    private final UserRepository userRepository;
    private final FeedbackService feedbackService;
    private final FeedbackRepository feedbackRepository;
    private final EmailService emailService;

    /**
     * Display hospital landing page with dynamic data
     */
    @GetMapping("/landing")
    public String hospitalLanding(Model model) {
        try {
            // Get current authenticated user
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            String userEmail = authentication.getName();
            
            log.info("Loading landing page for hospital: {}", userEmail);
            
            // Find hospital by email
            Optional<Hospital> hospitalOpt = hospitalRepository.findByEmail(userEmail);
            
            if (hospitalOpt.isPresent()) {
                Hospital hospital = hospitalOpt.get();
                
                // Add hospital data to model
                model.addAttribute("hospital", hospital);
                model.addAttribute("hospitalName", hospital.getUsername() != null ? hospital.getUsername() : "Hospital");
                
                // Add user information for contact form auto-fill
                addContactFormUserInfo(model, hospital);
                
                // Add approved feedback for reviews section
                model.addAttribute("reviews", feedbackService.getApprovedFeedbackForLanding());
                
                log.info("Hospital landing page loaded successfully for: {}", hospital.getUsername());
                return "hospital/landingPage/LandingPage";
            } else {
                log.error("Hospital not found for email: {}", userEmail);
                model.addAttribute("error", "Hospital profile not found");
                return "error/404";
            }
        } catch (Exception e) {
            log.error("Error loading hospital landing page: {}", e.getMessage(), e);
            model.addAttribute("error", "Error loading page");
            return "error/500";
        }
    }

    /**
     * Display hospital profile page with dynamic data
     */
    @GetMapping("/profile")
    public String hospitalProfile(Model model) {
        try {
            // Get current authenticated user
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            String userEmail = authentication.getName();
            
            log.info("Loading profile for hospital: {}", userEmail);
            
            // Find hospital by email
            Optional<Hospital> hospitalOpt = hospitalRepository.findByEmail(userEmail);
            
            if (hospitalOpt.isPresent()) {
                Hospital hospital = hospitalOpt.get();
                
                // Add hospital data to model
                model.addAttribute("hospital", hospital);
                model.addAttribute("hospitalName", hospital.getUsername() != null ? hospital.getUsername() : "Hospital");
                model.addAttribute("hospitalId", hospital.getId());
                model.addAttribute("hospitalEmail", hospital.getEmail());
                model.addAttribute("hospitalContact", hospital.getContact() != null ? hospital.getContact() : "N/A");
                model.addAttribute("hospitalLocation", hospital.getAddress() != null ? hospital.getAddress() : "N/A");
                model.addAttribute("hospitalInstitution", hospital.getInstitution() != null ? hospital.getInstitution() : "N/A");
                model.addAttribute("hospitalRnumber", hospital.getRnumber() != null ? hospital.getRnumber() : "N/A");
                model.addAttribute("hospitalStatus", hospital.getStatus() != null ? hospital.getStatus() : "N/A");
                model.addAttribute("hospitalAdminApproval", hospital.getAdminApproval() != null ? hospital.getAdminApproval() : "N/A");
                
                log.info("Hospital profile loaded successfully for: {}", hospital.getUsername());
                return "hospital/profilePage/HospitalProfilePage";
            } else {
                log.error("Hospital not found for email: {}", userEmail);
                model.addAttribute("error", "Hospital profile not found");
                return "error/404";
            }
        } catch (Exception e) {
            log.error("Error loading hospital profile: {}", e.getMessage(), e);
            model.addAttribute("error", "Error loading profile");
            return "error/500";
        }
    }

    /**
     * Update hospital profile
     */
    @PostMapping("/profile/update")
    public String updateProfile(
            @RequestParam String username,
            @RequestParam String email,
            @RequestParam String contact,
            @RequestParam(required = false) String address,
            @RequestParam(required = false) String institution,
            @RequestParam(required = false) String rnumber,
            @RequestParam(required = false) String newPassword,
            RedirectAttributes redirectAttributes) {
        try {
            // Get current authenticated user
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            String userEmail = authentication.getName();
            
            log.info("Updating profile for hospital: {}", userEmail);
            
            // Find hospital by email
            Optional<Hospital> hospitalOpt = hospitalRepository.findByEmail(userEmail);
            
            if (hospitalOpt.isPresent()) {
                Hospital hospital = hospitalOpt.get();
                
                // Store original email for comparison
                String originalEmail = hospital.getEmail();
                
                // Update hospital data
                hospital.setUsername(username);
                hospital.setEmail(email);
                hospital.setContact(contact);
                
                // Update optional fields if provided
                if (address != null && !address.trim().isEmpty()) {
                    hospital.setAddress(address);
                }
                if (institution != null && !institution.trim().isEmpty()) {
                    hospital.setInstitution(institution);
                }
                if (rnumber != null && !rnumber.trim().isEmpty()) {
                    hospital.setRnumber(rnumber);
                }
                
                // Also update email in UserAccount table if email changed
                if (!originalEmail.equals(email)) {
                    Optional<UserAccount> userOpt = userRepository.findByEmail(originalEmail);
                    if (userOpt.isPresent()) {
                        UserAccount userAccount = userOpt.get();
                        userAccount.setEmail(email);
                        userRepository.save(userAccount);
                        log.info("Email updated in UserAccount table from {} to {}", originalEmail, email);
                    }
                }
                
                // Update password if provided
                if (newPassword != null && !newPassword.trim().isEmpty()) {
                    log.info("Password update requested for hospital: {}", userEmail);
                    
                    // Update password in UserAccount table (this is where login credentials are stored)
                    Optional<UserAccount> userOpt = userRepository.findByEmail(userEmail);
                    if (userOpt.isPresent()) {
                        UserAccount userAccount = userOpt.get();
                        // Note: In a real application, you would hash the password here
                        userAccount.setPassword(newPassword);
                        userRepository.save(userAccount);
                        log.info("Password updated in UserAccount table for: {}", userEmail);
                    } else {
                        log.warn("UserAccount not found for email: {}", userEmail);
                    }
                }
                
                // Save updated hospital
                hospitalRepository.save(hospital);
                
                log.info("Hospital profile updated successfully for: {}", userEmail);
                redirectAttributes.addFlashAttribute("successMessage", "Profile updated successfully!");
                
                return "redirect:/hospital/profile";
            } else {
                log.error("Hospital not found for email: {}", userEmail);
                redirectAttributes.addFlashAttribute("errorMessage", "Hospital not found");
                return "redirect:/hospital/profile";
            }
        } catch (Exception e) {
            log.error("Error updating hospital profile: {}", e.getMessage(), e);
            redirectAttributes.addFlashAttribute("errorMessage", "Error updating profile. Please try again.");
            return "redirect:/hospital/profile";
        }
    }

    /**
     * Test endpoint to verify hospital data fetching
     */
    @GetMapping("/test-data")
    public String testHospitalData() {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            String userEmail = authentication.getName();
            
            log.info("Testing hospital data for: {}", userEmail);
            
            Optional<Hospital> hospitalOpt = hospitalRepository.findByEmail(userEmail);
            
            if (hospitalOpt.isPresent()) {
                Hospital hospital = hospitalOpt.get();
                return "Hospital found: " + hospital.getUsername() + " (ID: " + hospital.getId() + ")";
            } else {
                return "Hospital not found for email: " + userEmail;
            }
        } catch (Exception e) {
            log.error("Error testing hospital data: {}", e.getMessage(), e);
            return "Error: " + e.getMessage();
        }
    }

    /**
     * Simple test endpoint to check if hospital profile route is working
     */
    @GetMapping("/test-route")
    public String testRoute() {
        return "Hospital profile route is working!";
    }

    /**
     * Test endpoint to check hospital name data
     */
    @GetMapping("/test-name")
    public String testHospitalName() {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            String userEmail = authentication.getName();
            
            log.info("Testing hospital name for user: {}", userEmail);
            
            Optional<Hospital> hospitalOpt = hospitalRepository.findByEmail(userEmail);
            
            if (hospitalOpt.isPresent()) {
                Hospital hospital = hospitalOpt.get();
                String username = hospital.getUsername();
                String email = hospital.getEmail();
                String id = hospital.getId();
                
                return String.format("Hospital found - ID: %s, Email: %s, Username: %s (null: %s)", 
                    id, email, username, username == null);
            } else {
                return "Hospital not found for email: " + userEmail;
            }
        } catch (Exception e) {
            log.error("Error testing hospital name: {}", e.getMessage(), e);
            return "Error: " + e.getMessage();
        }
    }

    /**
     * Display hospital feedback page
     */
    @GetMapping("/feedback")
    public String hospitalFeedback(Model model) {
        try {
            // Get current authenticated user
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            String userEmail = authentication.getName();
            
            log.info("Loading feedback page for hospital: {}", userEmail);
            
            // Find hospital by email
            Optional<Hospital> hospitalOpt = hospitalRepository.findByEmail(userEmail);
            
            if (hospitalOpt.isPresent()) {
                Hospital hospital = hospitalOpt.get();
                
                // Add hospital data to model
                model.addAttribute("hospital", hospital);
                model.addAttribute("hospitalName", hospital.getUsername() != null ? hospital.getUsername() : "Hospital");
                model.addAttribute("hospitalId", hospital.getId());
                model.addAttribute("hospitalEmail", hospital.getEmail());
                
                log.info("Hospital feedback page loaded successfully for: {}", hospital.getUsername());
                return "hospital/feedbackPage/HospitalFeedbackPage";
            } else {
                log.error("Hospital not found for email: {}", userEmail);
                model.addAttribute("error", "Hospital profile not found");
                return "error/404";
            }
        } catch (Exception e) {
            log.error("Error loading hospital feedback page: {}", e.getMessage(), e);
            model.addAttribute("error", "Error loading page");
            return "error/500";
        }
    }

    @PostMapping("/feedback")
    public String submitHospitalFeedback(
            @RequestParam String name,
            @RequestParam String email,
            @RequestParam String contactno,
            @RequestParam Integer rating,
            @RequestParam String message,
            Model model) {
        try {
            // Get current authenticated user
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            String userEmail = authentication.getName();
            
            log.info("Submitting hospital feedback from: {}", userEmail);
            
            // Find hospital by email
            Optional<Hospital> hospitalOpt = hospitalRepository.findByEmail(userEmail);
            
            if (hospitalOpt.isPresent()) {
                Hospital hospital = hospitalOpt.get();
                
                // Create feedback object
                Feedback feedback = new Feedback();
                feedback.setName(name);
                feedback.setEmail(email);
                feedback.setContactNo(contactno);
                feedback.setRating(rating);
                feedback.setMessage(message);
                feedback.setUserType("HOSPITAL");
                feedback.setUserId(hospital.getId());
                
                // Submit feedback
                log.info("About to submit feedback: {}", feedback);
                Feedback savedFeedback = feedbackService.submitFeedback(feedback);
                log.info("Feedback submitted successfully with ID: {}", savedFeedback.getId());
                
                // Add success message
                model.addAttribute("successMessage", "Thank you for your feedback! It will be reviewed and may appear on our landing pages.");
                model.addAttribute("hospital", hospital);
                model.addAttribute("hospitalName", hospital.getUsername() != null ? hospital.getUsername() : "Hospital");
                model.addAttribute("hospitalId", hospital.getId());
                model.addAttribute("hospitalEmail", hospital.getEmail());
                
                log.info("Hospital feedback submitted successfully by: {}", userEmail);
                return "hospital/feedbackPage/HospitalFeedbackPage";
            } else {
                log.error("Hospital not found for email: {}", userEmail);
                return "error/404";
            }
        } catch (Exception e) {
            log.error("Error submitting hospital feedback: {}", e.getMessage(), e);
            model.addAttribute("errorMessage", "Error submitting feedback. Please try again.");
            return "hospital/feedbackPage/HospitalFeedbackPage";
        }
    }

    /**
     * Get user's own feedback (My Reviews section)
     */
    @GetMapping("/api/my-reviews")
    @ResponseBody
    public Map<String, Object> getMyReviews(HttpServletRequest request) {
        Map<String, Object> response = new HashMap<>();
        try {
            // Get current authenticated user
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            String userEmail = authentication.getName();
            
            // Find hospital by email
            Optional<Hospital> hospitalOpt = hospitalRepository.findByEmail(userEmail);
            
            if (hospitalOpt.isPresent()) {
                Hospital hospital = hospitalOpt.get();
                
                // Get user's feedback
                List<Feedback> myReviews = feedbackRepository.findByUserIdOrderByCreatedAtDesc(hospital.getId());
                
                response.put("success", true);
                response.put("data", myReviews);
                response.put("count", myReviews.size());
                
                return response;
            } else {
                response.put("success", false);
                response.put("message", "Hospital not found");
                return response;
            }
        } catch (Exception e) {
            log.error("Error getting hospital reviews: {}", e.getMessage(), e);
            response.put("success", false);
            response.put("message", "Error retrieving reviews");
            return response;
        }
    }

    /**
     * Update user's own feedback
     */
    @PostMapping("/api/update-review")
    @ResponseBody
    public Map<String, Object> updateMyReview(
            @RequestParam Long feedbackId,
            @RequestParam String name,
            @RequestParam String email,
            @RequestParam String contactno,
            @RequestParam Integer rating,
            @RequestParam String message,
            HttpServletRequest request) {
        Map<String, Object> response = new HashMap<>();
        try {
            // Get current authenticated user
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            String userEmail = authentication.getName();
            
            // Find hospital by email
            Optional<Hospital> hospitalOpt = hospitalRepository.findByEmail(userEmail);
            
            if (hospitalOpt.isPresent()) {
                Hospital hospital = hospitalOpt.get();
                
                // Update feedback
                boolean success = feedbackService.updateFeedback(feedbackId, hospital.getId(), name, email, contactno, rating, message);
                
                if (success) {
                    response.put("success", true);
                    response.put("message", "Review updated successfully! It will be reviewed again by admin.");
                } else {
                    response.put("success", false);
                    response.put("message", "Failed to update review. You can only edit your own reviews.");
                }
                
                return response;
            } else {
                response.put("success", false);
                response.put("message", "Hospital not found");
                return response;
            }
        } catch (Exception e) {
            log.error("Error updating hospital review: {}", e.getMessage(), e);
            response.put("success", false);
            response.put("message", "Error updating review");
            return response;
        }
    }

    /**
     * Delete user's own feedback
     */
    @PostMapping("/api/delete-review")
    @ResponseBody
    public Map<String, Object> deleteMyReview(
            @RequestParam Long feedbackId,
            HttpServletRequest request) {
        Map<String, Object> response = new HashMap<>();
        try {
            // Get current authenticated user
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            String userEmail = authentication.getName();
            
            // Find hospital by email
            Optional<Hospital> hospitalOpt = hospitalRepository.findByEmail(userEmail);
            
            if (hospitalOpt.isPresent()) {
                Hospital hospital = hospitalOpt.get();
                
                // Delete feedback
                boolean success = feedbackService.deleteFeedbackByUser(feedbackId, hospital.getId());
                
                if (success) {
                    response.put("success", true);
                    response.put("message", "Review deleted successfully!");
                } else {
                    response.put("success", false);
                    response.put("message", "Failed to delete review. You can only delete your own reviews.");
                }
                
                return response;
            } else {
                response.put("success", false);
                response.put("message", "Hospital not found");
                return response;
            }
        } catch (Exception e) {
            log.error("Error deleting hospital review: {}", e.getMessage(), e);
            response.put("success", false);
            response.put("message", "Error deleting review");
            return response;
        }
    }

    /**
     * Delete hospital account
     */
    @PostMapping("/delete-account")
    public String deleteAccount(RedirectAttributes redirectAttributes) {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            String userEmail = authentication.getName();

            log.info("Deleting account for hospital: {}", userEmail);

            // Find hospital by email
            Optional<Hospital> hospitalOpt = hospitalRepository.findByEmail(userEmail);

            if (hospitalOpt.isPresent()) {
                Hospital hospital = hospitalOpt.get();
                String hospitalId = hospital.getId();

                // Delete from Hospital table
                hospitalRepository.delete(hospital);

                // Delete from UserAccount table
                Optional<UserAccount> userOpt = userRepository.findByEmail(userEmail);
                if (userOpt.isPresent()) {
                    userRepository.delete(userOpt.get());
                }

                log.info("Hospital account deleted successfully: {}", hospitalId);
                
                // Send account deletion email
                try {
                    emailService.sendAccountDeletionEmail(userEmail, hospital.getUsername(), "Hospital");
                    log.info("Account deletion email sent to hospital: {}", userEmail);
                } catch (Exception e) {
                    log.error("Failed to send account deletion email to hospital: {}", userEmail, e);
                    // Don't fail the deletion if email fails
                }
                
                redirectAttributes.addFlashAttribute("successMessage", "Your account has been deleted successfully.");
                
                // Redirect to login page after deletion
                return "redirect:/login?deleted=true";
            } else {
                log.error("Hospital not found for email: {}", userEmail);
                redirectAttributes.addFlashAttribute("errorMessage", "Account not found");
                return "redirect:/hospital/profile";
            }
        } catch (Exception e) {
            log.error("Error deleting hospital account: {}", e.getMessage(), e);
            redirectAttributes.addFlashAttribute("errorMessage", "Error deleting account. Please try again.");
            return "redirect:/hospital/profile";
        }
    }
    
    /**
     * Add user information for contact form auto-fill
     */
    private void addContactFormUserInfo(Model model, Hospital hospital) {
        try {
            model.addAttribute("isLoggedIn", true);
            model.addAttribute("userEmail", hospital.getEmail());
            model.addAttribute("userName", hospital.getUsername() != null ? hospital.getUsername() : extractUserNameFromEmail(hospital.getEmail()));
            model.addAttribute("userPhone", hospital.getContact() != null ? hospital.getContact() : "");
            
        } catch (Exception e) {
            log.error("Error adding contact form user info: {}", e.getMessage());
            // Set default values
            model.addAttribute("isLoggedIn", true);
            model.addAttribute("userEmail", hospital.getEmail());
            model.addAttribute("userName", extractUserNameFromEmail(hospital.getEmail()));
            model.addAttribute("userPhone", "");
        }
    }
    
    /**
     * Extract a user-friendly name from email address
     */
    private String extractUserNameFromEmail(String email) {
        if (email == null || email.isEmpty()) {
            return "";
        }
        
        // Extract the part before @ and replace dots/underscores with spaces
        String namePart = email.split("@")[0];
        return namePart.replace(".", " ").replace("_", " ").replace("-", " ");
    }

}
