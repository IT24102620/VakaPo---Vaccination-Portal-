package lk.vakapo.vakapo.UserManagement.service;

import lk.vakapo.vakapo.MailManagement.EmailService;
import lk.vakapo.vakapo.UserManagement.model.*;
import lk.vakapo.vakapo.UserManagement.repository.*;
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
public class AccountDeletionService {

    private final UserRepository userRepository;
    private final PatientRepository patientRepository;
    private final HospitalRepository hospitalRepository;
    private final ClinicRepository clinicRepository;
    private final StaffRepository staffRepository;
    private final AppointmentRepository appointmentRepository;
    private final VaccinationHistoryRepository vaccinationHistoryRepository;
    private final EmailService emailService;

    /**
     * Delete patient account and all related data
     */
    @Transactional
    public String deletePatientAccount(String patientEmail) {
        try {
            log.info("Starting patient account deletion for: {}", patientEmail);
            
            // Find patient
            Optional<Patient> patientOpt = patientRepository.findByEmail(patientEmail);
            if (patientOpt.isEmpty()) {
                return "Patient not found";
            }
            
            Patient patient = patientOpt.get();
            String patientName = patient.getPatientName();
            
            // Get all appointments for this patient
            List<Appointment> appointments = appointmentRepository.findByPatientEmail(patientEmail);
            log.info("Found {} appointments for patient: {}", appointments.size(), patientEmail);
            
            // Get all vaccination history for this patient
            List<VaccinationHistory> vaccinationHistory = vaccinationHistoryRepository.findByPatientEmail(patientEmail);
            log.info("Found {} vaccination records for patient: {}", vaccinationHistory.size(), patientEmail);
            
            // Delete appointments
            appointmentRepository.deleteByPatientEmail(patientEmail);
            log.info("Deleted {} appointments for patient: {}", appointments.size(), patientEmail);
            
            // Delete vaccination history
            vaccinationHistoryRepository.deleteByPatientEmail(patientEmail);
            log.info("Deleted {} vaccination records for patient: {}", vaccinationHistory.size(), patientEmail);
            
            // Delete patient record
            patientRepository.deleteByEmail(patientEmail);
            log.info("Deleted patient record for: {}", patientEmail);
            
            // Delete user account
            userRepository.deleteByEmail(patientEmail);
            log.info("Deleted user account for: {}", patientEmail);
            
            log.info("Patient account deletion completed successfully for: {}", patientEmail);
            return "Patient account and all related data deleted successfully";
            
        } catch (Exception e) {
            log.error("Error deleting patient account: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to delete patient account: " + e.getMessage(), e);
        }
    }

    /**
     * Delete doctor/nurse account and all related data
     */
    @Transactional
    public String deleteStaffAccount(String staffEmail) {
        try {
            log.info("Starting staff account deletion for: {}", staffEmail);
            
            // Find staff record
            Optional<Staff> staffOpt = staffRepository.findByEmail(staffEmail);
            if (staffOpt.isEmpty()) {
                return "Staff member not found";
            }
            
            Staff staff = staffOpt.get();
            String staffName = staff.getName();
            String staffRole = staff.getRole();
            
            // Get all appointments for this staff member
            List<Appointment> appointments = appointmentRepository.findByDoctorName(staffName);
            log.info("Found {} appointments for staff: {}", appointments.size(), staffEmail);
            
            // Get all vaccination history for this staff member
            List<VaccinationHistory> vaccinationHistory = vaccinationHistoryRepository.findByDoctorName(staffName);
            log.info("Found {} vaccination records for staff: {}", vaccinationHistory.size(), staffEmail);
            
            // Send cancellation emails to patients with appointments
            for (Appointment appointment : appointments) {
                try {
                    emailService.sendAppointmentCancellationEmail(
                        appointment.getPatientEmail(),
                        appointment.getPatientName(),
                        staffName,
                        appointment.getAppointmentDate(),
                        appointment.getTimeSlot(),
                        "Staff account deleted"
                    );
                    log.info("Sent cancellation email to patient: {}", appointment.getPatientEmail());
                } catch (Exception e) {
                    log.error("Failed to send cancellation email to patient: {}", appointment.getPatientEmail(), e);
                }
            }
            
            // Delete appointments
            appointmentRepository.deleteByDoctorName(staffName);
            log.info("Deleted {} appointments for staff: {}", appointments.size(), staffEmail);
            
            // Delete vaccination history
            vaccinationHistoryRepository.deleteByDoctorName(staffName);
            log.info("Deleted {} vaccination records for staff: {}", vaccinationHistory.size(), staffEmail);
            
            // Delete staff record
            staffRepository.deleteByEmail(staffEmail);
            log.info("Deleted staff record for: {}", staffEmail);
            
            // Delete user account
            userRepository.deleteByEmail(staffEmail);
            log.info("Deleted user account for: {}", staffEmail);
            
            log.info("Staff account deletion completed successfully for: {}", staffEmail);
            return "Staff account and all related data deleted successfully";
            
        } catch (Exception e) {
            log.error("Error deleting staff account: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to delete staff account: " + e.getMessage(), e);
        }
    }

    /**
     * Delete hospital account and all related data
     */
    @Transactional
    public String deleteHospitalAccount(String hospitalEmail) {
        try {
            log.info("Starting hospital account deletion for: {}", hospitalEmail);
            
            // Find hospital
            Optional<Hospital> hospitalOpt = hospitalRepository.findByEmail(hospitalEmail);
            if (hospitalOpt.isEmpty()) {
                return "Hospital not found";
            }
            
            Hospital hospital = hospitalOpt.get();
            String hospitalId = hospital.getId();
            String hospitalName = hospital.getInstitution();
            
            // Get all staff members of this hospital
            List<Staff> hospitalStaff = staffRepository.findByInstitutionTypeAndInstitutionId("Hospital", hospitalId);
            log.info("Found {} staff members for hospital: {}", hospitalStaff.size(), hospitalEmail);
            
            // Get all appointments for this hospital
            List<Appointment> appointments = appointmentRepository.findByInstitutionTypeAndInstitutionId("Hospital", hospitalId);
            log.info("Found {} appointments for hospital: {}", appointments.size(), hospitalEmail);
            
            // Send cancellation emails to patients with appointments
            for (Appointment appointment : appointments) {
                try {
                    emailService.sendAppointmentCancellationEmail(
                        appointment.getPatientEmail(),
                        appointment.getPatientName(),
                        hospitalName,
                        appointment.getAppointmentDate(),
                        appointment.getTimeSlot(),
                        "Hospital account deleted"
                    );
                    log.info("Sent cancellation email to patient: {}", appointment.getPatientEmail());
                } catch (Exception e) {
                    log.error("Failed to send cancellation email to patient: {}", appointment.getPatientEmail(), e);
                }
            }
            
            // Delete all staff members of this hospital
            for (Staff staff : hospitalStaff) {
                deleteStaffAccount(staff.getEmail());
            }
            
            // Delete appointments
            appointmentRepository.deleteByInstitutionTypeAndInstitutionId("Hospital", hospitalId);
            log.info("Deleted {} appointments for hospital: {}", appointments.size(), hospitalEmail);
            
            // Delete vaccination history
            vaccinationHistoryRepository.deleteByInstitutionTypeAndInstitutionId("Hospital", hospitalId);
            log.info("Deleted vaccination records for hospital: {}", hospitalEmail);
            
            // Delete hospital record
            hospitalRepository.deleteByEmail(hospitalEmail);
            log.info("Deleted hospital record for: {}", hospitalEmail);
            
            // Delete user account
            userRepository.deleteByEmail(hospitalEmail);
            log.info("Deleted user account for: {}", hospitalEmail);
            
            log.info("Hospital account deletion completed successfully for: {}", hospitalEmail);
            return "Hospital account and all related data deleted successfully";
            
        } catch (Exception e) {
            log.error("Error deleting hospital account: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to delete hospital account: " + e.getMessage(), e);
        }
    }

    /**
     * Delete clinic account and all related data
     */
    @Transactional
    public String deleteClinicAccount(String clinicEmail) {
        try {
            log.info("Starting clinic account deletion for: {}", clinicEmail);
            
            // Find clinic
            Optional<Clinic> clinicOpt = clinicRepository.findByEmail(clinicEmail);
            if (clinicOpt.isEmpty()) {
                return "Clinic not found";
            }
            
            Clinic clinic = clinicOpt.get();
            String clinicId = clinic.getId();
            String clinicName = clinic.getInstitution();
            
            // Get all staff members of this clinic
            List<Staff> clinicStaff = staffRepository.findByInstitutionTypeAndInstitutionId("Clinic", clinicId);
            log.info("Found {} staff members for clinic: {}", clinicStaff.size(), clinicEmail);
            
            // Get all appointments for this clinic
            List<Appointment> appointments = appointmentRepository.findByInstitutionTypeAndInstitutionId("Clinic", clinicId);
            log.info("Found {} appointments for clinic: {}", appointments.size(), clinicEmail);
            
            // Send cancellation emails to patients with appointments
            for (Appointment appointment : appointments) {
                try {
                    emailService.sendAppointmentCancellationEmail(
                        appointment.getPatientEmail(),
                        appointment.getPatientName(),
                        clinicName,
                        appointment.getAppointmentDate(),
                        appointment.getTimeSlot(),
                        "Clinic account deleted"
                    );
                    log.info("Sent cancellation email to patient: {}", appointment.getPatientEmail());
                } catch (Exception e) {
                    log.error("Failed to send cancellation email to patient: {}", appointment.getPatientEmail(), e);
                }
            }
            
            // Delete all staff members of this clinic
            for (Staff staff : clinicStaff) {
                deleteStaffAccount(staff.getEmail());
            }
            
            // Delete appointments
            appointmentRepository.deleteByInstitutionTypeAndInstitutionId("Clinic", clinicId);
            log.info("Deleted {} appointments for clinic: {}", appointments.size(), clinicEmail);
            
            // Delete vaccination history
            vaccinationHistoryRepository.deleteByInstitutionTypeAndInstitutionId("Clinic", clinicId);
            log.info("Deleted vaccination records for clinic: {}", clinicEmail);
            
            // Delete clinic record
            clinicRepository.deleteByEmail(clinicEmail);
            log.info("Deleted clinic record for: {}", clinicEmail);
            
            // Delete user account
            userRepository.deleteByEmail(clinicEmail);
            log.info("Deleted user account for: {}", clinicEmail);
            
            log.info("Clinic account deletion completed successfully for: {}", clinicEmail);
            return "Clinic account and all related data deleted successfully";
            
        } catch (Exception e) {
            log.error("Error deleting clinic account: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to delete clinic account: " + e.getMessage(), e);
        }
    }

    /**
     * Remove doctor from hospital/clinic and handle related appointments
     */
    @Transactional
    public String removeDoctorFromInstitution(String doctorEmail, String institutionType, String institutionId) {
        try {
            log.info("Removing doctor {} from {}: {}", doctorEmail, institutionType, institutionId);
            
            // Find staff record
            Optional<Staff> staffOpt = staffRepository.findByEmailAndInstitutionTypeAndInstitutionId(
                doctorEmail, institutionType, institutionId);
            
            if (staffOpt.isEmpty()) {
                return "Doctor not found in this institution";
            }
            
            Staff staff = staffOpt.get();
            String doctorName = staff.getName();
            
            // Get all appointments for this doctor at this institution
            List<Appointment> appointments = appointmentRepository.findByDoctorNameAndInstitutionTypeAndInstitutionId(
                doctorName, institutionType, institutionId);
            log.info("Found {} appointments for doctor {} at {}: {}", 
                appointments.size(), doctorName, institutionType, institutionId);
            
            // Send cancellation emails to patients with appointments
            for (Appointment appointment : appointments) {
                try {
                    emailService.sendAppointmentCancellationEmail(
                        appointment.getPatientEmail(),
                        appointment.getPatientName(),
                        doctorName,
                        appointment.getAppointmentDate(),
                        appointment.getTimeSlot(),
                        "Doctor removed from " + institutionType
                    );
                    log.info("Sent cancellation email to patient: {}", appointment.getPatientEmail());
                } catch (Exception e) {
                    log.error("Failed to send cancellation email to patient: {}", appointment.getPatientEmail(), e);
                }
            }
            
            // Delete appointments for this doctor at this institution
            appointmentRepository.deleteByDoctorNameAndInstitutionTypeAndInstitutionId(
                doctorName, institutionType, institutionId);
            log.info("Deleted {} appointments for doctor {} at {}: {}", 
                appointments.size(), doctorName, institutionType, institutionId);
            
            // Delete vaccination history for this doctor at this institution
            vaccinationHistoryRepository.deleteByDoctorNameAndInstitutionTypeAndInstitutionId(
                doctorName, institutionType, institutionId);
            log.info("Deleted vaccination records for doctor {} at {}: {}", 
                doctorName, institutionType, institutionId);
            
            // Delete staff record for this institution
            staffRepository.deleteByEmailAndInstitutionTypeAndInstitutionId(
                doctorEmail, institutionType, institutionId);
            log.info("Deleted staff record for doctor {} at {}: {}", 
                doctorEmail, institutionType, institutionId);
            
            log.info("Doctor removal completed successfully for: {} from {}: {}", 
                doctorEmail, institutionType, institutionId);
            return "Doctor removed successfully and all related appointments cancelled";
            
        } catch (Exception e) {
            log.error("Error removing doctor from institution: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to remove doctor from institution: " + e.getMessage(), e);
        }
    }

    /**
     * Generic account deletion based on user role
     */
    @Transactional
    public String deleteAccountByRole(String userEmail, String userRole) {
        try {
            log.info("Deleting account for user: {} with role: {}", userEmail, userRole);
            
            switch (userRole.toLowerCase()) {
                case "patient":
                    return deletePatientAccount(userEmail);
                case "doctor":
                case "nurse":
                    return deleteStaffAccount(userEmail);
                case "hospital":
                    return deleteHospitalAccount(userEmail);
                case "clinic":
                    return deleteClinicAccount(userEmail);
                default:
                    return "Unknown user role: " + userRole;
            }
        } catch (Exception e) {
            log.error("Error deleting account by role: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to delete account: " + e.getMessage(), e);
        }
    }
}
