package lk.vakapo.vakapo.UserManagement.controller;

import lk.vakapo.vakapo.UserManagement.model.VaccinationHistorySimple;
import lk.vakapo.vakapo.UserManagement.service.VaccinationHistorySimpleService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/vaccination-history")
@RequiredArgsConstructor
public class VaccinationHistorySimpleController {

    private final VaccinationHistorySimpleService service;

    // Get patient vaccination history
    @GetMapping("/patient/{patientId}")
    public ResponseEntity<Map<String, Object>> getPatientHistory(@PathVariable String patientId) {
        try {
            List<VaccinationHistorySimple> history = service.getPatientVaccinationHistory(patientId);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("vaccinationHistory", history);
            response.put("count", history.size());
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("error", e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }

    // Update dosage level
    @PutMapping("/{historyId}/dosage")
    public ResponseEntity<Map<String, Object>> updateDosage(
            @PathVariable Long historyId, 
            @RequestParam String dosageLevel) {
        try {
            boolean success = service.updateDosageLevel(historyId, dosageLevel);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", success);
            response.put("message", success ? "Dosage updated successfully" : "Vaccination history not found");
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("error", e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }

    // Update additional notes
    @PutMapping("/{historyId}/notes")
    public ResponseEntity<Map<String, Object>> updateNotes(
            @PathVariable Long historyId, 
            @RequestParam String additionalNotes) {
        try {
            boolean success = service.updateAdditionalNotes(historyId, additionalNotes);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", success);
            response.put("message", success ? "Notes updated successfully" : "Vaccination history not found");
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("error", e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }

    // Create new vaccination history
    @PostMapping("/create")
    public ResponseEntity<Map<String, Object>> createVaccinationHistory(
            @RequestParam String patientId,
            @RequestParam String patientName,
            @RequestParam String vaccineName,
            @RequestParam String timeSlot,
            @RequestParam String location,
            @RequestParam String doctorName) {
        try {
            VaccinationHistorySimple history = service.createVaccinationHistory(
                patientId, patientName, vaccineName, timeSlot, location, doctorName);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Vaccination history created successfully");
            response.put("historyId", history.getId());
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("error", e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }

    // Delete vaccination history record
    @DeleteMapping("/{historyId}")
    public ResponseEntity<Map<String, Object>> deleteVaccinationHistory(
            @PathVariable Long historyId,
            @RequestParam String patientId) {
        try {
            boolean success = service.deleteVaccinationHistory(historyId, patientId);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", success);
            response.put("message", success ? "Vaccination record deleted successfully" : "Vaccination record not found or access denied");
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("error", e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }
}
