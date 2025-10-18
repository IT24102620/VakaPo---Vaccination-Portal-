package lk.vakapo.vakapo.UserManagement.controller;

import lk.vakapo.vakapo.UserManagement.model.*;
import lk.vakapo.vakapo.UserManagement.repository.*;
import lk.vakapo.vakapo.MailManagement.EmailService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Controller
@RequestMapping("/admin/user-management")
@RequiredArgsConstructor
@Slf4j
public class UserManagementController {

    private final UserRepository userRepository;
    private final StaffRepository staffRepository;
    private final HospitalRepository hospitalRepository;
    private final ClinicRepository clinicRepository;
    private final PatientRepository patientRepository;
    private final EmailService emailService;

    /**
     * Display user management page
     */
    @GetMapping
    public String userManagementPage(Model model) {
        try {
            log.info("Loading user management page");
            
            // Get all users by type
            List<Staff> doctors = staffRepository.findByRole("Doctor");
            log.info("Found {} doctors", doctors.size());
            
            List<Staff> nurses = staffRepository.findByRole("Nurse");
            log.info("Found {} nurses", nurses.size());
            
            List<Hospital> hospitals = hospitalRepository.findAll();
            log.info("Found {} hospitals", hospitals.size());
            
            List<Clinic> clinics = clinicRepository.findAll();
            log.info("Found {} clinics", clinics.size());
            
            List<Patient> patients = patientRepository.findAll();
            log.info("Found {} patients", patients.size());
            
            List<UserAccount> allUsers = userRepository.findAll();
            log.info("Found {} total users", allUsers.size());
            
            // Calculate statistics
            long totalUsers = allUsers.size();
            long totalDoctors = doctors.size();
            long totalNurses = nurses.size();
            long totalHospitals = hospitals.size();
            long totalClinics = clinics.size();
            long totalPatients = patients.size();
            
            // Add data to model
            model.addAttribute("doctors", doctors);
            model.addAttribute("nurses", nurses);
            model.addAttribute("hospitals", hospitals);
            model.addAttribute("clinics", clinics);
            model.addAttribute("patients", patients);
            model.addAttribute("totalUsers", totalUsers);
            model.addAttribute("totalDoctors", totalDoctors);
            model.addAttribute("totalNurses", totalNurses);
            model.addAttribute("totalHospitals", totalHospitals);
            model.addAttribute("totalClinics", totalClinics);
            model.addAttribute("totalPatients", totalPatients);
            
            log.info("User management page loaded successfully with {} total users", totalUsers);
            log.info("Returning template: admin/userManagement/UserManagement");
            return "admin/userManagement/UserManagement";
            
        } catch (Exception e) {
            log.error("Error loading user management page: {}", e.getMessage(), e);
            model.addAttribute("error", "Failed to load user management page");
            log.info("Returning template with error: admin/userManagement/UserManagement");
            return "admin/userManagement/UserManagement";
        }
    }
    
    /**
     * Test endpoint to check if controller is working
     */
    @GetMapping("/test")
    @ResponseBody
    public String testEndpoint() {
        log.info("Test endpoint accessed");
        return "User Management Controller is working!";
    }
    
    /**
     * Simple test endpoint without dependencies
     */
    @GetMapping("/simple-test")
    @ResponseBody
    public String simpleTest() {
        return "Simple test - no dependencies";
    }
    
    /**
     * Simple template test without data dependencies
     */
    @GetMapping("/simple-template")
    public String simpleTemplate() {
        log.info("Loading simple template");
        return "admin/userManagement/SimpleTest";
    }
    
    /**
     * Simple user management page without complex styling
     */
    @GetMapping("/simple")
    public String simpleUserManagement(Model model) {
        try {
            log.info("Loading simple user management page");
            
            // Get all users by type
            List<Staff> doctors = staffRepository.findByRole("Doctor");
            List<Staff> nurses = staffRepository.findByRole("Nurse");
            List<Hospital> hospitals = hospitalRepository.findAll();
            List<Clinic> clinics = clinicRepository.findAll();
            List<Patient> patients = patientRepository.findAll();
            List<UserAccount> allUsers = userRepository.findAll();
            
            // Add data to model
            model.addAttribute("doctors", doctors);
            model.addAttribute("nurses", nurses);
            model.addAttribute("hospitals", hospitals);
            model.addAttribute("clinics", clinics);
            model.addAttribute("patients", patients);
            model.addAttribute("totalUsers", allUsers.size());
            model.addAttribute("totalDoctors", doctors.size());
            model.addAttribute("totalNurses", nurses.size());
            model.addAttribute("totalHospitals", hospitals.size());
            model.addAttribute("totalClinics", clinics.size());
            model.addAttribute("totalPatients", patients.size());
            
            log.info("Simple user management page loaded successfully");
            return "admin/userManagement/UserManagementSimple";
            
        } catch (Exception e) {
            log.error("Error loading simple user management page: {}", e.getMessage(), e);
            model.addAttribute("error", "Failed to load user management page: " + e.getMessage());
            return "admin/userManagement/UserManagementSimple";
        }
    }
    
    /**
     * Test template endpoint
     */
    @GetMapping("/test-template")
    public String testTemplate(Model model) {
        try {
            log.info("Loading test template");
            
            // Get minimal data
            List<Staff> doctors = staffRepository.findByRole("Doctor");
            List<Staff> nurses = staffRepository.findByRole("Nurse");
            List<Hospital> hospitals = hospitalRepository.findAll();
            List<Clinic> clinics = clinicRepository.findAll();
            List<Patient> patients = patientRepository.findAll();
            List<UserAccount> allUsers = userRepository.findAll();
            
            // Add data to model
            model.addAttribute("doctors", doctors);
            model.addAttribute("nurses", nurses);
            model.addAttribute("hospitals", hospitals);
            model.addAttribute("clinics", clinics);
            model.addAttribute("patients", patients);
            model.addAttribute("totalUsers", allUsers.size());
            model.addAttribute("totalDoctors", doctors.size());
            model.addAttribute("totalNurses", nurses.size());
            model.addAttribute("totalHospitals", hospitals.size());
            model.addAttribute("totalClinics", clinics.size());
            model.addAttribute("totalPatients", patients.size());
            
            log.info("Test template loaded successfully");
            return "admin/userManagement/UserManagementTest";
            
        } catch (Exception e) {
            log.error("Error loading test template: {}", e.getMessage(), e);
            model.addAttribute("error", "Failed to load test template");
            return "admin/userManagement/UserManagementTest";
        }
    }

    /**
     * Delete user account
     */
    @PostMapping("/delete")
    @ResponseBody
    public ResponseEntity<?> deleteUser(@RequestBody Map<String, String> request) {
        try {
            String email = request.get("email");
            String userType = request.get("userType");
            
            if (email == null || userType == null) {
                return ResponseEntity.badRequest()
                    .body("{\"error\": \"Email and user type are required\"}");
            }
            
            log.info("Deleting user: {} of type: {}", email, userType);
            
            boolean deleted = false;
            String userName = "";
            
            switch (userType.toLowerCase()) {
                case "doctor":
                    deleted = deleteDoctor(email);
                    userName = getDoctorName(email);
                    break;
                case "nurse":
                    deleted = deleteNurse(email);
                    userName = getNurseName(email);
                    break;
                case "hospital":
                    deleted = deleteHospital(email);
                    userName = getHospitalName(email);
                    break;
                case "clinic":
                    deleted = deleteClinic(email);
                    userName = getClinicName(email);
                    break;
                case "patient":
                    deleted = deletePatient(email);
                    userName = getPatientName(email);
                    break;
                default:
                    return ResponseEntity.badRequest()
                        .body("{\"error\": \"Invalid user type\"}");
            }
            
            if (deleted) {
                // Send deletion notification email
                try {
                    sendDeletionNotificationEmail(email, userName, userType);
                } catch (Exception e) {
                    log.warn("Failed to send deletion notification email to {}: {}", email, e.getMessage());
                    // Don't fail the deletion if email fails
                }
                
                // Delete from Users table
                Optional<UserAccount> userAccount = userRepository.findByEmail(email);
                if (userAccount.isPresent()) {
                    userRepository.delete(userAccount.get());
                    log.info("Deleted user account for email: {}", email);
                }
                
                Map<String, Object> response = new HashMap<>();
                response.put("success", true);
                response.put("message", String.format("%s account deleted successfully. User has been notified via email.", 
                    Character.toUpperCase(userType.charAt(0)) + userType.substring(1)));
                
                return ResponseEntity.ok(response);
            } else {
                return ResponseEntity.badRequest()
                    .body("{\"error\": \"User not found or could not be deleted\"}");
            }
            
        } catch (Exception e) {
            log.error("Error deleting user: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError()
                .body("{\"error\": \"Failed to delete user: " + e.getMessage() + "\"}");
        }
    }

    // Private helper methods for deleting different user types

    private boolean deleteDoctor(String email) {
        try {
            Optional<Staff> doctor = staffRepository.findByEmail(email);
            if (doctor.isPresent()) {
                staffRepository.delete(doctor.get());
                log.info("Deleted doctor with email: {}", email);
                return true;
            }
            return false;
        } catch (Exception e) {
            log.error("Error deleting doctor {}: {}", email, e.getMessage());
            return false;
        }
    }

    private boolean deleteNurse(String email) {
        try {
            Optional<Staff> nurse = staffRepository.findByEmail(email);
            if (nurse.isPresent()) {
                staffRepository.delete(nurse.get());
                log.info("Deleted nurse with email: {}", email);
                return true;
            }
            return false;
        } catch (Exception e) {
            log.error("Error deleting nurse {}: {}", email, e.getMessage());
            return false;
        }
    }

    private boolean deleteHospital(String email) {
        try {
            Optional<Hospital> hospital = hospitalRepository.findByEmail(email);
            if (hospital.isPresent()) {
                hospitalRepository.delete(hospital.get());
                log.info("Deleted hospital with email: {}", email);
                return true;
            }
            return false;
        } catch (Exception e) {
            log.error("Error deleting hospital {}: {}", email, e.getMessage());
            return false;
        }
    }

    private boolean deleteClinic(String email) {
        try {
            Optional<Clinic> clinic = clinicRepository.findByEmail(email);
            if (clinic.isPresent()) {
                clinicRepository.delete(clinic.get());
                log.info("Deleted clinic with email: {}", email);
                return true;
            }
            return false;
        } catch (Exception e) {
            log.error("Error deleting clinic {}: {}", email, e.getMessage());
            return false;
        }
    }

    private boolean deletePatient(String email) {
        try {
            Optional<Patient> patient = patientRepository.findByEmail(email);
            if (patient.isPresent()) {
                patientRepository.delete(patient.get());
                log.info("Deleted patient with email: {}", email);
                return true;
            }
            return false;
        } catch (Exception e) {
            log.error("Error deleting patient {}: {}", email, e.getMessage());
            return false;
        }
    }

    // Helper methods to get user names for email notifications

    private String getDoctorName(String email) {
        try {
            Optional<Staff> doctor = staffRepository.findByEmail(email);
            return doctor.isPresent() ? doctor.get().getName() : "Doctor";
        } catch (Exception e) {
            return "Doctor";
        }
    }

    private String getNurseName(String email) {
        try {
            Optional<Staff> nurse = staffRepository.findByEmail(email);
            return nurse.isPresent() ? nurse.get().getName() : "Nurse";
        } catch (Exception e) {
            return "Nurse";
        }
    }

    private String getHospitalName(String email) {
        try {
            Optional<Hospital> hospital = hospitalRepository.findByEmail(email);
            return hospital.isPresent() ? hospital.get().getId() : "Hospital";
        } catch (Exception e) {
            return "Hospital";
        }
    }

    private String getClinicName(String email) {
        try {
            Optional<Clinic> clinic = clinicRepository.findByEmail(email);
            return clinic.isPresent() ? clinic.get().getId() : "Clinic";
        } catch (Exception e) {
            return "Clinic";
        }
    }

    private String getPatientName(String email) {
        try {
            Optional<Patient> patient = patientRepository.findByEmail(email);
            return patient.isPresent() ? 
                (patient.get().getPatientName() != null ? patient.get().getPatientName() : "Patient") : "Patient";
        } catch (Exception e) {
            return "Patient";
        }
    }

    /**
     * Send deletion notification email
     */
    private void sendDeletionNotificationEmail(String email, String userName, String userType) {
        try {
            String subject = "VakaPo Account Deletion Notification";
            String message = String.format(
                "Dear %s,\n\n" +
                "Your %s account with VakaPo has been deleted by the system administrator.\n\n" +
                "Account Details:\n" +
                "- Email: %s\n" +
                "- User Type: %s\n" +
                "- Deletion Date: %s\n\n" +
                "If you believe this is an error, please contact the VakaPo support team.\n\n" +
                "Thank you for using VakaPo.\n\n" +
                "Best regards,\n" +
                "VakaPo Administration Team",
                userName,
                userType,
                email,
                userType,
                LocalDateTime.now().toString()
            );
            
            emailService.sendSimpleEmail(email, subject, message);
            log.info("Deletion notification email sent to: {}", email);
            
        } catch (Exception e) {
            log.error("Failed to send deletion notification email to {}: {}", email, e.getMessage());
            throw e;
        }
    }
}
