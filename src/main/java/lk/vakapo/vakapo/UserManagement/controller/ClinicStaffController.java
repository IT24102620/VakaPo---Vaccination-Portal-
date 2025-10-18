package lk.vakapo.vakapo.UserManagement.controller;

import jakarta.validation.Valid;
import lk.vakapo.vakapo.UserManagement.dto.StaffInvitationRequest;
import lk.vakapo.vakapo.UserManagement.dto.StaffResponse;
import lk.vakapo.vakapo.UserManagement.model.Clinic;
import lk.vakapo.vakapo.UserManagement.model.Staff;
import lk.vakapo.vakapo.UserManagement.repository.ClinicRepository;
import lk.vakapo.vakapo.UserManagement.service.StaffService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Controller
@RequestMapping("/clinic")
@RequiredArgsConstructor
@Slf4j
public class ClinicStaffController {

    private final StaffService staffService;
    private final ClinicRepository clinicRepository;

    /**
     * Display add new staff page for clinic
     */
    @GetMapping("/add-new-staff")
    public String addNewStaffPage(Model model) {
        try {
            // Get current authenticated user
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            String userEmail = authentication.getName();
            
            // Find clinic by email
            Optional<Clinic> clinicOpt = clinicRepository.findByEmail(userEmail);
            
            if (clinicOpt.isPresent()) {
                Clinic clinic = clinicOpt.get();
                model.addAttribute("clinicName", clinic.getUsername());
                model.addAttribute("clinicId", clinic.getId());
                model.addAttribute("institutionType", "Clinic");
                
                log.info("Add staff page loaded for clinic: {}", clinic.getUsername());
                return "clinic/addNewStaffPage/HospitalAddStaffPage";
            } else {
                log.error("Clinic not found for email: {}", userEmail);
                return "error/404";
            }
        } catch (Exception e) {
            log.error("Error loading add staff page: {}", e.getMessage(), e);
            return "error/500";
        }
    }

    /**
     * Send staff invitation
     */
    @PostMapping("/send-invitation")
    @ResponseBody
    public ResponseEntity<?> sendStaffInvitation(@Valid @RequestBody StaffInvitationRequest request) {
        try {
            // Get current authenticated user
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            String userEmail = authentication.getName();
            
            // Find clinic by email
            Optional<Clinic> clinicOpt = clinicRepository.findByEmail(userEmail);
            
            if (clinicOpt.isEmpty()) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body("{\"success\": false, \"error\": \"Clinic not found\"}");
            }

            Clinic clinic = clinicOpt.get();
            
            // Send staff invitation
            String result = staffService.sendStaffInvitation(
                request.getEmail(),
                request.getName(),
                request.getContact(),
                request.getRole(),
                request.getQualifications(),
                request.getSpecialization(),
                "Clinic",
                clinic.getId()
            );

            log.info("Staff invitation sent successfully by clinic: {} to: {}", clinic.getUsername(), request.getEmail());
            return ResponseEntity.ok().body("{\"success\": true, \"message\": \"" + result + "\"}");

        } catch (IllegalArgumentException e) {
            log.error("Staff invitation validation error: {}", e.getMessage());
            // Escape quotes in the error message for proper JSON formatting
            String escapedMessage = e.getMessage().replace("\"", "\\\"");
            return ResponseEntity.badRequest().body("{\"success\": false, \"error\": \"" + escapedMessage + "\"}");
        } catch (Exception e) {
            log.error("Unexpected error during staff invitation: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("{\"success\": false, \"error\": \"An unexpected error occurred. Please try again.\"}");
        }
    }

    /**
     * Get all staff for clinic
     */
    @GetMapping("/staff")
    @ResponseBody
    public ResponseEntity<?> getClinicStaff() {
        try {
            // Get current authenticated user
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            String userEmail = authentication.getName();
            
            // Find clinic by email
            Optional<Clinic> clinicOpt = clinicRepository.findByEmail(userEmail);
            
            if (clinicOpt.isEmpty()) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body("{\"success\": false, \"error\": \"Clinic not found\"}");
            }

            Clinic clinic = clinicOpt.get();
            List<Staff> staffList = staffService.getStaffByInstitution(clinic.getId(), "Clinic");

            log.info("Retrieved {} staff members for clinic: {}", staffList.size(), clinic.getUsername());
            return ResponseEntity.ok().body("{\"success\": true, \"staff\": " + staffList + "}");

        } catch (Exception e) {
            log.error("Error retrieving clinic staff: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("{\"success\": false, \"error\": \"Failed to retrieve staff information\"}");
        }
    }

    /**
     * Display staff page for clinic
     */
    @GetMapping("/staff-page")
    public String staffPage(Model model) {
        try {
            // Get current authenticated user
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            String userEmail = authentication.getName();
            log.info("Loading staff page for user: {}", userEmail);
            
            // Find clinic by email
            Optional<Clinic> clinicOpt = clinicRepository.findByEmail(userEmail);
            
            if (clinicOpt.isPresent()) {
                Clinic clinic = clinicOpt.get();
                log.info("Found clinic: {} with ID: {}", clinic.getUsername(), clinic.getId());
                
                // Get all staff for this clinic with error handling
                List<Staff> staffList;
                try {
                    staffList = staffService.getStaffByInstitution(clinic.getId(), "Clinic");
                    log.info("Retrieved {} staff members for clinic: {}", staffList.size(), clinic.getUsername());
                } catch (Exception staffError) {
                    log.error("Error retrieving staff list: {}", staffError.getMessage(), staffError);
                    staffList = new ArrayList<>(); // Empty list as fallback
                }
                
                model.addAttribute("clinicName", clinic.getUsername());
                model.addAttribute("clinicId", clinic.getId());
                model.addAttribute("institutionType", "Clinic");
                model.addAttribute("staffList", staffList);
                model.addAttribute("staffCount", staffList.size());
                
                log.info("Staff page loaded successfully for clinic: {} with {} staff members", clinic.getUsername(), staffList.size());
                return "clinic/staffPage/HospitalStaffPage";
            } else {
                log.error("Clinic not found for email: {}", userEmail);
                return "error/404";
            }
        } catch (Exception e) {
            log.error("Error loading staff page: {}", e.getMessage(), e);
            // Add some basic attributes to prevent template errors
            model.addAttribute("clinicName", "Unknown");
            model.addAttribute("clinicId", "");
            model.addAttribute("institutionType", "Clinic");
            model.addAttribute("staffList", new ArrayList<>());
            model.addAttribute("staffCount", 0);
            return "error/500";
        }
    }

    /**
     * Debug endpoint to fetch staff info (non-destructive)
     */
    @GetMapping("/debug/staff-info/{staffId}")
    @ResponseBody
    public ResponseEntity<?> debugStaffInfo(@PathVariable Long staffId) {
        try {
            log.info("DEBUG: Staff info request received for staffId: {}", staffId);

            // Test if staff exists
            Optional<Staff> staffOpt = staffService.getStaffById(staffId);
            if (staffOpt.isPresent()) {
                Staff staff = staffOpt.get();
                return ResponseEntity.ok().body("{\"success\": true, \"message\": \"Staff found\", " +
                        "\"staffName\": \"" + staff.getName() + "\", " +
                        "\"staffEmail\": \"" + staff.getEmail() + "\", " +
                        "\"institutionId\": \"" + staff.getInstitutionId() + "\", " +
                        "\"institutionType\": \"" + staff.getInstitutionType() + "\"}");
            } else {
                return ResponseEntity.ok().body("{\"success\": false, \"error\": \"Staff not found with ID: " + staffId + "\"}");
            }
        } catch (Exception e) {
            log.error("DEBUG: Error in debug endpoint: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("{\"success\": false, \"error\": \"Debug error: " + e.getMessage() + "\"}");
        }
    }

    /**
     * Debug endpoint to test staff deletion (authorized & checks ownership)
     */
    @GetMapping("/debug/remove-staff/{staffId}")
    @ResponseBody
    public ResponseEntity<?> debugRemoveStaff(@PathVariable Long staffId) {
        try {
            log.info("DEBUG: Delete staff request received for staffId: {}", staffId);
            
            // Get current authenticated user
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            String userEmail = authentication.getName();
            
            // Find clinic by email
            Optional<Clinic> clinicOpt = clinicRepository.findByEmail(userEmail);
            if (clinicOpt.isEmpty()) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body("{\"success\": false, \"error\": \"Clinic not found\"}");
            }

            Clinic clinic = clinicOpt.get();
            
            // Check if staff belongs to this clinic
            Optional<Staff> staffOpt = staffService.getStaffById(staffId);
            if (staffOpt.isEmpty()) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body("{\"success\": false, \"error\": \"Staff member not found\"}");
            }
            
            Staff staff = staffOpt.get();
            if (!staff.getInstitutionId().equals(clinic.getId()) || !staff.getInstitutionType().equals("Clinic")) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                        .body("{\"success\": false, \"error\": \"Staff member does not belong to this clinic\"}");
            }
            
            // Remove staff
            staffService.removeStaff(staffId);
            
            log.info("DEBUG: Staff member {} removed from clinic: {}", staff.getName(), clinic.getUsername());
            return ResponseEntity.ok().body("{\"success\": true, \"message\": \"Staff member removed successfully\"}");

        } catch (Exception e) {
            log.error("DEBUG: Error removing staff member: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("{\"success\": false, \"error\": \"Failed to remove staff member: " + e.getMessage() + "\"}");
        }
    }

    /**
     * Remove staff member from clinic
     */
    @DeleteMapping("/remove-staff/{staffId}")
    @ResponseBody
    public ResponseEntity<?> removeStaff(@PathVariable Long staffId) {
        try {
            log.info("Delete staff request received for staffId: {}", staffId);
            // Get current authenticated user
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            String userEmail = authentication.getName();
            
            // Find clinic by email
            Optional<Clinic> clinicOpt = clinicRepository.findByEmail(userEmail);
            
            if (clinicOpt.isEmpty()) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body("{\"success\": false, \"error\": \"Clinic not found\"}");
            }

            Clinic clinic = clinicOpt.get();
            
            // Check if staff belongs to this clinic
            Optional<Staff> staffOpt = staffService.getStaffById(staffId);
            if (staffOpt.isEmpty()) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body("{\"success\": false, \"error\": \"Staff member not found\"}");
            }
            
            Staff staff = staffOpt.get();
            if (!staff.getInstitutionId().equals(clinic.getId()) || !staff.getInstitutionType().equals("Clinic")) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                        .body("{\"success\": false, \"error\": \"Staff member does not belong to this clinic\"}");
            }
            
            // Cancel staff connection (removes connection but keeps account)
            staffService.cancelStaffConnection(staffId);
            
            log.info("Staff connection {} cancelled from clinic: {}", staff.getName(), clinic.getUsername());
            return ResponseEntity.ok().body("{\"success\": true, \"message\": \"Staff connection cancelled successfully\"}");

        } catch (Exception e) {
            log.error("Error removing staff member: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("{\"success\": false, \"error\": \"Failed to remove staff member\"}");
        }
    }

    /**
     * Get doctors for clinic profile page
     */
    @GetMapping("/staff/doctors")
    @ResponseBody
    public ResponseEntity<?> getClinicDoctors() {
        try {
            // Get current authenticated user
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            String userEmail = authentication.getName();
            
            // Find clinic by email
            Optional<Clinic> clinicOpt = clinicRepository.findByEmail(userEmail);
            
            if (clinicOpt.isEmpty()) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body("{\"success\": false, \"error\": \"Clinic not found\"}");
            }

            Clinic clinic = clinicOpt.get();
            List<Staff> doctors = staffService.getStaffByRole(clinic.getId(), "Clinic", "Doctor");
            
            // Convert to DTOs for clean JSON serialization
            List<StaffResponse> doctorDTOs = doctors.stream()
                .map(this::convertToStaffResponse)
                .toList();

            log.info("Retrieved {} doctors for clinic: {}", doctorDTOs.size(), clinic.getUsername());
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("doctors", doctorDTOs);
            return ResponseEntity.ok().body(response);

        } catch (Exception e) {
            log.error("Error retrieving clinic doctors: {}", e.getMessage(), e);
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("error", "Failed to retrieve doctors information");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    /**
     * Get nurses for clinic profile page
     */
    @GetMapping("/staff/nurses")
    @ResponseBody
    public ResponseEntity<?> getClinicNurses() {
        try {
            // Get current authenticated user
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            String userEmail = authentication.getName();
            
            // Find clinic by email
            Optional<Clinic> clinicOpt = clinicRepository.findByEmail(userEmail);
            
            if (clinicOpt.isEmpty()) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body("{\"success\": false, \"error\": \"Clinic not found\"}");
            }

            Clinic clinic = clinicOpt.get();
            List<Staff> nurses = staffService.getStaffByRole(clinic.getId(), "Clinic", "Nurse");
            
            // Convert to DTOs for clean JSON serialization
            List<StaffResponse> nurseDTOs = nurses.stream()
                .map(this::convertToStaffResponse)
                .toList();

            log.info("Retrieved {} nurses for clinic: {}", nurseDTOs.size(), clinic.getUsername());
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("nurses", nurseDTOs);
            return ResponseEntity.ok().body(response);

        } catch (Exception e) {
            log.error("Error retrieving clinic nurses: {}", e.getMessage(), e);
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("error", "Failed to retrieve nurses information");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    /**
     * Get staff by role for clinic
     */
    @GetMapping("/staff/{role}")
    @ResponseBody
    public ResponseEntity<?> getClinicStaffByRole(@PathVariable String role) {
        try {
            // Get current authenticated user
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            String userEmail = authentication.getName();
            
            // Find clinic by email
            Optional<Clinic> clinicOpt = clinicRepository.findByEmail(userEmail);
            
            if (clinicOpt.isEmpty()) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body("{\"success\": false, \"error\": \"Clinic not found\"}");
            }

            Clinic clinic = clinicOpt.get();
            List<Staff> staffList = staffService.getStaffByRole(clinic.getId(), "Clinic", role);

            log.info("Retrieved {} {} staff members for clinic: {}", staffList.size(), role, clinic.getUsername());
            return ResponseEntity.ok().body("{\"success\": true, \"staff\": " + staffList + "}");

        } catch (Exception e) {
            log.error("Error retrieving clinic staff by role: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("{\"success\": false, \"error\": \"Failed to retrieve staff information\"}");
        }
    }

    
    /**
     * Convert Staff entity to StaffResponse DTO
     */
    private StaffResponse convertToStaffResponse(Staff staff) {
        StaffResponse response = new StaffResponse();
        response.setId(staff.getId());
        response.setEmail(staff.getEmail());
        response.setName(staff.getName());
        response.setContact(staff.getContact());
        response.setRole(staff.getRole());
        response.setQualifications(staff.getQualifications());
        response.setSpecialization(staff.getSpecialization());
        response.setInstitutionType(staff.getInstitutionType());
        response.setInstitutionId(staff.getInstitutionId());
        response.setInvitationAccepted(staff.getInvitationAccepted());
        
        // Format dates as strings
        if (staff.getCreatedAt() != null) {
            response.setCreatedAt(staff.getCreatedAt().toString());
        }
        if (staff.getUpdatedAt() != null) {
            response.setUpdatedAt(staff.getUpdatedAt().toString());
        }
        
        return response;
    }
}
