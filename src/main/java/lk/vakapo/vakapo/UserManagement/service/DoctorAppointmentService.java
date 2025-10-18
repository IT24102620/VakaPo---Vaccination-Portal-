package lk.vakapo.vakapo.UserManagement.service;

import lk.vakapo.vakapo.UserManagement.model.Appointment;
import lk.vakapo.vakapo.UserManagement.model.Clinic;
import lk.vakapo.vakapo.UserManagement.model.Hospital;
import lk.vakapo.vakapo.UserManagement.model.Staff;
import lk.vakapo.vakapo.UserManagement.repository.AppointmentRepository;
import lk.vakapo.vakapo.UserManagement.repository.ClinicRepository;
import lk.vakapo.vakapo.UserManagement.repository.HospitalRepository;
import lk.vakapo.vakapo.UserManagement.repository.StaffRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class DoctorAppointmentService {

    private final StaffRepository staffRepository;
    private final HospitalRepository hospitalRepository;
    private final ClinicRepository clinicRepository;
    private final AppointmentRepository appointmentRepository;

    /**
     * Get all institutions (hospitals and clinics) that have invited the doctor
     */
    public List<Map<String, Object>> getInstitutionsForDoctor(String doctorEmail) {
        try {
            log.info("Fetching institutions for doctor: {}", doctorEmail);
            
            // Get all accepted staff invitations for this doctor
            List<Staff> doctorInvitations = staffRepository.findAcceptedDoctorInvitations(doctorEmail);
            log.info("Found {} staff invitations for doctor: {}", doctorInvitations.size(), doctorEmail);
            
            List<Map<String, Object>> institutions = new ArrayList<>();
            
            for (Staff invitation : doctorInvitations) {
                log.info("Processing invitation: institutionType={}, institutionId={}", 
                        invitation.getInstitutionType(), invitation.getInstitutionId());
                
                if ("Hospital".equals(invitation.getInstitutionType())) {
                    Optional<Hospital> hospitalOpt = hospitalRepository.findById(invitation.getInstitutionId());
                    if (hospitalOpt.isPresent()) {
                        Hospital hospital = hospitalOpt.get();
                        log.info("Found hospital: id={}, username={}", hospital.getId(), hospital.getUsername());
                        
                        Map<String, Object> institution = new HashMap<>();
                        institution.put("id", hospital.getId());
                        institution.put("username", hospital.getUsername());
                        institution.put("type", "Hospital");
                        institution.put("email", hospital.getEmail());
                        institutions.add(institution);
                    } else {
                        log.warn("Hospital not found with ID: {}", invitation.getInstitutionId());
                    }
                } else if ("Clinic".equals(invitation.getInstitutionType())) {
                    Optional<Clinic> clinicOpt = clinicRepository.findById(invitation.getInstitutionId());
                    if (clinicOpt.isPresent()) {
                        Clinic clinic = clinicOpt.get();
                        log.info("Found clinic: id={}, username={}", clinic.getId(), clinic.getUsername());
                        
                        Map<String, Object> institution = new HashMap<>();
                        institution.put("id", clinic.getId());
                        institution.put("username", clinic.getUsername());
                        institution.put("type", "Clinic");
                        institution.put("email", clinic.getEmail());
                        institutions.add(institution);
                    } else {
                        log.warn("Clinic not found with ID: {}", invitation.getInstitutionId());
                    }
                }
            }
            
            log.info("Found {} institutions for doctor: {}", institutions.size(), doctorEmail);
            return institutions;
            
        } catch (Exception e) {
            log.error("Error fetching institutions for doctor: {}. Error: {}", doctorEmail, e.getMessage(), e);
            return new ArrayList<>();
        }
    }

    /**
     * Get all hospitals that have invited the doctor (legacy method for backward compatibility)
     */
    public List<Hospital> getHospitalsForDoctor(String doctorEmail) {
        try {
            log.info("Fetching hospitals for doctor: {}", doctorEmail);
            
            // Get all accepted staff invitations for this doctor
            List<Staff> doctorInvitations = staffRepository.findAcceptedDoctorInvitations(doctorEmail);
            log.info("Found {} staff invitations for doctor: {}", doctorInvitations.size(), doctorEmail);
            
            List<Hospital> hospitals = new ArrayList<>();
            
            for (Staff invitation : doctorInvitations) {
                log.info("Processing invitation: institutionType={}, institutionId={}", 
                        invitation.getInstitutionType(), invitation.getInstitutionId());
                
                if ("Hospital".equals(invitation.getInstitutionType())) {
                    Optional<Hospital> hospitalOpt = hospitalRepository.findById(invitation.getInstitutionId());
                    if (hospitalOpt.isPresent()) {
                        Hospital hospital = hospitalOpt.get();
                        log.info("Found hospital: id={}, username={}", hospital.getId(), hospital.getUsername());
                        hospitals.add(hospital);
                    } else {
                        log.warn("Hospital not found with ID: {}", invitation.getInstitutionId());
                    }
                }
            }
            
            log.info("Found {} hospitals for doctor: {}", hospitals.size(), doctorEmail);
            return hospitals;
            
        } catch (Exception e) {
            log.error("Error fetching hospitals for doctor: {}. Error: {}", doctorEmail, e.getMessage(), e);
            return new ArrayList<>();
        }
    }

    /**
     * Get appointments for a specific doctor and institution (hospital or clinic)
     */
    public List<Appointment> getAppointmentsForDoctorAndInstitution(String doctorName, String institutionId, String institutionType) {
        try {
            log.info("Fetching appointments for doctor: '{}' at institution: '{}' (Type: {})", doctorName, institutionId, institutionType);
            
            // Run repository tests first
            testRepositoryMethods(doctorName, institutionId, institutionType);
            
            // First, let's check what appointments exist for this institution
            List<Appointment> allInstitutionAppointments = appointmentRepository.findByInstitutionIdAndType(institutionId, institutionType);
            log.info("Found {} total appointments for institution: {} (Type: {})", allInstitutionAppointments.size(), institutionId, institutionType);
            
            // Log all appointments for debugging
            for (Appointment apt : allInstitutionAppointments) {
                log.info("Institution appointment - Doctor: '{}', Patient: '{}', Date: '{}', Status: '{}'", 
                        apt.getDoctorName(), apt.getPatientName(), apt.getAppointmentDate(), apt.getStatus());
            }
            
            // Try multiple doctor name formats to find appointments
            List<Appointment> appointments = new ArrayList<>();
            
            // Try 1: Exact doctor name match
            appointments = appointmentRepository.findByDoctorNameAndInstitution(doctorName, institutionId, institutionType);
            log.info("Found {} appointments with exact doctor name: '{}'", appointments.size(), doctorName);
            
            // Try 2: If no appointments found, try without "Dr. " prefix
            if (appointments.isEmpty() && doctorName.startsWith("Dr. ")) {
                String doctorNameWithoutPrefix = doctorName.substring(4); // Remove "Dr. " prefix
                appointments = appointmentRepository.findByDoctorNameAndInstitution(doctorNameWithoutPrefix, institutionId, institutionType);
                log.info("Found {} appointments with doctor name without prefix: '{}'", appointments.size(), doctorNameWithoutPrefix);
            }
            
            // Try 3: If still no appointments, try with just the ID number
            if (appointments.isEmpty() && doctorName.contains(" ")) {
                String[] parts = doctorName.split(" ");
                if (parts.length > 1) {
                    String doctorId = parts[parts.length - 1]; // Get the last part (ID)
                    appointments = appointmentRepository.findByDoctorNameAndInstitution(doctorId, institutionId, institutionType);
                    log.info("Found {} appointments with doctor ID only: '{}'", appointments.size(), doctorId);
                }
            }
            
            // Try 4: Search by partial name match (case insensitive)
            if (appointments.isEmpty()) {
                appointments = appointmentRepository.findByInstitutionAndDoctorNameContaining(doctorName, institutionId, institutionType);
                log.info("Found {} appointments with partial doctor name match: '{}'", appointments.size(), doctorName);
            }
            
            // Try 5: If still no appointments, try with just the ID number using partial match
            if (appointments.isEmpty() && doctorName.contains(" ")) {
                String[] parts = doctorName.split(" ");
                if (parts.length > 1) {
                    String doctorId = parts[parts.length - 1]; // Get the last part (ID)
                    appointments = appointmentRepository.findByInstitutionAndDoctorNameContaining(doctorId, institutionId, institutionType);
                    log.info("Found {} appointments with doctor ID partial match: '{}'", appointments.size(), doctorId);
                }
            }
            
            log.info("Final result: Found {} appointments for doctor: '{}' at institution: '{}' (Type: {})", 
                    appointments.size(), doctorName, institutionId, institutionType);
            
            // Log the specific appointments found
            for (Appointment apt : appointments) {
                log.info("Doctor appointment - Patient: '{}', Date: '{}', Time: '{}', Vaccine: '{}', Status: '{}'", 
                        apt.getPatientName(), apt.getAppointmentDate(), apt.getTimeSlot(), apt.getVaccineName(), apt.getStatus());
            }
            
            return appointments;
            
        } catch (Exception e) {
            log.error("Error fetching appointments for doctor: {} at institution: {} (Type: {}). Error: {}", 
                     doctorName, institutionId, institutionType, e.getMessage(), e);
            return new ArrayList<>();
        }
    }

    /**
     * Get appointments for a specific doctor and hospital (backward compatibility)
     */
    public List<Appointment> getAppointmentsForDoctorAndHospital(String doctorName, String hospitalId) {
        return getAppointmentsForDoctorAndInstitution(doctorName, hospitalId, "Hospital");
    }

    /**
     * Get appointments for a specific doctor and hospital with status filter
     */
    public List<Appointment> getAppointmentsForDoctorAndHospitalByStatus(String doctorName, String hospitalId, String status) {
        try {
            log.info("Fetching {} appointments for doctor: {} at hospital: {}", status, doctorName, hospitalId);
            
            List<Appointment> appointments = appointmentRepository.findByDoctorNameAndInstitutionAndStatus(
                doctorName, hospitalId, "Hospital", status);
            
            log.info("Found {} {} appointments for doctor: {} at hospital: {}", 
                    appointments.size(), status, doctorName, hospitalId);
            return appointments;
            
        } catch (Exception e) {
            log.error("Error fetching {} appointments for doctor: {} at hospital: {}. Error: {}", 
                     status, doctorName, hospitalId, e.getMessage(), e);
            return new ArrayList<>();
        }
    }

    /**
     * Update appointment status (confirm/cancel)
     */
    public boolean updateAppointmentStatus(Long appointmentId, String newStatus, String cancelledBy) {
        try {
            log.info("Updating appointment {} status to: {}", appointmentId, newStatus);
            
            Optional<Appointment> appointmentOpt = appointmentRepository.findById(appointmentId);
            if (appointmentOpt.isPresent()) {
                Appointment appointment = appointmentOpt.get();
                appointment.setStatus(newStatus);
                if ("cancelled".equals(newStatus)) {
                    appointment.setCancelledBy(cancelledBy);
                }
                appointmentRepository.save(appointment);
                
                log.info("Appointment {} status updated successfully to: {}", appointmentId, newStatus);
                return true;
            } else {
                log.error("Appointment not found with ID: {}", appointmentId);
                return false;
            }
            
        } catch (Exception e) {
            log.error("Error updating appointment {} status. Error: {}", appointmentId, e.getMessage(), e);
            return false;
        }
    }

    /**
     * Debug method to get all appointments in the database
     */
    public List<Appointment> getAllAppointments() {
        try {
            log.info("Fetching all appointments from database");
            List<Appointment> allAppointments = appointmentRepository.findAll();
            log.info("Found {} total appointments in database", allAppointments.size());
            
            for (Appointment apt : allAppointments) {
                log.info("Appointment - ID: {}, Doctor: '{}', Patient: '{}', Hospital: '{}', Date: '{}', Status: '{}'", 
                        apt.getId(), apt.getDoctorName(), apt.getPatientName(), apt.getInstitutionId(), apt.getAppointmentDate(), apt.getStatus());
            }
            
            return allAppointments;
        } catch (Exception e) {
            log.error("Error fetching all appointments: {}", e.getMessage(), e);
            return new ArrayList<>();
        }
    }

    /**
     * Debug method to test repository methods
     */
    public void testRepositoryMethods(String doctorName, String institutionId, String institutionType) {
        try {
            log.info("Testing repository methods with doctorName: '{}', institutionId: '{}', institutionType: '{}'", doctorName, institutionId, institutionType);
            
            // Test 1: Find all appointments
            List<Appointment> allAppointments = appointmentRepository.findAll();
            log.info("Repository test - Total appointments: {}", allAppointments.size());
            
            // Test 2: Find by institution
            List<Appointment> institutionAppointments = appointmentRepository.findByInstitutionIdAndType(institutionId, institutionType);
            log.info("Repository test - Institution appointments: {}", institutionAppointments.size());
            
            // Test 3: Find by doctor name
            List<Appointment> doctorAppointments = appointmentRepository.findByDoctorNameOrderByAppointmentDateAsc(doctorName);
            log.info("Repository test - Doctor appointments: {}", doctorAppointments.size());
            
            // Test 4: Find by doctor and institution
            List<Appointment> doctorInstitutionAppointments = appointmentRepository.findByDoctorNameAndInstitution(doctorName, institutionId, institutionType);
            log.info("Repository test - Doctor+Institution appointments: {}", doctorInstitutionAppointments.size());
            
        } catch (Exception e) {
            log.error("Error testing repository methods: {}", e.getMessage(), e);
        }
    }
}
