package lk.vakapo.vakapo.UserManagement.controller;

import lk.vakapo.vakapo.UserManagement.service.StaffService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping("/staff/invitation")
@RequiredArgsConstructor
@Slf4j
public class StaffInvitationController {

    private final StaffService staffService;

    /**
     * Accept staff invitation
     */
    @GetMapping("/accept")
    public String acceptInvitation(@RequestParam String token, Model model) {
        try {
            String result = staffService.acceptStaffInvitation(token);
            model.addAttribute("success", true);
            model.addAttribute("message", result);
            model.addAttribute("title", "Invitation Accepted");
            
            log.info("Staff invitation accepted successfully with token: {}", token);
            return "staff/invitation/InvitationResponse";
            
        } catch (IllegalArgumentException e) {
            log.error("Invalid invitation token: {}", e.getMessage());
            model.addAttribute("success", false);
            model.addAttribute("error", e.getMessage());
            model.addAttribute("title", "Invalid Invitation");
            return "staff/invitation/InvitationResponse";
            
        } catch (Exception e) {
            log.error("Error accepting staff invitation: {}", e.getMessage(), e);
            model.addAttribute("success", false);
            model.addAttribute("error", "An unexpected error occurred. Please try again.");
            model.addAttribute("title", "Error");
            return "staff/invitation/InvitationResponse";
        }
    }

    /**
     * Reject staff invitation
     */
    @GetMapping("/reject")
    public String rejectInvitation(@RequestParam String token, Model model) {
        try {
            String result = staffService.rejectStaffInvitation(token);
            model.addAttribute("success", true);
            model.addAttribute("message", result);
            model.addAttribute("title", "Invitation Declined");
            
            log.info("Staff invitation rejected successfully with token: {}", token);
            return "staff/invitation/InvitationResponse";
            
        } catch (IllegalArgumentException e) {
            log.error("Invalid invitation token: {}", e.getMessage());
            model.addAttribute("success", false);
            model.addAttribute("error", e.getMessage());
            model.addAttribute("title", "Invalid Invitation");
            return "staff/invitation/InvitationResponse";
            
        } catch (Exception e) {
            log.error("Error rejecting staff invitation: {}", e.getMessage(), e);
            model.addAttribute("success", false);
            model.addAttribute("error", "An unexpected error occurred. Please try again.");
            model.addAttribute("title", "Error");
            return "staff/invitation/InvitationResponse";
        }
    }

    /**
     * API endpoint to accept invitation (for AJAX calls)
     */
    @PostMapping("/accept")
    @ResponseBody
    public ResponseEntity<?> acceptInvitationApi(@RequestParam String token) {
        try {
            String result = staffService.acceptStaffInvitation(token);
            log.info("Staff invitation accepted successfully via API with token: {}", token);
            return ResponseEntity.ok().body("{\"success\": true, \"message\": \"" + result + "\"}");
            
        } catch (IllegalArgumentException e) {
            log.error("Invalid invitation token via API: {}", e.getMessage());
            return ResponseEntity.badRequest().body("{\"success\": false, \"error\": \"" + e.getMessage() + "\"}");
            
        } catch (Exception e) {
            log.error("Error accepting staff invitation via API: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("{\"success\": false, \"error\": \"An unexpected error occurred. Please try again.\"}");
        }
    }

    /**
     * API endpoint to reject invitation (for AJAX calls)
     */
    @PostMapping("/reject")
    @ResponseBody
    public ResponseEntity<?> rejectInvitationApi(@RequestParam String token) {
        try {
            String result = staffService.rejectStaffInvitation(token);
            log.info("Staff invitation rejected successfully via API with token: {}", token);
            return ResponseEntity.ok().body("{\"success\": true, \"message\": \"" + result + "\"}");
            
        } catch (IllegalArgumentException e) {
            log.error("Invalid invitation token via API: {}", e.getMessage());
            return ResponseEntity.badRequest().body("{\"success\": false, \"error\": \"" + e.getMessage() + "\"}");
            
        } catch (Exception e) {
            log.error("Error rejecting staff invitation via API: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("{\"success\": false, \"error\": \"An unexpected error occurred. Please try again.\"}");
        }
    }
}
