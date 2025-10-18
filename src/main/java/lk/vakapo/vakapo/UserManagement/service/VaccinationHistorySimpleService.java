package lk.vakapo.vakapo.UserManagement.service;

import lk.vakapo.vakapo.UserManagement.model.VaccinationHistorySimple;
import lk.vakapo.vakapo.UserManagement.repository.VaccinationHistorySimpleRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class VaccinationHistorySimpleService {

    private final VaccinationHistorySimpleRepository repository;

    // Get all vaccination history for a patient
    public List<VaccinationHistorySimple> getPatientVaccinationHistory(String patientId) {
        return repository.findByPatientIdOrderByVaccinationDateDesc(patientId);
    }

    // Get pending vaccinations
    public List<VaccinationHistorySimple> getPendingVaccinations() {
        return repository.findByStatusOrderByVaccinationDateDesc("pending");
    }

    // Get completed vaccinations
    public List<VaccinationHistorySimple> getCompletedVaccinations() {
        return repository.findByStatusOrderByVaccinationDateDesc("completed");
    }

    // Update dosage level
    public boolean updateDosageLevel(Long historyId, String dosageLevel) {
        Optional<VaccinationHistorySimple> historyOpt = repository.findById(historyId);
        if (historyOpt.isPresent()) {
            VaccinationHistorySimple history = historyOpt.get();
            history.setDosageLevel(dosageLevel);
            repository.save(history);
            return true;
        }
        return false;
    }

    // Update additional notes
    public boolean updateAdditionalNotes(Long historyId, String additionalNotes) {
        Optional<VaccinationHistorySimple> historyOpt = repository.findById(historyId);
        if (historyOpt.isPresent()) {
            VaccinationHistorySimple history = historyOpt.get();
            history.setAdditionalNotes(additionalNotes);
            repository.save(history);
            return true;
        }
        return false;
    }

    // Create new vaccination history
    public VaccinationHistorySimple createVaccinationHistory(
            String patientId, String patientName, String vaccineName, 
            String timeSlot, String location, String doctorName) {
        
        VaccinationHistorySimple history = new VaccinationHistorySimple();
        history.setPatientId(patientId);
        history.setPatientName(patientName);
        history.setVaccineName(vaccineName);
        history.setVaccinationDate(java.time.LocalDate.now());
        history.setTimeSlot(timeSlot);
        history.setLocation(location);
        history.setDoctorName(doctorName);
        history.setDosageLevel("0ml");
        history.setAdditionalNotes("");
        history.setStatus("pending");
        
        return repository.save(history);
    }

    // Delete vaccination history record
    public boolean deleteVaccinationHistory(Long historyId, String patientId) {
        Optional<VaccinationHistorySimple> historyOpt = repository.findById(historyId);
        if (historyOpt.isPresent()) {
            VaccinationHistorySimple history = historyOpt.get();
            // Verify that the record belongs to the requesting patient
            if (history.getPatientId().equals(patientId)) {
                repository.delete(history);
                return true;
            }
        }
        return false;
    }
}
