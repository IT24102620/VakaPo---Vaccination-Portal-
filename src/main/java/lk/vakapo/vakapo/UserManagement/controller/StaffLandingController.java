package lk.vakapo.vakapo.UserManagement.controller;

import lk.vakapo.vakapo.UserManagement.model.UserAccount;
import lk.vakapo.vakapo.UserManagement.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.Optional;

@Controller
@RequiredArgsConstructor
public class StaffLandingController {

    private final UserRepository userRepository;

    @GetMapping("/staff/landing")
    public String staffLanding() {
        // Security check - ensure user is a staff member
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String userEmail = authentication.getName();
        
        Optional<UserAccount> userOpt = userRepository.findByEmail(userEmail);
        if (userOpt.isEmpty() || !isStaffMember(userOpt.get().getId())) {
            return "error/403"; // Access denied
        }
        
        return "staff/landingPage/StaffLandingPage";
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
