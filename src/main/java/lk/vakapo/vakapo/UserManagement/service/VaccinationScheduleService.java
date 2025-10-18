package lk.vakapo.vakapo.UserManagement.service;

import lk.vakapo.vakapo.MailManagement.EmailService;
import lk.vakapo.vakapo.UserManagement.model.Clinic;
import lk.vakapo.vakapo.UserManagement.model.Hospital;
import lk.vakapo.vakapo.UserManagement.model.Staff;
import lk.vakapo.vakapo.UserManagement.model.VaccinationSchedule;
import lk.vakapo.vakapo.UserManagement.repository.ClinicRepository;
import lk.vakapo.vakapo.UserManagement.repository.HospitalRepository;
import lk.vakapo.vakapo.UserManagement.repository.VaccinationScheduleRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class VaccinationScheduleService {

    private final VaccinationScheduleRepository vaccinationScheduleRepository;
    private final StaffService staffService;
    private final HospitalRepository hospitalRepository;
    private final ClinicRepository clinicRepository;
    private final EmailService emailService;

    /**
     * Create a new vaccination schedule
     */
    @Transactional
    public VaccinationSchedule createSchedule(VaccinationSchedule schedule) {
        try {
            log.info("Creating vaccination schedule for doctor: {} at institution: {}:{}", 
                    schedule.getDoctorName(), schedule.getInstitutionType(), schedule.getInstitutionId());
            
            VaccinationSchedule savedSchedule = vaccinationScheduleRepository.save(schedule);
            
            log.info("Vaccination schedule created successfully with ID: {}", savedSchedule.getId());
            
            // Send email notification to the doctor
            sendDoctorNotification(savedSchedule);
            
            return savedSchedule;
            
        } catch (Exception e) {
            log.error("Error creating vaccination schedule: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to create vaccination schedule: " + e.getMessage(), e);
        }
    }

    /**
     * Send email notification to doctor about the new vaccination schedule
     */
    private void sendDoctorNotification(VaccinationSchedule schedule) {
        try {
            log.info("Attempting to send email notification to doctor: {}", schedule.getDoctorName());
            
            // Find the doctor by name and institution
            Optional<Staff> doctorOpt = staffService.getStaffByNameAndInstitution(
                schedule.getDoctorName(), 
                schedule.getInstitutionId(), 
                schedule.getInstitutionType()
            );
            
            if (doctorOpt.isEmpty()) {
                log.warn("Doctor not found: {} at institution {}:{}", 
                        schedule.getDoctorName(), schedule.getInstitutionType(), schedule.getInstitutionId());
                return;
            }
            
            Staff doctor = doctorOpt.get();
            log.info("Found doctor: {} with email: {}", doctor.getName(), doctor.getEmail());
            
            // Get institution name
            String institutionName = getInstitutionName(schedule.getInstitutionType(), schedule.getInstitutionId());
            
            // Send email notification
            emailService.sendVaccinationScheduleNotification(
                doctor.getEmail(),
                doctor.getName(),
                institutionName,
                schedule.getInstitutionType(),
                schedule.getVaccineName(),
                schedule.getTimeFrom(),
                schedule.getTimeTo(),
                schedule.getDays(),
                schedule.getNotes()
            );
            
            log.info("Email notification sent successfully to doctor: {} ({})", 
                    doctor.getName(), doctor.getEmail());
            
        } catch (Exception e) {
            log.error("Failed to send email notification to doctor: {}", e.getMessage(), e);
            // Don't throw exception - email failure shouldn't prevent schedule creation
        }
    }

    /**
     * Get institution name by type and ID
     */
    private String getInstitutionName(String institutionType, String institutionId) {
        try {
            if ("Hospital".equals(institutionType)) {
                Optional<Hospital> hospitalOpt = hospitalRepository.findById(institutionId);
                return hospitalOpt.map(Hospital::getUsername).orElse("Unknown Hospital");
            } else if ("Clinic".equals(institutionType)) {
                Optional<Clinic> clinicOpt = clinicRepository.findById(institutionId);
                return clinicOpt.map(Clinic::getUsername).orElse("Unknown Clinic");
            }
            return "Unknown Institution";
        } catch (Exception e) {
            log.error("Error getting institution name for type: {} and ID: {}", institutionType, institutionId, e);
            return "Unknown Institution";
        }
    }

    /**
     * Cancel a vaccination schedule and notify the doctor
     */
    @Transactional
    public boolean cancelSchedule(Long scheduleId) {
        try {
            log.info("Cancelling vaccination schedule with ID: {}", scheduleId);
            
            // Find the schedule first
            Optional<VaccinationSchedule> scheduleOpt = vaccinationScheduleRepository.findById(scheduleId);
            if (scheduleOpt.isEmpty()) {
                log.warn("Vaccination schedule not found with ID: {}", scheduleId);
                return false;
            }
            
            VaccinationSchedule schedule = scheduleOpt.get();
            log.info("Found schedule to cancel: doctor={}, institution={}:{}", 
                    schedule.getDoctorName(), schedule.getInstitutionType(), schedule.getInstitutionId());
            
            // Send cancellation notification to doctor before deleting
            sendDoctorCancellationNotification(schedule);
            
            // Delete the schedule from database
            vaccinationScheduleRepository.delete(schedule);
            
            log.info("Vaccination schedule cancelled and deleted successfully with ID: {}", scheduleId);
            return true;
            
        } catch (Exception e) {
            log.error("Error cancelling vaccination schedule with ID: {}", scheduleId, e);
            throw new RuntimeException("Failed to cancel vaccination schedule: " + e.getMessage(), e);
        }
    }

    /**
     * Send cancellation email notification to doctor
     */
    private void sendDoctorCancellationNotification(VaccinationSchedule schedule) {
        try {
            log.info("Attempting to send cancellation email notification to doctor: {}", schedule.getDoctorName());
            
            // Find the doctor by name and institution
            Optional<Staff> doctorOpt = staffService.getStaffByNameAndInstitution(
                schedule.getDoctorName(), 
                schedule.getInstitutionId(), 
                schedule.getInstitutionType()
            );
            
            if (doctorOpt.isEmpty()) {
                log.warn("Doctor not found for cancellation notification: {} at institution {}:{}", 
                        schedule.getDoctorName(), schedule.getInstitutionType(), schedule.getInstitutionId());
                return;
            }
            
            Staff doctor = doctorOpt.get();
            log.info("Found doctor for cancellation notification: {} with email: {}", doctor.getName(), doctor.getEmail());
            
            // Get institution name
            String institutionName = getInstitutionName(schedule.getInstitutionType(), schedule.getInstitutionId());
            
            // Send cancellation email notification
            emailService.sendVaccinationScheduleCancellationNotification(
                doctor.getEmail(),
                doctor.getName(),
                institutionName,
                schedule.getInstitutionType(),
                schedule.getVaccineName(),
                schedule.getTimeFrom(),
                schedule.getTimeTo(),
                schedule.getDays(),
                schedule.getNotes()
            );
            
            log.info("Cancellation email notification sent successfully to doctor: {} ({})", 
                    doctor.getName(), doctor.getEmail());
            
        } catch (Exception e) {
            log.error("Failed to send cancellation email notification to doctor: {}", e.getMessage(), e);
            // Don't throw exception - email failure shouldn't prevent schedule cancellation
        }
    }

    /**
     * Get all schedules for an institution
     */
    public List<VaccinationSchedule> getSchedulesByInstitution(String institutionId, String institutionType) {
        try {
            log.info("Retrieving vaccination schedules for institution: {}:{}", institutionType, institutionId);
            
            List<VaccinationSchedule> schedules = vaccinationScheduleRepository.findByInstitutionIdAndType(institutionId, institutionType);
            
            log.info("Retrieved {} vaccination schedules for institution: {}:{}", schedules.size(), institutionType, institutionId);
            return schedules;
            
        } catch (Exception e) {
            log.error("Error retrieving vaccination schedules: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to retrieve vaccination schedules: " + e.getMessage(), e);
        }
    }

    /**
     * Get upcoming schedules for an institution
     */
    public List<VaccinationSchedule> getUpcomingSchedules(String institutionId, String institutionType) {
        try {
            log.info("Retrieving upcoming vaccination schedules for institution: {}:{}", institutionType, institutionId);
            
            List<VaccinationSchedule> schedules = vaccinationScheduleRepository.findUpcomingSchedules(institutionId, institutionType);
            
            log.info("Retrieved {} upcoming vaccination schedules for institution: {}:{}", schedules.size(), institutionType, institutionId);
            return schedules;
            
        } catch (Exception e) {
            log.error("Error retrieving upcoming vaccination schedules: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to retrieve upcoming vaccination schedules: " + e.getMessage(), e);
        }
    }

    /**
     * Get schedule by ID
     */
    public Optional<VaccinationSchedule> getScheduleById(Long scheduleId) {
        return vaccinationScheduleRepository.findById(scheduleId);
    }


    /**
     * Complete a vaccination schedule
     */
    @Transactional
    public void completeSchedule(Long scheduleId) {
        try {
            log.info("Completing vaccination schedule with ID: {}", scheduleId);
            
            Optional<VaccinationSchedule> scheduleOpt = vaccinationScheduleRepository.findById(scheduleId);
            if (scheduleOpt.isEmpty()) {
                throw new IllegalArgumentException("Vaccination schedule not found with ID: " + scheduleId);
            }
            
            VaccinationSchedule schedule = scheduleOpt.get();
            schedule.setStatus("completed");
            schedule.setUpdatedAt(LocalDateTime.now());
            
            vaccinationScheduleRepository.save(schedule);
            
            log.info("Vaccination schedule completed successfully: {} for doctor: {}", 
                    scheduleId, schedule.getDoctorName());
            
        } catch (Exception e) {
            log.error("Error completing vaccination schedule: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to complete vaccination schedule: " + e.getMessage(), e);
        }
    }

    /**
     * Update a vaccination schedule
     */
    @Transactional
    public VaccinationSchedule updateSchedule(VaccinationSchedule schedule) {
        try {
            log.info("Updating vaccination schedule with ID: {}", schedule.getId());
            
            VaccinationSchedule updatedSchedule = vaccinationScheduleRepository.save(schedule);
            
            log.info("Vaccination schedule updated successfully: {}", schedule.getId());
            return updatedSchedule;
            
        } catch (Exception e) {
            log.error("Error updating vaccination schedule: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to update vaccination schedule: " + e.getMessage(), e);
        }
    }
}
