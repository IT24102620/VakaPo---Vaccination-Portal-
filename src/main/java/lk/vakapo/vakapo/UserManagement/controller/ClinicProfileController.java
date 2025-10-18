package lk.vakapo.vakapo.UserManagement.controller;

import lk.vakapo.vakapo.UserManagement.model.Clinic;
import lk.vakapo.vakapo.UserManagement.model.UserAccount;
import lk.vakapo.vakapo.UserManagement.model.Feedback;
import lk.vakapo.vakapo.UserManagement.repository.ClinicRepository;
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
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import jakarta.servlet.http.HttpServletRequest;
import java.util.Optional;
import java.util.List;
import java.util.Map;
import java.util.HashMap;

@Controller
@RequestMapping("/clinic")
@RequiredArgsConstructor
@Slf4j
public class ClinicProfileController {

    private final ClinicRepository clinicRepository;
    private final UserRepository userRepository;
    private final FeedbackService feedbackService;
    private final FeedbackRepository feedbackRepository;
    private final EmailService emailService;

    /**
     * Display clinic landing page with dynamic data
     */
    @GetMapping("/landing")
    public String clinicLanding(Model model) {
        try {
            // Get current authenticated user
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            String userEmail = authentication.getName();
            
            log.info("Loading landing page for clinic: {}", userEmail);
            
            // Find clinic by email
            Optional<Clinic> clinicOpt = clinicRepository.findByEmail(userEmail);
            
            if (clinicOpt.isPresent()) {
                Clinic clinic = clinicOpt.get();
                
                // Add clinic data to model
                model.addAttribute("clinic", clinic);
                model.addAttribute("clinicName", clinic.getUsername() != null ? clinic.getUsername() : "Clinic");
                
                // Add user information for contact form auto-fill
                addContactFormUserInfo(model, clinic);
                
                // Add approved feedback for reviews section
                model.addAttribute("reviews", feedbackService.getApprovedFeedbackForLanding());
                
                log.info("Clinic landing page loaded successfully for: {}", clinic.getUsername());
                return "clinic/landingPage/LandingPage";
            } else {
                log.error("Clinic not found for email: {}", userEmail);
                model.addAttribute("error", "Clinic profile not found");
                return "error/404";
            }
        } catch (Exception e) {
            log.error("Error loading clinic landing page: {}", e.getMessage(), e);
            model.addAttribute("error", "Error loading page");
            return "error/500";
        }
    }

    /**
     * Display clinic profile page with dynamic data
     */
    @GetMapping("/profile")
    public String clinicProfile(Model model) {
        try {
            // Get current authenticated user
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            String userEmail = authentication.getName();
            
            log.info("Loading profile for clinic: {}", userEmail);
            
            // Find clinic by email
            Optional<Clinic> clinicOpt = clinicRepository.findByEmail(userEmail);
            
            if (clinicOpt.isPresent()) {
                Clinic clinic = clinicOpt.get();
                
                // Add clinic data to model
                model.addAttribute("clinic", clinic);
                model.addAttribute("clinicName", clinic.getUsername() != null ? clinic.getUsername() : "Clinic");
                model.addAttribute("clinicId", clinic.getId());
                model.addAttribute("clinicEmail", clinic.getEmail());
                model.addAttribute("clinicContact", clinic.getContact() != null ? clinic.getContact() : "N/A");
                model.addAttribute("clinicLocation", clinic.getAddress() != null ? clinic.getAddress() : "N/A");
                model.addAttribute("clinicInstitution", clinic.getInstitution() != null ? clinic.getInstitution() : "N/A");
                model.addAttribute("clinicRnumber", clinic.getRnumber() != null ? clinic.getRnumber() : "N/A");
                model.addAttribute("clinicStatus", clinic.getStatus() != null ? clinic.getStatus() : "N/A");
                model.addAttribute("clinicAdminApproval", clinic.getAdminApproval() != null ? clinic.getAdminApproval() : "N/A");
                
                log.info("Clinic profile loaded successfully for: {}", clinic.getUsername());
                return "clinic/profilePage/HospitalProfilePage";
            } else {
                log.error("Clinic not found for email: {}", userEmail);
                model.addAttribute("error", "Clinic profile not found");
                return "error/404";
            }
        } catch (Exception e) {
            log.error("Error loading clinic profile: {}", e.getMessage(), e);
            model.addAttribute("error", "Error loading profile");
            return "error/500";
        }
    }

    /**
     * Update clinic profile
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
            
            log.info("Updating profile for clinic: {}", userEmail);
            
            // Find clinic by email
            Optional<Clinic> clinicOpt = clinicRepository.findByEmail(userEmail);
            
            if (clinicOpt.isPresent()) {
                Clinic clinic = clinicOpt.get();
                
                // Store original email for comparison
                String originalEmail = clinic.getEmail();
                
                // Update clinic data
                clinic.setUsername(username);
                clinic.setEmail(email);
                clinic.setContact(contact);
                
                // Update optional fields if provided
                if (address != null && !address.trim().isEmpty()) {
                    clinic.setAddress(address);
                }
                if (institution != null && !institution.trim().isEmpty()) {
                    clinic.setInstitution(institution);
                }
                if (rnumber != null && !rnumber.trim().isEmpty()) {
                    clinic.setRnumber(rnumber);
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
                    log.info("Password update requested for clinic: {}", userEmail);
                    
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
                
                // Save updated clinic
                clinicRepository.save(clinic);
                
                log.info("Clinic profile updated successfully for: {}", userEmail);
                redirectAttributes.addFlashAttribute("successMessage", "Profile updated successfully!");
                
                return "redirect:/clinic/profile";
            } else {
                log.error("Clinic not found for email: {}", userEmail);
                redirectAttributes.addFlashAttribute("errorMessage", "Clinic not found");
                return "redirect:/clinic/profile";
            }
        } catch (Exception e) {
            log.error("Error updating clinic profile: {}", e.getMessage(), e);
            redirectAttributes.addFlashAttribute("errorMessage", "Error updating profile. Please try again.");
            return "redirect:/clinic/profile";
        }
    }

    /**
     * Test endpoint to verify clinic data fetching
     */
    @GetMapping("/test-data")
    public String testClinicData() {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            String userEmail = authentication.getName();
            
            log.info("Testing clinic data for: {}", userEmail);
            
            Optional<Clinic> clinicOpt = clinicRepository.findByEmail(userEmail);
            
            if (clinicOpt.isPresent()) {
                Clinic clinic = clinicOpt.get();
                return "Clinic found: " + clinic.getUsername() + " (ID: " + clinic.getId() + ")";
            } else {
                return "Clinic not found for email: " + userEmail;
            }
        } catch (Exception e) {
            log.error("Error testing clinic data: {}", e.getMessage(), e);
            return "Error: " + e.getMessage();
        }
    }

    /**
     * Simple test endpoint to check if clinic profile route is working
     */
    @GetMapping("/test-route")
    public String testRoute() {
        return "Clinic profile route is working!";
    }

    /**
     * Test endpoint to check clinic name data
     */
    @GetMapping("/test-name")
    public String testClinicName() {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            String userEmail = authentication.getName();
            
            log.info("Testing clinic name for user: {}", userEmail);
            
            Optional<Clinic> clinicOpt = clinicRepository.findByEmail(userEmail);
            
            if (clinicOpt.isPresent()) {
                Clinic clinic = clinicOpt.get();
                String username = clinic.getUsername();
                String email = clinic.getEmail();
                String id = clinic.getId();
                
                return String.format("Clinic found - ID: %s, Email: %s, Username: %s (null: %s)", 
                    id, email, username, username == null);
            } else {
                return "Clinic not found for email: " + userEmail;
            }
        } catch (Exception e) {
            log.error("Error testing clinic name: {}", e.getMessage(), e);
            return "Error: " + e.getMessage();
        }
    }

    /**
     * Display clinic feedback page
     */
    @GetMapping("/feedback")
    public String clinicFeedback(Model model) {
        try {
            // Get current authenticated user
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            String userEmail = authentication.getName();
            
            log.info("Loading feedback page for clinic: {}", userEmail);
            
            // Find clinic by email
            Optional<Clinic> clinicOpt = clinicRepository.findByEmail(userEmail);
            
            if (clinicOpt.isPresent()) {
                Clinic clinic = clinicOpt.get();
                
                // Add clinic data to model
                model.addAttribute("clinic", clinic);
                model.addAttribute("clinicName", clinic.getUsername() != null ? clinic.getUsername() : "Clinic");
                model.addAttribute("clinicId", clinic.getId());
                model.addAttribute("clinicEmail", clinic.getEmail());
                
                log.info("Clinic feedback page loaded successfully for: {}", clinic.getUsername());
                return "clinic/feedbackPage/HospitalFeedbackPage";
            } else {
                log.error("Clinic not found for email: {}", userEmail);
                model.addAttribute("error", "Clinic profile not found");
                return "error/404";
            }
        } catch (Exception e) {
            log.error("Error loading clinic feedback page: {}", e.getMessage(), e);
            model.addAttribute("error", "Error loading page");
            return "error/500";
        }
    }

    /**
     * Submit clinic feedback
     */
    @PostMapping("/feedback")
    public String submitClinicFeedback(
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
            
            log.info("Submitting clinic feedback from: {}", userEmail);
            
            // Find clinic by email
            Optional<Clinic> clinicOpt = clinicRepository.findByEmail(userEmail);
            
            if (clinicOpt.isPresent()) {
                Clinic clinic = clinicOpt.get();
                
                // Create feedback object
                Feedback feedback = new Feedback();
                feedback.setName(name);
                feedback.setEmail(email);
                feedback.setContactNo(contactno);
                feedback.setRating(rating);
                feedback.setMessage(message);
                feedback.setUserType("CLINIC");
                feedback.setUserId(clinic.getId());
                
                // Submit feedback
                log.info("About to submit feedback: {}", feedback);
                Feedback savedFeedback = feedbackService.submitFeedback(feedback);
                log.info("Feedback submitted successfully with ID: {}", savedFeedback.getId());
                
                // Add success message
                model.addAttribute("successMessage", "Thank you for your feedback! It will be reviewed and may appear on our landing pages.");
                model.addAttribute("clinic", clinic);
                model.addAttribute("clinicName", clinic.getUsername() != null ? clinic.getUsername() : "Clinic");
                model.addAttribute("clinicId", clinic.getId());
                model.addAttribute("clinicEmail", clinic.getEmail());
                
                log.info("Clinic feedback submitted successfully by: {}", userEmail);
                return "clinic/feedbackPage/HospitalFeedbackPage";
            } else {
                log.error("Clinic not found for email: {}", userEmail);
                return "error/404";
            }
        } catch (Exception e) {
            log.error("Error submitting clinic feedback: {}", e.getMessage(), e);
            model.addAttribute("errorMessage", "Error submitting feedback. Please try again.");
            return "clinic/feedbackPage/HospitalFeedbackPage";
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
            
            // Find clinic by email
            Optional<Clinic> clinicOpt = clinicRepository.findByEmail(userEmail);
            
            if (clinicOpt.isPresent()) {
                Clinic clinic = clinicOpt.get();
                
                // Get user's feedback
                List<Feedback> myReviews = feedbackRepository.findByUserIdOrderByCreatedAtDesc(clinic.getId());
                
                response.put("success", true);
                response.put("data", myReviews);
                response.put("count", myReviews.size());
                
                return response;
            } else {
                response.put("success", false);
                response.put("message", "Clinic not found");
                return response;
            }
        } catch (Exception e) {
            log.error("Error getting clinic reviews: {}", e.getMessage(), e);
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
            
            // Find clinic by email
            Optional<Clinic> clinicOpt = clinicRepository.findByEmail(userEmail);
            
            if (clinicOpt.isPresent()) {
                Clinic clinic = clinicOpt.get();
                
                // Update feedback
                boolean success = feedbackService.updateFeedback(feedbackId, clinic.getId(), name, email, contactno, rating, message);
                
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
                response.put("message", "Clinic not found");
                return response;
            }
        } catch (Exception e) {
            log.error("Error updating clinic review: {}", e.getMessage(), e);
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
            
            // Find clinic by email
            Optional<Clinic> clinicOpt = clinicRepository.findByEmail(userEmail);
            
            if (clinicOpt.isPresent()) {
                Clinic clinic = clinicOpt.get();
                
                // Delete feedback
                boolean success = feedbackService.deleteFeedbackByUser(feedbackId, clinic.getId());
                
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
                response.put("message", "Clinic not found");
                return response;
            }
        } catch (Exception e) {
            log.error("Error deleting clinic review: {}", e.getMessage(), e);
            response.put("success", false);
            response.put("message", "Error deleting review");
            return response;
        }
    }

    /**
     * Delete clinic account
     */
    @PostMapping("/delete-account")
    public String deleteAccount(RedirectAttributes redirectAttributes) {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            String userEmail = authentication.getName();

            log.info("Deleting account for clinic: {}", userEmail);

            // Find clinic by email
            Optional<Clinic> clinicOpt = clinicRepository.findByEmail(userEmail);

            if (clinicOpt.isPresent()) {
                Clinic clinic = clinicOpt.get();
                String clinicId = clinic.getId();

                // Delete from Clinic table
                clinicRepository.delete(clinic);

                // Delete from UserAccount table
                Optional<UserAccount> userOpt = userRepository.findByEmail(userEmail);
                if (userOpt.isPresent()) {
                    userRepository.delete(userOpt.get());
                }

                log.info("Clinic account deleted successfully: {}", clinicId);
                
                // Send account deletion email
                try {
                    emailService.sendAccountDeletionEmail(userEmail, clinic.getUsername(), "Clinic");
                    log.info("Account deletion email sent to clinic: {}", userEmail);
                } catch (Exception e) {
                    log.error("Failed to send account deletion email to clinic: {}", userEmail, e);
                    // Don't fail the deletion if email fails
                }
                
                redirectAttributes.addFlashAttribute("successMessage", "Your account has been deleted successfully.");
                
                // Redirect to login page after deletion
                return "redirect:/login?deleted=true";
            } else {
                log.error("Clinic not found for email: {}", userEmail);
                redirectAttributes.addFlashAttribute("errorMessage", "Account not found");
                return "redirect:/clinic/profile";
            }
        } catch (Exception e) {
            log.error("Error deleting clinic account: {}", e.getMessage(), e);
            redirectAttributes.addFlashAttribute("errorMessage", "Error deleting account. Please try again.");
            return "redirect:/clinic/profile";
        }
    }
    
    /**
     * Add user information for contact form auto-fill
     */
    private void addContactFormUserInfo(Model model, Clinic clinic) {
        try {
            model.addAttribute("isLoggedIn", true);
            model.addAttribute("userEmail", clinic.getEmail());
            model.addAttribute("userName", clinic.getUsername() != null ? clinic.getUsername() : extractUserNameFromEmail(clinic.getEmail()));
            model.addAttribute("userPhone", clinic.getContact() != null ? clinic.getContact() : "");
            
        } catch (Exception e) {
            log.error("Error adding contact form user info: {}", e.getMessage());
            // Set default values
            model.addAttribute("isLoggedIn", true);
            model.addAttribute("userEmail", clinic.getEmail());
            model.addAttribute("userName", extractUserNameFromEmail(clinic.getEmail()));
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
