package lk.vakapo.vakapo.UserManagement.controller;

import lk.vakapo.vakapo.UserManagement.model.ContactInfo;
import lk.vakapo.vakapo.UserManagement.service.ContactService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.security.Principal;

import java.util.List;

@Controller
@RequiredArgsConstructor
@Slf4j
public class ContactController {
    
    private final ContactService contactService;
    
    /**
     * Handle contact form submission
     */
    @PostMapping("/contact")
    public String submitContactForm(
            @RequestParam String fullName,
            @RequestParam String emailAddress,
            @RequestParam String phoneNumber,
            @RequestParam String message,
            @RequestParam(required = false) String returnUrl,
            RedirectAttributes redirectAttributes) {
        
        try {
            log.info("Processing contact form submission from: {}", emailAddress);
            
            // Submit the contact form
            ContactInfo contactInfo = contactService.submitContactForm(
                fullName, emailAddress, phoneNumber, message);
            
            log.info("Contact form submitted successfully with ID: {}", contactInfo.getId());
            
            redirectAttributes.addFlashAttribute("successMessage", 
                "Thank you for your message! We have received your inquiry and will get back to you as soon as possible.");
            
            // Redirect to the return URL if provided, otherwise to the landing page
            String redirectUrl = (returnUrl != null && !returnUrl.isEmpty()) ? returnUrl : "/";
            return "redirect:" + redirectUrl + "?contact=success";
            
        } catch (Exception e) {
            log.error("Error processing contact form submission from: {}", emailAddress, e);
            redirectAttributes.addFlashAttribute("errorMessage", 
                "Sorry, there was an error submitting your message. Please try again or contact us directly.");
            
            // Redirect to the return URL if provided, otherwise to the landing page
            String redirectUrl = (returnUrl != null && !returnUrl.isEmpty()) ? returnUrl : "/";
            return "redirect:" + redirectUrl + "?contact=error";
        }
    }
    
    
    /**
     * Admin endpoint to view all contacts
     */
    @GetMapping("/admin/contacts")
    public String viewAllContacts(
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String search,
            Model model) {
        try {
            List<ContactInfo> contacts;
            
            if (status != null && !status.isEmpty()) {
                contacts = contactService.getContactsByStatus(status);
            } else if (search != null && !search.isEmpty()) {
                contacts = contactService.searchContacts(search);
            } else {
                contacts = contactService.getAllContacts();
            }
            
            model.addAttribute("contacts", contacts);
            
            // Add statistics
            long pendingCount = contactService.getContactCountByStatus("pending");
            long reviewedCount = contactService.getContactCountByStatus("reviewed");
            long respondedCount = contactService.getContactCountByStatus("responded");
            
            model.addAttribute("pendingCount", pendingCount);
            model.addAttribute("reviewedCount", reviewedCount);
            model.addAttribute("respondedCount", respondedCount);
            
            return "admin/contacts/ContactManagement";
            
        } catch (Exception e) {
            log.error("Error loading contacts for admin view", e);
            model.addAttribute("errorMessage", "Error loading contacts. Please try again.");
            model.addAttribute("contacts", List.of());
            model.addAttribute("pendingCount", 0);
            model.addAttribute("reviewedCount", 0);
            model.addAttribute("respondedCount", 0);
            return "admin/contacts/ContactManagement";
        }
    }
    
    /**
     * Admin endpoint to view pending contacts
     */
    @GetMapping("/admin/contacts/pending")
    public String viewPendingContacts(Model model) {
        try {
            List<ContactInfo> pendingContacts = contactService.getPendingContacts();
            model.addAttribute("contacts", pendingContacts);
            model.addAttribute("status", "pending");
            
            return "admin/contacts/ContactManagement";
            
        } catch (Exception e) {
            log.error("Error loading pending contacts", e);
            model.addAttribute("errorMessage", "Error loading pending contacts. Please try again.");
            return "admin/contacts/ContactManagement";
        }
    }
    
    /**
     * Admin endpoint to update contact status
     */
    @PostMapping("/admin/contacts/{id}/update-status")
    public String updateContactStatus(
            @PathVariable Long id,
            @RequestParam String status,
            @RequestParam(required = false) String response,
            @RequestParam(required = false) String respondedBy,
            Principal principal,
            RedirectAttributes redirectAttributes) {
        
        try {
            // Use the admin's email as respondedBy if not provided
            String adminEmail = (respondedBy != null && !respondedBy.isEmpty()) ? respondedBy : 
                               (principal != null ? principal.getName() : "Admin");
            
            if (response != null && !response.trim().isEmpty()) {
                // Update with response
                contactService.updateContactWithResponse(id, status, response, adminEmail);
                redirectAttributes.addFlashAttribute("successMessage", 
                    "Contact updated with response and email sent successfully.");
            } else {
                // Update status only
                contactService.updateContactStatus(id, status);
                redirectAttributes.addFlashAttribute("successMessage", 
                    "Contact status updated successfully.");
            }
            
            return "redirect:/admin/contacts";
            
        } catch (Exception e) {
            log.error("Error updating contact status for ID: {}", id, e);
            redirectAttributes.addFlashAttribute("errorMessage", 
                "Error updating contact status. Please try again.");
            
            return "redirect:/admin/contacts";
        }
    }
    
    /**
     * Admin endpoint to delete contact
     */
    @PostMapping("/admin/contacts/{id}/delete")
    public String deleteContact(
            @PathVariable Long id,
            RedirectAttributes redirectAttributes) {
        
        try {
            contactService.deleteContact(id);
            
            redirectAttributes.addFlashAttribute("successMessage", 
                "Contact deleted successfully.");
            
            return "redirect:/admin/contacts";
            
        } catch (Exception e) {
            log.error("Error deleting contact with ID: {}", id, e);
            redirectAttributes.addFlashAttribute("errorMessage", 
                "Error deleting contact. Please try again.");
            
            return "redirect:/admin/contacts";
        }
    }
    
    /**
     * API endpoint to get contact statistics
     */
    @GetMapping("/api/contacts/statistics")
    @ResponseBody
    public java.util.Map<String, Object> getContactStatistics() {
        try {
            java.util.Map<String, Object> stats = new java.util.HashMap<>();
            
            stats.put("total", contactService.getAllContacts().size());
            stats.put("pending", contactService.getContactCountByStatus("pending"));
            stats.put("reviewed", contactService.getContactCountByStatus("reviewed"));
            stats.put("responded", contactService.getContactCountByStatus("responded"));
            stats.put("recent", contactService.getRecentContacts().size());
            
            return stats;
            
        } catch (Exception e) {
            log.error("Error getting contact statistics", e);
            java.util.Map<String, Object> error = new java.util.HashMap<>();
            error.put("error", "Failed to get statistics");
            return error;
        }
    }
}
