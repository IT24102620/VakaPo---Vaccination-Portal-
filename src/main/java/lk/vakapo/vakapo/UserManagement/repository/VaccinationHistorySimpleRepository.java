package lk.vakapo.vakapo.UserManagement.repository;

import lk.vakapo.vakapo.UserManagement.model.VaccinationHistorySimple;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface VaccinationHistorySimpleRepository extends JpaRepository<VaccinationHistorySimple, Long> {
    
    // Find by patient ID
    List<VaccinationHistorySimple> findByPatientIdOrderByVaccinationDateDesc(String patientId);
    
    // Find by status
    List<VaccinationHistorySimple> findByStatusOrderByVaccinationDateDesc(String status);
    
    // Find by doctor name
    List<VaccinationHistorySimple> findByDoctorNameOrderByVaccinationDateDesc(String doctorName);
}

