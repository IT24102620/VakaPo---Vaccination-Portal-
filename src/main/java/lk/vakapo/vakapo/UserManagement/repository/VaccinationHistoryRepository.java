package lk.vakapo.vakapo.UserManagement.repository;

import lk.vakapo.vakapo.UserManagement.model.VaccinationHistory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface VaccinationHistoryRepository extends JpaRepository<VaccinationHistory, Long> {

    // Find vaccination history by patient ID
    List<VaccinationHistory> findByPatientIdOrderByVaccinationDateDesc(String patientId);

    // Find vaccination history by patient email
    List<VaccinationHistory> findByPatientEmailOrderByVaccinationDateDesc(String patientEmail);

    // Find vaccination history by doctor name
    List<VaccinationHistory> findByDoctorNameOrderByVaccinationDateDesc(String doctorName);

    // Find vaccination history by institution
    @Query("SELECT vh FROM VaccinationHistory vh WHERE vh.institutionId = :institutionId AND vh.institutionType = :institutionType ORDER BY vh.vaccinationDate DESC")
    List<VaccinationHistory> findByInstitutionIdAndType(@Param("institutionId") String institutionId, 
                                                        @Param("institutionType") String institutionType);

    // Find vaccination history by doctor and institution
    @Query("SELECT vh FROM VaccinationHistory vh WHERE vh.doctorName = :doctorName AND vh.institutionId = :institutionId AND vh.institutionType = :institutionType ORDER BY vh.vaccinationDate DESC")
    List<VaccinationHistory> findByDoctorNameAndInstitution(@Param("doctorName") String doctorName, 
                                                           @Param("institutionId") String institutionId, 
                                                           @Param("institutionType") String institutionType);

    // Find vaccination history by status
    List<VaccinationHistory> findByStatusOrderByVaccinationDateDesc(String status);

    // Find vaccination history by date range
    @Query("SELECT vh FROM VaccinationHistory vh WHERE vh.vaccinationDate BETWEEN :startDate AND :endDate ORDER BY vh.vaccinationDate DESC")
    List<VaccinationHistory> findByVaccinationDateBetween(@Param("startDate") LocalDate startDate, 
                                                         @Param("endDate") LocalDate endDate);

    // Find vaccination history by vaccine name
    List<VaccinationHistory> findByVaccineNameOrderByVaccinationDateDesc(String vaccineName);

    // Find pending vaccinations for a doctor
    @Query("SELECT vh FROM VaccinationHistory vh WHERE vh.doctorName = :doctorName AND vh.status = 'pending' ORDER BY vh.vaccinationDate ASC")
    List<VaccinationHistory> findPendingVaccinationsByDoctor(@Param("doctorName") String doctorName);

    // Find completed vaccinations for a patient
    @Query("SELECT vh FROM VaccinationHistory vh WHERE vh.patientId = :patientId AND vh.status = 'completed' ORDER BY vh.vaccinationDate DESC")
    List<VaccinationHistory> findCompletedVaccinationsByPatient(@Param("patientId") String patientId);

    // Find vaccination history with next vaccine dates
    @Query("SELECT vh FROM VaccinationHistory vh WHERE vh.nextVaccineDate IS NOT NULL ORDER BY vh.nextVaccineDate ASC")
    List<VaccinationHistory> findVaccinationsWithNextDate();

    // Find vaccination history by patient with next vaccine dates
    @Query("SELECT vh FROM VaccinationHistory vh WHERE vh.patientId = :patientId AND vh.nextVaccineDate IS NOT NULL ORDER BY vh.nextVaccineDate ASC")
    List<VaccinationHistory> findVaccinationsWithNextDateByPatient(@Param("patientId") String patientId);

    // Find recent updates (last 30 days)
    @Query("SELECT vh FROM VaccinationHistory vh WHERE vh.updatedAt >= :since ORDER BY vh.updatedAt DESC")
    List<VaccinationHistory> findRecentUpdates(@Param("since") LocalDateTime since);
    
    // Find recent updates (last 30 days) - default method
    default List<VaccinationHistory> findRecentUpdates() {
        LocalDateTime thirtyDaysAgo = LocalDateTime.now().minusDays(30);
        return findRecentUpdates(thirtyDaysAgo);
    }

    // Deletion methods for cascading deletes
    void deleteByPatientEmail(String patientEmail);
    void deleteByDoctorName(String doctorName);
    void deleteByInstitutionTypeAndInstitutionId(String institutionType, String institutionId);
    void deleteByDoctorNameAndInstitutionTypeAndInstitutionId(String doctorName, String institutionType, String institutionId);
    
    // Find methods for deletion service
    List<VaccinationHistory> findByPatientEmail(String patientEmail);
    List<VaccinationHistory> findByDoctorName(String doctorName);
    List<VaccinationHistory> findByInstitutionTypeAndInstitutionId(String institutionType, String institutionId);
    List<VaccinationHistory> findByDoctorNameAndInstitutionTypeAndInstitutionId(String doctorName, String institutionType, String institutionId);
}
