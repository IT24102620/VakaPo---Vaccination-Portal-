package lk.vakapo.vakapo.UserManagement.controller;

import lk.vakapo.vakapo.UserManagement.service.AccountDeletionService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
@Slf4j
public class AccountDeletionController {

    private final AccountDeletionService accountDeletionService;

    /**
     * Delete patient account and all related data
     */
    @PostMapping("/delete-patient")
    public ResponseEntity<Map<String, Object>> deletePatientAccount(@RequestParam String patientEmail) {
        try {
            log.info("Admin deleting patient account: {}", patientEmail);
            
            String result = accountDeletionService.deletePatientAccount(patientEmail);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", result);
            response.put("deletedEmail", patientEmail);
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            log.error("Error deleting patient account: {}", e.getMessage(), e);
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("error", "Failed to delete patient account: " + e.getMessage());
            return ResponseEntity.internalServerError().body(response);
        }
    }

    /**
     * Delete staff account (doctor/nurse) and all related data
     */
    @PostMapping("/delete-staff")
    public ResponseEntity<Map<String, Object>> deleteStaffAccount(@RequestParam String staffEmail) {
        try {
            log.info("Admin deleting staff account: {}", staffEmail);
            
            String result = accountDeletionService.deleteStaffAccount(staffEmail);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", result);
            response.put("deletedEmail", staffEmail);
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            log.error("Error deleting staff account: {}", e.getMessage(), e);
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("error", "Failed to delete staff account: " + e.getMessage());
            return ResponseEntity.internalServerError().body(response);
        }
    }

    /**
     * Delete hospital account and all related data
     */
    @PostMapping("/delete-hospital")
    public ResponseEntity<Map<String, Object>> deleteHospitalAccount(@RequestParam String hospitalEmail) {
        try {
            log.info("Admin deleting hospital account: {}", hospitalEmail);
            
            String result = accountDeletionService.deleteHospitalAccount(hospitalEmail);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", result);
            response.put("deletedEmail", hospitalEmail);
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            log.error("Error deleting hospital account: {}", e.getMessage(), e);
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("error", "Failed to delete hospital account: " + e.getMessage());
            return ResponseEntity.internalServerError().body(response);
        }
    }

    /**
     * Delete clinic account and all related data
     */
    @PostMapping("/delete-clinic")
    public ResponseEntity<Map<String, Object>> deleteClinicAccount(@RequestParam String clinicEmail) {
        try {
            log.info("Admin deleting clinic account: {}", clinicEmail);
            
            String result = accountDeletionService.deleteClinicAccount(clinicEmail);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", result);
            response.put("deletedEmail", clinicEmail);
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            log.error("Error deleting clinic account: {}", e.getMessage(), e);
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("error", "Failed to delete clinic account: " + e.getMessage());
            return ResponseEntity.internalServerError().body(response);
        }
    }

    /**
     * Remove doctor from hospital/clinic
     */
    @PostMapping("/remove-doctor")
    public ResponseEntity<Map<String, Object>> removeDoctorFromInstitution(
            @RequestParam String doctorEmail,
            @RequestParam String institutionType,
            @RequestParam String institutionId) {
        try {
            log.info("Admin removing doctor {} from {}: {}", doctorEmail, institutionType, institutionId);
            
            String result = accountDeletionService.removeDoctorFromInstitution(
                doctorEmail, institutionType, institutionId);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", result);
            response.put("doctorEmail", doctorEmail);
            response.put("institutionType", institutionType);
            response.put("institutionId", institutionId);
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            log.error("Error removing doctor from institution: {}", e.getMessage(), e);
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("error", "Failed to remove doctor: " + e.getMessage());
            return ResponseEntity.internalServerError().body(response);
        }
    }

    /**
     * Generic account deletion by role
     */
    @PostMapping("/delete-account")
    public ResponseEntity<Map<String, Object>> deleteAccountByRole(
            @RequestParam String userEmail,
            @RequestParam String userRole) {
        try {
            log.info("Admin deleting account: {} with role: {}", userEmail, userRole);
            
            String result = accountDeletionService.deleteAccountByRole(userEmail, userRole);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", result);
            response.put("deletedEmail", userEmail);
            response.put("userRole", userRole);
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            log.error("Error deleting account by role: {}", e.getMessage(), e);
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("error", "Failed to delete account: " + e.getMessage());
            return ResponseEntity.internalServerError().body(response);
        }
    }
}

@RestController
@RequestMapping("/api/institution")
@RequiredArgsConstructor
@Slf4j
class InstitutionStaffManagementController {

    private final AccountDeletionService accountDeletionService;

    /**
     * Remove doctor from current institution (for hospital/clinic admins)
     */
    @PostMapping("/remove-doctor")
    public ResponseEntity<Map<String, Object>> removeDoctorFromCurrentInstitution(
            @RequestParam String doctorEmail,
            Authentication authentication) {
        try {
            // Get current user's institution details
            String currentUserEmail = authentication.getName();
            // You would need to implement logic to get institution details from current user
            // For now, we'll use a generic approach
            
            log.info("Institution admin {} removing doctor: {}", currentUserEmail, doctorEmail);
            
            // This would need to be implemented based on your institution identification logic
            // For now, returning a placeholder response
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("error", "Institution identification logic needs to be implemented");
            
            return ResponseEntity.badRequest().body(response);
            
        } catch (Exception e) {
            log.error("Error removing doctor from institution: {}", e.getMessage(), e);
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("error", "Failed to remove doctor: " + e.getMessage());
            return ResponseEntity.internalServerError().body(response);
        }
    }
}
