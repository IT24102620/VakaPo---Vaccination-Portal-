package lk.vakapo.vakapo.controller;

import lk.vakapo.vakapo.UserManagement.service.FeedbackService;
import lk.vakapo.vakapo.UserManagement.service.ContactService;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

@Controller
@RequiredArgsConstructor
public class LandingController {

    private final FeedbackService feedbackService;
    private final ContactService contactService;

    @GetMapping("/")
    public String landingPage(
            @RequestParam(required = false) String contact,
            Model model) {
        try {
            // Add approved feedback for reviews section
            model.addAttribute("reviews", feedbackService.getApprovedFeedbackForLanding());
            
            // Add recent contact submissions for display (optional - for admin insights)
            model.addAttribute("recentContacts", contactService.getRecentContacts());
            
            // Add user information for contact form auto-fill
            addUserInfoToModel(model);
            
            // Handle contact form success/error messages
            if ("success".equals(contact)) {
                model.addAttribute("successMessage", 
                    "Thank you for your message! We have received your inquiry and will get back to you as soon as possible.");
            } else if ("error".equals(contact)) {
                model.addAttribute("errorMessage", 
                    "Sorry, there was an error submitting your message. Please try again or contact us directly.");
            }
            
        } catch (Exception e) {
            // If there's an error, just continue without reviews and contacts
            model.addAttribute("reviews", java.util.List.of());
            model.addAttribute("recentContacts", java.util.List.of());
        }
        return "LandingPage"; // corresponds to templates/LandingPage.html
    }

    @GetMapping("/login")
    public String loginPage() {
        return "Login"; // corresponds to templates/Login.html
    }

    @GetMapping("/register")
    public String signupPage() {
        return "Signup"; // corresponds to templates/Login.html
    }

    @GetMapping("/favicon.ico")
    public ResponseEntity<Resource> favicon() {
        try {
            Resource resource = new ClassPathResource("static/Images/logo.png");
            if (resource.exists()) {
                return ResponseEntity.ok()
                        .contentType(MediaType.IMAGE_PNG)
                        .body(resource);
            } else {
                return ResponseEntity.notFound().build();
            }
        } catch (Exception e) {
            return ResponseEntity.notFound().build();
        }
    }

    
    /**
     * Add user information to model for contact form auto-fill
     */
    private void addUserInfoToModel(Model model) {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            if (authentication != null && authentication.isAuthenticated() && 
                !"anonymousUser".equals(authentication.getName())) {
                
                String userEmail = authentication.getName();
                model.addAttribute("userEmail", userEmail);
                model.addAttribute("isLoggedIn", true);
                
                // Try to get user details based on role
                // This is a simplified approach - you might want to enhance this
                // by fetching actual user details from the database
                model.addAttribute("userName", extractUserNameFromEmail(userEmail));
                model.addAttribute("userPhone", ""); // Could be fetched from database if needed
                
            } else {
                model.addAttribute("isLoggedIn", false);
                model.addAttribute("userEmail", "");
                model.addAttribute("userName", "");
                model.addAttribute("userPhone", "");
            }
        } catch (Exception e) {
            // If there's an error, set default values
            model.addAttribute("isLoggedIn", false);
            model.addAttribute("userEmail", "");
            model.addAttribute("userName", "");
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
