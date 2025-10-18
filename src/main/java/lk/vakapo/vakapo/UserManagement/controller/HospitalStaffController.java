package lk.vakapo.vakapo.UserManagement.controller;

import jakarta.validation.Valid;
import lk.vakapo.vakapo.UserManagement.dto.StaffInvitationRequest;
import lk.vakapo.vakapo.UserManagement.dto.StaffResponse;
import lk.vakapo.vakapo.UserManagement.model.Hospital;
import lk.vakapo.vakapo.UserManagement.model.Staff;
import lk.vakapo.vakapo.UserManagement.repository.HospitalRepository;
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
@RequestMapping("/hospital")
@RequiredArgsConstructor
@Slf4j
public class HospitalStaffController {

    private final StaffService staffService;
    private final HospitalRepository hospitalRepository;

    /**
     * Display add new staff page for hospital
     */
    @GetMapping("/add-new-staff")
    public String addNewStaffPage(Model model) {
        try {
            // Get current authenticated user
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            String userEmail = authentication.getName();

            // Find hospital by email
            Optional<Hospital> hospitalOpt = hospitalRepository.findByEmail(userEmail);

            if (hospitalOpt.isPresent()) {
                Hospital hospital = hospitalOpt.get();
                model.addAttribute("hospitalName", hospital.getUsername());
                model.addAttribute("hospitalId", hospital.getId());
                model.addAttribute("institutionType", "Hospital");

                log.info("Add staff page loaded for hospital: {}", hospital.getUsername());
                return "hospital/addNewStaffPage/HospitalAddStaffPage";
            } else {
                log.error("Hospital not found for email: {}", userEmail);
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
            log.info("Received staff invitation request: {}", request);

            // Get current authenticated user
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            String userEmail = authentication.getName();
            log.info("Current user email: {}", userEmail);

            // Find hospital by email
            Optional<Hospital> hospitalOpt = hospitalRepository.findByEmail(userEmail);

            if (hospitalOpt.isEmpty()) {
                log.error("Hospital not found for email: {}", userEmail);
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body("{\"success\": false, \"error\": \"Hospital not found\"}");
            }

            Hospital hospital = hospitalOpt.get();
            log.info("Found hospital: {} with ID: {}", hospital.getUsername(), hospital.getId());

            // Send staff invitation
            log.info("Calling staffService.sendStaffInvitation with email: {}", request.getEmail());
            String result = staffService.sendStaffInvitation(
                    request.getEmail(),
                    request.getName(),
                    request.getContact(),
                    request.getRole(),
                    request.getQualifications(),
                    request.getSpecialization(),
                    "Hospital",
                    hospital.getId()
            );

            log.info("Staff invitation sent successfully by hospital: {} to: {}", hospital.getUsername(), request.getEmail());
            return ResponseEntity.ok().body("{\"success\": true, \"message\": \"" + result + "\"}");

        } catch (IllegalArgumentException e) {
            log.error("Staff invitation validation error: {}", e.getMessage());
            String escapedMessage = e.getMessage().replace("\"", "\\\"");
            return ResponseEntity.badRequest().body("{\"success\": false, \"error\": \"" + escapedMessage + "\"}");
        } catch (Exception e) {
            log.error("Unexpected error during staff invitation: {}", e.getMessage(), e);
            String errorMessage = "An unexpected error occurred: " + e.getMessage();
            if (e.getCause() != null) {
                errorMessage += " (Caused by: " + e.getCause().getMessage() + ")";
            }
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("{\"success\": false, \"error\": \"" + errorMessage.replace("\"", "\\\"") + "\"}");
        }
    }

    /**
     * Get all staff for hospital
     */
    @GetMapping("/staff")
    @ResponseBody
    public ResponseEntity<?> getHospitalStaff() {
        try {
            // Get current authenticated user
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            String userEmail = authentication.getName();

            // Find hospital by email
            Optional<Hospital> hospitalOpt = hospitalRepository.findByEmail(userEmail);

            if (hospitalOpt.isEmpty()) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body("{\"success\": false, \"error\": \"Hospital not found\"}");
            }

            Hospital hospital = hospitalOpt.get();
            List<Staff> staffList = staffService.getStaffByInstitution(hospital.getId(), "Hospital");

            log.info("Retrieved {} staff members for hospital: {}", staffList.size(), hospital.getUsername());
            return ResponseEntity.ok().body("{\"success\": true, \"staff\": " + staffList + "}");

        } catch (Exception e) {
            log.error("Error retrieving hospital staff: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("{\"success\": false, \"error\": \"Failed to retrieve staff information\"}");
        }
    }

    /**
     * Display staff page for hospital
     */
    @GetMapping("/staff-page")
    public String staffPage(Model model) {
        log.info("=== STAFF PAGE DEBUG START ===");
        try {
            // Get current authenticated user
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            if (authentication == null) {
                log.error("No authentication found");
                return "error/500";
            }

            String userEmail = authentication.getName();
            log.info("Loading staff page for user: {}", userEmail);

            // Find hospital by email
            Optional<Hospital> hospitalOpt = hospitalRepository.findByEmail(userEmail);
            log.info("Hospital found: {}", hospitalOpt.isPresent());

            if (hospitalOpt.isPresent()) {
                Hospital hospital = hospitalOpt.get();
                log.info("Found hospital: {} with ID: {}", hospital.getUsername(), hospital.getId());

                // Get all staff for this hospital with error handling
                List<Staff> staffList = new ArrayList<>();
                try {
                    staffList = staffService.getStaffByInstitution(hospital.getId(), "Hospital");
                    log.info("Retrieved {} staff members for hospital: {}", staffList.size(), hospital.getUsername());
                } catch (Exception staffError) {
                    log.error("Error retrieving staff list: {}", staffError.getMessage(), staffError);
                    staffList = new ArrayList<>(); // Empty list as fallback
                }

                // Set model attributes
                model.addAttribute("hospitalName", hospital.getUsername() != null ? hospital.getUsername() : "Unknown Hospital");
                model.addAttribute("hospitalId", hospital.getId() != null ? hospital.getId() : "");
                model.addAttribute("institutionType", "Hospital");
                model.addAttribute("staffList", staffList);
                model.addAttribute("staffCount", staffList.size());

                log.info("Staff page loaded successfully for hospital: {} with {} staff members", hospital.getUsername(), staffList.size());
                log.info("=== STAFF PAGE DEBUG END ===");
                return "hospital/staffPage/HospitalStaffPage";
            } else {
                log.error("Hospital not found for email: {}", userEmail);
                return "error/404";
            }
        } catch (Exception e) {
            log.error("Error loading staff page: {}", e.getMessage(), e);
            log.error("Stack trace: ", e);

            // Add some basic attributes to prevent template errors
            model.addAttribute("hospitalName", "Unknown");
            model.addAttribute("hospitalId", "");
            model.addAttribute("institutionType", "Hospital");
            model.addAttribute("staffList", new ArrayList<>());
            model.addAttribute("staffCount", 0);

            log.info("=== STAFF PAGE DEBUG END (ERROR) ===");
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

            // Find hospital by email
            Optional<Hospital> hospitalOpt = hospitalRepository.findByEmail(userEmail);
            if (hospitalOpt.isEmpty()) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body("{\"success\": false, \"error\": \"Hospital not found\"}");
            }

            Hospital hospital = hospitalOpt.get();

            // Check if staff belongs to this hospital
            Optional<Staff> staffOpt = staffService.getStaffById(staffId);
            if (staffOpt.isEmpty()) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body("{\"success\": false, \"error\": \"Staff member not found\"}");
            }

            Staff staff = staffOpt.get();
            if (!staff.getInstitutionId().equals(hospital.getId()) || !staff.getInstitutionType().equals("Hospital")) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                        .body("{\"success\": false, \"error\": \"Staff member does not belong to this hospital\"}");
            }

            // Remove staff
            staffService.removeStaff(staffId);

            log.info("DEBUG: Staff member {} removed from hospital: {}", staff.getName(), hospital.getUsername());
            return ResponseEntity.ok().body("{\"success\": true, \"message\": \"Staff member removed successfully\"}");

        } catch (Exception e) {
            log.error("DEBUG: Error removing staff member: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("{\"success\": false, \"error\": \"Failed to remove staff member: " + e.getMessage() + "\"}");
        }
    }

    /**
     * Remove staff member from hospital
     */
    @DeleteMapping("/remove-staff/{staffId}")
    @ResponseBody
    public ResponseEntity<?> removeStaff(@PathVariable Long staffId) {
        log.info("=== DELETE STAFF DEBUG START ===");
        try {
            log.info("Delete staff request received for staffId: {}", staffId);

            // Get current authenticated user
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            if (authentication == null) {
                log.error("No authentication found");
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body("{\"success\": false, \"error\": \"No authentication found\"}");
            }

            String userEmail = authentication.getName();
            log.info("Authenticated user: {}", userEmail);

            // Find hospital by email
            Optional<Hospital> hospitalOpt = hospitalRepository.findByEmail(userEmail);
            log.info("Hospital found: {}", hospitalOpt.isPresent());

            if (hospitalOpt.isEmpty()) {
                log.error("Hospital not found for email: {}", userEmail);
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body("{\"success\": false, \"error\": \"Hospital not found\"}");
            }

            Hospital hospital = hospitalOpt.get();
            log.info("Hospital: {} with ID: {}", hospital.getUsername(), hospital.getId());

            // Check if staff belongs to this hospital
            Optional<Staff> staffOpt = staffService.getStaffById(staffId);
            log.info("Staff found: {}", staffOpt.isPresent());

            if (staffOpt.isEmpty()) {
                log.error("Staff member not found with ID: {}", staffId);
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body("{\"success\": false, \"error\": \"Staff member not found\"}");
            }

            Staff staff = staffOpt.get();
            log.info("Staff: {} belongs to {}: {}", staff.getName(), staff.getInstitutionType(), staff.getInstitutionId());

            if (!staff.getInstitutionId().equals(hospital.getId()) || !staff.getInstitutionType().equals("Hospital")) {
                log.error("Staff member does not belong to this hospital. Staff institution: {}:{}, Hospital: {}:{}",
                        staff.getInstitutionType(), staff.getInstitutionId(), "Hospital", hospital.getId());
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                        .body("{\"success\": false, \"error\": \"Staff member does not belong to this hospital\"}");
            }

            // Cancel staff connection (removes connection but keeps account)
            log.info("Attempting to cancel staff connection: {}", staff.getName());
            staffService.cancelStaffConnection(staffId);

            log.info("Staff connection {} cancelled from hospital: {}", staff.getName(), hospital.getUsername());
            log.info("=== CANCEL STAFF CONNECTION DEBUG END (SUCCESS) ===");
            return ResponseEntity.ok().body("{\"success\": true, \"message\": \"Staff connection cancelled successfully\"}");

        } catch (Exception e) {
            log.error("Error removing staff member: {}", e.getMessage(), e);
            log.error("Stack trace: ", e);
            log.info("=== DELETE STAFF DEBUG END (ERROR) ===");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("{\"success\": false, \"error\": \"Failed to remove staff member: " + e.getMessage() + "\"}");
        }
    }

    /**
     * Get doctors for hospital profile page
     */
    @GetMapping("/staff/doctors")
    @ResponseBody
    public ResponseEntity<?> getHospitalDoctors() {
        try {
            // Get current authenticated user
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            String userEmail = authentication.getName();

            // Find hospital by email
            Optional<Hospital> hospitalOpt = hospitalRepository.findByEmail(userEmail);

            if (hospitalOpt.isEmpty()) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body("{\"success\": false, \"error\": \"Hospital not found\"}");
            }

            Hospital hospital = hospitalOpt.get();
            List<Staff> doctors = staffService.getStaffByRole(hospital.getId(), "Hospital", "Doctor");

            // Convert to DTOs for clean JSON serialization
            List<StaffResponse> doctorDTOs = doctors.stream()
                    .map(this::convertToStaffResponse)
                    .toList();

            log.info("Retrieved {} doctors for hospital: {}", doctorDTOs.size(), hospital.getUsername());

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("doctors", doctorDTOs);
            return ResponseEntity.ok().body(response);

        } catch (Exception e) {
            log.error("Error retrieving hospital doctors: {}", e.getMessage(), e);
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("error", "Failed to retrieve doctors information");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    /**
     * Get nurses for hospital profile page
     */
    @GetMapping("/staff/nurses")
    @ResponseBody
    public ResponseEntity<?> getHospitalNurses() {
        try {
            // Get current authenticated user
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            String userEmail = authentication.getName();

            // Find hospital by email
            Optional<Hospital> hospitalOpt = hospitalRepository.findByEmail(userEmail);

            if (hospitalOpt.isEmpty()) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body("{\"success\": false, \"error\": \"Hospital not found\"}");
            }

            Hospital hospital = hospitalOpt.get();
            List<Staff> nurses = staffService.getStaffByRole(hospital.getId(), "Hospital", "Nurse");

            // Convert to DTOs for clean JSON serialization
            List<StaffResponse> nurseDTOs = nurses.stream()
                    .map(this::convertToStaffResponse)
                    .toList();

            log.info("Retrieved {} nurses for hospital: {}", nurseDTOs.size(), hospital.getUsername());

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("nurses", nurseDTOs);
            return ResponseEntity.ok().body(response);

        } catch (Exception e) {
            log.error("Error retrieving hospital nurses: {}", e.getMessage(), e);
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("error", "Failed to retrieve nurses information");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    /**
     * Get staff by role for hospital
     */
    @GetMapping("/staff/{role}")
    @ResponseBody
    public ResponseEntity<?> getHospitalStaffByRole(@PathVariable String role) {
        try {
            // Get current authenticated user
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            String userEmail = authentication.getName();

            // Find hospital by email
            Optional<Hospital> hospitalOpt = hospitalRepository.findByEmail(userEmail);

            if (hospitalOpt.isEmpty()) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body("{\"success\": false, \"error\": \"Hospital not found\"}");
            }

            Hospital hospital = hospitalOpt.get();
            List<Staff> staffList = staffService.getStaffByRole(hospital.getId(), "Hospital", role);

            log.info("Retrieved {} {} staff members for hospital: {}", staffList.size(), role, hospital.getUsername());
            return ResponseEntity.ok().body("{\"success\": true, \"staff\": " + staffList + "}");

        } catch (Exception e) {
            log.error("Error retrieving hospital staff by role: {}", e.getMessage(), e);
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
