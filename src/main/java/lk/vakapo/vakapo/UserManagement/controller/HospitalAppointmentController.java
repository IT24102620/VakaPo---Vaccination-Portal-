package lk.vakapo.vakapo.UserManagement.controller;

import lk.vakapo.vakapo.UserManagement.model.Appointment;
import lk.vakapo.vakapo.UserManagement.model.Hospital;
import lk.vakapo.vakapo.UserManagement.model.Staff;
import lk.vakapo.vakapo.UserManagement.model.VaccinationSchedule;
import lk.vakapo.vakapo.UserManagement.repository.HospitalRepository;
import lk.vakapo.vakapo.UserManagement.service.AppointmentService;
import lk.vakapo.vakapo.UserManagement.service.StaffService;
import lk.vakapo.vakapo.UserManagement.service.VaccinationScheduleService;
import lk.vakapo.vakapo.UserManagement.service.VaccineService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Controller
@RequestMapping("/hospital")
@RequiredArgsConstructor
@Slf4j
public class HospitalAppointmentController {

    private final HospitalRepository hospitalRepository;
    private final VaccinationScheduleService vaccinationScheduleService;
    private final StaffService staffService;
    private final AppointmentService appointmentService;
    private final VaccineService vaccineService;

    /**
     * Display hospital appointment page
     */
    @GetMapping("/appointments")
    public String hospitalAppointments(Model model) {
        try {
            // Get current authenticated user
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            String userEmail = authentication.getName();
            log.info("Loading appointment page for hospital: {}", userEmail);
            
            // Find hospital by email
            Optional<Hospital> hospitalOpt = hospitalRepository.findByEmail(userEmail);
            
            if (hospitalOpt.isPresent()) {
                Hospital hospital = hospitalOpt.get();
                
                // Get vaccination schedules for this hospital
                List<VaccinationSchedule> schedules = vaccinationScheduleService.getSchedulesByInstitution(hospital.getId(), "Hospital");
                
                // Get appointments for this hospital
                List<Appointment> appointments = appointmentService.getAppointmentsByInstitution(hospital.getId().toString(), "Hospital");
                
                // Add hospital data to model
                model.addAttribute("hospitalName", hospital.getUsername() != null ? hospital.getUsername() : "Hospital");
                model.addAttribute("hospitalId", hospital.getId());
                model.addAttribute("institutionType", "Hospital");
                model.addAttribute("schedules", schedules);
                model.addAttribute("scheduleCount", schedules.size());
                model.addAttribute("appointments", appointments);
                model.addAttribute("appointmentCount", appointments.size());
                
                log.info("Hospital appointment page loaded successfully for: {} with {} schedules", hospital.getUsername(), schedules.size());
                return "hospital/appointmentPage/HospitalAppointmentPage";
            } else {
                log.error("Hospital not found for email: {}", userEmail);
                return "error/404";
            }
        } catch (Exception e) {
            log.error("Error loading hospital appointment page: {}", e.getMessage(), e);
            return "error/500";
        }
    }

    /**
     * Get doctors for the current hospital
     */
    @GetMapping("/doctors")
    @ResponseBody
    public ResponseEntity<?> getDoctors() {
        try {
            log.info("Fetching doctors for current hospital...");
            
            // Get current authenticated user
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            String userEmail = authentication.getName();
            log.info("Current user email: {}", userEmail);
            
            // Find hospital by email
            Optional<Hospital> hospitalOpt = hospitalRepository.findByEmail(userEmail);
            if (hospitalOpt.isEmpty()) {
                log.error("Hospital not found for email: {}", userEmail);
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body("{\"success\": false, \"error\": \"Hospital not found\"}");
            }
            
            Hospital hospital = hospitalOpt.get();
            log.info("Hospital found: {} with ID: {}", hospital.getUsername(), hospital.getId());
            
            // Get doctors for this hospital
            List<Staff> doctors = staffService.getStaffByRole(hospital.getId().toString(), "Hospital", "Doctor");
            log.info("Found {} doctors for hospital: {}", doctors.size(), hospital.getUsername());
            
            // Convert to simple list for frontend
            List<Map<String, String>> doctorList = doctors.stream()
                    .map(doctor -> {
                        Map<String, String> doctorInfo = new HashMap<>();
                        doctorInfo.put("id", doctor.getId().toString());
                        doctorInfo.put("name", doctor.getName());
                        doctorInfo.put("email", doctor.getEmail());
                        return doctorInfo;
                    })
                    .collect(java.util.stream.Collectors.toList());
            
            return ResponseEntity.ok().body("{\"success\": true, \"doctors\": " + 
                    new com.fasterxml.jackson.databind.ObjectMapper().writeValueAsString(doctorList) + "}");
            
        } catch (Exception e) {
            log.error("Error fetching doctors: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("{\"success\": false, \"error\": \"Failed to fetch doctors: " + e.getMessage() + "\"}");
        }
    }

    /**
     * Get vaccines for the dropdown
     */
    @GetMapping("/vaccines")
    @ResponseBody
    public ResponseEntity<?> getVaccines() {
        try {
            log.info("Fetching vaccines for dropdown...");
            
            // Get all active vaccines
            List<lk.vakapo.vakapo.UserManagement.model.Vaccine> vaccines = vaccineService.getActiveVaccines();
            log.info("Found {} active vaccines", vaccines.size());
            
            // Convert to simple list for frontend
            List<Map<String, String>> vaccineList = vaccines.stream()
                    .map(vaccine -> {
                        Map<String, String> vaccineInfo = new HashMap<>();
                        vaccineInfo.put("id", vaccine.getId().toString());
                        vaccineInfo.put("name", vaccine.getVaccineName());
                        return vaccineInfo;
                    })
                    .collect(java.util.stream.Collectors.toList());
            
            return ResponseEntity.ok().body("{\"success\": true, \"vaccines\": " + 
                    new com.fasterxml.jackson.databind.ObjectMapper().writeValueAsString(vaccineList) + "}");
            
        } catch (Exception e) {
            log.error("Error fetching vaccines: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("{\"success\": false, \"error\": \"Failed to fetch vaccines: " + e.getMessage() + "\"}");
        }
    }

    /**
     * Debug endpoint to test vaccination schedule creation
     */
    @GetMapping("/debug/test-schedule")
    @ResponseBody
    public ResponseEntity<?> testSchedule() {
        try {
            log.info("Testing vaccination schedule creation...");
            
            // Get current authenticated user
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            String userEmail = authentication.getName();
            log.info("Current user: {}", userEmail);
            
            // Find hospital by email
            Optional<Hospital> hospitalOpt = hospitalRepository.findByEmail(userEmail);
            if (hospitalOpt.isEmpty()) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body("{\"success\": false, \"error\": \"Hospital not found\"}");
            }
            
            Hospital hospital = hospitalOpt.get();
            log.info("Hospital found: {} with ID: {}", hospital.getUsername(), hospital.getId());
            
            return ResponseEntity.ok().body("{\"success\": true, \"message\": \"Test successful\", \"hospitalId\": \"" + hospital.getId() + "\", \"hospitalName\": \"" + hospital.getUsername() + "\"}");
            
        } catch (Exception e) {
            log.error("Error in test endpoint: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("{\"success\": false, \"error\": \"Test failed: " + e.getMessage() + "\"}");
        }
    }

    /**
     * Add new vaccination schedule
     */
    @PostMapping("/add-schedule")
    @ResponseBody
    public ResponseEntity<?> addSchedule(@RequestBody VaccinationScheduleRequest request) {
        try {
            log.info("Adding new vaccination schedule for doctor: {}", request.getDoctorName());
            log.info("Request data: doctorName={}, vaccineName={}, timeFrom={}, timeTo={}, days={}", 
                    request.getDoctorName(), request.getVaccineName(), request.getTimeFrom(), 
                    request.getTimeTo(), request.getDays());
            
            // Get current authenticated user
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            String userEmail = authentication.getName();
            log.info("Current user email: {}", userEmail);
            
            // Find hospital by email
            Optional<Hospital> hospitalOpt = hospitalRepository.findByEmail(userEmail);
            if (hospitalOpt.isEmpty()) {
                log.error("Hospital not found for email: {}", userEmail);
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body("{\"success\": false, \"error\": \"Hospital not found\"}");
            }
            
            Hospital hospital = hospitalOpt.get();
            log.info("Hospital found: {} with ID: {}", hospital.getUsername(), hospital.getId());
            
            // Create vaccination schedule
            log.info("Creating VaccinationSchedule object...");
            VaccinationSchedule schedule = new VaccinationSchedule();
            schedule.setDoctorName(request.getDoctorName());
            schedule.setVaccineName(request.getVaccineName());
            schedule.setInstitutionType("Hospital");
            schedule.setInstitutionId(hospital.getId().toString());
            schedule.setTimeFrom(request.getTimeFrom());
            schedule.setTimeTo(request.getTimeTo());
            schedule.setDays(request.getDays());
            schedule.setStatus("scheduled");
            
            log.info("VaccinationSchedule object created, calling service...");
            VaccinationSchedule savedSchedule = vaccinationScheduleService.createSchedule(schedule);
            
            log.info("Vaccination schedule created successfully with ID: {}", savedSchedule.getId());
            return ResponseEntity.ok().body("{\"success\": true, \"message\": \"Vaccination schedule added successfully\", \"scheduleId\": " + savedSchedule.getId() + "}");
            
        } catch (Exception e) {
            log.error("Error adding vaccination schedule: {}", e.getMessage(), e);
            log.error("Stack trace: ", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("{\"success\": false, \"error\": \"Failed to add vaccination schedule: " + e.getMessage() + "\"}");
        }
    }

    /**
     * Get appointments for the current hospital
     */
    @GetMapping("/appointments-data")
    @ResponseBody
    public ResponseEntity<?> getAppointments() {
        try {
            log.info("Fetching appointments for current hospital...");
            
            // Get current authenticated user
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            String userEmail = authentication.getName();
            log.info("Current user email: {}", userEmail);
            
            // Find hospital by email
            Optional<Hospital> hospitalOpt = hospitalRepository.findByEmail(userEmail);
            if (hospitalOpt.isEmpty()) {
                log.error("Hospital not found for email: {}", userEmail);
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body("{\"success\": false, \"error\": \"Hospital not found\"}");
            }
            
            Hospital hospital = hospitalOpt.get();
            log.info("Hospital found: {} with ID: {}", hospital.getUsername(), hospital.getId());
            
            // Get appointments for this hospital
            List<Appointment> appointments = appointmentService.getAppointmentsByInstitution(hospital.getId().toString(), "Hospital");
            log.info("Found {} appointments for hospital: {}", appointments.size(), hospital.getUsername());
            
            // Convert to simple list for frontend
            List<Map<String, Object>> appointmentList = appointments.stream()
                    .map(appointment -> {
                        Map<String, Object> appointmentInfo = new HashMap<>();
                        appointmentInfo.put("id", appointment.getId());
                        appointmentInfo.put("patientName", appointment.getPatientName());
                        appointmentInfo.put("patientEmail", appointment.getPatientEmail());
                        appointmentInfo.put("appointmentDate", appointment.getAppointmentDate().toString());
                        appointmentInfo.put("timeSlot", appointment.getTimeSlot());
                        appointmentInfo.put("vaccineName", appointment.getVaccineName());
                        appointmentInfo.put("doctorName", appointment.getDoctorName());
                        appointmentInfo.put("status", appointment.getStatus());
                        appointmentInfo.put("cancelledBy", appointment.getCancelledBy());
                        appointmentInfo.put("notes", appointment.getNotes());
                        return appointmentInfo;
                    })
                    .collect(java.util.stream.Collectors.toList());
            
            return ResponseEntity.ok().body("{\"success\": true, \"appointments\": " + 
                    new com.fasterxml.jackson.databind.ObjectMapper().writeValueAsString(appointmentList) + "}");
            
        } catch (Exception e) {
            log.error("Error fetching appointments: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("{\"success\": false, \"error\": \"Failed to fetch appointments: " + e.getMessage() + "\"}");
        }
    }

    /**
     * Cancel an appointment
     */
    @DeleteMapping("/cancel-appointment/{appointmentId}")
    @ResponseBody
    public ResponseEntity<?> cancelAppointment(@PathVariable Long appointmentId) {
        try {
            log.info("Cancelling appointment with ID: {}", appointmentId);
            
            // Get current authenticated user
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            String userEmail = authentication.getName();
            
            // Find hospital by email
            Optional<Hospital> hospitalOpt = hospitalRepository.findByEmail(userEmail);
            if (hospitalOpt.isEmpty()) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body("{\"success\": false, \"error\": \"Hospital not found\"}");
            }
            
            Hospital hospital = hospitalOpt.get();
            
            // Get the appointment and verify it belongs to this hospital
            Optional<Appointment> appointmentOpt = appointmentService.getAppointmentById(appointmentId);
            if (appointmentOpt.isEmpty()) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body("{\"success\": false, \"error\": \"Appointment not found\"}");
            }
            
            Appointment appointment = appointmentOpt.get();
            if (!appointment.getInstitutionId().equals(hospital.getId().toString()) || !appointment.getInstitutionType().equals("Hospital")) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                        .body("{\"success\": false, \"error\": \"Appointment does not belong to this hospital\"}");
            }
            
            // Cancel the appointment
            boolean cancelled = appointmentService.cancelAppointmentByHospital(appointmentId, hospital.getId().toString());
            
            if (cancelled) {
                log.info("Appointment cancelled successfully: {}", appointmentId);
                return ResponseEntity.ok().body("{\"success\": true, \"message\": \"Appointment cancelled successfully\"}");
            } else {
                log.warn("Failed to cancel appointment: {}", appointmentId);
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body("{\"success\": false, \"error\": \"Appointment not found or already cancelled\"}");
            }
            
        } catch (Exception e) {
            log.error("Error cancelling appointment: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("{\"success\": false, \"error\": \"Failed to cancel appointment: " + e.getMessage() + "\"}");
        }
    }

    /**
     * Reverse a cancelled appointment
     */
    @PostMapping("/reverse-appointment/{appointmentId}")
    @ResponseBody
    public ResponseEntity<?> reverseAppointment(@PathVariable Long appointmentId) {
        try {
            log.info("Reversing appointment with ID: {}", appointmentId);
            
            // Get current authenticated user
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            String userEmail = authentication.getName();
            
            // Find hospital by email
            Optional<Hospital> hospitalOpt = hospitalRepository.findByEmail(userEmail);
            if (hospitalOpt.isEmpty()) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body("{\"success\": false, \"error\": \"Hospital not found\"}");
            }
            
            Hospital hospital = hospitalOpt.get();
            
            // Get the appointment and verify it belongs to this hospital
            Optional<Appointment> appointmentOpt = appointmentService.getAppointmentById(appointmentId);
            if (appointmentOpt.isEmpty()) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body("{\"success\": false, \"error\": \"Appointment not found\"}");
            }
            
            Appointment appointment = appointmentOpt.get();
            if (!appointment.getInstitutionId().equals(hospital.getId().toString()) || !appointment.getInstitutionType().equals("Hospital")) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                        .body("{\"success\": false, \"error\": \"Appointment does not belong to this hospital\"}");
            }
            
            if (!"cancelled".equals(appointment.getStatus())) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body("{\"success\": false, \"error\": \"Only cancelled appointments can be reversed\"}");
            }

            if (!"hospital".equals(appointment.getCancelledBy())) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body("{\"success\": false, \"error\": \"Only hospital-cancelled appointments can be reversed\"}");
            }
            
            // Reverse the appointment
            boolean reversed = appointmentService.reverseAppointment(appointmentId, hospital.getId().toString());
            
            if (reversed) {
                log.info("Appointment reversed successfully: {}", appointmentId);
                return ResponseEntity.ok().body("{\"success\": true, \"message\": \"Appointment reversed and rescheduled successfully\"}");
            } else {
                log.warn("Failed to reverse appointment: {}", appointmentId);
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body("{\"success\": false, \"error\": \"Unable to reverse appointment. No available time slots found.\"}");
            }
            
        } catch (Exception e) {
            log.error("Error reversing appointment: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("{\"success\": false, \"error\": \"Failed to reverse appointment: " + e.getMessage() + "\"}");
        }
    }

    /**
     * Cancel vaccination schedule
     */
    @DeleteMapping("/cancel-schedule/{scheduleId}")
    @ResponseBody
    public ResponseEntity<?> cancelSchedule(@PathVariable Long scheduleId) {
        try {
            log.info("Cancelling vaccination schedule with ID: {}", scheduleId);
            
            // Get current authenticated user
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            String userEmail = authentication.getName();
            
            // Find hospital by email
            Optional<Hospital> hospitalOpt = hospitalRepository.findByEmail(userEmail);
            if (hospitalOpt.isEmpty()) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body("{\"success\": false, \"error\": \"Hospital not found\"}");
            }
            
            Hospital hospital = hospitalOpt.get();
            
            // Verify schedule belongs to this hospital
            Optional<VaccinationSchedule> scheduleOpt = vaccinationScheduleService.getScheduleById(scheduleId);
            if (scheduleOpt.isEmpty()) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body("{\"success\": false, \"error\": \"Schedule not found\"}");
            }
            
            VaccinationSchedule schedule = scheduleOpt.get();
            if (!schedule.getInstitutionId().equals(hospital.getId()) || !schedule.getInstitutionType().equals("Hospital")) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                        .body("{\"success\": false, \"error\": \"Schedule does not belong to this hospital\"}");
            }
            
            // Cancel the schedule
            boolean cancelled = vaccinationScheduleService.cancelSchedule(scheduleId);
            
            if (cancelled) {
                log.info("Vaccination schedule cancelled successfully: {}", scheduleId);
                return ResponseEntity.ok().body("{\"success\": true, \"message\": \"Vaccination schedule cancelled successfully\"}");
            } else {
                log.warn("Failed to cancel vaccination schedule: {}", scheduleId);
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body("{\"success\": false, \"error\": \"Schedule not found or already cancelled\"}");
            }
            
        } catch (Exception e) {
            log.error("Error cancelling vaccination schedule: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("{\"success\": false, \"error\": \"Failed to cancel vaccination schedule: " + e.getMessage() + "\"}");
        }
    }

    /**
     * Request DTO for vaccination schedule
     */
    public static class VaccinationScheduleRequest {
        private String doctorName;
        private String vaccineName;
        private String timeFrom;
        private String timeTo;
        private String days;

        // Getters and setters
        public String getDoctorName() { return doctorName; }
        public void setDoctorName(String doctorName) { this.doctorName = doctorName; }
        
        public String getVaccineName() { return vaccineName; }
        public void setVaccineName(String vaccineName) { this.vaccineName = vaccineName; }
        
        public String getTimeFrom() { return timeFrom; }
        public void setTimeFrom(String timeFrom) { this.timeFrom = timeFrom; }
        
        public String getTimeTo() { return timeTo; }
        public void setTimeTo(String timeTo) { this.timeTo = timeTo; }
        
        public String getDays() { return days; }
        public void setDays(String days) { this.days = days; }
    }
}
