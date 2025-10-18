package lk.vakapo.vakapo.UserManagement.controller;

import lk.vakapo.vakapo.UserManagement.model.Feedback;
import lk.vakapo.vakapo.UserManagement.service.FeedbackService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Controller
@RequestMapping("/admin")
@RequiredArgsConstructor
@Slf4j
public class AdminFeedbackController {

    private final FeedbackService feedbackService;

    /**
     * Display admin feedback management page
     */
    @GetMapping("/feedback-manager")
    public String adminFeedback(Model model) {
        try {
            // Get current authenticated user
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            String userEmail = authentication.getName();
            
            log.info("Loading admin feedback management for: {}", userEmail);
            
            // Get all feedback and pending feedback
            List<Feedback> allFeedback = feedbackService.getAllFeedback();
            List<Feedback> pendingFeedback = feedbackService.getPendingFeedback();
            FeedbackService.FeedbackStats stats = feedbackService.getFeedbackStats();
            
            // Add data to model
            model.addAttribute("allFeedback", allFeedback);
            model.addAttribute("pendingFeedback", pendingFeedback);
            model.addAttribute("stats", stats);
            model.addAttribute("adminEmail", userEmail);
            
            log.info("Admin feedback management loaded successfully for: {}", userEmail);
            return "admin/feedback/AdminFeedback";
        } catch (Exception e) {
            log.error("Error loading admin feedback management: {}", e.getMessage(), e);
            model.addAttribute("error", "Error loading feedback management");
            return "error/500";
        }
    }

    /**
     * Approve feedback
     */
    @PostMapping("/feedback/approve/{feedbackId}")
    public String approveFeedback(@PathVariable Long feedbackId, RedirectAttributes redirectAttributes) {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            String adminEmail = authentication.getName();
            
            log.info("Approving feedback {} by admin: {}", feedbackId, adminEmail);
            
            boolean success = feedbackService.approveFeedback(feedbackId, adminEmail);
            
            if (success) {
                redirectAttributes.addFlashAttribute("successMessage", "Feedback approved successfully!");
                log.info("Feedback {} approved successfully by {}", feedbackId, adminEmail);
            } else {
                redirectAttributes.addFlashAttribute("errorMessage", "Failed to approve feedback. Feedback may not exist.");
                log.warn("Failed to approve feedback {} by {}", feedbackId, adminEmail);
            }
            
            return "redirect:/admin/feedback-manager";
        } catch (Exception e) {
            log.error("Error approving feedback {}: {}", feedbackId, e.getMessage(), e);
            redirectAttributes.addFlashAttribute("errorMessage", "Error approving feedback: " + e.getMessage());
            return "redirect:/admin/feedback-manager";
        }
    }

    /**
     * Reject feedback
     */
    @PostMapping("/feedback/reject/{feedbackId}")
    public String rejectFeedback(@PathVariable Long feedbackId, RedirectAttributes redirectAttributes) {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            String adminEmail = authentication.getName();
            
            log.info("Rejecting feedback {} by admin: {}", feedbackId, adminEmail);
            
            boolean success = feedbackService.rejectFeedback(feedbackId);
            
            if (success) {
                redirectAttributes.addFlashAttribute("successMessage", "Feedback rejected and deleted successfully!");
                log.info("Feedback {} rejected successfully by {}", feedbackId, adminEmail);
            } else {
                redirectAttributes.addFlashAttribute("errorMessage", "Failed to reject feedback. Feedback may not exist.");
                log.warn("Failed to reject feedback {} by {}", feedbackId, adminEmail);
            }
            
            return "redirect:/admin/feedback-manager";
        } catch (Exception e) {
            log.error("Error rejecting feedback {}: {}", feedbackId, e.getMessage(), e);
            redirectAttributes.addFlashAttribute("errorMessage", "Error rejecting feedback: " + e.getMessage());
            return "redirect:/admin/feedback-manager";
        }
    }

    /**
     * Get feedback statistics (API endpoint)
     */
    @GetMapping("/feedback/stats")
    @ResponseBody
    public FeedbackService.FeedbackStats getFeedbackStats() {
        try {
            return feedbackService.getFeedbackStats();
        } catch (Exception e) {
            log.error("Error getting feedback statistics: {}", e.getMessage(), e);
            return new FeedbackService.FeedbackStats(0, 0, 0.0);
        }
    }

    /**
     * Test endpoint to verify database connection and feedback service
     */
    @GetMapping("/api/feedback/test")
    @ResponseBody
    public Map<String, Object> testFeedbackService() {
        Map<String, Object> response = new HashMap<>();
        try {
            log.info("Testing feedback service...");
            
            // Test getting all feedback
            List<Feedback> allFeedback = feedbackService.getAllFeedback();
            log.info("Retrieved {} total feedback items", allFeedback.size());
            
            // Test getting pending feedback
            List<Feedback> pendingFeedback = feedbackService.getPendingFeedback();
            log.info("Retrieved {} pending feedback items", pendingFeedback.size());
            
            // Test getting approved feedback
            List<Feedback> approvedFeedback = feedbackService.getApprovedFeedbackForLanding();
            log.info("Retrieved {} approved feedback items", approvedFeedback.size());
            
            // Test getting stats
            FeedbackService.FeedbackStats stats = feedbackService.getFeedbackStats();
            log.info("Stats: approved={}, pending={}", stats.getApprovedCount(), stats.getPendingCount());
            
            response.put("success", true);
            response.put("message", "Feedback service is working correctly");
            response.put("totalFeedback", allFeedback.size());
            response.put("pendingFeedback", pendingFeedback.size());
            response.put("approvedFeedback", approvedFeedback.size());
            response.put("stats", stats);
            
            return response;
        } catch (Exception e) {
            log.error("Error testing feedback service: {}", e.getMessage(), e);
            response.put("success", false);
            response.put("message", "Error testing feedback service: " + e.getMessage());
            response.put("error", e.getClass().getSimpleName());
            return response;
        }
    }

    /**
     * API endpoint to get feedback list with pagination and filtering
     */
    @GetMapping("/api/feedback")
    @ResponseBody
    public Map<String, Object> getFeedbackList(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String search,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDir) {
        
        Map<String, Object> response = new HashMap<>();
        try {
            log.info("API call: getFeedbackList with params - page: {}, size: {}, status: {}, search: {}", page, size, status, search);
            
            List<Feedback> feedbackList = feedbackService.getFeedbackList(page, size, status, search, sortBy, sortDir);
            FeedbackService.FeedbackStats stats = feedbackService.getFeedbackStats();
            
            log.info("Retrieved {} feedback items, stats: approved={}, pending={}", 
                feedbackList.size(), stats.getApprovedCount(), stats.getPendingCount());
            
            response.put("success", true);
            response.put("data", feedbackList);
            response.put("stats", stats);
            response.put("totalElements", feedbackList.size());
            response.put("totalPages", 1); // Simplified for now
            
            return response;
        } catch (Exception e) {
            log.error("Error getting feedback list: {}", e.getMessage(), e);
            response.put("success", false);
            response.put("message", "Error retrieving feedback list: " + e.getMessage());
            return response;
        }
    }

    /**
     * API endpoint to get specific feedback details
     */
    @GetMapping("/api/feedback/{id}")
    @ResponseBody
    public Map<String, Object> getFeedbackDetails(@PathVariable Long id) {
        Map<String, Object> response = new HashMap<>();
        try {
            log.info("API call: getFeedbackDetails for ID: {}", id);
            Optional<Feedback> feedbackOpt = feedbackService.getFeedbackById(id);
            
            if (feedbackOpt.isPresent()) {
                Feedback feedback = feedbackOpt.get();
                log.info("Found feedback: ID={}, Name={}, Email={}, Approved={}", 
                    feedback.getId(), feedback.getName(), feedback.getEmail(), feedback.getIsApproved());
                response.put("success", true);
                response.put("data", feedback);
            } else {
                log.warn("Feedback not found for ID: {}", id);
                response.put("success", false);
                response.put("message", "Feedback not found");
            }
            
            return response;
        } catch (Exception e) {
            log.error("Error getting feedback details for ID {}: {}", id, e.getMessage(), e);
            response.put("success", false);
            response.put("message", "Error retrieving feedback details: " + e.getMessage());
            return response;
        }
    }

    /**
     * API endpoint to update feedback status
     */
    @PostMapping("/api/feedback/{id}/status")
    @ResponseBody
    public Map<String, Object> updateFeedbackStatus(
            @PathVariable Long id,
            @RequestParam String status,
            @RequestParam(required = false) String adminResponse) {
        
        Map<String, Object> response = new HashMap<>();
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            String adminEmail = authentication.getName();
            
            boolean success = false;
            if ("APPROVED".equals(status)) {
                success = feedbackService.approveFeedback(id, adminEmail);
            } else if ("REJECTED".equals(status)) {
                success = feedbackService.rejectFeedback(id, adminResponse);
            }
            
            if (success) {
                response.put("success", true);
                if ("APPROVED".equals(status)) {
                    response.put("message", "Feedback approved successfully and user has been notified via email");
                } else if ("REJECTED".equals(status)) {
                    if (adminResponse != null && !adminResponse.trim().isEmpty()) {
                        response.put("message", "Feedback rejected and deleted successfully. User has been notified via email.");
                    } else {
                        response.put("message", "Feedback rejected and deleted successfully.");
                    }
                } else {
                    response.put("message", "Feedback status updated successfully");
                }
            } else {
                response.put("success", false);
                response.put("message", "Failed to update feedback status. Feedback may not exist.");
            }
            
            return response;
        } catch (Exception e) {
            log.error("Error updating feedback status for ID {}: {}", id, e.getMessage(), e);
            response.put("success", false);
            response.put("message", "Error updating feedback status");
            return response;
        }
    }
}
