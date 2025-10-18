package lk.vakapo.vakapo.UserManagement.repository;

import lk.vakapo.vakapo.UserManagement.model.VaccinationSchedule;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface VaccinationScheduleRepository extends JpaRepository<VaccinationSchedule, Long> {

    // Find all schedules for a specific institution
    @Query("SELECT vs FROM VaccinationSchedule vs WHERE vs.institutionId = :institutionId AND vs.institutionType = :institutionType ORDER BY vs.createdAt ASC")
    List<VaccinationSchedule> findByInstitutionIdAndType(@Param("institutionId") String institutionId, 
                                                         @Param("institutionType") String institutionType);

    // Find schedules by status for an institution
    @Query("SELECT vs FROM VaccinationSchedule vs WHERE vs.institutionId = :institutionId AND vs.institutionType = :institutionType AND vs.status = :status ORDER BY vs.createdAt ASC")
    List<VaccinationSchedule> findByInstitutionIdAndTypeAndStatus(@Param("institutionId") String institutionId, 
                                                                  @Param("institutionType") String institutionType,
                                                                  @Param("status") String status);

    // Find upcoming schedules (scheduled status)
    @Query("SELECT vs FROM VaccinationSchedule vs WHERE vs.institutionId = :institutionId AND vs.institutionType = :institutionType AND vs.status = 'scheduled' ORDER BY vs.createdAt ASC")
    List<VaccinationSchedule> findUpcomingSchedules(@Param("institutionId") String institutionId, 
                                                    @Param("institutionType") String institutionType);

    // Find schedules by doctor name
    List<VaccinationSchedule> findByDoctorName(String doctorName);

    // Find schedules by doctor name and institution
    @Query("SELECT vs FROM VaccinationSchedule vs WHERE vs.doctorName = :doctorName AND vs.institutionId = :institutionId AND vs.institutionType = :institutionType ORDER BY vs.createdAt ASC")
    List<VaccinationSchedule> findByDoctorNameAndInstitution(@Param("doctorName") String doctorName,
                                                             @Param("institutionId") String institutionId,
                                                             @Param("institutionType") String institutionType);

    // Find all unique vaccine names
    @Query("SELECT DISTINCT vs.vaccineName FROM VaccinationSchedule vs WHERE vs.status = 'scheduled' ORDER BY vs.vaccineName ASC")
    List<String> findDistinctVaccineNames();

    // Find institutions that offer a specific vaccine
    @Query("SELECT DISTINCT vs.institutionId, vs.institutionType FROM VaccinationSchedule vs WHERE vs.vaccineName = :vaccineName AND vs.status = 'scheduled' ORDER BY vs.institutionType ASC, vs.institutionId ASC")
    List<Object[]> findInstitutionsByVaccine(@Param("vaccineName") String vaccineName);

    // Find schedules for a specific vaccine and institution
    @Query("SELECT vs FROM VaccinationSchedule vs WHERE vs.vaccineName = :vaccineName AND vs.institutionId = :institutionId AND vs.institutionType = :institutionType AND vs.status = 'scheduled' ORDER BY vs.createdAt ASC")
    List<VaccinationSchedule> findByVaccineAndInstitution(@Param("vaccineName") String vaccineName,
                                                         @Param("institutionId") String institutionId,
                                                         @Param("institutionType") String institutionType);
}
