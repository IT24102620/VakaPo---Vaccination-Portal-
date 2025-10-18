package lk.vakapo.vakapo.UserManagement.controller;

import lk.vakapo.vakapo.UserManagement.model.Appointment;
import lk.vakapo.vakapo.UserManagement.model.Patient;
import lk.vakapo.vakapo.UserManagement.model.UserAccount;
import lk.vakapo.vakapo.UserManagement.model.VaccinationHistory;
import lk.vakapo.vakapo.UserManagement.model.Feedback;
import lk.vakapo.vakapo.UserManagement.repository.PatientRepository;
import lk.vakapo.vakapo.UserManagement.repository.UserRepository;
import lk.vakapo.vakapo.UserManagement.repository.VaccinationScheduleRepository;
import lk.vakapo.vakapo.UserManagement.repository.VaccinationHistoryRepository;
import lk.vakapo.vakapo.UserManagement.repository.FeedbackRepository;
import lk.vakapo.vakapo.UserManagement.service.AppointmentService;
import lk.vakapo.vakapo.UserManagement.service.FeedbackService;
import lk.vakapo.vakapo.PDFManagement.PDFGenerationService;
import lk.vakapo.vakapo.MailManagement.EmailService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;

import jakarta.servlet.http.HttpServletRequest;
import java.time.LocalDate;
import java.time.Period;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Controller
@RequestMapping("/patient")
@RequiredArgsConstructor
@Slf4j
public class PatientProfileController {

    private final PatientRepository patientRepository;
    private final UserRepository userRepository;
    private final AppointmentService appointmentService;
    private final VaccinationScheduleRepository vaccinationScheduleRepository;
    private final VaccinationHistoryRepository vaccinationHistoryRepository;
    private final PDFGenerationService pdfGenerationService;
    private final FeedbackService feedbackService;
    private final FeedbackRepository feedbackRepository;
    private final EmailService emailService;

    /**
     * Display patient landing page with dynamic data
     */
    @GetMapping("/landing")
    public String patientLanding(Model model) {
        try {
            // Get current authenticated user
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            String userEmail = authentication.getName();
            
            log.info("Loading landing page for patient: {}", userEmail);
            
            // Find patient by email
            Optional<Patient> patientOpt = patientRepository.findByEmail(userEmail);
            
            if (patientOpt.isPresent()) {
                Patient patient = patientOpt.get();
                
                // Add patient data to model
                model.addAttribute("patient", patient);
                model.addAttribute("patientName", patient.getPatientName() != null ? patient.getPatientName() : "Patient");
                
                // Add user information for contact form auto-fill
                addContactFormUserInfo(model, patient);
                
                // Add approved feedback for reviews section
                model.addAttribute("reviews", feedbackService.getApprovedFeedbackForLanding());
                
                log.info("Patient landing page loaded successfully for: {}", patient.getPatientName());
                return "patient/landingPage/LandingPage";
            } else {
                log.error("Patient not found for email: {}", userEmail);
                model.addAttribute("error", "Patient profile not found");
                return "error/404";
            }
        } catch (Exception e) {
            log.error("Error loading patient landing page: {}", e.getMessage(), e);
            model.addAttribute("error", "Error loading page");
            return "error/500";
        }
    }

    /**
     * Display patient profile page with dynamic data
     */
    @GetMapping("/profile")
    public String patientProfile(Model model) {
        try {
            // Get current authenticated user
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            String userEmail = authentication.getName();
            
            log.info("Loading profile for patient: {}", userEmail);
            
            // Find patient by email
            Optional<Patient> patientOpt = patientRepository.findByEmail(userEmail);
            
            if (patientOpt.isPresent()) {
                Patient patient = patientOpt.get();
                
                // Add patient data to model
                model.addAttribute("patient", patient);
                model.addAttribute("patientName", patient.getPatientName() != null ? patient.getPatientName() : "N/A");
                model.addAttribute("patientId", patient.getId());
                model.addAttribute("patientEmail", patient.getEmail());
                model.addAttribute("patientContact", patient.getContact() != null ? patient.getContact() : "N/A");
                model.addAttribute("patientNic", patient.getNic() != null ? patient.getNic() : "N/A");
                model.addAttribute("patientAddress", patient.getAddress() != null ? patient.getAddress() : "");
                
                // Calculate age from date of birth
                String patientAge = "N/A";
                if (patient.getDob() != null) {
                    int age = calculateAge(patient.getDob());
                    patientAge = String.valueOf(age);
                }
                model.addAttribute("patientAge", patientAge);
                
                // Get recent appointments (last 3)
                try {
                    List<Appointment> allAppointments = appointmentService.getPatientAppointments(userEmail);
                    List<Appointment> recentAppointments = allAppointments.stream()
                        .limit(3)
                        .collect(java.util.stream.Collectors.toList());
                    model.addAttribute("recentAppointments", recentAppointments);
                    model.addAttribute("appointmentCount", allAppointments.size());
                    log.info("Loaded {} recent appointments for profile", recentAppointments.size());
                } catch (Exception e) {
                    log.error("Error loading recent appointments: {}", e.getMessage(), e);
                    model.addAttribute("recentAppointments", new ArrayList<Appointment>());
                    model.addAttribute("appointmentCount", 0);
                }
                
                // Get recent vaccination history (last 3)
                try {
                    List<VaccinationHistory> allVaccinationHistory = vaccinationHistoryRepository.findByPatientIdOrderByVaccinationDateDesc(patient.getId());
                    List<VaccinationHistory> completedVaccinations = allVaccinationHistory.stream()
                        .filter(history -> "approved".equals(history.getNurseApproval()))
                        .limit(3)
                        .collect(java.util.stream.Collectors.toList());
                    model.addAttribute("recentVaccinationHistory", completedVaccinations);
                    model.addAttribute("vaccinationCount", completedVaccinations.size());
                    log.info("Loaded {} recent vaccination records for profile", completedVaccinations.size());
                } catch (Exception e) {
                    log.error("Error loading recent vaccination history: {}", e.getMessage(), e);
                    model.addAttribute("recentVaccinationHistory", new ArrayList<VaccinationHistory>());
                    model.addAttribute("vaccinationCount", 0);
                }
                
                log.info("Patient profile loaded successfully for: {}", patient.getPatientName());
                return "patient/profilePage/ProfilePage";
            } else {
                log.error("Patient not found for email: {}", userEmail);
                model.addAttribute("error", "Patient profile not found");
                return "error/404";
            }
        } catch (Exception e) {
            log.error("Error loading patient profile: {}", e.getMessage(), e);
            model.addAttribute("error", "Error loading profile");
            return "error/500";
        }
    }

    /**
     * Test endpoint to verify password update functionality
     */
    @GetMapping("/test-password")
    public String testPasswordUpdate() {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            String userEmail = authentication.getName();
            
            log.info("Testing password update for: {}", userEmail);
            
            // Check Patient table
            Optional<Patient> patientOpt = patientRepository.findByEmail(userEmail);
            String patientPassword = patientOpt.isPresent() ? "Patient found" : "Patient not found";
            
            // Check UserAccount table
            Optional<UserAccount> userOpt = userRepository.findByEmail(userEmail);
            String userPassword = userOpt.isPresent() ? "UserAccount found" : "UserAccount not found";
            
            return "Patient: " + patientPassword + " | UserAccount: " + userPassword + 
                   " | Email: " + userEmail;
        } catch (Exception e) {
            log.error("Error testing password update: {}", e.getMessage(), e);
            return "Error: " + e.getMessage();
        }
    }

    /**
     * Test endpoint to verify patient data fetching
     */
    @GetMapping("/test-data")
    public String testPatientData(Model model) {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            String userEmail = authentication.getName();
            
            log.info("Testing patient data for: {}", userEmail);
            
            Optional<Patient> patientOpt = patientRepository.findByEmail(userEmail);
            
            if (patientOpt.isPresent()) {
                Patient patient = patientOpt.get();
                String age = "N/A";
                if (patient.getDob() != null) {
                    age = String.valueOf(calculateAge(patient.getDob()));
                }
                return "Patient found: " + patient.getPatientName() + 
                       " (ID: " + patient.getId() + 
                       ", Age: " + age + 
                       ", DOB: " + (patient.getDob() != null ? patient.getDob().toString() : "N/A") + ")";
            } else {
                return "Patient not found for email: " + userEmail;
            }
        } catch (Exception e) {
            log.error("Error testing patient data: {}", e.getMessage(), e);
            return "Error: " + e.getMessage();
        }
    }

    /**
     * Update patient profile
     */
    @PostMapping("/profile/update")
    public String updateProfile(
            @RequestParam String patientName,
            @RequestParam String email,
            @RequestParam String contact,
            @RequestParam(required = false) String address,
            @RequestParam(required = false) String newPassword,
            RedirectAttributes redirectAttributes) {
        try {
            // Get current authenticated user
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            String userEmail = authentication.getName();
            
            log.info("Updating profile for patient: {}", userEmail);
            
            // Find patient by email
            Optional<Patient> patientOpt = patientRepository.findByEmail(userEmail);
            
            if (patientOpt.isPresent()) {
                Patient patient = patientOpt.get();
                
                // Store original email for comparison
                String originalEmail = patient.getEmail();
                
                // Update patient data
                patient.setPatientName(patientName);
                patient.setEmail(email);
                patient.setContact(contact);
                
                // Also update email in UserAccount table if email changed
                if (!originalEmail.equals(email)) {
                    Optional<UserAccount> userOpt = userRepository.findByEmail(originalEmail);
                    if (userOpt.isPresent()) {
                        UserAccount userAccount = userOpt.get();
                        userAccount.setEmail(email);
                        userRepository.save(userAccount);
                        log.info("Email updated in UserAccount table from {} to {}", originalEmail, email);
                    }
                }
                
                // Update address if provided
                if (address != null && !address.trim().isEmpty()) {
                    patient.setAddress(address);
                }
                
                // Update password if provided
                if (newPassword != null && !newPassword.trim().isEmpty()) {
                    log.info("Password update requested for patient: {}", userEmail);
                    
                    // Update password in UserAccount table (this is where login credentials are stored)
                    Optional<UserAccount> userOpt = userRepository.findByEmail(userEmail);
                    if (userOpt.isPresent()) {
                        UserAccount userAccount = userOpt.get();
                        // Note: In a real application, you would hash the password here
                        // For now, we'll store it as plain text (you should implement proper password hashing)
                        userAccount.setPassword(newPassword);
                        userRepository.save(userAccount);
                        log.info("Password updated in UserAccount table for: {}", userEmail);
                    } else {
                        log.warn("UserAccount not found for email: {}", userEmail);
                    }
                }
                
                // Save updated patient
                patientRepository.save(patient);
                
                log.info("Profile updated successfully for patient: {}", userEmail);
                redirectAttributes.addFlashAttribute("successMessage", "Profile updated successfully!");
                
                return "redirect:/patient/profile";
            } else {
                log.error("Patient not found for email: {}", userEmail);
                redirectAttributes.addFlashAttribute("errorMessage", "Patient not found");
                return "redirect:/patient/profile";
            }
        } catch (Exception e) {
            log.error("Error updating patient profile: {}", e.getMessage(), e);
            redirectAttributes.addFlashAttribute("errorMessage", "Error updating profile. Please try again.");
            return "redirect:/patient/profile";
        }
    }

    /**
     * Calculate age from date of birth
     */
    private int calculateAge(LocalDate dob) {
        return Period.between(dob, LocalDate.now()).getYears();
    }

    /**
     * Simple appointments page without complex logic
     */
    @GetMapping("/appointments-simple")
    public String patientAppointmentsSimple(Model model) {
        try {
            model.addAttribute("patientName", "Test Patient");
            model.addAttribute("availableVaccines", new ArrayList<String>());
            model.addAttribute("appointments", new ArrayList<Appointment>());
            return "patient/appointmentPage/AppointmentPage";
        } catch (Exception e) {
            log.error("Error in simple appointments page: {}", e.getMessage(), e);
            return "error/500";
        }
    }

    /**
     * Simplified appointments page - step by step
     */
    @GetMapping("/appointments-step1")
    public String patientAppointmentsStep1(Model model) {
        try {
            // Just authentication and patient lookup
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            String userEmail = authentication.getName();
            
            Optional<Patient> patientOpt = patientRepository.findByEmail(userEmail);
            if (patientOpt.isPresent()) {
                Patient patient = patientOpt.get();
                model.addAttribute("patient", patient);
                model.addAttribute("patientName", patient.getPatientName() != null ? patient.getPatientName() : "Patient");
                model.addAttribute("patientId", patient.getId());
                model.addAttribute("availableVaccines", new ArrayList<String>());
                model.addAttribute("appointments", new ArrayList<Appointment>());
                return "patient/appointmentPage/AppointmentPage";
            } else {
                model.addAttribute("error", "Patient profile not found");
                return "error/404";
            }
        } catch (Exception e) {
            log.error("Error in step1 appointments: {}", e.getMessage(), e);
            model.addAttribute("error", "Error: " + e.getMessage());
            return "error/500";
        }
    }

    /**
     * Simplified appointments page - with vaccines
     */
    @GetMapping("/appointments-step2")
    public String patientAppointmentsStep2(Model model) {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            String userEmail = authentication.getName();
            
            Optional<Patient> patientOpt = patientRepository.findByEmail(userEmail);
            if (patientOpt.isPresent()) {
                Patient patient = patientOpt.get();
                model.addAttribute("patient", patient);
                model.addAttribute("patientName", patient.getPatientName() != null ? patient.getPatientName() : "Patient");
                model.addAttribute("patientId", patient.getId());
                
                // Add vaccines
                List<String> availableVaccines = appointmentService.getAvailableVaccines();
                model.addAttribute("availableVaccines", availableVaccines);
                model.addAttribute("appointments", new ArrayList<Appointment>());
                return "patient/appointmentPage/AppointmentPage";
            } else {
                model.addAttribute("error", "Patient profile not found");
                return "error/404";
            }
        } catch (Exception e) {
            log.error("Error in step2 appointments: {}", e.getMessage(), e);
            model.addAttribute("error", "Error: " + e.getMessage());
            return "error/500";
        }
    }

    /**
     * Minimal appointments page - no services, no database calls
     */
    @GetMapping("/appointments-minimal")
    public String patientAppointmentsMinimal(Model model) {
        try {
            // Just add basic attributes without any service calls
            model.addAttribute("patientName", "Test Patient");
            model.addAttribute("patientId", "TEST001");
            model.addAttribute("availableVaccines", new ArrayList<String>());
            model.addAttribute("appointments", new ArrayList<Appointment>());
            
            log.info("Minimal appointments page loaded successfully");
            return "patient/appointmentPage/AppointmentPage";
        } catch (Exception e) {
            log.error("Error in minimal appointments page: {}", e.getMessage(), e);
            model.addAttribute("error", "Error: " + e.getMessage());
            return "error/500";
        }
    }

    /**
     * Display patient appointment page
     */
    @GetMapping("/appointments")
    public String patientAppointments(Model model) {
        try {
            // Get current authenticated user
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            String userEmail = authentication.getName();
            
            log.info("Loading appointments page for patient: {}", userEmail);
            
            // Find patient by email
            Optional<Patient> patientOpt = patientRepository.findByEmail(userEmail);
            
            if (patientOpt.isPresent()) {
                Patient patient = patientOpt.get();
                
                // Add patient data to model
                model.addAttribute("patient", patient);
                model.addAttribute("patientName", patient.getPatientName() != null ? patient.getPatientName() : "Patient");
                model.addAttribute("patientId", patient.getId());
                
                // Add available vaccines to model (with error handling)
                try {
                    List<String> availableVaccines = appointmentService.getAvailableVaccines();
                    model.addAttribute("availableVaccines", availableVaccines);
                    log.info("Loaded {} available vaccines", availableVaccines.size());
                } catch (Exception e) {
                    log.error("Error loading available vaccines: {}", e.getMessage(), e);
                    model.addAttribute("availableVaccines", new ArrayList<String>());
                    model.addAttribute("vaccineError", "Unable to load available vaccines");
                }
                
                // Get patient's appointments (with error handling)
                try {
                    List<Appointment> patientAppointments = appointmentService.getPatientAppointments(userEmail);
                    model.addAttribute("appointments", patientAppointments);
                    log.info("Loaded {} appointments for patient", patientAppointments.size());
                } catch (Exception e) {
                    log.error("Error loading patient appointments: {}", e.getMessage(), e);
                    model.addAttribute("appointments", new ArrayList<Appointment>());
                    model.addAttribute("appointmentError", "Unable to load appointments");
                }
                
                log.info("Patient appointments page loaded successfully for: {}", patient.getPatientName());
                return "patient/appointmentPage/AppointmentPage";
            } else {
                log.error("Patient not found for email: {}", userEmail);
                model.addAttribute("error", "Patient profile not found. Please contact support.");
                return "error/404";
            }
        } catch (Exception e) {
            log.error("Error loading patient appointments page: {}", e.getMessage(), e);
            model.addAttribute("error", "Error loading appointments page: " + e.getMessage());
            return "error/500";
        }
    }

    /**
     * Get institutions that offer a specific vaccine (AJAX endpoint)
     */
    @GetMapping("/api/institutions")
    @ResponseBody
    public List<Map<String, String>> getInstitutionsByVaccine(@RequestParam String vaccineName) {
        try {
            log.info("Fetching institutions for vaccine: {}", vaccineName);
            return appointmentService.getInstitutionsByVaccine(vaccineName);
        } catch (Exception e) {
            log.error("Error fetching institutions for vaccine {}: {}", vaccineName, e.getMessage(), e);
            return List.of();
        }
    }

    /**
     * Get available days for a specific vaccine and institution (AJAX endpoint)
     */
    @GetMapping("/api/days")
    @ResponseBody
    public List<String> getAvailableDays(@RequestParam String vaccineName, 
                                       @RequestParam String institutionId, 
                                       @RequestParam String institutionType) {
        try {
            log.info("Fetching available days for vaccine {} at institution {}", vaccineName, institutionId);
            return appointmentService.getAvailableDays(vaccineName, institutionId, institutionType);
        } catch (Exception e) {
            log.error("Error fetching available days: {}", e.getMessage(), e);
            return List.of();
        }
    }

    /**
     * Get available time slots for a specific vaccine, institution, and date (AJAX endpoint)
     */
    @GetMapping("/api/time-slots")
    @ResponseBody
    public List<String> getAvailableTimeSlots(@RequestParam String vaccineName, 
                                            @RequestParam String institutionId, 
                                            @RequestParam String institutionType, 
                                            @RequestParam String appointmentDate) {
        try {
            LocalDate date = LocalDate.parse(appointmentDate);
            log.info("Fetching available time slots for vaccine {} at institution {} on {}", 
                    vaccineName, institutionId, date);
            return appointmentService.getAvailableTimeSlots(vaccineName, institutionId, institutionType, date);
        } catch (Exception e) {
            log.error("Error fetching available time slots: {}", e.getMessage(), e);
            return List.of();
        }
    }

    /**
     * Debug booking endpoint - shows what data is being received
     */
    @PostMapping("/appointments/debug-book")
    @ResponseBody
    public String debugBookAppointment(@RequestParam String vaccineName,
                                     @RequestParam String institutionId,
                                     @RequestParam String institutionType,
                                     @RequestParam String appointmentDate,
                                     @RequestParam String timeSlot) {
        try {
            StringBuilder result = new StringBuilder();
            result.append("=== DEBUGGING BOOK APPOINTMENT ===\n\n");
            
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            String userEmail = authentication.getName();
            
            result.append("✅ Authentication: ").append(userEmail).append("\n");
            result.append("✅ Vaccine: ").append(vaccineName).append("\n");
            result.append("✅ Institution ID: ").append(institutionId).append("\n");
            result.append("✅ Institution Type: ").append(institutionType).append("\n");
            result.append("✅ Appointment Date: ").append(appointmentDate).append("\n");
            result.append("✅ Time Slot: ").append(timeSlot).append("\n\n");
            
            // Test date parsing
            try {
                LocalDate date = LocalDate.parse(appointmentDate);
                result.append("✅ Date parsing successful: ").append(date).append("\n");
            } catch (Exception e) {
                result.append("❌ Date parsing failed: ").append(e.getMessage()).append("\n");
            }
            
            // Test patient lookup
            try {
                Optional<Patient> patientOpt = patientRepository.findByEmail(userEmail);
                if (patientOpt.isPresent()) {
                    result.append("✅ Patient found: ").append(patientOpt.get().getPatientName()).append("\n");
                } else {
                    result.append("❌ Patient not found for email: ").append(userEmail).append("\n");
                }
            } catch (Exception e) {
                result.append("❌ Patient lookup error: ").append(e.getMessage()).append("\n");
            }
            
            // Test vaccination schedule lookup
            try {
                List<lk.vakapo.vakapo.UserManagement.model.VaccinationSchedule> schedules = 
                    vaccinationScheduleRepository.findByVaccineAndInstitution(vaccineName, institutionId, institutionType);
                result.append("✅ Vaccination schedules found: ").append(schedules.size()).append("\n");
                if (!schedules.isEmpty()) {
                    result.append("✅ Doctor: ").append(schedules.get(0).getDoctorName()).append("\n");
                }
            } catch (Exception e) {
                result.append("❌ Vaccination schedule lookup error: ").append(e.getMessage()).append("\n");
            }
            
            return result.toString();
        } catch (Exception e) {
            log.error("Error in debug booking: {}", e.getMessage(), e);
            return "Error: " + e.getMessage();
        }
    }

    /**
     * Book an appointment
     */
    @PostMapping("/appointments/book")
    public String bookAppointment(@RequestParam String vaccineName,
                                @RequestParam String institutionId,
                                @RequestParam String institutionType,
                                @RequestParam String appointmentDate,
                                @RequestParam String timeSlot,
                                RedirectAttributes redirectAttributes) {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            String userEmail = authentication.getName();
            
            log.info("Booking appointment for patient {}: vaccine={}, institution={}, date={}, time={}", 
                    userEmail, vaccineName, institutionId, appointmentDate, timeSlot);
            
            LocalDate date = LocalDate.parse(appointmentDate);
            
            appointmentService.bookAppointment(userEmail, vaccineName, institutionId, institutionType, date, timeSlot);
            
            redirectAttributes.addFlashAttribute("successMessage", "Appointment booked successfully!");
            return "redirect:/patient/appointments";
        } catch (Exception e) {
            log.error("Error booking appointment: {}", e.getMessage(), e);
            redirectAttributes.addFlashAttribute("errorMessage", "Error booking appointment: " + e.getMessage());
            return "redirect:/patient/appointments";
        }
    }

    /**
     * Cancel an appointment
     */
    @PostMapping("/appointments/cancel")
    public String cancelAppointment(@RequestParam Long appointmentId, RedirectAttributes redirectAttributes) {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            String userEmail = authentication.getName();
            
            log.info("Cancelling appointment {} for patient {}", appointmentId, userEmail);
            
            boolean cancelled = appointmentService.cancelAppointment(appointmentId, userEmail);
            if (cancelled) {
                redirectAttributes.addFlashAttribute("successMessage", "Appointment cancelled successfully!");
            } else {
                redirectAttributes.addFlashAttribute("errorMessage", "Failed to cancel appointment");
            }
            
            return "redirect:/patient/appointments";
        } catch (Exception e) {
            log.error("Error cancelling appointment: {}", e.getMessage(), e);
            redirectAttributes.addFlashAttribute("errorMessage", "Error cancelling appointment: " + e.getMessage());
            return "redirect:/patient/appointments";
        }
    }

    /**
     * Reschedule an appointment (patient)
     * Allows changing only the appointment date (and auto-assigns an available time slot for that date)
     * Enforces 24-hour rule and keeps vaccine/institution the same
     */
    @PostMapping("/appointments/reschedule")
    public String rescheduleAppointment(@RequestParam Long appointmentId,
                                        @RequestParam("newDate") @org.springframework.format.annotation.DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate newDate,
                                        @RequestParam("newTimeSlot") String newTimeSlot,
                                        RedirectAttributes redirectAttributes) {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            String userEmail = authentication.getName();

            boolean ok = appointmentService.rescheduleAppointment(appointmentId, userEmail, newDate, newTimeSlot);
            if (ok) {
                redirectAttributes.addFlashAttribute("successMessage", "Appointment rescheduled successfully!");
            } else {
                redirectAttributes.addFlashAttribute("errorMessage", "Unable to reschedule. Ensure it's more than 24 hours before and the selected date has availability.");
            }
            return "redirect:/patient/appointments";
        } catch (Exception e) {
            log.error("Error rescheduling appointment: {}", e.getMessage(), e);
            redirectAttributes.addFlashAttribute("errorMessage", "Error rescheduling appointment: " + e.getMessage());
            return "redirect:/patient/appointments";
        }
    }

    /**
     * Debug endpoint to check all vaccination schedules
     */
    @GetMapping("/debug/all-schedules")
    @ResponseBody
    public String debugAllSchedules() {
        try {
            StringBuilder result = new StringBuilder();
            result.append("=== ALL VACCINATION SCHEDULES ===\n\n");
            
            // Get all vaccination schedules
            List<lk.vakapo.vakapo.UserManagement.model.VaccinationSchedule> allSchedules = 
                appointmentService.getAllVaccinationSchedules();
            
            if (allSchedules.isEmpty()) {
                result.append("❌ NO VACCINATION SCHEDULES FOUND IN DATABASE!\n");
                result.append("You need to run the database setup script: create_appointments_table.sql\n");
            } else {
                result.append("✅ Found ").append(allSchedules.size()).append(" vaccination schedule(s):\n\n");
                for (lk.vakapo.vakapo.UserManagement.model.VaccinationSchedule schedule : allSchedules) {
                    result.append("ID: ").append(schedule.getId()).append("\n");
                    result.append("Doctor: ").append(schedule.getDoctorName()).append("\n");
                    result.append("Vaccine: ").append(schedule.getVaccineName()).append("\n");
                    result.append("Institution: ").append(schedule.getInstitutionId()).append(" (").append(schedule.getInstitutionType()).append(")\n");
                    result.append("Time: ").append(schedule.getTimeFrom()).append(" - ").append(schedule.getTimeTo()).append("\n");
                    result.append("Days: ").append(schedule.getDays()).append("\n");
                    result.append("Status: ").append(schedule.getStatus()).append("\n");
                    result.append("---\n");
                }
            }
            
            return result.toString();
        } catch (Exception e) {
            log.error("Error in debug all schedules endpoint: {}", e.getMessage(), e);
            return "Error: " + e.getMessage();
        }
    }

    /**
     * Debug endpoint to check vaccination schedules and time slots
     */
    @GetMapping("/debug/schedules")
    @ResponseBody
    public String debugSchedules(@RequestParam(required = false) String vaccineName,
                               @RequestParam(required = false) String institutionId,
                               @RequestParam(required = false) String institutionType) {
        try {
            StringBuilder result = new StringBuilder();
            result.append("=== VACCINATION SCHEDULES DEBUG ===\n\n");
            
            if (vaccineName != null && institutionId != null && institutionType != null) {
                result.append("Checking schedules for:\n");
                result.append("Vaccine: ").append(vaccineName).append("\n");
                result.append("Institution: ").append(institutionId).append(" (").append(institutionType).append(")\n\n");
                
                // Get schedules from repository
                List<lk.vakapo.vakapo.UserManagement.model.VaccinationSchedule> schedules = 
                    appointmentService.getVaccinationSchedulesForDebug(vaccineName, institutionId, institutionType);
                
                if (schedules.isEmpty()) {
                    result.append("❌ NO SCHEDULES FOUND!\n");
                    result.append("This means either:\n");
                    result.append("1. No vaccination schedule exists for this vaccine/institution combination\n");
                    result.append("2. The institution ID or type doesn't match\n");
                    result.append("3. The vaccine name doesn't match exactly\n\n");
                } else {
                    result.append("✅ Found ").append(schedules.size()).append(" schedule(s):\n\n");
                    for (lk.vakapo.vakapo.UserManagement.model.VaccinationSchedule schedule : schedules) {
                        result.append("Schedule ID: ").append(schedule.getId()).append("\n");
                        result.append("Doctor: ").append(schedule.getDoctorName()).append("\n");
                        result.append("Vaccine: ").append(schedule.getVaccineName()).append("\n");
                        result.append("Institution: ").append(schedule.getInstitutionId()).append(" (").append(schedule.getInstitutionType()).append(")\n");
                        result.append("Time: ").append(schedule.getTimeFrom()).append(" - ").append(schedule.getTimeTo()).append("\n");
                        result.append("Days: ").append(schedule.getDays()).append("\n");
                        result.append("Status: ").append(schedule.getStatus()).append("\n");
                        result.append("---\n");
                    }
                }
            } else {
                result.append("Usage: /patient/debug/schedules?vaccineName=Influenza&institutionId=Vak H 1000&institutionType=Hospital\n\n");
                result.append("Available parameters:\n");
                result.append("- vaccineName: Name of the vaccine\n");
                result.append("- institutionId: ID of the institution\n");
                result.append("- institutionType: Type (Hospital or Clinic)\n");
            }
            
            return result.toString();
        } catch (Exception e) {
            log.error("Error in debug endpoint: {}", e.getMessage(), e);
            return "Error: " + e.getMessage();
        }
    }

    /**
     * Test endpoint to check time slots with hardcoded values
     */
    @GetMapping("/test/time-slots-hardcoded")
    @ResponseBody
    public String testTimeSlotsHardcoded() {
        try {
            StringBuilder result = new StringBuilder();
            result.append("=== TESTING TIME SLOTS WITH HARDCODED VALUES ===\n\n");
            
            // Test with hardcoded values that should work
            String vaccineName = "Influenza Vaccine";
            String institutionId = "Vak H 1000";
            String institutionType = "Hospital";
            LocalDate testDate = LocalDate.of(2025, 1, 13); // Monday
            
            result.append("Testing with:\n");
            result.append("Vaccine: ").append(vaccineName).append("\n");
            result.append("Institution: ").append(institutionId).append(" (").append(institutionType).append(")\n");
            result.append("Date: ").append(testDate).append(" (").append(testDate.getDayOfWeek()).append(")\n\n");
            
            // Get available time slots
            List<String> timeSlots = appointmentService.getAvailableTimeSlots(
                vaccineName, institutionId, institutionType, testDate);
            
            if (timeSlots.isEmpty()) {
                result.append("❌ NO TIME SLOTS FOUND!\n");
                result.append("This means either:\n");
                result.append("1. No vaccination schedule exists for this combination\n");
                result.append("2. The day doesn't match the schedule\n");
                result.append("3. Database connection issue\n");
            } else {
                result.append("✅ Found ").append(timeSlots.size()).append(" time slots:\n");
                for (String slot : timeSlots) {
                    result.append("- ").append(slot).append("\n");
                }
            }
            
            return result.toString();
        } catch (Exception e) {
            log.error("Error in hardcoded test: {}", e.getMessage(), e);
            return "Error: " + e.getMessage();
        }
    }

    /**
     * Ultra-simple test - just return text
     */
    @GetMapping("/test/ultra-simple")
    @ResponseBody
    public String testUltraSimple() {
        return "Ultra simple test - controller is working!";
    }

    /**
     * Test if we can return a simple HTML page
     */
    @GetMapping("/test/simple-page")
    public String testSimplePage(Model model) {
        model.addAttribute("message", "Simple page test");
        return "patient/appointmentPage/AppointmentPage";
    }

    /**
     * Simple test endpoint without authentication
     */
    @GetMapping("/test/simple")
    @ResponseBody
    public String testSimple() {
        try {
            return "✅ Simple test endpoint working!";
        } catch (Exception e) {
            return "❌ Error: " + e.getMessage();
        }
    }

    /**
     * Test database connection
     */
    @GetMapping("/test/database")
    @ResponseBody
    public String testDatabase() {
        try {
            // Test if we can query vaccination schedules
            List<lk.vakapo.vakapo.UserManagement.model.VaccinationSchedule> schedules = 
                vaccinationScheduleRepository.findAll();
            return "✅ Database connection working! Found " + schedules.size() + " vaccination schedules.";
        } catch (Exception e) {
            return "❌ Database error: " + e.getMessage();
        }
    }

    /**
     * Debug the main appointments endpoint step by step
     */
    @GetMapping("/test/appointments-debug")
    @ResponseBody
    public String testAppointmentsDebug() {
        try {
            StringBuilder result = new StringBuilder();
            result.append("=== DEBUGGING MAIN APPOINTMENTS ENDPOINT ===\n\n");
            
            // Step 1: Authentication
            try {
                Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
                String userEmail = authentication.getName();
                result.append("✅ Step 1 - Authentication: ").append(userEmail).append("\n");
                
                // Step 2: Find patient
                try {
                    Optional<Patient> patientOpt = patientRepository.findByEmail(userEmail);
                    if (patientOpt.isPresent()) {
                        Patient patient = patientOpt.get();
                        result.append("✅ Step 2 - Patient found: ").append(patient.getPatientName()).append("\n");
                        
                        // Step 3: Get available vaccines
                        try {
                            List<String> availableVaccines = appointmentService.getAvailableVaccines();
                            result.append("✅ Step 3 - Available vaccines: ").append(availableVaccines.size()).append(" found\n");
                            for (String vaccine : availableVaccines) {
                                result.append("  - ").append(vaccine).append("\n");
                            }
                        } catch (Exception e) {
                            result.append("❌ Step 3 - Error getting vaccines: ").append(e.getMessage()).append("\n");
                        }
                        
                        // Step 4: Get patient appointments
                        try {
                            List<Appointment> patientAppointments = appointmentService.getPatientAppointments(userEmail);
                            result.append("✅ Step 4 - Patient appointments: ").append(patientAppointments.size()).append(" found\n");
                        } catch (Exception e) {
                            result.append("❌ Step 4 - Error getting appointments: ").append(e.getMessage()).append("\n");
                        }
                        
                    } else {
                        result.append("❌ Step 2 - Patient not found for email: ").append(userEmail).append("\n");
                    }
                } catch (Exception e) {
                    result.append("❌ Step 2 - Error finding patient: ").append(e.getMessage()).append("\n");
                }
                
            } catch (Exception e) {
                result.append("❌ Step 1 - Authentication error: ").append(e.getMessage()).append("\n");
            }
            
            return result.toString();
        } catch (Exception e) {
            log.error("Error in appointments debug: {}", e.getMessage(), e);
            return "Error: " + e.getMessage();
        }
    }

    /**
     * Test endpoint to check basic services
     */
    @GetMapping("/test/basic-services")
    @ResponseBody
    public String testBasicServices() {
        try {
            StringBuilder result = new StringBuilder();
            result.append("=== TESTING BASIC SERVICES ===\n\n");
            
            // Test 1: Check if we can get current user
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            String userEmail = authentication.getName();
            result.append("✅ Current user: ").append(userEmail).append("\n");
            
            // Test 2: Check if we can find patient
            Optional<Patient> patientOpt = patientRepository.findByEmail(userEmail);
            if (patientOpt.isPresent()) {
                result.append("✅ Patient found: ").append(patientOpt.get().getPatientName()).append("\n");
            } else {
                result.append("❌ Patient not found for email: ").append(userEmail).append("\n");
            }
            
            // Test 3: Check if we can get available vaccines
            try {
                List<String> vaccines = appointmentService.getAvailableVaccines();
                result.append("✅ Available vaccines: ").append(vaccines.size()).append(" found\n");
                for (String vaccine : vaccines) {
                    result.append("  - ").append(vaccine).append("\n");
                }
            } catch (Exception e) {
                result.append("❌ Error getting vaccines: ").append(e.getMessage()).append("\n");
            }
            
            // Test 4: Check if we can get patient appointments
            try {
                List<Appointment> appointments = appointmentService.getPatientAppointments(userEmail);
                result.append("✅ Patient appointments: ").append(appointments.size()).append(" found\n");
            } catch (Exception e) {
                result.append("❌ Error getting appointments: ").append(e.getMessage()).append("\n");
            }
            
            return result.toString();
        } catch (Exception e) {
            log.error("Error in basic services test: {}", e.getMessage(), e);
            return "Error: " + e.getMessage();
        }
    }

    /**
     * Test endpoint to verify time slot generation with real data
     */
    @GetMapping("/test/time-slots-real")
    @ResponseBody
    public String testTimeSlotsReal() {
        try {
            StringBuilder result = new StringBuilder();
            result.append("=== TESTING TIME SLOTS WITH REAL DATABASE DATA ===\n\n");
            
            // Test with the exact data from the database
            String vaccineName = "Influenza Vaccine";
            String institutionId = "Vak H 1000";
            String institutionType = "Hospital";
            LocalDate testDate = LocalDate.of(2025, 1, 13); // Monday
            
            result.append("Testing with:\n");
            result.append("Vaccine: ").append(vaccineName).append("\n");
            result.append("Institution: ").append(institutionId).append(" (").append(institutionType).append(")\n");
            result.append("Date: ").append(testDate).append(" (").append(testDate.getDayOfWeek()).append(")\n\n");
            
            // Get available time slots
            List<String> timeSlots = appointmentService.getAvailableTimeSlots(
                vaccineName, institutionId, institutionType, testDate);
            
            if (timeSlots.isEmpty()) {
                result.append("❌ NO TIME SLOTS FOUND!\n");
                result.append("This means either:\n");
                result.append("1. No vaccination schedule exists for this combination\n");
                result.append("2. The day doesn't match the schedule\n");
                result.append("3. Database connection issue\n");
            } else {
                result.append("✅ Found ").append(timeSlots.size()).append(" time slots:\n");
                for (String slot : timeSlots) {
                    result.append("- ").append(slot).append("\n");
                }
            }
            
            return result.toString();
        } catch (Exception e) {
            log.error("Error in real data test: {}", e.getMessage(), e);
            return "Error: " + e.getMessage();
        }
    }

    /**
     * Test endpoint to demonstrate time slot generation
     */
    @GetMapping("/test/time-slots")
    @ResponseBody
    public String testTimeSlotGeneration(@RequestParam String timeFrom, 
                                       @RequestParam String timeTo) {
        try {
            // This is a test endpoint to show how time slots are generated
            // In a real scenario, you would call appointmentService.getAvailableTimeSlots()
            
            log.info("Testing time slot generation from {} to {}", timeFrom, timeTo);
            
            // Simulate the time slot generation logic
            java.time.format.DateTimeFormatter formatter = java.time.format.DateTimeFormatter.ofPattern("HH:mm");
            java.time.LocalTime startTime = java.time.LocalTime.parse(timeFrom, formatter);
            java.time.LocalTime endTime = java.time.LocalTime.parse(timeTo, formatter);
            
            StringBuilder result = new StringBuilder();
            result.append("Time slots generated from ").append(timeFrom).append(" to ").append(timeTo).append(":\n");
            
            java.time.LocalTime currentTime = startTime;
            int slotCount = 0;
            
            while (currentTime.isBefore(endTime)) {
                java.time.LocalTime nextTime = currentTime.plusMinutes(20);
                
                if (!nextTime.isAfter(endTime)) {
                    String timeSlot = currentTime.format(formatter) + "-" + nextTime.format(formatter);
                    result.append("Slot ").append(++slotCount).append(": ").append(timeSlot).append("\n");
                } else {
                    result.append("Skipped: ").append(currentTime.format(formatter)).append(" (would exceed end time)\n");
                    break;
                }
                
                currentTime = nextTime;
            }
            
            result.append("\nTotal slots generated: ").append(slotCount);
            return result.toString();
        } catch (Exception e) {
            log.error("Error testing time slot generation: {}", e.getMessage(), e);
            return "Error: " + e.getMessage();
        }
    }

    /**
     * Delete patient account
     */
    @PostMapping("/delete-account")
    public String deleteAccount(RedirectAttributes redirectAttributes) {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            String userEmail = authentication.getName();

            log.info("Deleting account for patient: {}", userEmail);

            // Find patient by email
            Optional<Patient> patientOpt = patientRepository.findByEmail(userEmail);

            if (patientOpt.isPresent()) {
                Patient patient = patientOpt.get();
                String patientId = patient.getId();

                // Delete from Patient table
                patientRepository.delete(patient);

                // Delete from UserAccount table
                Optional<UserAccount> userOpt = userRepository.findByEmail(userEmail);
                if (userOpt.isPresent()) {
                    userRepository.delete(userOpt.get());
                }

                log.info("Patient account deleted successfully: {}", patientId);
                
                // Send account deletion email
                try {
                    emailService.sendAccountDeletionEmail(userEmail, patient.getPatientName(), "Patient");
                    log.info("Account deletion email sent to patient: {}", userEmail);
                } catch (Exception e) {
                    log.error("Failed to send account deletion email to patient: {}", userEmail, e);
                    // Don't fail the deletion if email fails
                }
                
                redirectAttributes.addFlashAttribute("successMessage", "Your account has been deleted successfully.");
                
                // Redirect to login page after deletion
                return "redirect:/login?deleted=true";
            } else {
                log.error("Patient not found for email: {}", userEmail);
                redirectAttributes.addFlashAttribute("errorMessage", "Account not found");
                return "redirect:/patient/profile";
            }
        } catch (Exception e) {
            log.error("Error deleting patient account: {}", e.getMessage(), e);
            redirectAttributes.addFlashAttribute("errorMessage", "Error deleting account. Please try again.");
            return "redirect:/patient/profile";
        }
    }

    /**
     * Test endpoint to send appointment confirmation email
     */
    @GetMapping("/test/email-confirmation")
    @ResponseBody
    public String testEmailConfirmation() {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            String userEmail = authentication.getName();
            
            log.info("Testing email confirmation for patient: {}", userEmail);
            
            // Find patient by email
            Optional<Patient> patientOpt = patientRepository.findByEmail(userEmail);
            
            if (patientOpt.isPresent()) {
                Patient patient = patientOpt.get();
                
                // Create a test appointment object
                Appointment testAppointment = new Appointment();
                testAppointment.setId(999L); // Test ID
                testAppointment.setPatientEmail(patient.getEmail());
                testAppointment.setPatientName(patient.getPatientName() != null ? patient.getPatientName() : "Test Patient");
                testAppointment.setVaccineName("COVID-19 Vaccine");
                testAppointment.setInstitutionName("Test Hospital");
                testAppointment.setInstitutionType("Hospital");
                testAppointment.setDoctorName("Dr. Test Doctor");
                testAppointment.setAppointmentDate(LocalDate.now().plusDays(7));
                testAppointment.setTimeSlot("10:00-10:20");
                
                // Send test email
                appointmentService.sendAppointmentConfirmationEmail(testAppointment);
                
                return "✅ Test appointment confirmation email sent successfully to: " + userEmail + 
                       "\n\nTest appointment details:\n" +
                       "• Patient: " + testAppointment.getPatientName() + "\n" +
                       "• Vaccine: " + testAppointment.getVaccineName() + "\n" +
                       "• Date: " + testAppointment.getAppointmentDate() + "\n" +
                       "• Time: " + testAppointment.getTimeSlot() + "\n" +
                       "• Location: " + testAppointment.getInstitutionName() + "\n" +
                       "• Doctor: " + testAppointment.getDoctorName() + "\n\n" +
                       "Please check your email inbox for the confirmation email.";
            } else {
                return "❌ Patient not found for email: " + userEmail;
            }
        } catch (Exception e) {
            log.error("Error testing email confirmation: {}", e.getMessage(), e);
            return "❌ Error testing email confirmation: " + e.getMessage();
        }
    }

    /**
     * Test endpoint to send appointment cancellation confirmation email
     */
    @GetMapping("/test/email-cancellation")
    @ResponseBody
    public String testEmailCancellation() {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            String userEmail = authentication.getName();
            
            log.info("Testing email cancellation for patient: {}", userEmail);
            
            // Find patient by email
            Optional<Patient> patientOpt = patientRepository.findByEmail(userEmail);
            
            if (patientOpt.isPresent()) {
                Patient patient = patientOpt.get();
                
                // Create a test appointment object for cancellation
                Appointment testAppointment = new Appointment();
                testAppointment.setId(888L); // Test ID
                testAppointment.setPatientEmail(patient.getEmail());
                testAppointment.setPatientName(patient.getPatientName() != null ? patient.getPatientName() : "Test Patient");
                testAppointment.setVaccineName("Influenza Vaccine");
                testAppointment.setInstitutionName("Test Hospital");
                testAppointment.setInstitutionType("Hospital");
                testAppointment.setDoctorName("Dr. Test Doctor");
                testAppointment.setAppointmentDate(LocalDate.now().plusDays(5));
                testAppointment.setTimeSlot("14:00-14:20");
                testAppointment.setStatus("cancelled"); // Set as cancelled for testing
                
                // Send test cancellation email
                appointmentService.sendAppointmentCancellationEmail(testAppointment);
                
                return "✅ Test appointment cancellation email sent successfully to: " + userEmail + 
                       "\n\nTest cancelled appointment details:\n" +
                       "• Patient: " + testAppointment.getPatientName() + "\n" +
                       "• Vaccine: " + testAppointment.getVaccineName() + "\n" +
                       "• Date: " + testAppointment.getAppointmentDate() + "\n" +
                       "• Time: " + testAppointment.getTimeSlot() + "\n" +
                       "• Location: " + testAppointment.getInstitutionName() + "\n" +
                       "• Doctor: " + testAppointment.getDoctorName() + "\n" +
                       "• Status: " + testAppointment.getStatus() + "\n\n" +
                       "Please check your email inbox for the cancellation confirmation email.";
            } else {
                return "❌ Patient not found for email: " + userEmail;
            }
        } catch (Exception e) {
            log.error("Error testing email cancellation: {}", e.getMessage(), e);
            return "❌ Error testing email cancellation: " + e.getMessage();
        }
    }

    /**
     * Display patient vaccination history page
     */
    @GetMapping("/vaccination-history")
    public String patientVaccinationHistory(Model model) {
        try {
            // Get current authenticated user
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            String userEmail = authentication.getName();
            
            log.info("Loading vaccination history for patient: {}", userEmail);
            
            // Find patient by email
            Optional<Patient> patientOpt = patientRepository.findByEmail(userEmail);
            
            if (patientOpt.isPresent()) {
                Patient patient = patientOpt.get();
                
                // Add patient data to model
                model.addAttribute("patient", patient);
                model.addAttribute("patientName", patient.getPatientName() != null ? patient.getPatientName() : "Patient");
                model.addAttribute("patientId", patient.getId());
                
                log.info("Patient vaccination history page loaded successfully for: {}", patient.getPatientName());
                return "patient/vaccinationHistoryPage/VaccinationHistoryPage";
            } else {
                log.error("Patient not found for email: {}", userEmail);
                model.addAttribute("error", "Patient profile not found");
                return "error/404";
            }
        } catch (Exception e) {
            log.error("Error loading patient vaccination history: {}", e.getMessage(), e);
            model.addAttribute("error", "Error loading vaccination history");
            return "error/500";
        }
    }

    /**
     * API endpoint to get patient vaccination history
     */
    @GetMapping("/api/vaccination-history")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> getPatientVaccinationHistory() {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            String userEmail = authentication.getName();
            
            log.info("Getting vaccination history for patient: {}", userEmail);
            
            // Find patient by email
            Optional<Patient> patientOpt = patientRepository.findByEmail(userEmail);
            if (patientOpt.isEmpty()) {
                Map<String, Object> response = new HashMap<>();
                response.put("success", false);
                response.put("error", "Patient not found");
                return ResponseEntity.badRequest().body(response);
            }
            
            Patient patient = patientOpt.get();
            String patientId = patient.getId();
            
            // Get patient's vaccination history
            List<VaccinationHistory> vaccinationHistory = vaccinationHistoryRepository.findByPatientIdOrderByVaccinationDateDesc(patientId);
            
            // Filter only approved vaccinations (completed ones)
            List<VaccinationHistory> completedVaccinations = vaccinationHistory.stream()
                .filter(history -> "approved".equals(history.getNurseApproval()))
                .collect(java.util.stream.Collectors.toList());
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("vaccinationHistory", completedVaccinations);
            response.put("count", completedVaccinations.size());
            response.put("patientId", patientId);
            response.put("patientName", patient.getPatientName());
            
            log.info("Found {} completed vaccinations for patient: {}", completedVaccinations.size(), patientId);
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            log.error("Error getting patient vaccination history: {}", e.getMessage(), e);
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("error", "Failed to fetch vaccination history: " + e.getMessage());
            return ResponseEntity.internalServerError().body(response);
        }
    }

    /**
     * Delete vaccination history record
     */
    @DeleteMapping("/api/vaccination-history/{historyId}")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> deleteVaccinationHistory(@PathVariable Long historyId) {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            String userEmail = authentication.getName();
            
            log.info("Deleting vaccination history ID: {} for patient: {}", historyId, userEmail);
            
            // Find patient by email
            Optional<Patient> patientOpt = patientRepository.findByEmail(userEmail);
            if (patientOpt.isEmpty()) {
                Map<String, Object> response = new HashMap<>();
                response.put("success", false);
                response.put("error", "Patient not found");
                return ResponseEntity.badRequest().body(response);
            }
            
            Patient patient = patientOpt.get();
            String patientId = patient.getId();
            
            // Find vaccination history record
            Optional<VaccinationHistory> historyOpt = vaccinationHistoryRepository.findById(historyId);
            if (historyOpt.isEmpty()) {
                Map<String, Object> response = new HashMap<>();
                response.put("success", false);
                response.put("error", "Vaccination record not found");
                return ResponseEntity.badRequest().body(response);
            }
            
            VaccinationHistory history = historyOpt.get();
            
            // Verify this vaccination belongs to the current patient
            if (!patientId.equals(history.getPatientId())) {
                log.error("Access denied - vaccination history {} does not belong to patient {}", historyId, patientId);
                Map<String, Object> response = new HashMap<>();
                response.put("success", false);
                response.put("error", "Access denied. This vaccination record does not belong to you.");
                return ResponseEntity.status(403).body(response);
            }
            
            // Delete the vaccination history record
            vaccinationHistoryRepository.delete(history);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Vaccination record deleted successfully");
            
            log.info("Successfully deleted vaccination history ID: {} for patient: {}", historyId, patientId);
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            log.error("Error deleting vaccination history: {}", e.getMessage(), e);
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("error", "Failed to delete vaccination record: " + e.getMessage());
            return ResponseEntity.internalServerError().body(response);
        }
    }

    /**
     * Download vaccination certificate PDF
     */
    @GetMapping("/download-certificate/{historyId}")
    public ResponseEntity<byte[]> downloadVaccinationCertificate(@PathVariable Long historyId) {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            String userEmail = authentication.getName();
            
            log.info("Downloading vaccination certificate for history ID: {} by patient: {}", historyId, userEmail);
            
            // Find patient by email
            Optional<Patient> patientOpt = patientRepository.findByEmail(userEmail);
            if (patientOpt.isEmpty()) {
                log.error("Patient not found for email: {}", userEmail);
                return ResponseEntity.notFound().build();
            }
            
            Patient patient = patientOpt.get();
            
            // Find vaccination history record
            Optional<VaccinationHistory> historyOpt = vaccinationHistoryRepository.findById(historyId);
            if (historyOpt.isEmpty()) {
                log.error("Vaccination history not found for ID: {}", historyId);
                return ResponseEntity.notFound().build();
            }
            
            VaccinationHistory history = historyOpt.get();
            
            // Verify this vaccination belongs to the current patient
            if (!patient.getId().equals(history.getPatientId())) {
                log.error("Access denied - vaccination history {} does not belong to patient {}", historyId, patient.getId());
                return ResponseEntity.status(403).build();
            }
            
            // Verify this is an approved vaccination
            if (!"approved".equals(history.getNurseApproval())) {
                log.error("Vaccination history {} is not approved for download", historyId);
                return ResponseEntity.status(403).build();
            }
            
            // Generate batch/lot number
            String batchLotNo = generateBatchLotNumber(history.getVaccineName(), history.getVaccinationDate());
            
            // Get nurse name (you might want to store this in the vaccination history)
            String nurseName = "Registered Nurse"; // Default value
            
            // Generate vaccination certificate PDF
            byte[] vaccinationCardPdf = pdfGenerationService.generateVaccinationRecordCard(
                history.getPatientId(),
                history.getPatientName(),
                patient.getDob() != null ? patient.getDob().toString() : "N/A",
                history.getPatientId(), // Using patient ID as health ID
                history.getVaccineName(),
                history.getVaccinationDate().toString(),
                history.getLocation(),
                batchLotNo,
                history.getDoctorName(),
                nurseName
            );
            
            // Create filename
            String fileName = "Vaccination_Certificate_" + 
                history.getPatientName().replaceAll("\\s+", "_") + "_" + 
                history.getVaccinationDate().toString().replaceAll("-", "") + ".pdf";
            
            // Set response headers
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_PDF);
            headers.setContentDispositionFormData("attachment", fileName);
            headers.setContentLength(vaccinationCardPdf.length);
            
            log.info("Vaccination certificate generated successfully for history ID: {}", historyId);
            return ResponseEntity.ok()
                .headers(headers)
                .body(vaccinationCardPdf);
            
        } catch (Exception e) {
            log.error("Error generating vaccination certificate: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * Display patient feedback page
     */
    @GetMapping("/feedback")
    public String patientFeedback(Model model) {
        try {
            // Get current authenticated user
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            String userEmail = authentication.getName();
            
            log.info("Loading feedback page for patient: {}", userEmail);
            
            // Find patient by email
            Optional<Patient> patientOpt = patientRepository.findByEmail(userEmail);
            
            if (patientOpt.isPresent()) {
                Patient patient = patientOpt.get();
                
                // Add patient data to model
                model.addAttribute("patient", patient);
                model.addAttribute("patientName", patient.getPatientName() != null ? patient.getPatientName() : "Patient");
                model.addAttribute("patientId", patient.getId());
                model.addAttribute("patientEmail", patient.getEmail());
                
                log.info("Patient feedback page loaded successfully for: {}", patient.getPatientName());
                return "patient/feedbackPage/FeedbackPage";
            } else {
                log.error("Patient not found for email: {}", userEmail);
                model.addAttribute("error", "Patient profile not found");
                return "error/404";
            }
        } catch (Exception e) {
            log.error("Error loading patient feedback page: {}", e.getMessage(), e);
            model.addAttribute("error", "Error loading page");
            return "error/500";
        }
    }

    /**
     * Submit patient feedback
     */
    @PostMapping("/feedback")
    public String submitPatientFeedback(
            @RequestParam String name,
            @RequestParam String email,
            @RequestParam String contactno,
            @RequestParam Integer rating,
            @RequestParam String message,
            Model model) {
        try {
            // Get current authenticated user
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            String userEmail = authentication.getName();
            
            log.info("Submitting patient feedback from: {}", userEmail);
            
            // Find patient by email
            Optional<Patient> patientOpt = patientRepository.findByEmail(userEmail);
            
            if (patientOpt.isPresent()) {
                Patient patient = patientOpt.get();
                
                // Create feedback object
                Feedback feedback = new Feedback();
                feedback.setName(name);
                feedback.setEmail(email);
                feedback.setContactNo(contactno);
                feedback.setRating(rating);
                feedback.setMessage(message);
                feedback.setUserType("PATIENT");
                feedback.setUserId(patient.getId());
                
                // Submit feedback
                log.info("About to submit feedback: {}", feedback);
                Feedback savedFeedback = feedbackService.submitFeedback(feedback);
                log.info("Feedback submitted successfully with ID: {}", savedFeedback.getId());
                
                // Add success message
                model.addAttribute("successMessage", "Thank you for your feedback! It will be reviewed and may appear on our landing pages.");
                model.addAttribute("patient", patient);
                model.addAttribute("patientName", patient.getPatientName() != null ? patient.getPatientName() : "Patient");
                model.addAttribute("patientId", patient.getId());
                model.addAttribute("patientEmail", patient.getEmail());
                
                log.info("Patient feedback submitted successfully by: {}", userEmail);
                return "patient/feedbackPage/FeedbackPage";
            } else {
                log.error("Patient not found for email: {}", userEmail);
                return "error/404";
            }
        } catch (Exception e) {
            log.error("Error submitting patient feedback: {}", e.getMessage(), e);
            model.addAttribute("errorMessage", "Error submitting feedback. Please try again.");
            return "patient/feedbackPage/FeedbackPage";
        }
    }

    /**
     * Simple test endpoint to check database connection
     */
    @GetMapping("/test/db")
    @ResponseBody
    public Map<String, Object> testFeedbackDatabase() {
        Map<String, Object> response = new HashMap<>();
        try {
            long feedbackCount = feedbackRepository.count();
            response.put("success", true);
            response.put("message", "Database connection successful");
            response.put("feedbackCount", feedbackCount);
            return response;
        } catch (Exception e) {
            response.put("success", false);
            response.put("error", e.getMessage());
            return response;
        }
    }

    /**
     * Debug endpoint to check all feedback in database
     */
    @GetMapping("/debug/feedback")
    @ResponseBody
    public Map<String, Object> debugFeedback() {
        Map<String, Object> response = new HashMap<>();
        try {
            log.info("Debug endpoint called");
            
            // Test database connection first
            long feedbackCount = feedbackRepository.count();
            log.info("Total feedback count: {}", feedbackCount);
            
            // Get all feedback
            List<Feedback> allFeedback = feedbackRepository.findAll();
            log.info("Retrieved {} feedback records", allFeedback.size());
            
            // Get current user info (if authenticated)
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            String userEmail = "NOT_AUTHENTICATED";
            String patientId = "NOT_FOUND";
            
            if (authentication != null && !authentication.getName().equals("anonymousUser")) {
                userEmail = authentication.getName();
                log.info("Current user email: {}", userEmail);
                
                Optional<Patient> patientOpt = patientRepository.findByEmail(userEmail);
                if (patientOpt.isPresent()) {
                    patientId = patientOpt.get().getId();
                    log.info("Current patient ID: {}", patientId);
                }
            }
            
            response.put("success", true);
            response.put("totalFeedback", feedbackCount);
            response.put("feedbackRecords", allFeedback.size());
            response.put("allFeedback", allFeedback);
            response.put("currentUserEmail", userEmail);
            response.put("currentPatientId", patientId);
            response.put("isAuthenticated", authentication != null && !authentication.getName().equals("anonymousUser"));
            
            return response;
        } catch (Exception e) {
            log.error("Error in debug feedback: {}", e.getMessage(), e);
            response.put("success", false);
            response.put("error", e.getMessage());
            response.put("errorType", e.getClass().getSimpleName());
            return response;
        }
    }

    /**
     * Get user's own feedback (My Reviews section)
     */
    @GetMapping("/api/my-reviews")
    @ResponseBody
    public Map<String, Object> getMyReviews(HttpServletRequest request) {
        Map<String, Object> response = new HashMap<>();
        try {
            // Get current authenticated user
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            String userEmail = authentication.getName();
            
            log.info("Getting reviews for patient email: {}", userEmail);
            
            // Find patient by email
            Optional<Patient> patientOpt = patientRepository.findByEmail(userEmail);
            
            if (patientOpt.isPresent()) {
                Patient patient = patientOpt.get();
                log.info("Found patient with ID: {}", patient.getId());
                
                // Get user's feedback
                List<Feedback> myReviews = feedbackRepository.findByUserIdOrderByCreatedAtDesc(patient.getId());
                log.info("Found {} reviews for patient ID: {}", myReviews.size(), patient.getId());
                
                response.put("success", true);
                response.put("data", myReviews);
                response.put("count", myReviews.size());
                
                return response;
            } else {
                log.warn("Patient not found for email: {}", userEmail);
                response.put("success", false);
                response.put("message", "Patient not found");
                return response;
            }
        } catch (Exception e) {
            log.error("Error getting patient reviews: {}", e.getMessage(), e);
            response.put("success", false);
            response.put("message", "Error retrieving reviews");
            return response;
        }
    }

    /**
     * Update user's own feedback
     */
    @PostMapping("/api/update-review")
    @ResponseBody
    public Map<String, Object> updateMyReview(
            @RequestParam Long feedbackId,
            @RequestParam String name,
            @RequestParam String email,
            @RequestParam String contactNo,
            @RequestParam Integer rating,
            @RequestParam String message,
            HttpServletRequest request) {
        Map<String, Object> response = new HashMap<>();
        try {
            // Get current authenticated user
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            String userEmail = authentication.getName();
            
            // Find patient by email
            Optional<Patient> patientOpt = patientRepository.findByEmail(userEmail);
            
            if (patientOpt.isPresent()) {
                Patient patient = patientOpt.get();
                
                // Update feedback
                boolean success = feedbackService.updateFeedback(feedbackId, patient.getId(), name, email, contactNo, rating, message);
                
                if (success) {
                    response.put("success", true);
                    response.put("message", "Review updated successfully! It will be reviewed again by admin.");
                } else {
                    response.put("success", false);
                    response.put("message", "Failed to update review. You can only edit your own reviews.");
                }
                
                return response;
            } else {
                response.put("success", false);
                response.put("message", "Patient not found");
                return response;
            }
        } catch (Exception e) {
            log.error("Error updating patient review: {}", e.getMessage(), e);
            response.put("success", false);
            response.put("message", "Error updating review");
            return response;
        }
    }

    /**
     * Delete user's own feedback
     */
    @PostMapping("/api/delete-review")
    @ResponseBody
    public Map<String, Object> deleteMyReview(
            @RequestParam Long feedbackId,
            HttpServletRequest request) {
        Map<String, Object> response = new HashMap<>();
        try {
            // Get current authenticated user
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            String userEmail = authentication.getName();
            
            // Find patient by email
            Optional<Patient> patientOpt = patientRepository.findByEmail(userEmail);
            
            if (patientOpt.isPresent()) {
                Patient patient = patientOpt.get();
                
                // Delete feedback
                boolean success = feedbackService.deleteFeedbackByUser(feedbackId, patient.getId());
                
                if (success) {
                    response.put("success", true);
                    response.put("message", "Review deleted successfully!");
                } else {
                    response.put("success", false);
                    response.put("message", "Failed to delete review. You can only delete your own reviews.");
                }
                
                return response;
            } else {
                response.put("success", false);
                response.put("message", "Patient not found");
                return response;
            }
        } catch (Exception e) {
            log.error("Error deleting patient review: {}", e.getMessage(), e);
            response.put("success", false);
            response.put("message", "Error deleting review");
            return response;
        }
    }

    /**
     * Generate batch/lot number for vaccination
     */
    private String generateBatchLotNumber(String vaccineName, LocalDate vaccinationDate) {
        // Simple batch number generation - you can customize this logic
        String vaccineCode = vaccineName.replaceAll("[^A-Za-z0-9]", "").substring(0, Math.min(3, vaccineName.length()));
        String dateCode = vaccinationDate.toString().replaceAll("-", "");
        return vaccineCode + "-" + dateCode + "-" + System.currentTimeMillis() % 10000;
    }
    
    /**
     * Add user information for contact form auto-fill
     */
    private void addContactFormUserInfo(Model model, Patient patient) {
        try {
            model.addAttribute("isLoggedIn", true);
            model.addAttribute("userEmail", patient.getEmail());
            model.addAttribute("userName", patient.getPatientName() != null ? patient.getPatientName() : extractUserNameFromEmail(patient.getEmail()));
            model.addAttribute("userPhone", patient.getContact() != null ? patient.getContact() : "");
            
        } catch (Exception e) {
            log.error("Error adding contact form user info: {}", e.getMessage());
            // Set default values
            model.addAttribute("isLoggedIn", true);
            model.addAttribute("userEmail", patient.getEmail());
            model.addAttribute("userName", extractUserNameFromEmail(patient.getEmail()));
            model.addAttribute("userPhone", "");
        }
    }
    
    /**
     * Extract a user-friendly name from email address
     */
    private String extractUserNameFromEmail(String email) {
        if (email == null || email.isEmpty()) {
            return "";
        }
        
        // Extract the part before @ and replace dots/underscores with spaces
        String namePart = email.split("@")[0];
        return namePart.replace(".", " ").replace("_", " ").replace("-", " ");
    }
}