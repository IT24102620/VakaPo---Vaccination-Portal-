package lk.vakapo.vakapo.UserManagement.controller;

import lk.vakapo.vakapo.UserManagement.service.VaccinationMonitoringService;
import lk.vakapo.vakapo.UserManagement.service.VaccinationAnalyticsService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@Controller
@RequestMapping("/admin/vaccination-monitoring")
@RequiredArgsConstructor
@Slf4j
public class VaccinationMonitoringController {

    private final VaccinationMonitoringService vaccinationMonitoringService;
    private final VaccinationAnalyticsService vaccinationAnalyticsService;

    /**
     * Display vaccination monitoring page
     */
    @GetMapping
    public String vaccinationMonitoringPage(Model model) {
        try {
            VaccinationMonitoringService.VaccinationMonitoringStats stats = 
                    vaccinationMonitoringService.getVaccinationMonitoringStats();
            
            model.addAttribute("stats", stats);
            model.addAttribute("institutionStats", stats.getInstitutionStats());
            model.addAttribute("vaccineStats", stats.getVaccineStats());
            model.addAttribute("recentActivity", stats.getRecentActivity());
            
            return "admin/vaccination/VakaPo_Range";
            
        } catch (Exception e) {
            log.error("Error loading vaccination monitoring page: {}", e.getMessage(), e);
            model.addAttribute("error", "Failed to load vaccination monitoring data");
            return "admin/vaccination/VakaPo_Range";
        }
    }

    /**
     * Get vaccination monitoring statistics (AJAX endpoint)
     */
    @GetMapping("/stats")
    @ResponseBody
    public ResponseEntity<?> getVaccinationStats() {
        try {
            VaccinationMonitoringService.VaccinationMonitoringStats stats = 
                    vaccinationMonitoringService.getVaccinationMonitoringStats();
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("stats", stats);
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            log.error("Error getting vaccination statistics: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError().body("{\"error\": \"Failed to get vaccination statistics\"}");
        }
    }

    /**
     * Get institution details (AJAX endpoint)
     */
    @GetMapping("/institution/{institutionEmail}")
    @ResponseBody
    public ResponseEntity<?> getInstitutionDetails(
            @PathVariable String institutionEmail,
            @RequestParam String institutionType) {
        try {
            VaccinationMonitoringService.InstitutionDetails details = 
                    vaccinationMonitoringService.getInstitutionDetails(institutionEmail, institutionType);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("details", details);
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            log.error("Error getting institution details: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError().body("{\"error\": \"Failed to get institution details\"}");
        }
    }

    /**
     * Get vaccine statistics (AJAX endpoint)
     */
    @GetMapping("/vaccines")
    @ResponseBody
    public ResponseEntity<?> getVaccineStatistics() {
        try {
            VaccinationMonitoringService.VaccinationMonitoringStats stats = 
                    vaccinationMonitoringService.getVaccinationMonitoringStats();
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("vaccineStats", stats.getVaccineStats());
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            log.error("Error getting vaccine statistics: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError().body("{\"error\": \"Failed to get vaccine statistics\"}");
        }
    }

    /**
     * Get institution statistics (AJAX endpoint)
     */
    @GetMapping("/institutions")
    @ResponseBody
    public ResponseEntity<?> getInstitutionStatistics() {
        try {
            VaccinationMonitoringService.VaccinationMonitoringStats stats = 
                    vaccinationMonitoringService.getVaccinationMonitoringStats();
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("institutionStats", stats.getInstitutionStats());
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            log.error("Error getting institution statistics: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError().body("{\"error\": \"Failed to get institution statistics\"}");
        }
    }

    /**
     * Get chart data for analytics dashboard (AJAX endpoint)
     */
    @GetMapping("/chart-data")
    @ResponseBody
    public ResponseEntity<?> getChartData() {
        try {
            log.info("Fetching real-time chart data from database...");
            
            Map<String, Object> chartData = new HashMap<>();
            
            // Get real data from database using VaccinationAnalyticsService
            chartData.put("vaccineDistribution", vaccinationAnalyticsService.getVaccineDistributionData());
            chartData.put("vaccinationStatus", vaccinationAnalyticsService.getVaccinationStatusData());
            chartData.put("monthlyTrends", vaccinationAnalyticsService.getMonthlyTrendsData());
            chartData.put("institutionPerformance", vaccinationAnalyticsService.getInstitutionPerformanceData());
            chartData.put("vaccineUsage", vaccinationAnalyticsService.getVaccineUsageOverTimeData());
            chartData.put("ageGroup", vaccinationAnalyticsService.getAgeGroupData());
            
            // Get dashboard statistics
            Map<String, Object> stats = vaccinationAnalyticsService.getDashboardStatistics();
            chartData.put("statistics", stats);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("chartData", chartData);
            response.put("timestamp", System.currentTimeMillis());
            
            log.info("Successfully fetched data for 6 charts with {} statistics", stats.size());
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            log.error("Error getting chart data: {}", e.getMessage(), e);
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("error", "Failed to get chart data: " + e.getMessage());
            return ResponseEntity.internalServerError().body(errorResponse);
        }
    }
}
