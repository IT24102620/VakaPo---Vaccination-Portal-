package lk.vakapo.vakapo.UserManagement.controller;

import lk.vakapo.vakapo.UserManagement.model.UserAccount;
import lk.vakapo.vakapo.UserManagement.model.Hospital;
import lk.vakapo.vakapo.UserManagement.model.Appointment;
import lk.vakapo.vakapo.UserManagement.model.Patient;
import lk.vakapo.vakapo.UserManagement.model.Staff;
import lk.vakapo.vakapo.UserManagement.model.VaccinationHistory;
import lk.vakapo.vakapo.UserManagement.repository.UserRepository;
import lk.vakapo.vakapo.UserManagement.repository.AppointmentRepository;
import lk.vakapo.vakapo.UserManagement.repository.PatientRepository;
import lk.vakapo.vakapo.UserManagement.repository.StaffRepository;
import lk.vakapo.vakapo.UserManagement.repository.VaccinationHistoryRepository;
import lk.vakapo.vakapo.UserManagement.repository.FeedbackRepository;
import lk.vakapo.vakapo.UserManagement.repository.HospitalRepository;
import lk.vakapo.vakapo.UserManagement.service.DoctorAppointmentService;
import lk.vakapo.vakapo.UserManagement.service.FeedbackService;
import lk.vakapo.vakapo.UserManagement.service.AccountDeletionService;
import lk.vakapo.vakapo.UserManagement.model.Feedback;
import lk.vakapo.vakapo.MailManagement.EmailService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import jakarta.servlet.http.HttpServletRequest;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Controller
@RequiredArgsConstructor
@Slf4j
public class DoctorLandingController {

    private final UserRepository userRepository;
    private final DoctorAppointmentService doctorAppointmentService;
    private final AppointmentRepository appointmentRepository;
    private final PatientRepository patientRepository;
    private final AccountDeletionService accountDeletionService;
    private final StaffRepository staffRepository;
    private final VaccinationHistoryRepository vaccinationHistoryRepository;
    private final FeedbackService feedbackService;
    private final FeedbackRepository feedbackRepository;
    private final HospitalRepository hospitalRepository;
    private final EmailService emailService;

    @GetMapping("/doctor/landing")
    public String doctorLanding(Model model) {
        try {
            // Get current authenticated user
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            String userEmail = authentication.getName();
            
            log.info("Loading doctor landing page for: {}", userEmail);
            
            // Find user account
            Optional<UserAccount> userOpt = userRepository.findByEmail(userEmail);
            
            if (userOpt.isPresent()) {
                UserAccount user = userOpt.get();
                
                // Verify this is a doctor (ID starts with "Vak D ")
                if (!isDoctor(user.getId())) {
                    log.error("Access denied - user is not a doctor: {}", userEmail);
                    return "error/403";
                }
                
                // Add user data to model
                model.addAttribute("doctorId", user.getId());
                model.addAttribute("doctorEmail", user.getEmail());
                model.addAttribute("doctorName", extractDoctorName(user.getId()));
                
                // Add user information for contact form auto-fill
                addContactFormUserInfo(model, user);
                
                // Add approved feedback for reviews section
                model.addAttribute("reviews", feedbackService.getApprovedFeedbackForLanding());
                
                log.info("Doctor landing page loaded successfully for: {}", userEmail);
                return "doctorPage/landingPage/DoctorLandingPage";
            } else {
                log.error("User not found for email: {}", userEmail);
                return "error/404";
            }
        } catch (Exception e) {
            log.error("Error loading doctor landing page: {}", e.getMessage(), e);
            return "error/500";
        }
    }

    @GetMapping("/doctor/profile")
    public String doctorProfile(Model model) {
        try {
            // Get current authenticated user
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            String userEmail = authentication.getName();
            
            log.info("Loading doctor profile for: {}", userEmail);
            
            // Find user account
            Optional<UserAccount> userOpt = userRepository.findByEmail(userEmail);
            
            if (userOpt.isPresent()) {
                UserAccount user = userOpt.get();
                
                // Verify this is a doctor
                if (!isDoctor(user.getId())) {
                    log.error("Access denied - user is not a doctor: {}", userEmail);
                    return "error/403";
                }
                
                // Find staff details
                Optional<Staff> staffOpt = staffRepository.findByEmail(userEmail);
                if (staffOpt.isPresent()) {
                    Staff staff = staffOpt.get();
                    
                    // Add staff data to model
                    model.addAttribute("staffId", user.getId()); // Use UserAccount ID instead of Staff ID
                    model.addAttribute("staffName", staff.getName());
                    model.addAttribute("staffEmail", staff.getEmail());
                    model.addAttribute("staffContact", staff.getContact());
                    model.addAttribute("staffRole", staff.getRole());
                    model.addAttribute("staffQualifications", staff.getQualifications());
                    model.addAttribute("staffSpecialization", staff.getSpecialization());
                    model.addAttribute("staffCreatedAt", staff.getCreatedAt());
                    model.addAttribute("staffUpdatedAt", staff.getUpdatedAt());
                }
                
                // Add user data to model (for backward compatibility)
                model.addAttribute("doctorId", user.getId());
                model.addAttribute("doctorEmail", user.getEmail());
                model.addAttribute("doctorName", extractDoctorName(user.getId()));
                
                log.info("Doctor profile loaded successfully for: {}", userEmail);
                return "doctorPage/ProfilePage/DoctorProfilePage";
            } else {
                log.error("User not found for email: {}", userEmail);
                return "error/404";
            }
        } catch (Exception e) {
            log.error("Error loading doctor profile: {}", e.getMessage(), e);
            return "error/500";
        }
    }

    /**
     * Update doctor profile
     */
    @PostMapping("/doctor/profile/update")
    public String updateProfile(
            @RequestParam String name,
            @RequestParam String email,
            @RequestParam String contact,
            @RequestParam(required = false) String newPassword,
            RedirectAttributes redirectAttributes) {
        try {
            // Get current authenticated user
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            String userEmail = authentication.getName();
            
            log.info("Updating profile for doctor: {}", userEmail);
            
            // Find staff by email
            Optional<Staff> staffOpt = staffRepository.findByEmail(userEmail);
            
            if (staffOpt.isPresent()) {
                Staff staff = staffOpt.get();
                
                // Store original email for comparison
                String originalEmail = staff.getEmail();
                
                // Update staff data
                staff.setName(name);
                staff.setEmail(email);
                staff.setContact(contact);
                
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
                
                // Update password if provided
                if (newPassword != null && !newPassword.trim().isEmpty()) {
                    Optional<UserAccount> userOpt = userRepository.findByEmail(email);
                    if (userOpt.isPresent()) {
                        UserAccount userAccount = userOpt.get();
                        userAccount.setPassword(newPassword.trim());
                        userRepository.save(userAccount);
                        log.info("Password updated for doctor: {}", email);
                    }
                }
                
                // Save staff
                staffRepository.save(staff);
                
                log.info("Doctor profile updated successfully for: {}", email);
                redirectAttributes.addFlashAttribute("successMessage", "Profile updated successfully!");
                return "redirect:/doctor/profile";
            } else {
                log.error("Staff not found for email: {}", userEmail);
                redirectAttributes.addFlashAttribute("errorMessage", "Staff profile not found.");
                return "redirect:/doctor/profile";
            }
        } catch (Exception e) {
            log.error("Error updating doctor profile: {}", e.getMessage(), e);
            redirectAttributes.addFlashAttribute("errorMessage", "Error updating profile. Please try again.");
            return "redirect:/doctor/profile";
        }
    }

    @GetMapping("/doctor/appointments")
    public String doctorAppointments(Model model) {
        try {
            // Get current authenticated user
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            String userEmail = authentication.getName();
            
            log.info("Loading doctor appointments for: {}", userEmail);
            
            // Find user account
            Optional<UserAccount> userOpt = userRepository.findByEmail(userEmail);
            
            if (userOpt.isPresent()) {
                UserAccount user = userOpt.get();
                
                // Verify this is a doctor
                if (!isDoctor(user.getId())) {
                    log.error("Access denied - user is not a doctor: {}", userEmail);
                    return "error/403";
                }
                
                // Add user data to model
                model.addAttribute("doctorId", user.getId());
                model.addAttribute("doctorEmail", user.getEmail());
                model.addAttribute("doctorName", extractDoctorName(user.getId()));
                
                log.info("Doctor appointments loaded successfully for: {}", userEmail);
                return "doctorPage/appointmentPage/DoctorAppointmentPage";
            } else {
                log.error("User not found for email: {}", userEmail);
                return "error/404";
            }
        } catch (Exception e) {
            log.error("Error loading doctor appointments: {}", e.getMessage(), e);
            return "error/500";
        }
    }

    @GetMapping("/doctor/patient-history")
    public String doctorPatientHistory(Model model) {
        try {
            // Get current authenticated user
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            String userEmail = authentication.getName();
            
            log.info("Loading doctor patient history for: {}", userEmail);
            
            // Find user account
            Optional<UserAccount> userOpt = userRepository.findByEmail(userEmail);
            
            if (userOpt.isPresent()) {
                UserAccount user = userOpt.get();
                
                // Verify this is a doctor
                if (!isDoctor(user.getId())) {
                    log.error("Access denied - user is not a doctor: {}", userEmail);
                    return "error/403";
                }
                
                // Add user data to model
                model.addAttribute("doctorId", user.getId());
                model.addAttribute("doctorEmail", user.getEmail());
                model.addAttribute("doctorName", extractDoctorName(user.getId()));
                
                log.info("Doctor patient history loaded successfully for: {}", userEmail);
                return "doctorPage/patientHistory/DoctorPatientHistory";
            } else {
                log.error("User not found for email: {}", userEmail);
                return "error/404";
            }
        } catch (Exception e) {
            log.error("Error loading doctor patient history: {}", e.getMessage(), e);
            return "error/500";
        }
    }

    @GetMapping("/doctor/feedback")
    public String doctorFeedback(Model model) {
        try {
            // Get current authenticated user
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            String userEmail = authentication.getName();
            
            log.info("Loading doctor feedback for: {}", userEmail);
            
            // Find user account
            Optional<UserAccount> userOpt = userRepository.findByEmail(userEmail);
            
            if (userOpt.isPresent()) {
                UserAccount user = userOpt.get();
                
                // Verify this is a doctor
                if (!isDoctor(user.getId())) {
                    log.error("Access denied - user is not a doctor: {}", userEmail);
                    return "error/403";
                }
                
                // Add user data to model
                model.addAttribute("doctorId", user.getId());
                model.addAttribute("doctorEmail", user.getEmail());
                model.addAttribute("doctorName", extractDoctorName(user.getId()));
                
                log.info("Doctor feedback loaded successfully for: {}", userEmail);
                return "doctorPage/feedbackPage/DoctorFeedbackPage";
            } else {
                log.error("User not found for email: {}", userEmail);
                return "error/404";
            }
        } catch (Exception e) {
            log.error("Error loading doctor feedback: {}", e.getMessage(), e);
            return "error/500";
        }
    }

    @PostMapping("/doctor/feedback")
    public String submitDoctorFeedback(
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
            
            log.info("Submitting doctor feedback from: {}", userEmail);
            
            // Find user account
            Optional<UserAccount> userOpt = userRepository.findByEmail(userEmail);
            
            if (userOpt.isPresent()) {
                UserAccount user = userOpt.get();
                
                // Verify this is a doctor
                if (!isDoctor(user.getId())) {
                    log.error("Access denied - user is not a doctor: {}", userEmail);
                    return "error/403";
                }
                
                // Create feedback object
                Feedback feedback = new Feedback();
                feedback.setName(name);
                feedback.setEmail(email);
                feedback.setContactNo(contactno);
                feedback.setRating(rating);
                feedback.setMessage(message);
                feedback.setUserType("DOCTOR");
                feedback.setUserId(user.getId());
                
                // Submit feedback
                log.info("About to submit feedback: {}", feedback);
                Feedback savedFeedback = feedbackService.submitFeedback(feedback);
                log.info("Feedback submitted successfully with ID: {}", savedFeedback.getId());
                
                // Add success message
                model.addAttribute("successMessage", "Thank you for your feedback! It will be reviewed and may appear on our landing pages.");
                model.addAttribute("doctorId", user.getId());
                model.addAttribute("doctorEmail", user.getEmail());
                model.addAttribute("doctorName", extractDoctorName(user.getId()));
                
                log.info("Doctor feedback submitted successfully by: {}", userEmail);
                return "doctorPage/feedbackPage/DoctorFeedbackPage";
            } else {
                log.error("User not found for email: {}", userEmail);
                return "error/404";
            }
        } catch (Exception e) {
            log.error("Error submitting doctor feedback: {}", e.getMessage(), e);
            model.addAttribute("errorMessage", "Error submitting feedback. Please try again.");
            return "doctorPage/feedbackPage/DoctorFeedbackPage";
        }
    }

    /**
     * Get user's own feedback (My Reviews section)
     */
    @GetMapping("/doctor/api/my-reviews")
    @ResponseBody
    public Map<String, Object> getMyReviews(HttpServletRequest request) {
        Map<String, Object> response = new HashMap<>();
        try {
            // Get current authenticated user
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            String userEmail = authentication.getName();
            
            // Find user account
            Optional<UserAccount> userOpt = userRepository.findByEmail(userEmail);
            
            if (userOpt.isPresent()) {
                UserAccount user = userOpt.get();
                
                // Verify this is a doctor
                if (!isDoctor(user.getId())) {
                    response.put("success", false);
                    response.put("message", "Access denied - user is not a doctor");
                    return response;
                }
                
                // Get user's feedback
                List<Feedback> myReviews = feedbackRepository.findByUserIdOrderByCreatedAtDesc(user.getId());
                
                response.put("success", true);
                response.put("data", myReviews);
                response.put("count", myReviews.size());
                
                return response;
            } else {
                response.put("success", false);
                response.put("message", "User not found");
                return response;
            }
        } catch (Exception e) {
            log.error("Error getting doctor reviews: {}", e.getMessage(), e);
            response.put("success", false);
            response.put("message", "Error retrieving reviews");
            return response;
        }
    }

    /**
     * Update user's own feedback
     */
    @PostMapping("/doctor/api/update-review")
    @ResponseBody
    public Map<String, Object> updateMyReview(
            @RequestParam Long feedbackId,
            @RequestParam String name,
            @RequestParam String email,
            @RequestParam String contactno,
            @RequestParam Integer rating,
            @RequestParam String message,
            HttpServletRequest request) {
        Map<String, Object> response = new HashMap<>();
        try {
            // Get current authenticated user
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            String userEmail = authentication.getName();
            
            // Find user account
            Optional<UserAccount> userOpt = userRepository.findByEmail(userEmail);
            
            if (userOpt.isPresent()) {
                UserAccount user = userOpt.get();
                
                // Verify this is a doctor
                if (!isDoctor(user.getId())) {
                    response.put("success", false);
                    response.put("message", "Access denied - user is not a doctor");
                    return response;
                }
                
                // Update feedback
                boolean success = feedbackService.updateFeedback(feedbackId, user.getId(), name, email, contactno, rating, message);
                
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
                response.put("message", "User not found");
                return response;
            }
        } catch (Exception e) {
            log.error("Error updating doctor review: {}", e.getMessage(), e);
            response.put("success", false);
            response.put("message", "Error updating review");
            return response;
        }
    }

    /**
     * Delete user's own feedback
     */
    @PostMapping("/doctor/api/delete-review")
    @ResponseBody
    public Map<String, Object> deleteMyReview(
            @RequestParam Long feedbackId,
            HttpServletRequest request) {
        Map<String, Object> response = new HashMap<>();
        try {
            // Get current authenticated user
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            String userEmail = authentication.getName();
            
            // Find user account
            Optional<UserAccount> userOpt = userRepository.findByEmail(userEmail);
            
            if (userOpt.isPresent()) {
                UserAccount user = userOpt.get();
                
                // Verify this is a doctor
                if (!isDoctor(user.getId())) {
                    response.put("success", false);
                    response.put("message", "Access denied - user is not a doctor");
                    return response;
                }
                
                // Delete feedback
                boolean success = feedbackService.deleteFeedbackByUser(feedbackId, user.getId());
                
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
                response.put("message", "User not found");
                return response;
            }
        } catch (Exception e) {
            log.error("Error deleting doctor review: {}", e.getMessage(), e);
            response.put("success", false);
            response.put("message", "Error deleting review");
            return response;
        }
    }

    @GetMapping("/doctor/change-password")
    public String doctorChangePassword(Model model) {
        try {
            // Get current authenticated user
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            String userEmail = authentication.getName();
            
            log.info("Loading doctor password change for: {}", userEmail);
            
            // Find user account
            Optional<UserAccount> userOpt = userRepository.findByEmail(userEmail);
            
            if (userOpt.isPresent()) {
                UserAccount user = userOpt.get();
                
                // Verify this is a doctor
                if (!isDoctor(user.getId())) {
                    log.error("Access denied - user is not a doctor: {}", userEmail);
                    return "error/403";
                }
                
                // Add user data to model
                model.addAttribute("doctorId", user.getId());
                model.addAttribute("doctorEmail", user.getEmail());
                model.addAttribute("doctorName", extractDoctorName(user.getId()));
                
                log.info("Doctor password change loaded successfully for: {}", userEmail);
                return "doctorPage/PasswordChange/DoctorPasswordChange";
            } else {
                log.error("User not found for email: {}", userEmail);
                return "error/404";
            }
        } catch (Exception e) {
            log.error("Error loading doctor password change: {}", e.getMessage(), e);
            return "error/500";
        }
    }

    /**
     * Check if user ID indicates a doctor
     */
    private boolean isDoctor(String userId) {
        if (userId == null) return false;
        return userId.startsWith("Vak D ");
    }

    /**
     * Extract doctor name from ID by looking up in Staff table
     */
    private String extractDoctorName(String userId) {
        if (userId == null) return "Doctor";
        
        try {
            // Get current authenticated user email
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            String userEmail = authentication.getName();
            
            log.info("Extracting doctor name for user ID: '{}', email: '{}'", userId, userEmail);
            
            // First, try to find any staff record for this email (regardless of approval status)
            Optional<Staff> staffOpt = staffRepository.findByEmail(userEmail);
            if (staffOpt.isPresent()) {
                String doctorName = staffOpt.get().getName();
                log.info("Found doctor name from staff record: '{}' for user ID: '{}'", doctorName, userId);
                return doctorName;
            }
            
            // If no staff record found, try to find accepted doctor invitations
            List<Staff> staffList = staffRepository.findAcceptedDoctorInvitations(userEmail);
            if (!staffList.isEmpty()) {
                String doctorName = staffList.get(0).getName();
                log.info("Found doctor name from accepted invitations: '{}' for user ID: '{}'", doctorName, userId);
                return doctorName;
            }
            
            // If still no record found, try to find any doctor records for this email
            List<Staff> doctorList = staffRepository.findByEmailAndRole(userEmail, "Doctor");
            if (!doctorList.isEmpty()) {
                String doctorName = doctorList.get(0).getName();
                log.info("Found doctor name from any doctor role: '{}' for user ID: '{}'", doctorName, userId);
                return doctorName;
            }
            
            // If no staff record exists, try to create one for this doctor
            log.warn("No staff record found for doctor email: {}, attempting to create one", userEmail);
            String doctorName = createMissingStaffRecord(userEmail, userId);
            if (doctorName != null) {
                log.info("Created missing staff record and found doctor name: '{}' for user ID: '{}'", doctorName, userId);
                return doctorName;
            }
            
            log.warn("Could not create staff record for doctor email: {}, using fallback name", userEmail);
            return "Dr. " + userId.substring(6); // Fallback to ID-based name
            
        } catch (Exception e) {
            log.error("Error extracting doctor name for user ID: {}", userId, e);
            return "Dr. " + userId.substring(6); // Fallback to ID-based name
        }
    }

    /**
     * Create a missing Staff record for an existing doctor UserAccount
     */
    private String createMissingStaffRecord(String userEmail, String userId) {
        try {
            log.info("Creating missing staff record for doctor email: {}, user ID: {}", userEmail, userId);
            
            // Create a basic staff record for this doctor
            Staff staff = new Staff();
            staff.setEmail(userEmail);
            staff.setName("Dr. VakaPo Doctor"); // Use a more meaningful default name
            staff.setContact("N/A"); // Default contact
            staff.setRole("Doctor");
            staff.setQualifications("Medical Doctor"); // Default qualification
            staff.setSpecialization("General Practice"); // Default specialization
            staff.setInstitutionType("Hospital"); // Default institution type
            staff.setInstitutionId("Vak H 0001"); // Default institution ID
            staff.setInvitationAccepted("approved"); // Mark as approved since they can log in
            staff.setInvitationToken(null); // No token needed
            staff.setInvitationSentAt(LocalDateTime.now());
            staff.setInvitationAcceptedAt(LocalDateTime.now());
            staff.setCreatedAt(LocalDateTime.now());
            staff.setUpdatedAt(LocalDateTime.now());
            
            // Save the staff record
            Staff savedStaff = staffRepository.save(staff);
            log.info("Created staff record with ID: {} for doctor: {}", savedStaff.getId(), userEmail);
            
            return savedStaff.getName();
            
        } catch (Exception e) {
            log.error("Error creating missing staff record for doctor: {}", userEmail, e);
            return null;
        }
    }

    /**
     * API endpoint to get hospitals that have invited the doctor
     */
    @GetMapping("/api/doctor/hospitals")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> getDoctorHospitals() {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            String userEmail = authentication.getName();
            
            log.info("API: Getting hospitals for doctor: {}", userEmail);
            
            // Verify this is a doctor
            Optional<UserAccount> userOpt = userRepository.findByEmail(userEmail);
            if (userOpt.isEmpty()) {
                log.error("API: User not found for email: {}", userEmail);
                Map<String, Object> response = new HashMap<>();
                response.put("success", false);
                response.put("error", "User not found");
                return ResponseEntity.badRequest().body(response);
            }
            
            UserAccount user = userOpt.get();
            if (!isDoctor(user.getId())) {
                log.error("API: Access denied - user is not a doctor: {}", userEmail);
                Map<String, Object> response = new HashMap<>();
                response.put("success", false);
                response.put("error", "Access denied - not a doctor");
                return ResponseEntity.badRequest().body(response);
            }
            
            // Get institutions (hospitals and clinics) that have invited this doctor
            List<Map<String, Object>> institutions = doctorAppointmentService.getInstitutionsForDoctor(userEmail);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("hospitals", institutions); // Keep "hospitals" key for backward compatibility
            response.put("institutions", institutions); // Add new "institutions" key
            response.put("count", institutions.size());
            response.put("doctorEmail", userEmail);
            response.put("doctorId", user.getId());
            
            log.info("API: Returning {} institutions for doctor: {}", institutions.size(), userEmail);
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            log.error("Error getting doctor hospitals: {}", e.getMessage(), e);
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("error", "Failed to fetch hospitals: " + e.getMessage());
            return ResponseEntity.internalServerError().body(response);
        }
    }

    /**
     * API endpoint to get appointments for a specific hospital
     */
    @GetMapping("/api/doctor/appointments/{hospitalId}")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> getDoctorAppointments(@PathVariable String hospitalId) {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            String userEmail = authentication.getName();
            
            log.info("API: Getting appointments for doctor: {} at hospital: {}", userEmail, hospitalId);
            
            // Get user account to extract doctor name
            Optional<UserAccount> userOpt = userRepository.findByEmail(userEmail);
            if (userOpt.isEmpty()) {
                Map<String, Object> response = new HashMap<>();
                response.put("success", false);
                response.put("error", "Doctor not found");
                return ResponseEntity.badRequest().body(response);
            }
            
            String doctorName = extractDoctorName(userOpt.get().getId());
            log.info("API: Extracted doctor name: '{}' from user ID: '{}'", doctorName, userOpt.get().getId());
            
            // Determine institution type and get appointments
            List<Appointment> appointments;
            
            // First, try to find if it's a hospital
            Optional<Hospital> hospitalOpt = hospitalRepository.findById(hospitalId);
            if (hospitalOpt.isPresent()) {
                appointments = doctorAppointmentService.getAppointmentsForDoctorAndInstitution(
                    doctorName, hospitalId, "Hospital");
            } else {
                // If not a hospital, try as a clinic
                appointments = doctorAppointmentService.getAppointmentsForDoctorAndInstitution(
                    doctorName, hospitalId, "Clinic");
            }
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("appointments", appointments);
            response.put("count", appointments.size());
            response.put("hospitalId", hospitalId);
            response.put("doctorName", doctorName);
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            log.error("Error getting doctor appointments: {}", e.getMessage(), e);
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("error", "Failed to fetch appointments");
            return ResponseEntity.internalServerError().body(response);
        }
    }

    /**
     * API endpoint to update appointment status
     */
    @PostMapping("/api/doctor/appointments/{appointmentId}/status")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> updateAppointmentStatus(
            @PathVariable Long appointmentId, 
            @RequestParam String status) {
        try {
            log.info("API: Updating appointment {} status to: {}", appointmentId, status);
            
            boolean success = doctorAppointmentService.updateAppointmentStatus(
                appointmentId, status, "doctor");
            
            Map<String, Object> response = new HashMap<>();
            if (success) {
                response.put("success", true);
                response.put("message", "Appointment status updated successfully");
            } else {
                response.put("success", false);
                response.put("error", "Failed to update appointment status");
            }
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            log.error("Error updating appointment status: {}", e.getMessage(), e);
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("error", "Failed to update appointment status");
            return ResponseEntity.internalServerError().body(response);
        }
    }

    /**
     * Temporary debug endpoint to list all appointments
     */
    @GetMapping("/api/debug/appointments")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> debugAllAppointments() {
        try {
            log.info("Debug: Getting all appointments");
            
            // Get all appointments from database
            List<Appointment> allAppointments = doctorAppointmentService.getAllAppointments();
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("totalAppointments", allAppointments.size());
            response.put("appointments", allAppointments);
            response.put("message", "Check server logs for detailed appointment information");
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            log.error("Error in debug endpoint: {}", e.getMessage(), e);
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("error", "Debug endpoint error: " + e.getMessage());
            return ResponseEntity.internalServerError().body(response);
        }
    }

    /**
     * Simple test endpoint to check if the controller is working
     */
    @GetMapping("/api/test")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> testEndpoint() {
        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("message", "API is working");
        response.put("timestamp", System.currentTimeMillis());
        return ResponseEntity.ok(response);
    }

    /**
     * Create test appointments for debugging
     */
    @PostMapping("/api/debug/create-test-appointments")
    @GetMapping("/api/debug/create-test-appointments") // Allow both GET and POST
    @ResponseBody
    public ResponseEntity<Map<String, Object>> createTestAppointments() {
        try {
            log.info("Creating test appointments...");
            
            // Get current doctor info
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            String userEmail = authentication.getName();
            Optional<UserAccount> userOpt = userRepository.findByEmail(userEmail);
            
            if (userOpt.isEmpty()) {
                Map<String, Object> response = new HashMap<>();
                response.put("success", false);
                response.put("error", "User not found");
                return ResponseEntity.badRequest().body(response);
            }
            
            String doctorName = extractDoctorName(userOpt.get().getId());
            String hospitalId = "Vak H 1031"; // From your console logs
            
            // Create test appointments
            List<Appointment> testAppointments = createSampleAppointments(doctorName, hospitalId);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Test appointments created");
            response.put("count", testAppointments.size());
            response.put("doctorName", doctorName);
            response.put("hospitalId", hospitalId);
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            log.error("Error creating test appointments: {}", e.getMessage(), e);
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("error", "Failed to create test appointments: " + e.getMessage());
            return ResponseEntity.internalServerError().body(response);
        }
    }

    /**
     * Fix existing appointment doctor names to match current doctor
     */
    @GetMapping("/api/debug/fix-appointment-doctor-names")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> fixAppointmentDoctorNames() {
        try {
            log.info("Fixing appointment doctor names...");
            
            // Get current doctor info
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            String userEmail = authentication.getName();
            Optional<UserAccount> userOpt = userRepository.findByEmail(userEmail);
            
            if (userOpt.isEmpty()) {
                Map<String, Object> response = new HashMap<>();
                response.put("success", false);
                response.put("error", "User not found");
                return ResponseEntity.badRequest().body(response);
            }
            
            String correctDoctorName = extractDoctorName(userOpt.get().getId());
            String hospitalId = "Vak H 1031";
            
            // Find all appointments for this hospital
            List<Appointment> hospitalAppointments = appointmentRepository.findByInstitutionIdAndType(hospitalId, "Hospital");
            log.info("Found {} appointments for hospital: {}", hospitalAppointments.size(), hospitalId);
            
            int updatedCount = 0;
            for (Appointment appointment : hospitalAppointments) {
                if (!correctDoctorName.equals(appointment.getDoctorName())) {
                    log.info("Updating appointment {} doctor name from '{}' to '{}'", 
                            appointment.getId(), appointment.getDoctorName(), correctDoctorName);
                    appointment.setDoctorName(correctDoctorName);
                    appointmentRepository.save(appointment);
                    updatedCount++;
                }
            }
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Fixed appointment doctor names");
            response.put("updatedCount", updatedCount);
            response.put("correctDoctorName", correctDoctorName);
            response.put("hospitalId", hospitalId);
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            log.error("Error fixing appointment doctor names: {}", e.getMessage(), e);
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("error", "Failed to fix appointment doctor names: " + e.getMessage());
            return ResponseEntity.internalServerError().body(response);
        }
    }

    /**
     * Create sample appointments for testing
     */
    private List<Appointment> createSampleAppointments(String doctorName, String hospitalId) {
        try {
            log.info("Creating test appointments for doctor: {} at hospital: {}", doctorName, hospitalId);
            
            List<Appointment> testAppointments = new ArrayList<>();
            
            // Create test appointment 1
            Appointment appointment1 = new Appointment();
            appointment1.setPatientId("Vak P 1001");
            appointment1.setPatientEmail("patient1@test.com");
            appointment1.setPatientName("John Smith");
            appointment1.setVaccineName("COVID-19 Vaccine");
            appointment1.setInstitutionType("Hospital");
            appointment1.setInstitutionId(hospitalId);
            appointment1.setInstitutionName("Royal Hospital");
            appointment1.setDoctorName(doctorName);
            appointment1.setAppointmentDate(LocalDate.now().plusDays(1));
            appointment1.setTimeSlot("09:00-09:20");
            appointment1.setStatus("scheduled");
            appointment1.setNotes("First dose");
            appointment1.setCreatedAt(LocalDateTime.now());
            appointment1.setUpdatedAt(LocalDateTime.now());
            
            // Create test appointment 2
            Appointment appointment2 = new Appointment();
            appointment2.setPatientId("Vak P 1002");
            appointment2.setPatientEmail("patient2@test.com");
            appointment2.setPatientName("Jane Doe");
            appointment2.setVaccineName("Influenza Vaccine");
            appointment2.setInstitutionType("Hospital");
            appointment2.setInstitutionId(hospitalId);
            appointment2.setInstitutionName("Royal Hospital");
            appointment2.setDoctorName(doctorName);
            appointment2.setAppointmentDate(LocalDate.now().plusDays(2));
            appointment2.setTimeSlot("10:00-10:20");
            appointment2.setStatus("scheduled");
            appointment2.setNotes("Annual flu shot");
            appointment2.setCreatedAt(LocalDateTime.now());
            appointment2.setUpdatedAt(LocalDateTime.now());
            
            // Create test appointment 3
            Appointment appointment3 = new Appointment();
            appointment3.setPatientId("Vak P 1003");
            appointment3.setPatientEmail("patient3@test.com");
            appointment3.setPatientName("Bob Johnson");
            appointment3.setVaccineName("Hepatitis B Vaccine");
            appointment3.setInstitutionType("Hospital");
            appointment3.setInstitutionId(hospitalId);
            appointment3.setInstitutionName("Royal Hospital");
            appointment3.setDoctorName(doctorName);
            appointment3.setAppointmentDate(LocalDate.now().plusDays(3));
            appointment3.setTimeSlot("11:00-11:20");
            appointment3.setStatus("scheduled");
            appointment3.setNotes("Travel vaccination");
            appointment3.setCreatedAt(LocalDateTime.now());
            appointment3.setUpdatedAt(LocalDateTime.now());
            
            // Save appointments to database
            testAppointments.add(appointmentRepository.save(appointment1));
            testAppointments.add(appointmentRepository.save(appointment2));
            testAppointments.add(appointmentRepository.save(appointment3));
            
            log.info("Created {} test appointments successfully", testAppointments.size());
            return testAppointments;
            
        } catch (Exception e) {
            log.error("Error creating test appointments: {}", e.getMessage(), e);
            return new ArrayList<>();
        }
    }

    /**
     * Debug endpoint to show detailed appointment information
     */
    @GetMapping("/api/debug/appointment-details")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> debugAppointmentDetails() {
        try {
            log.info("Getting detailed appointment information...");
            
            // Get current doctor info
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            String userEmail = authentication.getName();
            Optional<UserAccount> userOpt = userRepository.findByEmail(userEmail);
            
            if (userOpt.isEmpty()) {
                Map<String, Object> response = new HashMap<>();
                response.put("success", false);
                response.put("error", "User not found");
                return ResponseEntity.badRequest().body(response);
            }
            
            String doctorName = extractDoctorName(userOpt.get().getId());
            String hospitalId = "Vak H 1031";
            
            // Get all appointments
            List<Appointment> allAppointments = appointmentRepository.findAll();
            log.info("Total appointments in database: {}", allAppointments.size());
            
            // Get hospital appointments
            List<Appointment> hospitalAppointments = appointmentRepository.findByInstitutionIdAndType(hospitalId, "Hospital");
            log.info("Appointments for hospital {}: {}", hospitalId, hospitalAppointments.size());
            
            // Get doctor appointments
            List<Appointment> doctorAppointments = appointmentRepository.findByDoctorNameOrderByAppointmentDateAsc(doctorName);
            log.info("Appointments for doctor {}: {}", doctorName, doctorAppointments.size());
            
            // Get doctor + hospital appointments
            List<Appointment> doctorHospitalAppointments = appointmentRepository.findByDoctorNameAndInstitution(doctorName, hospitalId, "Hospital");
            log.info("Appointments for doctor {} at hospital {}: {}", doctorName, hospitalId, doctorHospitalAppointments.size());
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("doctorName", doctorName);
            response.put("hospitalId", hospitalId);
            response.put("totalAppointments", allAppointments.size());
            response.put("hospitalAppointments", hospitalAppointments.size());
            response.put("doctorAppointments", doctorAppointments.size());
            response.put("doctorHospitalAppointments", doctorHospitalAppointments.size());
            response.put("allAppointments", allAppointments);
            response.put("hospitalAppointmentsList", hospitalAppointments);
            response.put("doctorAppointmentsList", doctorAppointments);
            response.put("doctorHospitalAppointmentsList", doctorHospitalAppointments);
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            log.error("Error getting appointment details: {}", e.getMessage(), e);
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("error", "Failed to get appointment details: " + e.getMessage());
            return ResponseEntity.internalServerError().body(response);
        }
    }

    /**
     * Test endpoint to verify API is working
     */
    @GetMapping("/api/doctor/test-search")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> testSearch() {
        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("message", "API is working");
        response.put("timestamp", System.currentTimeMillis());
        return ResponseEntity.ok(response);
    }

    /**
     * Debug endpoint to check doctor name extraction
     */
    @GetMapping("/api/doctor/debug-name")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> debugDoctorName() {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            String userEmail = authentication.getName();
            
            Optional<UserAccount> userOpt = userRepository.findByEmail(userEmail);
            if (userOpt.isEmpty()) {
                Map<String, Object> response = new HashMap<>();
                response.put("success", false);
                response.put("error", "User not found");
                return ResponseEntity.badRequest().body(response);
            }
            
            UserAccount user = userOpt.get();
            String userId = user.getId();
            String extractedName = extractDoctorName(userId);
            
            // Get all staff records for this email
            Optional<Staff> staffOpt = staffRepository.findByEmail(userEmail);
            List<Staff> acceptedInvitations = staffRepository.findAcceptedDoctorInvitations(userEmail);
            List<Staff> doctorRecords = staffRepository.findByEmailAndRole(userEmail, "Doctor");
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("userId", userId);
            response.put("userEmail", userEmail);
            response.put("extractedName", extractedName);
            response.put("hasStaffRecord", staffOpt.isPresent());
            response.put("staffRecordName", staffOpt.map(Staff::getName).orElse("N/A"));
            response.put("staffRecordRole", staffOpt.map(Staff::getRole).orElse("N/A"));
            response.put("staffRecordStatus", staffOpt.map(Staff::getInvitationAccepted).orElse("N/A"));
            response.put("acceptedInvitationsCount", acceptedInvitations.size());
            response.put("doctorRecordsCount", doctorRecords.size());
            
            if (!acceptedInvitations.isEmpty()) {
                response.put("firstAcceptedName", acceptedInvitations.get(0).getName());
            }
            if (!doctorRecords.isEmpty()) {
                response.put("firstDoctorName", doctorRecords.get(0).getName());
            }
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            log.error("Error in debug doctor name: {}", e.getMessage(), e);
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("error", "Debug error: " + e.getMessage());
            return ResponseEntity.internalServerError().body(response);
        }
    }

    /**
     * Manual endpoint to create missing staff record for current doctor
     */
    @PostMapping("/api/doctor/create-staff-record")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> createStaffRecord() {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            String userEmail = authentication.getName();
            
            Optional<UserAccount> userOpt = userRepository.findByEmail(userEmail);
            if (userOpt.isEmpty()) {
                Map<String, Object> response = new HashMap<>();
                response.put("success", false);
                response.put("error", "User not found");
                return ResponseEntity.badRequest().body(response);
            }
            
            UserAccount user = userOpt.get();
            String userId = user.getId();
            
            // Check if staff record already exists
            Optional<Staff> existingStaff = staffRepository.findByEmail(userEmail);
            if (existingStaff.isPresent()) {
                Map<String, Object> response = new HashMap<>();
                response.put("success", false);
                response.put("error", "Staff record already exists");
                response.put("staffName", existingStaff.get().getName());
                return ResponseEntity.badRequest().body(response);
            }
            
            // Create the missing staff record
            String doctorName = createMissingStaffRecord(userEmail, userId);
            
            Map<String, Object> response = new HashMap<>();
            if (doctorName != null) {
                response.put("success", true);
                response.put("message", "Staff record created successfully");
                response.put("doctorName", doctorName);
                response.put("userId", userId);
                response.put("userEmail", userEmail);
            } else {
                response.put("success", false);
                response.put("error", "Failed to create staff record");
            }
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            log.error("Error creating staff record: {}", e.getMessage(), e);
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("error", "Error creating staff record: " + e.getMessage());
            return ResponseEntity.internalServerError().body(response);
        }
    }

    /**
     * Update doctor name directly
     */
    @PostMapping("/api/doctor/update-name")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> updateDoctorName(@RequestParam String newName) {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            String userEmail = authentication.getName();
            
            log.info("Updating doctor name to '{}' for email: {}", newName, userEmail);
            
            // Find staff record
            Optional<Staff> staffOpt = staffRepository.findByEmail(userEmail);
            if (staffOpt.isEmpty()) {
                Map<String, Object> response = new HashMap<>();
                response.put("success", false);
                response.put("error", "Staff record not found");
                return ResponseEntity.badRequest().body(response);
            }
            
            Staff staff = staffOpt.get();
            String oldName = staff.getName();
            
            // Update the name
            staff.setName(newName);
            staff.setUpdatedAt(LocalDateTime.now());
            staffRepository.save(staff);
            
            log.info("Doctor name updated from '{}' to '{}' for email: {}", oldName, newName, userEmail);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Doctor name updated successfully");
            response.put("oldName", oldName);
            response.put("newName", newName);
            response.put("userEmail", userEmail);
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            log.error("Error updating doctor name: {}", e.getMessage(), e);
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("error", "Error updating doctor name: " + e.getMessage());
            return ResponseEntity.internalServerError().body(response);
        }
    }

    /**
     * Quick fix endpoint to update doctor name from ID-based to proper name
     */
    @PostMapping("/api/doctor/fix-name")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> fixDoctorName() {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            String userEmail = authentication.getName();
            
            log.info("Fixing doctor name for email: {}", userEmail);
            
            // Find staff record
            Optional<Staff> staffOpt = staffRepository.findByEmail(userEmail);
            if (staffOpt.isEmpty()) {
                Map<String, Object> response = new HashMap<>();
                response.put("success", false);
                response.put("error", "Staff record not found");
                return ResponseEntity.badRequest().body(response);
            }
            
            Staff staff = staffOpt.get();
            String oldName = staff.getName();
            
            // Check if the name is ID-based (like "Dr. 1050")
            if (oldName.matches("Dr\\. \\d+")) {
                // Update to a more meaningful name
                staff.setName("Dr. VakaPo Doctor");
                staff.setUpdatedAt(LocalDateTime.now());
                staffRepository.save(staff);
                
                log.info("Fixed doctor name from '{}' to 'Dr. VakaPo Doctor' for email: {}", oldName, userEmail);
                
                Map<String, Object> response = new HashMap<>();
                response.put("success", true);
                response.put("message", "Doctor name fixed successfully");
                response.put("oldName", oldName);
                response.put("newName", "Dr. VakaPo Doctor");
                response.put("userEmail", userEmail);
                
                return ResponseEntity.ok(response);
            } else {
                Map<String, Object> response = new HashMap<>();
                response.put("success", false);
                response.put("message", "Doctor name is already set to a proper name");
                response.put("currentName", oldName);
                response.put("userEmail", userEmail);
                
                return ResponseEntity.ok(response);
            }
            
        } catch (Exception e) {
            log.error("Error fixing doctor name: {}", e.getMessage(), e);
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("error", "Error fixing doctor name: " + e.getMessage());
            return ResponseEntity.internalServerError().body(response);
        }
    }

    /**
     * Simple test endpoint to check all appointments
     */
    @GetMapping("/api/doctor/test-appointments")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> testAppointments() {
        try {
            // Get all appointments
            List<Appointment> allAppointments = appointmentRepository.findAll();
            log.info("Test: Found {} total appointments in database", allAppointments.size());
            
            // Get all appointments for hospitals
            List<Appointment> hospitalAppointments = appointmentRepository.findByInstitutionTypeOrderByAppointmentDateAsc("Hospital");
            log.info("Test: Found {} hospital appointments", hospitalAppointments.size());
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("totalAppointments", allAppointments.size());
            response.put("hospitalAppointments", hospitalAppointments.size());
            
            // Add sample appointments
            List<Map<String, Object>> sampleAppointments = new ArrayList<>();
            for (Appointment apt : hospitalAppointments) {
                Map<String, Object> aptData = new HashMap<>();
                aptData.put("id", apt.getId());
                aptData.put("doctorName", apt.getDoctorName());
                aptData.put("patientName", apt.getPatientName());
                aptData.put("institutionId", apt.getInstitutionId());
                aptData.put("institutionType", apt.getInstitutionType());
                sampleAppointments.add(aptData);
            }
            response.put("sampleAppointments", sampleAppointments);
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            log.error("Error in test appointments: {}", e.getMessage(), e);
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("error", "Failed to test appointments: " + e.getMessage());
            return ResponseEntity.internalServerError().body(response);
        }
    }

    /**
     * Debug endpoint to check appointment data
     */
    @GetMapping("/api/doctor/debug-appointments/{hospitalId}")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> debugAppointments(@PathVariable String hospitalId) {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            String userEmail = authentication.getName();
            
            log.info("Debug: Checking appointments for doctor: {} at hospital: {}", userEmail, hospitalId);
            
            // Get user account to extract doctor name
            Optional<UserAccount> userOpt = userRepository.findByEmail(userEmail);
            if (userOpt.isEmpty()) {
                Map<String, Object> response = new HashMap<>();
                response.put("success", false);
                response.put("error", "Doctor not found");
                return ResponseEntity.badRequest().body(response);
            }
            
            String doctorName = extractDoctorName(userOpt.get().getId());
            log.info("Debug: Extracted doctor name: '{}' from user ID: '{}'", doctorName, userOpt.get().getId());
            
            // Get all appointments for this hospital
            List<Appointment> allHospitalAppointments = appointmentRepository.findByInstitutionIdAndType(hospitalId, "Hospital");
            log.info("Debug: Found {} total appointments for hospital: {}", allHospitalAppointments.size(), hospitalId);
            
            // Get all appointments for this doctor (any hospital)
            List<Appointment> allDoctorAppointments = appointmentRepository.findByDoctorNameOrderByAppointmentDateAsc(doctorName);
            log.info("Debug: Found {} total appointments for doctor: '{}'", allDoctorAppointments.size(), doctorName);
            
            // Get appointments for this doctor at this hospital
            List<Appointment> doctorHospitalAppointments = appointmentRepository.findByDoctorNameAndInstitution(doctorName, hospitalId, "Hospital");
            log.info("Debug: Found {} appointments for doctor: '{}' at hospital: '{}'", doctorHospitalAppointments.size(), doctorName, hospitalId);
            
            // Build debug response
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("doctorName", doctorName);
            response.put("hospitalId", hospitalId);
            response.put("totalHospitalAppointments", allHospitalAppointments.size());
            response.put("totalDoctorAppointments", allDoctorAppointments.size());
            response.put("doctorHospitalAppointments", doctorHospitalAppointments.size());
            
            // Add sample appointments for debugging
            List<Map<String, Object>> sampleAppointments = new ArrayList<>();
            for (Appointment apt : allHospitalAppointments) {
                Map<String, Object> aptData = new HashMap<>();
                aptData.put("id", apt.getId());
                aptData.put("doctorName", apt.getDoctorName());
                aptData.put("patientName", apt.getPatientName());
                aptData.put("vaccineName", apt.getVaccineName());
                aptData.put("appointmentDate", apt.getAppointmentDate());
                aptData.put("timeSlot", apt.getTimeSlot());
                aptData.put("status", apt.getStatus());
                aptData.put("institutionId", apt.getInstitutionId());
                aptData.put("institutionType", apt.getInstitutionType());
                sampleAppointments.add(aptData);
            }
            response.put("sampleAppointments", sampleAppointments);
            
            // Add all doctor names found in appointments for this hospital
            List<String> allDoctorNames = allHospitalAppointments.stream()
                .map(Appointment::getDoctorName)
                .distinct()
                .collect(java.util.stream.Collectors.toList());
            response.put("allDoctorNamesInHospital", allDoctorNames);
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            log.error("Error in debug appointments: {}", e.getMessage(), e);
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("error", "Failed to debug appointments: " + e.getMessage());
            return ResponseEntity.internalServerError().body(response);
        }
    }

    /**
     * Search for patient by ID and return patient information with vaccination history
     */
    @GetMapping("/api/doctor/patient-search/{patientNumber}")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> searchPatient(@PathVariable String patientNumber) {
        try {
            log.info("Searching for patient with number: {}", patientNumber);
            
            // Validate input
            if (patientNumber == null || patientNumber.trim().isEmpty()) {
                Map<String, Object> response = new HashMap<>();
                response.put("success", false);
                response.put("error", "Patient number is required");
                return ResponseEntity.badRequest().body(response);
            }
            
            // Construct full patient ID
            String patientId = "Vak P " + patientNumber.trim();
            log.info("Searching for patient ID: {}", patientId);
            
            // Find patient by ID
            Optional<Patient> patientOpt = patientRepository.findById(patientId);
            if (patientOpt.isEmpty()) {
                log.info("Patient not found with ID: {}", patientId);
                Map<String, Object> response = new HashMap<>();
                response.put("success", false);
                response.put("error", "Patient not found");
                return ResponseEntity.ok(response);
            }
            
            Patient patient = patientOpt.get();
            log.info("Patient found: {} - {}", patient.getId(), patient.getPatientName());
            
            // Get patient's appointments
            List<Appointment> patientAppointments = appointmentRepository.findByPatientIdOrderByAppointmentDateDesc(patientId);
            log.info("Found {} appointments for patient: {}", patientAppointments.size(), patientId);
            
            // Separate completed and pending appointments
            List<Appointment> completedAppointments = new ArrayList<>();
            List<Appointment> pendingAppointments = new ArrayList<>();
            
            for (Appointment appointment : patientAppointments) {
                if ("completed".equals(appointment.getStatus())) {
                    completedAppointments.add(appointment);
                } else if ("scheduled".equals(appointment.getStatus())) {
                    pendingAppointments.add(appointment);
                }
            }
            
            // Get existing vaccination history from database
            List<VaccinationHistory> existingVaccinationHistory = vaccinationHistoryRepository.findByPatientIdOrderByVaccinationDateDesc(patientId);
            log.info("Found {} existing vaccination history records for patient: {}", existingVaccinationHistory.size(), patientId);
            
            // If no vaccination history exists, create it from appointments
            if (existingVaccinationHistory.isEmpty()) {
                log.info("No vaccination history found, creating from appointments");
                existingVaccinationHistory = createVaccinationHistoryFromAppointments(patientId);
            }
            
            // Separate completed and pending vaccinations based on nurse approval
            List<VaccinationHistory> completedVaccinations = new ArrayList<>();
            List<VaccinationHistory> pendingVaccinations = new ArrayList<>();
            
            for (VaccinationHistory history : existingVaccinationHistory) {
                // Only show as completed if nurse has approved
                if ("approved".equals(history.getNurseApproval())) {
                    completedVaccinations.add(history);
                } else {
                    // Show as pending if nurse approval is pending or rejected
                    pendingVaccinations.add(history);
                }
            }
            
            // If we still don't have data, fall back to creating from appointments
            if (completedVaccinations.isEmpty() && pendingVaccinations.isEmpty()) {
                log.info("No vaccination history data found, creating from appointments as fallback");
                
                // Convert all appointments to pending vaccinations (since no nurse approval yet)
                for (Appointment appointment : completedAppointments) {
                    VaccinationHistory history = new VaccinationHistory();
                    history.setPatientId(appointment.getPatientId());
                    history.setPatientEmail(appointment.getPatientEmail());
                    history.setPatientName(appointment.getPatientName());
                    history.setVaccineName(appointment.getVaccineName());
                    history.setVaccinationDate(appointment.getAppointmentDate());
                    history.setTimeSlot(appointment.getTimeSlot());
                    history.setLocation(appointment.getInstitutionName());
                    history.setInstitutionType(appointment.getInstitutionType());
                    history.setInstitutionId(appointment.getInstitutionId());
                    history.setDoctorName(appointment.getDoctorName());
                    history.setDosageLevel("0ml"); // Default
                    history.setAdditionalNotes(""); // Default
                    history.setNextVaccineDate(null); // Default to no next shot needed
                    history.setNurseApproval("pending"); // Default to pending nurse approval
                    history.setStatus("completed"); // Keep original status
                    
                    // Save to database and add to pending list
                    VaccinationHistory savedHistory = vaccinationHistoryRepository.save(history);
                    pendingVaccinations.add(savedHistory);
                }
                
                // Convert scheduled appointments to pending vaccinations
                for (Appointment appointment : pendingAppointments) {
                    VaccinationHistory history = new VaccinationHistory();
                    history.setPatientId(appointment.getPatientId());
                    history.setPatientEmail(appointment.getPatientEmail());
                    history.setPatientName(appointment.getPatientName());
                    history.setVaccineName(appointment.getVaccineName());
                    history.setVaccinationDate(appointment.getAppointmentDate());
                    history.setTimeSlot(appointment.getTimeSlot());
                    history.setLocation(appointment.getInstitutionName());
                    history.setInstitutionType(appointment.getInstitutionType());
                    history.setInstitutionId(appointment.getInstitutionId());
                    history.setDoctorName(appointment.getDoctorName());
                    history.setDosageLevel("0ml"); // Default
                    history.setAdditionalNotes(""); // Default
                    history.setNextVaccineDate(null); // Default to no next shot needed
                    history.setNurseApproval("pending"); // Default to pending nurse approval
                    history.setStatus("pending"); // Keep original status
                    
                    // Save to database and add to pending list
                    VaccinationHistory savedHistory = vaccinationHistoryRepository.save(history);
                    pendingVaccinations.add(savedHistory);
                }
            }
            
            // Build response
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("patient", Map.of(
                "id", patient.getId(),
                "name", patient.getPatientName() != null ? patient.getPatientName() : "N/A",
                "email", patient.getEmail(),
                "contact", patient.getContact() != null ? patient.getContact() : "N/A",
                "nic", patient.getNic() != null ? patient.getNic() : "N/A",
                "address", patient.getAddress() != null ? patient.getAddress() : "N/A"
            ));
            response.put("completedVaccinations", completedVaccinations);
            response.put("pendingVaccinations", pendingVaccinations);
            response.put("totalVaccinations", existingVaccinationHistory.size());
            response.put("completedCount", completedVaccinations.size());
            response.put("pendingCount", pendingVaccinations.size());
            
            log.info("Patient search successful for: {} - {} completed, {} pending", 
                    patientId, completedVaccinations.size(), pendingVaccinations.size());
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            log.error("Error searching for patient: {}", e.getMessage(), e);
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("error", "Failed to search patient: " + e.getMessage());
            return ResponseEntity.internalServerError().body(response);
        }
    }

    /**
     * Create vaccination history from existing appointments
     */
    private List<VaccinationHistory> createVaccinationHistoryFromAppointments(String patientId) {
        try {
            log.info("Creating vaccination history from appointments for patient: {}", patientId);
            
            List<Appointment> appointments = appointmentRepository.findByPatientIdOrderByAppointmentDateDesc(patientId);
            List<VaccinationHistory> vaccinationHistory = new ArrayList<>();
            
            for (Appointment appointment : appointments) {
                VaccinationHistory history = new VaccinationHistory();
                history.setPatientId(appointment.getPatientId());
                history.setPatientEmail(appointment.getPatientEmail());
                history.setPatientName(appointment.getPatientName());
                history.setVaccineName(appointment.getVaccineName());
                history.setVaccinationDate(appointment.getAppointmentDate());
                history.setTimeSlot(appointment.getTimeSlot());
                history.setLocation(appointment.getInstitutionName());
                history.setInstitutionType(appointment.getInstitutionType());
                history.setInstitutionId(appointment.getInstitutionId());
                history.setDoctorName(appointment.getDoctorName());
                history.setDosageLevel("0ml"); // Default dosage
                history.setAdditionalNotes(""); // Default empty notes
                history.setNextVaccineDate(null); // Default to no next shot needed
                history.setNurseApproval("pending"); // Default to pending nurse approval
                history.setStatus("pending".equals(appointment.getStatus()) ? "pending" : "completed");
                
                vaccinationHistory.add(vaccinationHistoryRepository.save(history));
            }
            
            log.info("Created {} vaccination history records from appointments", vaccinationHistory.size());
            return vaccinationHistory;
            
        } catch (Exception e) {
            log.error("Error creating vaccination history from appointments: {}", e.getMessage(), e);
            return new ArrayList<>();
        }
    }

    /**
     * Update vaccination history (dosage level and notes)
     * This updates the vaccination history record directly
     */
    @PutMapping("/api/doctor/vaccination-history/{historyId}")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> updateVaccinationHistory(
            @PathVariable Long historyId, 
            @RequestBody Map<String, String> updateData) {
        try {
            log.info("Updating vaccination history {} with data: {}", historyId, updateData);
            
            // Find the vaccination history record directly
            Optional<VaccinationHistory> historyOpt = vaccinationHistoryRepository.findById(historyId);
            if (historyOpt.isEmpty()) {
                Map<String, Object> response = new HashMap<>();
                response.put("success", false);
                response.put("error", "Vaccination history not found");
                return ResponseEntity.badRequest().body(response);
            }
            
            VaccinationHistory history = historyOpt.get();
            
            // Update dosage level if provided
            if (updateData.containsKey("dosageLevel")) {
                history.setDosageLevel(updateData.get("dosageLevel"));
            }
            
            // Update additional notes if provided
            if (updateData.containsKey("additionalNotes")) {
                history.setAdditionalNotes(updateData.get("additionalNotes"));
            }
            
            // Update next vaccine date if provided
            if (updateData.containsKey("nextVaccineDate")) {
                String nextDateStr = updateData.get("nextVaccineDate");
                if (nextDateStr != null && !nextDateStr.trim().isEmpty()) {
                    try {
                        history.setNextVaccineDate(LocalDate.parse(nextDateStr));
                    } catch (Exception e) {
                        log.error("Error parsing next vaccine date: {}", nextDateStr, e);
                        // Continue without updating the date
                    }
                } else {
                    history.setNextVaccineDate(null);
                }
            }
            
            // Update nurse approval if provided
            if (updateData.containsKey("nurseApproval")) {
                String nurseApproval = updateData.get("nurseApproval");
                if (nurseApproval != null && !nurseApproval.trim().isEmpty()) {
                    // Validate nurse approval values
                    if ("pending".equals(nurseApproval) || "approved".equals(nurseApproval) || "rejected".equals(nurseApproval)) {
                        history.setNurseApproval(nurseApproval);
                    } else {
                        log.warn("Invalid nurse approval value: {}", nurseApproval);
                    }
                }
            }
            
            // Save updated history
            vaccinationHistoryRepository.save(history);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Vaccination history updated successfully");
            response.put("historyId", historyId);
            
            log.info("Vaccination history updated successfully for history ID {}", historyId);
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            log.error("Error updating vaccination history: {}", e.getMessage(), e);
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("error", "Failed to update vaccination history: " + e.getMessage());
            return ResponseEntity.internalServerError().body(response);
        }
    }

    /**
     * Get recent patient updates (top 5, most recent per patient)
     */
    @GetMapping("/api/doctor/recent-updates")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> getRecentUpdates() {
        try {
            log.info("Fetching recent patient updates");
            
            // Get recent vaccination history updates (last 30 days)
            List<VaccinationHistory> recentUpdates = vaccinationHistoryRepository.findRecentUpdates();
            
            // Group by patient and get most recent update per patient
            Map<String, VaccinationHistory> latestPerPatient = new LinkedHashMap<>();
            for (VaccinationHistory update : recentUpdates) {
                String patientId = update.getPatientId();
                if (!latestPerPatient.containsKey(patientId) || 
                    update.getUpdatedAt().isAfter(latestPerPatient.get(patientId).getUpdatedAt())) {
                    latestPerPatient.put(patientId, update);
                }
            }
            
            // Convert to list and sort by update time (most recent first)
            List<Map<String, Object>> recentUpdatesList = latestPerPatient.values().stream()
                .sorted((a, b) -> b.getUpdatedAt().compareTo(a.getUpdatedAt()))
                .limit(5) // Top 5
                .map(update -> {
                    Map<String, Object> updateInfo = new HashMap<>();
                    updateInfo.put("patientId", update.getPatientId());
                    updateInfo.put("patientName", update.getPatientName());
                    updateInfo.put("vaccineName", update.getVaccineName());
                    updateInfo.put("updatedAt", update.getUpdatedAt());
                    updateInfo.put("timeAgo", getTimeAgo(update.getUpdatedAt()));
                    return updateInfo;
                })
                .collect(java.util.stream.Collectors.toList());
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("recentUpdates", recentUpdatesList);
            
            log.info("Found {} recent patient updates", recentUpdatesList.size());
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            log.error("Error fetching recent updates: {}", e.getMessage(), e);
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("error", "Failed to fetch recent updates: " + e.getMessage());
            return ResponseEntity.internalServerError().body(response);
        }
    }
    
    /**
     * Calculate time ago string
     */
    private String getTimeAgo(LocalDateTime dateTime) {
        LocalDateTime now = LocalDateTime.now();
        long days = java.time.Duration.between(dateTime, now).toDays();
        
        if (days == 0) {
            long hours = java.time.Duration.between(dateTime, now).toHours();
            if (hours == 0) {
                long minutes = java.time.Duration.between(dateTime, now).toMinutes();
                return minutes <= 1 ? "Just now" : minutes + " minutes ago";
            }
            return hours == 1 ? "1 hour ago" : hours + " hours ago";
        } else if (days == 1) {
            return "1 day ago";
        } else if (days < 7) {
            return days + " days ago";
        } else if (days < 30) {
            long weeks = days / 7;
            return weeks == 1 ? "1 week ago" : weeks + " weeks ago";
        } else {
            long months = days / 30;
            return months == 1 ? "1 month ago" : months + " months ago";
        }
    }

    /**
     * Delete doctor account with comprehensive cleanup
     */
    @PostMapping("/doctor/delete-account")
    public String deleteAccount(RedirectAttributes redirectAttributes) {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            String userEmail = authentication.getName();

            log.info("Deleting account for doctor: {}", userEmail);

            // Use the comprehensive deletion service
            String result = accountDeletionService.deleteStaffAccount(userEmail);
            
            log.info("Doctor account deletion completed: {}", result);
            
            // Send account deletion email
            try {
                emailService.sendAccountDeletionEmail(userEmail, "Doctor", "Doctor");
                log.info("Account deletion email sent to doctor: {}", userEmail);
            } catch (Exception e) {
                log.error("Failed to send account deletion email to doctor: {}", userEmail, e);
                // Don't fail the deletion if email fails
            }
            
            redirectAttributes.addFlashAttribute("successMessage", "Your account and all related data have been deleted successfully.");
            
            // Redirect to login page after deletion
            return "redirect:/login?deleted=true";
            
        } catch (Exception e) {
            log.error("Error deleting doctor account: {}", e.getMessage(), e);
            redirectAttributes.addFlashAttribute("errorMessage", "Error deleting account. Please try again.");
            return "redirect:/doctor/landing";
        }
    }
    
    /**
     * Add user information for contact form auto-fill
     */
    private void addContactFormUserInfo(Model model, UserAccount user) {
        try {
            model.addAttribute("isLoggedIn", true);
            model.addAttribute("userEmail", user.getEmail());
            
            // Extract name from email or use doctor name if available
            String userName = extractDoctorName(user.getId());
            if (userName == null || userName.isEmpty() || userName.startsWith("Dr. ")) {
                // If we have a proper doctor name, use it; otherwise extract from email
                if (userName != null && !userName.isEmpty() && !userName.startsWith("Dr. ")) {
                    model.addAttribute("userName", userName);
                } else {
                    model.addAttribute("userName", extractUserNameFromEmail(user.getEmail()));
                }
            } else {
                model.addAttribute("userName", userName);
            }
            
            // Try to get phone number from staff record if available
            Optional<Staff> staffOpt = staffRepository.findByEmail(user.getEmail());
            if (staffOpt.isPresent()) {
                model.addAttribute("userPhone", staffOpt.get().getContact() != null ? staffOpt.get().getContact() : "");
            } else {
                model.addAttribute("userPhone", "");
            }
            
        } catch (Exception e) {
            log.error("Error adding contact form user info: {}", e.getMessage());
            // Set default values
            model.addAttribute("isLoggedIn", true);
            model.addAttribute("userEmail", user.getEmail());
            model.addAttribute("userName", extractUserNameFromEmail(user.getEmail()));
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
