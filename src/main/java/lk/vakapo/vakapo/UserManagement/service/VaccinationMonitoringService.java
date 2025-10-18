package lk.vakapo.vakapo.UserManagement.service;

import lk.vakapo.vakapo.UserManagement.model.*;
import lk.vakapo.vakapo.UserManagement.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class VaccinationMonitoringService {

    private final VaccinationHistoryRepository vaccinationHistoryRepository;
    private final VaccinationScheduleRepository vaccinationScheduleRepository;
    private final UserRepository userRepository;
    private final VaccineRepository vaccineRepository;
    private final HospitalRepository hospitalRepository;
    private final ClinicRepository clinicRepository;

    /**
     * Get comprehensive vaccination monitoring statistics
     */
    public VaccinationMonitoringStats getVaccinationMonitoringStats() {
        try {
            // Get all vaccination history
            List<VaccinationHistory> allVaccinations = vaccinationHistoryRepository.findAll();
            
            // Get all institutions
            List<UserAccount> hospitals = userRepository.findByRole("HOSPITAL");
            List<UserAccount> clinics = userRepository.findByRole("CLINIC");
            
            // Get all vaccines
            List<Vaccine> allVaccines = vaccineRepository.findAllByOrderByIdAsc();
            
            // Calculate statistics
            long totalVaccinations = allVaccinations.size();
            long completedVaccinations = allVaccinations.stream()
                    .filter(v -> "completed".equals(v.getStatus()))
                    .count();
            long pendingVaccinations = allVaccinations.stream()
                    .filter(v -> "pending".equals(v.getStatus()))
                    .count();
            
            // Calculate vaccination rate
            double vaccinationRate = totalVaccinations > 0 ? 
                    (double) completedVaccinations / totalVaccinations * 100 : 0;
            
            // Get institution statistics
            List<InstitutionStats> institutionStats = getInstitutionStatistics(allVaccinations, hospitals, clinics);
            
            // Get vaccine statistics
            List<VaccineStats> vaccineStats = getVaccineStatistics(allVaccinations, allVaccines);
            
            // Get recent activity
            List<RecentActivity> recentActivity = getRecentActivity(allVaccinations);
            
            return new VaccinationMonitoringStats(
                    totalVaccinations,
                    completedVaccinations,
                    pendingVaccinations,
                    vaccinationRate,
                    institutionStats,
                    vaccineStats,
                    recentActivity,
                    allVaccines.size(),
                    hospitals.size() + clinics.size()
            );
            
        } catch (Exception e) {
            log.error("Error getting vaccination monitoring statistics: {}", e.getMessage(), e);
            return new VaccinationMonitoringStats(0, 0, 0, 0, List.of(), List.of(), List.of(), 0, 0);
        }
    }

    /**
     * Get institution-specific statistics
     */
    private List<InstitutionStats> getInstitutionStatistics(List<VaccinationHistory> vaccinations, 
                                                           List<UserAccount> hospitals, 
                                                           List<UserAccount> clinics) {
        Map<String, InstitutionStats> statsMap = new HashMap<>();
        
        // Initialize with hospitals - get actual hospital names
        for (UserAccount hospital : hospitals) {
            String key = "HOSPITAL_" + hospital.getEmail();
            String hospitalName = getHospitalName(hospital.getEmail());
            statsMap.put(key, new InstitutionStats(
                    hospitalName,
                    "Hospital",
                    new ArrayList<>(),
                    0,
                    0,
                    0.0,
                    LocalDateTime.now()
            ));
        }
        
        // Initialize with clinics - get actual clinic names
        for (UserAccount clinic : clinics) {
            String key = "CLINIC_" + clinic.getEmail();
            String clinicName = getClinicName(clinic.getEmail());
            statsMap.put(key, new InstitutionStats(
                    clinicName,
                    "Clinic",
                    new ArrayList<>(),
                    0,
                    0,
                    0.0,
                    LocalDateTime.now()
            ));
        }
        
        // Process vaccinations
        for (VaccinationHistory vaccination : vaccinations) {
            String key = vaccination.getInstitutionType() + "_" + vaccination.getLocation();
            InstitutionStats stats = statsMap.get(key);
            
            if (stats != null) {
                // Add vaccine to available vaccines if not already present
                if (!stats.getAvailableVaccines().contains(vaccination.getVaccineName())) {
                    stats.getAvailableVaccines().add(vaccination.getVaccineName());
                }
                
                // Update counts
                stats.setTotalVaccinations(stats.getTotalVaccinations() + 1);
                if ("completed".equals(vaccination.getStatus())) {
                    stats.setCompletedVaccinations(stats.getCompletedVaccinations() + 1);
                }
                
                // Update last activity
                if (vaccination.getUpdatedAt() != null && 
                    vaccination.getUpdatedAt().isAfter(stats.getLastActivity())) {
                    stats.setLastActivity(vaccination.getUpdatedAt());
                }
            }
        }
        
        // Calculate vaccination rates
        for (InstitutionStats stats : statsMap.values()) {
            if (stats.getTotalVaccinations() > 0) {
                stats.setVaccinationRate((double) stats.getCompletedVaccinations() / 
                                       stats.getTotalVaccinations() * 100);
            }
        }
        
        return new ArrayList<>(statsMap.values());
    }

    /**
     * Get hospital name by email
     */
    private String getHospitalName(String email) {
        try {
            Optional<Hospital> hospital = hospitalRepository.findByEmail(email);
            if (hospital.isPresent() && hospital.get().getUsername() != null && !hospital.get().getUsername().trim().isEmpty()) {
                return hospital.get().getUsername();
            }
            return email; // Fallback to email if username is not available
        } catch (Exception e) {
            log.warn("Error getting hospital name for email {}: {}", email, e.getMessage());
            return email;
        }
    }

    /**
     * Get clinic name by email
     */
    private String getClinicName(String email) {
        try {
            Optional<Clinic> clinic = clinicRepository.findByEmail(email);
            if (clinic.isPresent() && clinic.get().getUsername() != null && !clinic.get().getUsername().trim().isEmpty()) {
                return clinic.get().getUsername();
            }
            return email; // Fallback to email if username is not available
        } catch (Exception e) {
            log.warn("Error getting clinic name for email {}: {}", email, e.getMessage());
            return email;
        }
    }

    /**
     * Get vaccine-specific statistics
     */
    private List<VaccineStats> getVaccineStatistics(List<VaccinationHistory> vaccinations, 
                                                   List<Vaccine> vaccines) {
        Map<String, VaccineStats> statsMap = new HashMap<>();
        
        // Initialize with all vaccines
        for (Vaccine vaccine : vaccines) {
            statsMap.put(vaccine.getVaccineName(), new VaccineStats(
                    vaccine.getVaccineName(),
                    vaccine.getIsActive(),
                    new ArrayList<>(),
                    0,
                    0,
                    0.0
            ));
        }
        
        // Process vaccinations
        for (VaccinationHistory vaccination : vaccinations) {
            VaccineStats stats = statsMap.get(vaccination.getVaccineName());
            
            if (stats != null) {
                // Add institution to providing institutions if not already present
                String institutionKey = vaccination.getInstitutionType() + ": " + vaccination.getLocation();
                if (!stats.getProvidingInstitutions().contains(institutionKey)) {
                    stats.getProvidingInstitutions().add(institutionKey);
                }
                
                // Update counts
                stats.setTotalAdministered(stats.getTotalAdministered() + 1);
                if ("completed".equals(vaccination.getStatus())) {
                    stats.setCompletedAdministered(stats.getCompletedAdministered() + 1);
                }
            }
        }
        
        // Calculate completion rates
        for (VaccineStats stats : statsMap.values()) {
            if (stats.getTotalAdministered() > 0) {
                stats.setCompletionRate((double) stats.getCompletedAdministered() / 
                                      stats.getTotalAdministered() * 100);
            }
        }
        
        return new ArrayList<>(statsMap.values());
    }

    /**
     * Get recent activity
     */
    private List<RecentActivity> getRecentActivity(List<VaccinationHistory> vaccinations) {
        return vaccinations.stream()
                .filter(v -> v.getUpdatedAt() != null)
                .sorted((v1, v2) -> v2.getUpdatedAt().compareTo(v1.getUpdatedAt()))
                .limit(10)
                .map(v -> new RecentActivity(
                        v.getPatientName(),
                        v.getVaccineName(),
                        v.getLocation(),
                        v.getStatus(),
                        v.getUpdatedAt()
                ))
                .collect(Collectors.toList());
    }

    /**
     * Get detailed institution data
     */
    public InstitutionDetails getInstitutionDetails(String institutionEmail, String institutionType) {
        try {
            List<VaccinationHistory> vaccinations = vaccinationHistoryRepository.findAll().stream()
                    .filter(v -> v.getLocation().equals(institutionEmail) && 
                               v.getInstitutionType().equals(institutionType))
                    .collect(Collectors.toList());
            
            // Get available vaccines
            Set<String> availableVaccines = vaccinations.stream()
                    .map(VaccinationHistory::getVaccineName)
                    .collect(Collectors.toSet());
            
            // Get patient statistics
            Map<String, Long> patientCounts = vaccinations.stream()
                    .collect(Collectors.groupingBy(
                            VaccinationHistory::getPatientId,
                            Collectors.counting()
                    ));
            
            // Get vaccine statistics
            Map<String, Long> vaccineCounts = vaccinations.stream()
                    .collect(Collectors.groupingBy(
                            VaccinationHistory::getVaccineName,
                            Collectors.counting()
                    ));
            
            // Get proper institution name
            String institutionName;
            if ("Hospital".equals(institutionType)) {
                institutionName = getHospitalName(institutionEmail);
            } else {
                institutionName = getClinicName(institutionEmail);
            }
            
            return new InstitutionDetails(
                    institutionName,
                    institutionType,
                    new ArrayList<>(availableVaccines),
                    vaccinations.size(),
                    patientCounts.size(),
                    vaccineCounts,
                    vaccinations
            );
            
        } catch (Exception e) {
            log.error("Error getting institution details: {}", e.getMessage(), e);
            return new InstitutionDetails(institutionEmail, institutionType, List.of(), 0, 0, Map.of(), List.of());
        }
    }

    // Data transfer objects
    public static class VaccinationMonitoringStats {
        private final long totalVaccinations;
        private final long completedVaccinations;
        private final long pendingVaccinations;
        private final double vaccinationRate;
        private final List<InstitutionStats> institutionStats;
        private final List<VaccineStats> vaccineStats;
        private final List<RecentActivity> recentActivity;
        private final int totalVaccines;
        private final int totalInstitutions;

        public VaccinationMonitoringStats(long totalVaccinations, long completedVaccinations, 
                                        long pendingVaccinations, double vaccinationRate,
                                        List<InstitutionStats> institutionStats, 
                                        List<VaccineStats> vaccineStats,
                                        List<RecentActivity> recentActivity,
                                        int totalVaccines, int totalInstitutions) {
            this.totalVaccinations = totalVaccinations;
            this.completedVaccinations = completedVaccinations;
            this.pendingVaccinations = pendingVaccinations;
            this.vaccinationRate = vaccinationRate;
            this.institutionStats = institutionStats;
            this.vaccineStats = vaccineStats;
            this.recentActivity = recentActivity;
            this.totalVaccines = totalVaccines;
            this.totalInstitutions = totalInstitutions;
        }

        // Getters
        public long getTotalVaccinations() { return totalVaccinations; }
        public long getCompletedVaccinations() { return completedVaccinations; }
        public long getPendingVaccinations() { return pendingVaccinations; }
        public double getVaccinationRate() { return vaccinationRate; }
        public List<InstitutionStats> getInstitutionStats() { return institutionStats; }
        public List<VaccineStats> getVaccineStats() { return vaccineStats; }
        public List<RecentActivity> getRecentActivity() { return recentActivity; }
        public int getTotalVaccines() { return totalVaccines; }
        public int getTotalInstitutions() { return totalInstitutions; }
    }

    public static class InstitutionStats {
        private String institutionName;
        private String institutionType;
        private List<String> availableVaccines;
        private long totalVaccinations;
        private long completedVaccinations;
        private double vaccinationRate;
        private LocalDateTime lastActivity;

        public InstitutionStats(String institutionName, String institutionType, 
                              List<String> availableVaccines, long totalVaccinations,
                              long completedVaccinations, double vaccinationRate,
                              LocalDateTime lastActivity) {
            this.institutionName = institutionName;
            this.institutionType = institutionType;
            this.availableVaccines = availableVaccines;
            this.totalVaccinations = totalVaccinations;
            this.completedVaccinations = completedVaccinations;
            this.vaccinationRate = vaccinationRate;
            this.lastActivity = lastActivity;
        }

        // Getters and Setters
        public String getInstitutionName() { return institutionName; }
        public void setInstitutionName(String institutionName) { this.institutionName = institutionName; }
        public String getInstitutionType() { return institutionType; }
        public void setInstitutionType(String institutionType) { this.institutionType = institutionType; }
        public List<String> getAvailableVaccines() { return availableVaccines; }
        public void setAvailableVaccines(List<String> availableVaccines) { this.availableVaccines = availableVaccines; }
        public long getTotalVaccinations() { return totalVaccinations; }
        public void setTotalVaccinations(long totalVaccinations) { this.totalVaccinations = totalVaccinations; }
        public long getCompletedVaccinations() { return completedVaccinations; }
        public void setCompletedVaccinations(long completedVaccinations) { this.completedVaccinations = completedVaccinations; }
        public double getVaccinationRate() { return vaccinationRate; }
        public void setVaccinationRate(double vaccinationRate) { this.vaccinationRate = vaccinationRate; }
        public LocalDateTime getLastActivity() { return lastActivity; }
        public void setLastActivity(LocalDateTime lastActivity) { this.lastActivity = lastActivity; }
    }

    public static class VaccineStats {
        private String vaccineName;
        private boolean isActive;
        private List<String> providingInstitutions;
        private long totalAdministered;
        private long completedAdministered;
        private double completionRate;

        public VaccineStats(String vaccineName, boolean isActive, List<String> providingInstitutions,
                          long totalAdministered, long completedAdministered, double completionRate) {
            this.vaccineName = vaccineName;
            this.isActive = isActive;
            this.providingInstitutions = providingInstitutions;
            this.totalAdministered = totalAdministered;
            this.completedAdministered = completedAdministered;
            this.completionRate = completionRate;
        }

        // Getters and Setters
        public String getVaccineName() { return vaccineName; }
        public void setVaccineName(String vaccineName) { this.vaccineName = vaccineName; }
        public boolean getIsActive() { return isActive; }
        public void setIsActive(boolean isActive) { this.isActive = isActive; }
        public List<String> getProvidingInstitutions() { return providingInstitutions; }
        public void setProvidingInstitutions(List<String> providingInstitutions) { this.providingInstitutions = providingInstitutions; }
        public long getTotalAdministered() { return totalAdministered; }
        public void setTotalAdministered(long totalAdministered) { this.totalAdministered = totalAdministered; }
        public long getCompletedAdministered() { return completedAdministered; }
        public void setCompletedAdministered(long completedAdministered) { this.completedAdministered = completedAdministered; }
        public double getCompletionRate() { return completionRate; }
        public void setCompletionRate(double completionRate) { this.completionRate = completionRate; }
    }

    public static class RecentActivity {
        private String patientName;
        private String vaccineName;
        private String institutionName;
        private String status;
        private LocalDateTime timestamp;

        public RecentActivity(String patientName, String vaccineName, String institutionName,
                            String status, LocalDateTime timestamp) {
            this.patientName = patientName;
            this.vaccineName = vaccineName;
            this.institutionName = institutionName;
            this.status = status;
            this.timestamp = timestamp;
        }

        // Getters
        public String getPatientName() { return patientName; }
        public String getVaccineName() { return vaccineName; }
        public String getInstitutionName() { return institutionName; }
        public String getStatus() { return status; }
        public LocalDateTime getTimestamp() { return timestamp; }
    }

    public static class InstitutionDetails {
        private String institutionName;
        private String institutionType;
        private List<String> availableVaccines;
        private long totalVaccinations;
        private long uniquePatients;
        private Map<String, Long> vaccineCounts;
        private List<VaccinationHistory> vaccinationHistory;

        public InstitutionDetails(String institutionName, String institutionType,
                                List<String> availableVaccines, long totalVaccinations,
                                long uniquePatients, Map<String, Long> vaccineCounts,
                                List<VaccinationHistory> vaccinationHistory) {
            this.institutionName = institutionName;
            this.institutionType = institutionType;
            this.availableVaccines = availableVaccines;
            this.totalVaccinations = totalVaccinations;
            this.uniquePatients = uniquePatients;
            this.vaccineCounts = vaccineCounts;
            this.vaccinationHistory = vaccinationHistory;
        }

        // Getters
        public String getInstitutionName() { return institutionName; }
        public String getInstitutionType() { return institutionType; }
        public List<String> getAvailableVaccines() { return availableVaccines; }
        public long getTotalVaccinations() { return totalVaccinations; }
        public long getUniquePatients() { return uniquePatients; }
        public Map<String, Long> getVaccineCounts() { return vaccineCounts; }
        public List<VaccinationHistory> getVaccinationHistory() { return vaccinationHistory; }
    }
}
