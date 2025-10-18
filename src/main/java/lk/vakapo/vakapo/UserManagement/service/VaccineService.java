package lk.vakapo.vakapo.UserManagement.service;

import lk.vakapo.vakapo.UserManagement.model.Vaccine;
import lk.vakapo.vakapo.UserManagement.repository.VaccineRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class VaccineService {

    private final VaccineRepository vaccineRepository;

    /**
     * Get all vaccines
     */
    public List<Vaccine> getAllVaccines() {
        try {
            return vaccineRepository.findAllByOrderByIdAsc();
        } catch (Exception e) {
            log.error("Error fetching all vaccines: {}", e.getMessage(), e);
            return List.of();
        }
    }

    /**
     * Get all active vaccines
     */
    public List<Vaccine> getActiveVaccines() {
        try {
            return vaccineRepository.findByIsActiveTrueOrderByVaccineNameAsc();
        } catch (Exception e) {
            log.error("Error fetching active vaccines: {}", e.getMessage(), e);
            return List.of();
        }
    }

    /**
     * Get vaccine by ID
     */
    public Optional<Vaccine> getVaccineById(Long id) {
        try {
            return vaccineRepository.findById(id);
        } catch (Exception e) {
            log.error("Error fetching vaccine by ID {}: {}", id, e.getMessage(), e);
            return Optional.empty();
        }
    }

    /**
     * Get vaccine by name
     */
    public Optional<Vaccine> getVaccineByName(String vaccineName) {
        try {
            return vaccineRepository.findByVaccineName(vaccineName);
        } catch (Exception e) {
            log.error("Error fetching vaccine by name {}: {}", vaccineName, e.getMessage(), e);
            return Optional.empty();
        }
    }

    /**
     * Create a new vaccine
     */
    @Transactional
    public Vaccine createVaccine(String vaccineName) {
        try {
            // Check if vaccine name already exists
            if (vaccineRepository.existsByVaccineName(vaccineName)) {
                throw new IllegalArgumentException("Vaccine with name '" + vaccineName + "' already exists");
            }

            Vaccine vaccine = new Vaccine();
            vaccine.setVaccineName(vaccineName.trim());
            vaccine.setIsActive(true);

            Vaccine savedVaccine = vaccineRepository.save(vaccine);
            log.info("Created new vaccine: {}", savedVaccine.getVaccineName());
            return savedVaccine;

        } catch (Exception e) {
            log.error("Error creating vaccine '{}': {}", vaccineName, e.getMessage(), e);
            throw e;
        }
    }

    /**
     * Update vaccine
     */
    @Transactional
    public Vaccine updateVaccine(Long id, String vaccineName) {
        try {
            Optional<Vaccine> existingVaccine = vaccineRepository.findById(id);
            if (existingVaccine.isEmpty()) {
                throw new IllegalArgumentException("Vaccine with ID " + id + " not found");
            }

            Vaccine vaccine = existingVaccine.get();
            
            // Check if new name already exists (excluding current vaccine)
            if (!vaccine.getVaccineName().equals(vaccineName.trim()) && 
                vaccineRepository.existsByVaccineName(vaccineName.trim())) {
                throw new IllegalArgumentException("Vaccine with name '" + vaccineName + "' already exists");
            }

            vaccine.setVaccineName(vaccineName.trim());
            Vaccine updatedVaccine = vaccineRepository.save(vaccine);
            log.info("Updated vaccine ID {}: {}", id, updatedVaccine.getVaccineName());
            return updatedVaccine;

        } catch (Exception e) {
            log.error("Error updating vaccine ID {}: {}", id, e.getMessage(), e);
            throw e;
        }
    }

    /**
     * Toggle vaccine active status
     */
    @Transactional
    public Vaccine toggleVaccineStatus(Long id) {
        try {
            Optional<Vaccine> existingVaccine = vaccineRepository.findById(id);
            if (existingVaccine.isEmpty()) {
                throw new IllegalArgumentException("Vaccine with ID " + id + " not found");
            }

            Vaccine vaccine = existingVaccine.get();
            vaccine.setIsActive(!vaccine.getIsActive());
            Vaccine updatedVaccine = vaccineRepository.save(vaccine);
            
            log.info("Toggled vaccine ID {} status to: {}", id, updatedVaccine.getIsActive() ? "Active" : "Inactive");
            return updatedVaccine;

        } catch (Exception e) {
            log.error("Error toggling vaccine status ID {}: {}", id, e.getMessage(), e);
            throw e;
        }
    }

    /**
     * Delete vaccine
     */
    @Transactional
    public boolean deleteVaccine(Long id) {
        try {
            if (!vaccineRepository.existsById(id)) {
                throw new IllegalArgumentException("Vaccine with ID " + id + " not found");
            }

            vaccineRepository.deleteById(id);
            log.info("Deleted vaccine ID: {}", id);
            return true;

        } catch (Exception e) {
            log.error("Error deleting vaccine ID {}: {}", id, e.getMessage(), e);
            throw e;
        }
    }

    /**
     * Search vaccines by name
     */
    public List<Vaccine> searchVaccines(String searchTerm) {
        try {
            if (searchTerm == null || searchTerm.trim().isEmpty()) {
                return getAllVaccines();
            }
            return vaccineRepository.findByVaccineNameContainingIgnoreCaseOrderByVaccineNameAsc(searchTerm.trim());
        } catch (Exception e) {
            log.error("Error searching vaccines with term '{}': {}", searchTerm, e.getMessage(), e);
            return List.of();
        }
    }

    /**
     * Get vaccine statistics
     */
    public VaccineStats getVaccineStats() {
        try {
            long totalVaccines = vaccineRepository.countAllBy();
            long activeVaccines = vaccineRepository.countByIsActiveTrue();
            long inactiveVaccines = totalVaccines - activeVaccines;

            return new VaccineStats(totalVaccines, activeVaccines, inactiveVaccines);
        } catch (Exception e) {
            log.error("Error getting vaccine statistics: {}", e.getMessage(), e);
            return new VaccineStats(0, 0, 0);
        }
    }

    /**
     * Inner class for vaccine statistics
     */
    public static class VaccineStats {
        private final long totalVaccines;
        private final long activeVaccines;
        private final long inactiveVaccines;

        public VaccineStats(long totalVaccines, long activeVaccines, long inactiveVaccines) {
            this.totalVaccines = totalVaccines;
            this.activeVaccines = activeVaccines;
            this.inactiveVaccines = inactiveVaccines;
        }

        public long getTotalVaccines() { return totalVaccines; }
        public long getActiveVaccines() { return activeVaccines; }
        public long getInactiveVaccines() { return inactiveVaccines; }
    }
}
