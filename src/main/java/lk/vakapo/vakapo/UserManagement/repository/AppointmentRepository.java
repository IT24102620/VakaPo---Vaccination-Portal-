package lk.vakapo.vakapo.UserManagement.repository;

import lk.vakapo.vakapo.UserManagement.model.Appointment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface AppointmentRepository extends JpaRepository<Appointment, Long> {

    // Find appointments by patient email
    List<Appointment> findByPatientEmailOrderByAppointmentDateDesc(String patientEmail);

    // Find appointments by patient ID
    List<Appointment> findByPatientIdOrderByAppointmentDateDesc(String patientId);

    // Find appointments by institution
    @Query("SELECT a FROM Appointment a WHERE a.institutionId = :institutionId AND a.institutionType = :institutionType ORDER BY a.appointmentDate ASC, a.timeSlot ASC")
    List<Appointment> findByInstitutionIdAndType(@Param("institutionId") String institutionId, 
                                                @Param("institutionType") String institutionType);

    // Find appointments by doctor
    List<Appointment> findByDoctorNameOrderByAppointmentDateAsc(String doctorName);

    // Find appointments by date and institution
    @Query("SELECT a FROM Appointment a WHERE a.appointmentDate = :date AND a.institutionId = :institutionId AND a.institutionType = :institutionType ORDER BY a.timeSlot ASC")
    List<Appointment> findByDateAndInstitution(@Param("date") LocalDate date, 
                                              @Param("institutionId") String institutionId, 
                                              @Param("institutionType") String institutionType);

    // Find appointments by date, institution, and time slot
    @Query("SELECT a FROM Appointment a WHERE a.appointmentDate = :date AND a.institutionId = :institutionId AND a.institutionType = :institutionType AND a.timeSlot = :timeSlot")
    List<Appointment> findByDateAndInstitutionAndTimeSlot(@Param("date") LocalDate date, 
                                                         @Param("institutionId") String institutionId, 
                                                         @Param("institutionType") String institutionType,
                                                         @Param("timeSlot") String timeSlot);

    // Find appointments by status
    List<Appointment> findByStatusOrderByAppointmentDateAsc(String status);

    // Find upcoming appointments for a patient
    @Query("SELECT a FROM Appointment a WHERE a.patientEmail = :patientEmail AND a.appointmentDate >= :today AND a.status = 'scheduled' ORDER BY a.appointmentDate ASC, a.timeSlot ASC")
    List<Appointment> findUpcomingAppointmentsForPatient(@Param("patientEmail") String patientEmail, 
                                                        @Param("today") LocalDate today);

    // Find appointments by vaccine name
    List<Appointment> findByVaccineNameOrderByAppointmentDateDesc(String vaccineName);

    // Check if a time slot is available
    @Query("SELECT COUNT(a) FROM Appointment a WHERE a.appointmentDate = :date AND a.institutionId = :institutionId AND a.institutionType = :institutionType AND a.timeSlot = :timeSlot AND a.status IN ('scheduled', 'completed')")
    long countByDateAndInstitutionAndTimeSlot(@Param("date") LocalDate date, 
                                             @Param("institutionId") String institutionId, 
                                             @Param("institutionType") String institutionType,
                                             @Param("timeSlot") String timeSlot);

    // Find appointments by doctor name and institution
    @Query("SELECT a FROM Appointment a WHERE a.doctorName = :doctorName AND a.institutionId = :institutionId AND a.institutionType = :institutionType ORDER BY a.appointmentDate ASC, a.timeSlot ASC")
    List<Appointment> findByDoctorNameAndInstitution(@Param("doctorName") String doctorName, 
                                                    @Param("institutionId") String institutionId, 
                                                    @Param("institutionType") String institutionType);

    // Find appointments by doctor name and institution with status filter
    @Query("SELECT a FROM Appointment a WHERE a.doctorName = :doctorName AND a.institutionId = :institutionId AND a.institutionType = :institutionType AND a.status = :status ORDER BY a.appointmentDate ASC, a.timeSlot ASC")
    List<Appointment> findByDoctorNameAndInstitutionAndStatus(@Param("doctorName") String doctorName, 
                                                             @Param("institutionId") String institutionId, 
                                                             @Param("institutionType") String institutionType,
                                                             @Param("status") String status);

    // Find appointments by institution and doctor name (case insensitive and partial match)
    @Query("SELECT a FROM Appointment a WHERE LOWER(a.doctorName) LIKE LOWER(CONCAT('%', :doctorName, '%')) AND a.institutionId = :institutionId AND a.institutionType = :institutionType ORDER BY a.appointmentDate ASC, a.timeSlot ASC")
    List<Appointment> findByInstitutionAndDoctorNameContaining(@Param("doctorName") String doctorName, 
                                                              @Param("institutionId") String institutionId, 
                                                              @Param("institutionType") String institutionType);

    // Find appointments by institution type
    List<Appointment> findByInstitutionTypeOrderByAppointmentDateAsc(String institutionType);

    // Deletion methods for cascading deletes
    void deleteByPatientEmail(String patientEmail);
    void deleteByDoctorName(String doctorName);
    void deleteByInstitutionTypeAndInstitutionId(String institutionType, String institutionId);
    void deleteByDoctorNameAndInstitutionTypeAndInstitutionId(String doctorName, String institutionType, String institutionId);
    
    // Find methods for deletion service
    List<Appointment> findByPatientEmail(String patientEmail);
    List<Appointment> findByDoctorName(String doctorName);
    List<Appointment> findByInstitutionTypeAndInstitutionId(String institutionType, String institutionId);
    List<Appointment> findByDoctorNameAndInstitutionTypeAndInstitutionId(String doctorName, String institutionType, String institutionId);
}
