package lk.vakapo.vakapo.UserManagement.controller;

import lk.vakapo.vakapo.UserManagement.model.Vaccine;
import lk.vakapo.vakapo.UserManagement.service.VaccineService;
import lk.vakapo.vakapo.UserManagement.service.VaccineNotificationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Controller
@RequestMapping("/admin/vaccines")
@RequiredArgsConstructor
@Slf4j
public class VaccineController {

    private final VaccineService vaccineService;
    private final VaccineNotificationService vaccineNotificationService;

    /**
     * Display vaccine management page
     */
    @GetMapping("/manage")
    public String vaccineManagementPage(Model model) {
        try {
            List<Vaccine> vaccines = vaccineService.getAllVaccines();
            VaccineService.VaccineStats stats = vaccineService.getVaccineStats();
            
            model.addAttribute("vaccines", vaccines);
            model.addAttribute("stats", stats);
            model.addAttribute("newVaccine", new Vaccine());
            
            return "admin/vaccines/VaccineManager";
            
        } catch (Exception e) {
            log.error("Error loading vaccine management page: {}", e.getMessage(), e);
            model.addAttribute("error", "Failed to load vaccine management page");
            return "admin/vaccines/VaccineManager";
        }
    }

    /**
     * Create a new vaccine
     */
    @PostMapping("/create")
    public String createVaccine(@RequestParam("vaccineName") String vaccineName,
                               RedirectAttributes redirectAttributes) {
        try {
            if (vaccineName == null || vaccineName.trim().isEmpty()) {
                redirectAttributes.addFlashAttribute("error", "Vaccine name cannot be empty");
                return "redirect:/admin/vaccines/manage";
            }

            Vaccine createdVaccine = vaccineService.createVaccine(vaccineName.trim());
            redirectAttributes.addFlashAttribute("success", 
                "Vaccine '" + createdVaccine.getVaccineName() + "' created successfully");
            
            // Send notification to all hospitals and clinics
            vaccineNotificationService.notifyVaccineAdded(createdVaccine.getVaccineName());
            
            log.info("Admin created new vaccine: {}", createdVaccine.getVaccineName());
            
        } catch (IllegalArgumentException e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        } catch (Exception e) {
            log.error("Error creating vaccine: {}", e.getMessage(), e);
            redirectAttributes.addFlashAttribute("error", "Failed to create vaccine. Please try again.");
        }
        
        return "redirect:/admin/vaccines/manage";
    }

    /**
     * Update vaccine
     */
    @PostMapping("/update/{id}")
    public String updateVaccine(@PathVariable Long id,
                               @RequestParam("vaccineName") String vaccineName,
                               RedirectAttributes redirectAttributes) {
        try {
            if (vaccineName == null || vaccineName.trim().isEmpty()) {
                redirectAttributes.addFlashAttribute("error", "Vaccine name cannot be empty");
                return "redirect:/admin/vaccines/manage";
            }

            Vaccine updatedVaccine = vaccineService.updateVaccine(id, vaccineName.trim());
            redirectAttributes.addFlashAttribute("success", 
                "Vaccine updated to '" + updatedVaccine.getVaccineName() + "' successfully");
            
            log.info("Admin updated vaccine ID {}: {}", id, updatedVaccine.getVaccineName());
            
        } catch (IllegalArgumentException e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        } catch (Exception e) {
            log.error("Error updating vaccine ID {}: {}", id, e.getMessage(), e);
            redirectAttributes.addFlashAttribute("error", "Failed to update vaccine. Please try again.");
        }
        
        return "redirect:/admin/vaccines/manage";
    }

    /**
     * Toggle vaccine status (active/inactive)
     */
    @PostMapping("/toggle-status/{id}")
    public String toggleVaccineStatus(@PathVariable Long id,
                                    RedirectAttributes redirectAttributes) {
        try {
            Vaccine updatedVaccine = vaccineService.toggleVaccineStatus(id);
            String status = updatedVaccine.getIsActive() ? "activated" : "deactivated";
            redirectAttributes.addFlashAttribute("success", 
                "Vaccine '" + updatedVaccine.getVaccineName() + "' " + status + " successfully");
            
            // Send notification to all hospitals and clinics
            vaccineNotificationService.notifyVaccineStatusChanged(updatedVaccine.getVaccineName(), updatedVaccine.getIsActive());
            
            log.info("Admin toggled vaccine ID {} status: {}", id, status);
            
        } catch (IllegalArgumentException e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        } catch (Exception e) {
            log.error("Error toggling vaccine status ID {}: {}", id, e.getMessage(), e);
            redirectAttributes.addFlashAttribute("error", "Failed to update vaccine status. Please try again.");
        }
        
        return "redirect:/admin/vaccines/manage";
    }

    /**
     * Delete vaccine
     */
    @PostMapping("/delete/{id}")
    public String deleteVaccine(@PathVariable Long id,
                               RedirectAttributes redirectAttributes) {
        try {
            // Get vaccine name before deletion for success message
            Vaccine vaccine = vaccineService.getVaccineById(id).orElse(null);
            String vaccineName = vaccine != null ? vaccine.getVaccineName() : "Unknown";
            
            boolean deleted = vaccineService.deleteVaccine(id);
            if (deleted) {
                redirectAttributes.addFlashAttribute("success", 
                    "Vaccine '" + vaccineName + "' deleted successfully");
                
                // Send notification to all hospitals and clinics
                vaccineNotificationService.notifyVaccineDeleted(vaccineName);
                
                log.info("Admin deleted vaccine: {}", vaccineName);
            }
            
        } catch (IllegalArgumentException e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        } catch (Exception e) {
            log.error("Error deleting vaccine ID {}: {}", id, e.getMessage(), e);
            redirectAttributes.addFlashAttribute("error", "Failed to delete vaccine. Please try again.");
        }
        
        return "redirect:/admin/vaccines/manage";
    }

    /**
     * Search vaccines (AJAX endpoint)
     */
    @GetMapping("/search")
    @ResponseBody
    public ResponseEntity<?> searchVaccines(@RequestParam(required = false) String searchTerm) {
        try {
            List<Vaccine> vaccines = vaccineService.searchVaccines(searchTerm);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("vaccines", vaccines);
            response.put("count", vaccines.size());
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            log.error("Error searching vaccines: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError().body("{\"error\": \"Failed to search vaccines\"}");
        }
    }

    /**
     * Get vaccine statistics (AJAX endpoint)
     */
    @GetMapping("/stats")
    @ResponseBody
    public ResponseEntity<?> getVaccineStats() {
        try {
            VaccineService.VaccineStats stats = vaccineService.getVaccineStats();
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("stats", stats);
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            log.error("Error getting vaccine statistics: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError().body("{\"error\": \"Failed to get vaccine statistics\"}");
        }
    }
}
