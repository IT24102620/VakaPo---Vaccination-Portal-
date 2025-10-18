package lk.vakapo.vakapo.UserManagement.controller;

import lk.vakapo.vakapo.UserManagement.model.UserAccount;
import lk.vakapo.vakapo.UserManagement.model.Patient;
import lk.vakapo.vakapo.UserManagement.model.VaccinationHistory;
import lk.vakapo.vakapo.UserManagement.model.Staff;
import lk.vakapo.vakapo.UserManagement.model.Appointment;
import lk.vakapo.vakapo.UserManagement.repository.UserRepository;
import lk.vakapo.vakapo.UserManagement.repository.PatientRepository;
import lk.vakapo.vakapo.UserManagement.repository.VaccinationHistoryRepository;
import lk.vakapo.vakapo.UserManagement.repository.StaffRepository;
import lk.vakapo.vakapo.UserManagement.repository.AppointmentRepository;
import lk.vakapo.vakapo.UserManagement.repository.FeedbackRepository;
import lk.vakapo.vakapo.MailManagement.EmailService;
import lk.vakapo.vakapo.PDFManagement.PDFGenerationService;
import lk.vakapo.vakapo.UserManagement.service.FeedbackService;
import lk.vakapo.vakapo.UserManagement.model.Feedback;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.http.ResponseEntity;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import jakarta.servlet.http.HttpServletRequest;
import java.util.Optional;
import java.util.List;
import java.util.ArrayList;
import java.util.Map;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.time.LocalDateTime;
import java.util.stream.Collectors;

@Controller
@RequiredArgsConstructor
@Slf4j
public class NurseLandingController {

    private final UserRepository userRepository;
    private final PatientRepository patientRepository;
    private final VaccinationHistoryRepository vaccinationHistoryRepository;
    private final StaffRepository staffRepository;
    private final AppointmentRepository appointmentRepository;
    private final EmailService emailService;
    private final PDFGenerationService pdfGenerationService;
    private final FeedbackService feedbackService;
    private final FeedbackRepository feedbackRepository;

    @GetMapping("/nurse/landing")
    public String nurseLanding(Model model) {
        try {
            // Get current authenticated user
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            String userEmail = authentication.getName();
            
            log.info("Loading nurse landing page for: {}", userEmail);
            
            // Find user account
            Optional<UserAccount> userOpt = userRepository.findByEmail(userEmail);
            
            if (userOpt.isPresent()) {
                UserAccount user = userOpt.get();
                
                // Verify this is a nurse (ID starts with "Vak N ")
                if (!isNurse(user.getId())) {
                    log.error("Access denied - user is not a nurse: {}", userEmail);
                    return "error/403";
                }
                
                // Add user data to model
                model.addAttribute("nurseId", user.getId());
                model.addAttribute("nurseEmail", user.getEmail());
                model.addAttribute("nurseName", extractNurseName(user.getId()));
                
                // Add user information for contact form auto-fill
                addContactFormUserInfo(model, user);
                
                // Add approved feedback for reviews section
                model.addAttribute("reviews", feedbackService.getApprovedFeedbackForLanding());
                
                log.info("Nurse landing page loaded successfully for: {}", userEmail);
                return "nursePage/landingPage/NurseLandingPage";
            } else {
                log.error("User not found for email: {}", userEmail);
                return "error/404";
            }
        } catch (Exception e) {
            log.error("Error loading nurse landing page: {}", e.getMessage(), e);
            return "error/500";
        }
    }

    @GetMapping("/nurse/profile")
    public String nurseProfile(Model model) {
        try {
            // Get current authenticated user
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            String userEmail = authentication.getName();
            
            log.info("Loading nurse profile for: {}", userEmail);
            
            // Find user account
            Optional<UserAccount> userOpt = userRepository.findByEmail(userEmail);
            
            if (userOpt.isPresent()) {
                UserAccount user = userOpt.get();
                
                // Verify this is a nurse
                if (!isNurse(user.getId())) {
                    log.error("Access denied - user is not a nurse: {}", userEmail);
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
                model.addAttribute("nurseId", user.getId());
                model.addAttribute("nurseEmail", user.getEmail());
                model.addAttribute("nurseName", extractNurseName(user.getId()));
                
                log.info("Nurse profile loaded successfully for: {}", userEmail);
                return "nursePage/ProfilePage/NurseProfilePage";
            } else {
                log.error("User not found for email: {}", userEmail);
                return "error/404";
            }
        } catch (Exception e) {
            log.error("Error loading nurse profile: {}", e.getMessage(), e);
            return "error/500";
        }
    }

    /**
     * Update nurse profile
     */
    @PostMapping("/nurse/profile/update")
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
            
            log.info("Updating profile for nurse: {}", userEmail);
            
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
                        log.info("Password updated for nurse: {}", email);
                    }
                }
                
                // Save staff
                staffRepository.save(staff);
                
                log.info("Nurse profile updated successfully for: {}", email);
                redirectAttributes.addFlashAttribute("successMessage", "Profile updated successfully!");
                return "redirect:/nurse/profile";
            } else {
                log.error("Staff not found for email: {}", userEmail);
                redirectAttributes.addFlashAttribute("errorMessage", "Staff profile not found.");
                return "redirect:/nurse/profile";
            }
        } catch (Exception e) {
            log.error("Error updating nurse profile: {}", e.getMessage(), e);
            redirectAttributes.addFlashAttribute("errorMessage", "Error updating profile. Please try again.");
            return "redirect:/nurse/profile";
        }
    }

    @GetMapping("/nurse/patient-history")
    public String nursePatientHistory(Model model) {
        try {
            // Get current authenticated user
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            String userEmail = authentication.getName();
            
            log.info("Loading nurse patient history for: {}", userEmail);
            
            // Find user account
            Optional<UserAccount> userOpt = userRepository.findByEmail(userEmail);
            
            if (userOpt.isPresent()) {
                UserAccount user = userOpt.get();
                
                // Verify this is a nurse
                if (!isNurse(user.getId())) {
                    log.error("Access denied - user is not a nurse: {}", userEmail);
                    return "error/403";
                }
                
                // Add user data to model
                model.addAttribute("nurseId", user.getId());
                model.addAttribute("nurseEmail", user.getEmail());
                model.addAttribute("nurseName", extractNurseName(user.getId()));
                
                log.info("Nurse patient history loaded successfully for: {}", userEmail);
                return "nursePage/patientHistory/NursePatientHistory";
            } else {
                log.error("User not found for email: {}", userEmail);
                return "error/404";
            }
        } catch (Exception e) {
            log.error("Error loading nurse patient history: {}", e.getMessage(), e);
            return "error/500";
        }
    }

    @GetMapping("/nurse/feedback")
    public String nurseFeedback(Model model) {
        try {
            // Get current authenticated user
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            String userEmail = authentication.getName();
            
            log.info("Loading nurse feedback for: {}", userEmail);
            
            // Find user account
            Optional<UserAccount> userOpt = userRepository.findByEmail(userEmail);
            
            if (userOpt.isPresent()) {
                UserAccount user = userOpt.get();
                
                // Verify this is a nurse
                if (!isNurse(user.getId())) {
                    log.error("Access denied - user is not a nurse: {}", userEmail);
                    return "error/403";
                }
                
                // Add user data to model
                model.addAttribute("nurseId", user.getId());
                model.addAttribute("nurseEmail", user.getEmail());
                model.addAttribute("nurseName", extractNurseName(user.getId()));
                
                log.info("Nurse feedback loaded successfully for: {}", userEmail);
                return "nursePage/feedbackPage/NurseFeedbackPage";
            } else {
                log.error("User not found for email: {}", userEmail);
                return "error/404";
            }
        } catch (Exception e) {
            log.error("Error loading nurse feedback: {}", e.getMessage(), e);
            return "error/500";
        }
    }

    @PostMapping("/nurse/feedback")
    public String submitNurseFeedback(
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
            
            log.info("Submitting nurse feedback from: {}", userEmail);
            
            // Find user account
            Optional<UserAccount> userOpt = userRepository.findByEmail(userEmail);
            
            if (userOpt.isPresent()) {
                UserAccount user = userOpt.get();
                
                // Verify this is a nurse
                if (!isNurse(user.getId())) {
                    log.error("Access denied - user is not a nurse: {}", userEmail);
                    return "error/403";
                }
                
                // Create feedback object
                Feedback feedback = new Feedback();
                feedback.setName(name);
                feedback.setEmail(email);
                feedback.setContactNo(contactno);
                feedback.setRating(rating);
                feedback.setMessage(message);
                feedback.setUserType("NURSE");
                feedback.setUserId(user.getId());
                
                // Submit feedback
                feedbackService.submitFeedback(feedback);
                
                // Add success message
                model.addAttribute("successMessage", "Thank you for your feedback! It will be reviewed and may appear on our landing pages.");
                model.addAttribute("nurseId", user.getId());
                model.addAttribute("nurseEmail", user.getEmail());
                model.addAttribute("nurseName", extractNurseName(user.getId()));
                
                log.info("Nurse feedback submitted successfully by: {}", userEmail);
                return "nursePage/feedbackPage/NurseFeedbackPage";
            } else {
                log.error("User not found for email: {}", userEmail);
                return "error/404";
            }
        } catch (Exception e) {
            log.error("Error submitting nurse feedback: {}", e.getMessage(), e);
            model.addAttribute("errorMessage", "Error submitting feedback. Please try again.");
            return "nursePage/feedbackPage/NurseFeedbackPage";
        }
    }

    /**
     * Get user's own feedback (My Reviews section)
     */
    @GetMapping("/nurse/api/my-reviews")
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
                
                // Verify this is a nurse
                if (!isNurse(user.getId())) {
                    response.put("success", false);
                    response.put("message", "Access denied - user is not a nurse");
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
            log.error("Error getting nurse reviews: {}", e.getMessage(), e);
            response.put("success", false);
            response.put("message", "Error retrieving reviews");
            return response;
        }
    }

    /**
     * Update user's own feedback
     */
    @PostMapping("/nurse/api/update-review")
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
                
                // Verify this is a nurse
                if (!isNurse(user.getId())) {
                    response.put("success", false);
                    response.put("message", "Access denied - user is not a nurse");
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
            log.error("Error updating nurse review: {}", e.getMessage(), e);
            response.put("success", false);
            response.put("message", "Error updating review");
            return response;
        }
    }

    /**
     * Delete user's own feedback
     */
    @PostMapping("/nurse/api/delete-review")
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
                
                // Verify this is a nurse
                if (!isNurse(user.getId())) {
                    response.put("success", false);
                    response.put("message", "Access denied - user is not a nurse");
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
            log.error("Error deleting nurse review: {}", e.getMessage(), e);
            response.put("success", false);
            response.put("message", "Error deleting review");
            return response;
        }
    }

    @GetMapping("/nurse/change-password")
    public String nurseChangePassword(Model model) {
        try {
            // Get current authenticated user
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            String userEmail = authentication.getName();
            
            log.info("Loading nurse password change for: {}", userEmail);
            
            // Find user account
            Optional<UserAccount> userOpt = userRepository.findByEmail(userEmail);
            
            if (userOpt.isPresent()) {
                UserAccount user = userOpt.get();
                
                // Verify this is a nurse
                if (!isNurse(user.getId())) {
                    log.error("Access denied - user is not a nurse: {}", userEmail);
                    return "error/403";
                }
                
                // Add user data to model
                model.addAttribute("nurseId", user.getId());
                model.addAttribute("nurseEmail", user.getEmail());
                model.addAttribute("nurseName", extractNurseName(user.getId()));
                
                log.info("Nurse password change loaded successfully for: {}", userEmail);
                return "nursePage/PasswordChange/NursePasswordChange";
            } else {
                log.error("User not found for email: {}", userEmail);
                return "error/404";
            }
        } catch (Exception e) {
            log.error("Error loading nurse password change: {}", e.getMessage(), e);
            return "error/500";
        }
    }

    /**
     * Check if user ID indicates a nurse
     */
    private boolean isNurse(String userId) {
        if (userId == null) return false;
        return userId.startsWith("Vak N ");
    }

    /**
     * Extract nurse name from ID by looking up in Staff table
     */
    private String extractNurseName(String userId) {
        if (userId == null) return "Nurse";
        
        try {
            // Get current authenticated user email
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            String userEmail = authentication.getName();
            
            // Find the staff record for this nurse
            List<Staff> staffList = staffRepository.findByEmailAndRole(userEmail, "Nurse");
            
            if (!staffList.isEmpty()) {
                // Return the actual nurse name from the first staff record
                String nurseName = staffList.get(0).getName();
                log.info("Found nurse name: '{}' for user ID: '{}'", nurseName, userId);
                return nurseName;
            } else {
                log.warn("No staff record found for nurse email: {}, using fallback name", userEmail);
                return "Nurse " + userId.substring(6); // Fallback to ID-based name
            }
        } catch (Exception e) {
            log.error("Error extracting nurse name for user ID: {}", userId, e);
            return "Nurse " + userId.substring(6); // Fallback to ID-based name
        }
    }

    /**
     * Test endpoint to verify API is working
     */
    @GetMapping("/api/nurse/test-search")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> testSearch() {
        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("message", "Nurse API is working");
        response.put("timestamp", System.currentTimeMillis());
        return ResponseEntity.ok(response);
    }

    /**
     * Debug endpoint to check all appointments and vaccination history
     */
    @GetMapping("/api/nurse/debug-data")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> debugData() {
        try {
            log.info("Debug: Getting all appointments and vaccination history");
            
            // Get all appointments
            List<Appointment> allAppointments = appointmentRepository.findAll();
            log.info("Debug: Found {} total appointments", allAppointments.size());
            
            // Get all vaccination history
            List<VaccinationHistory> allVaccinationHistory = vaccinationHistoryRepository.findAll();
            log.info("Debug: Found {} total vaccination history records", allVaccinationHistory.size());
            
            // Get appointments by status
            List<Appointment> scheduledAppointments = appointmentRepository.findByStatusOrderByAppointmentDateAsc("scheduled");
            List<Appointment> completedAppointments = appointmentRepository.findByStatusOrderByAppointmentDateAsc("completed");
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("totalAppointments", allAppointments.size());
            response.put("totalVaccinationHistory", allVaccinationHistory.size());
            response.put("scheduledAppointments", scheduledAppointments.size());
            response.put("completedAppointments", completedAppointments.size());
            response.put("allAppointments", allAppointments);
            response.put("allVaccinationHistory", allVaccinationHistory);
            response.put("message", "Check server logs for detailed information");
            
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
     * Search for patient by ID and return patient information with vaccination history
     */
    @GetMapping("/api/nurse/patient-search/{patientNumber}")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> searchPatient(@PathVariable String patientNumber) {
        try {
            log.info("Nurse searching for patient with number: {}", patientNumber);
            
            // Validate input
            if (patientNumber == null || patientNumber.trim().isEmpty()) {
                Map<String, Object> response = new HashMap<>();
                response.put("success", false);
                response.put("error", "Patient number is required");
                return ResponseEntity.badRequest().body(response);
            }
            
            // Construct full patient ID
            String patientId = "Vak P " + patientNumber.trim();
            log.info("Nurse searching for patient ID: {}", patientId);
            
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
            
            // Get patient's appointments first
            List<Appointment> patientAppointments = appointmentRepository.findByPatientIdOrderByAppointmentDateDesc(patientId);
            log.info("Found {} appointments for patient: {}", patientAppointments.size(), patientId);
            
            // Get patient's vaccination history from VaccinationHistory table
            List<VaccinationHistory> vaccinationHistory = vaccinationHistoryRepository.findByPatientIdOrderByVaccinationDateDesc(patientId);
            log.info("Found {} vaccination history records for patient: {}", vaccinationHistory.size(), patientId);
            
            // If no vaccination history exists, create it from appointments
            if (vaccinationHistory.isEmpty() && !patientAppointments.isEmpty()) {
                log.info("No vaccination history found, creating from appointments");
                vaccinationHistory = createVaccinationHistoryFromAppointments(patientId);
            }
            
            // Separate completed and pending vaccinations based on nurse approval
            List<VaccinationHistory> completedVaccinations = new ArrayList<>();
            List<VaccinationHistory> pendingVaccinations = new ArrayList<>();
            
            for (VaccinationHistory history : vaccinationHistory) {
                // Only show as completed if nurse has approved
                if ("approved".equals(history.getNurseApproval())) {
                    completedVaccinations.add(history);
                } else {
                    // Show as pending if nurse approval is pending or rejected
                    pendingVaccinations.add(history);
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
            response.put("totalVaccinations", vaccinationHistory.size());
            response.put("completedCount", completedVaccinations.size());
            response.put("pendingCount", pendingVaccinations.size());
            
            log.info("Nurse patient search successful for: {} - {} completed, {} pending", 
                    patientId, completedVaccinations.size(), pendingVaccinations.size());
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            log.error("Error in nurse patient search: {}", e.getMessage(), e);
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("error", "Failed to search patient: " + e.getMessage());
            return ResponseEntity.internalServerError().body(response);
        }
    }

    /**
     * Nurse confirms a vaccination (approves it)
     */
    @PostMapping("/api/nurse/confirm-vaccination/{historyId}")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> confirmVaccination(@PathVariable Long historyId) {
        try {
            log.info("Nurse confirming vaccination for history ID: {}", historyId);

            // Find the vaccination history record
            Optional<VaccinationHistory> historyOpt = vaccinationHistoryRepository.findById(historyId);
            if (historyOpt.isEmpty()) {
                Map<String, Object> response = new HashMap<>();
                response.put("success", false);
                response.put("error", "Vaccination record not found");
                return ResponseEntity.badRequest().body(response);
            }

            VaccinationHistory history = historyOpt.get();

            // Update nurse approval to approved
            history.setNurseApproval("approved");
            history.setStatus("completed");
            vaccinationHistoryRepository.save(history);

            // Also update the corresponding appointment status to completed
            try {
                updateCorrespondingAppointmentStatus(history);
            } catch (Exception e) {
                log.error("Failed to update appointment status: {}", e.getMessage(), e);
                // Don't fail the operation if appointment update fails
            }

            // Send email notification to patient with vaccination card PDF
            try {
                sendVaccinationConfirmationEmailWithCard(history);
            } catch (Exception e) {
                log.error("Failed to send confirmation email with PDF card: {}", e.getMessage(), e);
                // Don't fail the operation if email fails
            }

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Vaccination confirmed successfully");
            response.put("historyId", historyId);

            log.info("Vaccination confirmed successfully for history ID: {}", historyId);
            return ResponseEntity.ok(response);

        } catch (Exception e) {
            log.error("Error confirming vaccination: {}", e.getMessage(), e);
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("error", "Failed to confirm vaccination: " + e.getMessage());
            return ResponseEntity.internalServerError().body(response);
        }
    }

    /**
     * Nurse cancels/rejects a vaccination
     */
    @PostMapping("/api/nurse/cancel-vaccination/{historyId}")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> cancelVaccination(@PathVariable Long historyId) {
        try {
            log.info("Nurse cancelling vaccination for history ID: {}", historyId);
            
            // Find the vaccination history record
            Optional<VaccinationHistory> historyOpt = vaccinationHistoryRepository.findById(historyId);
            if (historyOpt.isEmpty()) {
                Map<String, Object> response = new HashMap<>();
                response.put("success", false);
                response.put("error", "Vaccination record not found");
                return ResponseEntity.badRequest().body(response);
            }
            
            VaccinationHistory history = historyOpt.get();
            
            // Update nurse approval to rejected
            history.setNurseApproval("rejected");
            history.setStatus("cancelled");
            vaccinationHistoryRepository.save(history);
            
            // Also update the corresponding appointment status to cancelled
            try {
                updateCorrespondingAppointmentStatus(history);
            } catch (Exception e) {
                log.error("Failed to update appointment status: {}", e.getMessage(), e);
                // Don't fail the operation if appointment update fails
            }
            
            // Send email notification to patient
            try {
                sendVaccinationCancellationEmail(history);
            } catch (Exception e) {
                log.error("Failed to send cancellation email: {}", e.getMessage(), e);
                // Don't fail the operation if email fails
            }
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Vaccination cancelled successfully");
            response.put("historyId", historyId);
            
            log.info("Vaccination cancelled successfully for history ID: {}", historyId);
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            log.error("Error cancelling vaccination: {}", e.getMessage(), e);
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("error", "Failed to cancel vaccination: " + e.getMessage());
            return ResponseEntity.internalServerError().body(response);
        }
    }

    /**
     * Send vaccination confirmation email to patient with vaccination card PDF
     */
    private void sendVaccinationConfirmationEmailWithCard(VaccinationHistory history) {
        try {
            log.info("Sending vaccination confirmation email with PDF card to: {}", history.getPatientEmail());
            
            // Get patient information for PDF generation
            Optional<Patient> patientOpt = patientRepository.findById(history.getPatientId());
            if (patientOpt.isEmpty()) {
                log.error("Patient not found for ID: {}", history.getPatientId());
                // Fallback to simple email without PDF
                sendVaccinationConfirmationEmail(history);
                return;
            }
            
            Patient patient = patientOpt.get();
            
            // Get nurse name
            String nurseName = extractNurseNameFromHistory(history);
            
            // Generate batch/lot number (you can customize this logic)
            String batchLotNo = generateBatchLotNumber(history.getVaccineName(), history.getVaccinationDate());
            
            // Generate vaccination record card PDF
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
            
            // Send email with PDF attachment
            emailService.sendVaccinationConfirmationWithCard(
                history.getPatientEmail(),
                history.getPatientName(),
                history.getVaccineName(),
                history.getVaccinationDate().toString(),
                history.getLocation(),
                history.getDoctorName(),
                nurseName,
                vaccinationCardPdf
            );
            
            log.info("Vaccination confirmation email with PDF card sent successfully to: {}", history.getPatientEmail());
            
        } catch (Exception e) {
            log.error("Error sending vaccination confirmation email with PDF card: {}", e.getMessage(), e);
            // Fallback to simple email without PDF
            try {
                sendVaccinationConfirmationEmail(history);
            } catch (Exception fallbackError) {
                log.error("Fallback email also failed: {}", fallbackError.getMessage(), fallbackError);
            }
        }
    }

    /**
     * Extract nurse name from vaccination history
     */
    private String extractNurseNameFromHistory(VaccinationHistory history) {
        try {
            // Get current authenticated user email
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            String userEmail = authentication.getName();
            
            // Find the staff record for this nurse
            List<Staff> staffList = staffRepository.findByEmailAndRole(userEmail, "Nurse");
            
            if (!staffList.isEmpty()) {
                return staffList.get(0).getName();
            } else {
                return "Nurse"; // Fallback
            }
        } catch (Exception e) {
            log.error("Error extracting nurse name: {}", e.getMessage(), e);
            return "Nurse"; // Fallback
        }
    }

    /**
     * Generate a batch/lot number for the vaccine
     */
    private String generateBatchLotNumber(String vaccineName, java.time.LocalDate vaccinationDate) {
        try {
            // Create a simple batch number based on vaccine name and date
            String vaccineCode = vaccineName.replaceAll("[^A-Za-z0-9]", "").substring(0, Math.min(3, vaccineName.length()));
            String dateCode = vaccinationDate.toString().replaceAll("-", "");
            return vaccineCode.toUpperCase() + dateCode.substring(2); // Remove year prefix
        } catch (Exception e) {
            log.error("Error generating batch lot number: {}", e.getMessage(), e);
            return "BATCH001"; // Fallback
        }
    }

    /**
     * Send vaccination confirmation email to patient (fallback method)
     */
    private void sendVaccinationConfirmationEmail(VaccinationHistory history) {
        try {
            String subject = "✅ Vaccination Confirmed - VakaPo";
            StringBuilder emailBody = new StringBuilder();
            
            // HTML-styled email body
            emailBody.append("<!DOCTYPE html>");
            emailBody.append("<html><head><meta charset='UTF-8'>");
            emailBody.append("<style>");
            emailBody.append("body { font-family: 'Segoe UI', Tahoma, Geneva, Verdana, sans-serif; line-height: 1.6; color: #333; margin: 0; padding: 20px; background-color: #f8f9fa; }");
            emailBody.append(".container { max-width: 600px; margin: 0 auto; background: white; border-radius: 10px; box-shadow: 0 4px 6px rgba(0,0,0,0.1); overflow: hidden; }");
            emailBody.append(".header { background: linear-gradient(135deg, #667eea 0%, #764ba2 100%); color: white; padding: 30px; text-align: center; }");
            emailBody.append(".header h1 { margin: 0; font-size: 28px; font-weight: 300; }");
            emailBody.append(".header p { margin: 10px 0 0 0; opacity: 0.9; font-size: 16px; }");
            emailBody.append(".content { padding: 30px; }");
            emailBody.append(".greeting { font-size: 18px; margin-bottom: 20px; color: #2c3e50; }");
            emailBody.append(".success-message { background: #d4edda; border: 1px solid #c3e6cb; color: #155724; padding: 15px; border-radius: 5px; margin-bottom: 25px; }");
            emailBody.append(".details-card { background: #f8f9fa; border: 1px solid #e9ecef; border-radius: 8px; padding: 20px; margin: 20px 0; }");
            emailBody.append(".details-title { font-size: 20px; font-weight: 600; color: #495057; margin-bottom: 15px; border-bottom: 2px solid #007bff; padding-bottom: 8px; }");
            emailBody.append(".detail-row { display: flex; margin-bottom: 12px; padding: 8px 0; border-bottom: 1px solid #e9ecef; }");
            emailBody.append(".detail-row:last-child { border-bottom: none; }");
            emailBody.append(".detail-label { font-weight: 600; color: #495057; min-width: 120px; }");
            emailBody.append(".detail-value { color: #6c757d; flex: 1; }");
            emailBody.append(".next-vaccine { background: #fff3cd; border: 1px solid #ffeaa7; color: #856404; padding: 15px; border-radius: 5px; margin: 20px 0; }");
            emailBody.append(".next-vaccine h3 { margin: 0 0 10px 0; color: #856404; }");
            emailBody.append(".notes { background: #e7f3ff; border: 1px solid #b3d9ff; color: #004085; padding: 15px; border-radius: 5px; margin: 15px 0; }");
            emailBody.append(".footer { background: #f8f9fa; padding: 20px; text-align: center; border-top: 1px solid #e9ecef; }");
            emailBody.append(".footer p { margin: 5px 0; color: #6c757d; }");
            emailBody.append(".logo { font-size: 24px; font-weight: bold; color: #007bff; }");
            emailBody.append("</style></head><body>");
            
            emailBody.append("<div class='container'>");
            emailBody.append("<div class='header'>");
            emailBody.append("<h1>🎉 Vaccination Confirmed!</h1>");
            emailBody.append("<p>Your vaccination has been successfully processed</p>");
            emailBody.append("</div>");
            
            emailBody.append("<div class='content'>");
            emailBody.append("<div class='greeting'>Dear ").append(history.getPatientName()).append(",</div>");
            
            emailBody.append("<div class='success-message'>");
            emailBody.append("✅ <strong>Great news!</strong> Your vaccination has been successfully confirmed by our nursing staff and is now part of your medical records.");
            emailBody.append("</div>");
            
            emailBody.append("<div class='details-card'>");
            emailBody.append("<div class='details-title'>📋 Vaccination Details</div>");
            
            emailBody.append("<div class='detail-row'>");
            emailBody.append("<div class='detail-label'>💉 Vaccine:</div>");
            emailBody.append("<div class='detail-value'>").append(history.getVaccineName()).append("</div>");
            emailBody.append("</div>");
            
            emailBody.append("<div class='detail-row'>");
            emailBody.append("<div class='detail-label'>📅 Date:</div>");
            emailBody.append("<div class='detail-value'>").append(history.getVaccinationDate()).append("</div>");
            emailBody.append("</div>");
            
            emailBody.append("<div class='detail-row'>");
            emailBody.append("<div class='detail-label'>🕐 Time:</div>");
            emailBody.append("<div class='detail-value'>").append(history.getTimeSlot()).append("</div>");
            emailBody.append("</div>");
            
            emailBody.append("<div class='detail-row'>");
            emailBody.append("<div class='detail-label'>🏥 Location:</div>");
            emailBody.append("<div class='detail-value'>").append(history.getLocation()).append("</div>");
            emailBody.append("</div>");
            
            emailBody.append("<div class='detail-row'>");
            emailBody.append("<div class='detail-label'>👨‍⚕️ Doctor:</div>");
            emailBody.append("<div class='detail-value'>").append(history.getDoctorName()).append("</div>");
            emailBody.append("</div>");
            
            emailBody.append("<div class='detail-row'>");
            emailBody.append("<div class='detail-label'>💊 Dosage:</div>");
            emailBody.append("<div class='detail-value'>").append(history.getDosageLevel()).append("</div>");
            emailBody.append("</div>");
            
            emailBody.append("</div>");
            
            // Add doctor notes if available
            if (history.getAdditionalNotes() != null && !history.getAdditionalNotes().trim().isEmpty()) {
                emailBody.append("<div class='notes'>");
                emailBody.append("<strong>📝 Doctor's Notes:</strong><br>");
                emailBody.append(history.getAdditionalNotes());
                emailBody.append("</div>");
            }
            
            // Add next vaccination date if specified
            if (history.getNextVaccineDate() != null) {
                emailBody.append("<div class='next-vaccine'>");
                emailBody.append("<h3>📅 Next Vaccination Scheduled</h3>");
                emailBody.append("<p><strong>Date:</strong> ").append(history.getNextVaccineDate()).append("</p>");
                emailBody.append("<p>Please make sure to schedule your next vaccination appointment. You can book it through your VakaPo account or contact your healthcare provider.</p>");
                emailBody.append("</div>");
            }
            
            emailBody.append("<div style='margin: 25px 0; padding: 20px; background: #f8f9fa; border-radius: 8px; text-align: center;'>");
            emailBody.append("<h3 style='color: #495057; margin-top: 0;'>Thank you for choosing VakaPo!</h3>");
            emailBody.append("<p style='color: #6c757d; margin-bottom: 0;'>We're committed to keeping you healthy and safe. Your vaccination record has been updated in our system.</p>");
            emailBody.append("</div>");
            
            emailBody.append("</div>");
            
            emailBody.append("<div class='footer'>");
            emailBody.append("<div class='logo'>VakaPo</div>");
            emailBody.append("<p>Making Vaccination Simple & Secure</p>");
            emailBody.append("<p>Best regards,<br><strong>VakaPo Healthcare Team</strong></p>");
            emailBody.append("<p style='font-size: 12px; color: #adb5bd;'>This is an automated message. Please do not reply to this email.</p>");
            emailBody.append("</div>");
            
            emailBody.append("</div></body></html>");
            
            emailService.sendSimpleEmail(history.getPatientEmail(), subject, emailBody.toString());
            log.info("Styled vaccination confirmation email sent to: {}", history.getPatientEmail());
            
        } catch (Exception e) {
            log.error("Error sending vaccination confirmation email: {}", e.getMessage(), e);
            throw e;
        }
    }

    /**
     * Send vaccination cancellation email to patient
     */
    private void sendVaccinationCancellationEmail(VaccinationHistory history) {
        try {
            String subject = "⚠️ Vaccination Cancelled - VakaPo";
            StringBuilder emailBody = new StringBuilder();
            
            // HTML-styled email body
            emailBody.append("<!DOCTYPE html>");
            emailBody.append("<html><head><meta charset='UTF-8'>");
            emailBody.append("<style>");
            emailBody.append("body { font-family: 'Segoe UI', Tahoma, Geneva, Verdana, sans-serif; line-height: 1.6; color: #333; margin: 0; padding: 20px; background-color: #f8f9fa; }");
            emailBody.append(".container { max-width: 600px; margin: 0 auto; background: white; border-radius: 10px; box-shadow: 0 4px 6px rgba(0,0,0,0.1); overflow: hidden; }");
            emailBody.append(".header { background: linear-gradient(135deg, #ff6b6b 0%, #ee5a24 100%); color: white; padding: 30px; text-align: center; }");
            emailBody.append(".header h1 { margin: 0; font-size: 28px; font-weight: 300; }");
            emailBody.append(".header p { margin: 10px 0 0 0; opacity: 0.9; font-size: 16px; }");
            emailBody.append(".content { padding: 30px; }");
            emailBody.append(".greeting { font-size: 18px; margin-bottom: 20px; color: #2c3e50; }");
            emailBody.append(".cancellation-message { background: #f8d7da; border: 1px solid #f5c6cb; color: #721c24; padding: 15px; border-radius: 5px; margin-bottom: 25px; }");
            emailBody.append(".details-card { background: #f8f9fa; border: 1px solid #e9ecef; border-radius: 8px; padding: 20px; margin: 20px 0; }");
            emailBody.append(".details-title { font-size: 20px; font-weight: 600; color: #495057; margin-bottom: 15px; border-bottom: 2px solid #dc3545; padding-bottom: 8px; }");
            emailBody.append(".detail-row { display: flex; margin-bottom: 12px; padding: 8px 0; border-bottom: 1px solid #e9ecef; }");
            emailBody.append(".detail-row:last-child { border-bottom: none; }");
            emailBody.append(".detail-label { font-weight: 600; color: #495057; min-width: 120px; }");
            emailBody.append(".detail-value { color: #6c757d; flex: 1; }");
            emailBody.append(".reschedule-info { background: #fff3cd; border: 1px solid #ffeaa7; color: #856404; padding: 15px; border-radius: 5px; margin: 20px 0; }");
            emailBody.append(".reschedule-info h3 { margin: 0 0 10px 0; color: #856404; }");
            emailBody.append(".footer { background: #f8f9fa; padding: 20px; text-align: center; border-top: 1px solid #e9ecef; }");
            emailBody.append(".footer p { margin: 5px 0; color: #6c757d; }");
            emailBody.append(".logo { font-size: 24px; font-weight: bold; color: #dc3545; }");
            emailBody.append("</style></head><body>");
            
            emailBody.append("<div class='container'>");
            emailBody.append("<div class='header'>");
            emailBody.append("<h1>⚠️ Vaccination Cancelled</h1>");
            emailBody.append("<p>Your vaccination appointment has been cancelled</p>");
            emailBody.append("</div>");
            
            emailBody.append("<div class='content'>");
            emailBody.append("<div class='greeting'>Dear ").append(history.getPatientName()).append(",</div>");
            
            emailBody.append("<div class='cancellation-message'>");
            emailBody.append("⚠️ <strong>Important Notice:</strong> We regret to inform you that your vaccination appointment has been cancelled by our nursing staff.");
            emailBody.append("</div>");
            
            emailBody.append("<div class='details-card'>");
            emailBody.append("<div class='details-title'>📋 Cancelled Vaccination Details</div>");
            
            emailBody.append("<div class='detail-row'>");
            emailBody.append("<div class='detail-label'>💉 Vaccine:</div>");
            emailBody.append("<div class='detail-value'>").append(history.getVaccineName()).append("</div>");
            emailBody.append("</div>");
            
            emailBody.append("<div class='detail-row'>");
            emailBody.append("<div class='detail-label'>📅 Date:</div>");
            emailBody.append("<div class='detail-value'>").append(history.getVaccinationDate()).append("</div>");
            emailBody.append("</div>");
            
            emailBody.append("<div class='detail-row'>");
            emailBody.append("<div class='detail-label'>🕐 Time:</div>");
            emailBody.append("<div class='detail-value'>").append(history.getTimeSlot()).append("</div>");
            emailBody.append("</div>");
            
            emailBody.append("<div class='detail-row'>");
            emailBody.append("<div class='detail-label'>🏥 Location:</div>");
            emailBody.append("<div class='detail-value'>").append(history.getLocation()).append("</div>");
            emailBody.append("</div>");
            
            emailBody.append("<div class='detail-row'>");
            emailBody.append("<div class='detail-label'>👨‍⚕️ Doctor:</div>");
            emailBody.append("<div class='detail-value'>").append(history.getDoctorName()).append("</div>");
            emailBody.append("</div>");
            
            emailBody.append("</div>");
            
            emailBody.append("<div class='reschedule-info'>");
            emailBody.append("<h3>📅 Next Steps</h3>");
            emailBody.append("<p><strong>Please contact your healthcare provider to reschedule your vaccination appointment.</strong></p>");
            emailBody.append("<p>You can also log into your VakaPo account to book a new appointment at your convenience.</p>");
            emailBody.append("<p>We apologize for any inconvenience this may cause.</p>");
            emailBody.append("</div>");
            
            emailBody.append("<div style='margin: 25px 0; padding: 20px; background: #f8f9fa; border-radius: 8px; text-align: center;'>");
            emailBody.append("<h3 style='color: #495057; margin-top: 0;'>We're here to help!</h3>");
            emailBody.append("<p style='color: #6c757d; margin-bottom: 0;'>If you have any questions or need assistance rescheduling, please don't hesitate to contact us.</p>");
            emailBody.append("</div>");
            
            emailBody.append("</div>");
            
            emailBody.append("<div class='footer'>");
            emailBody.append("<div class='logo'>VakaPo</div>");
            emailBody.append("<p>Making Vaccination Simple & Secure</p>");
            emailBody.append("<p>Best regards,<br><strong>VakaPo Healthcare Team</strong></p>");
            emailBody.append("<p style='font-size: 12px; color: #adb5bd;'>This is an automated message. Please do not reply to this email.</p>");
            emailBody.append("</div>");
            
            emailBody.append("</div></body></html>");
            
            emailService.sendSimpleEmail(history.getPatientEmail(), subject, emailBody.toString());
            log.info("Styled vaccination cancellation email sent to: {}", history.getPatientEmail());
            
        } catch (Exception e) {
            log.error("Error sending vaccination cancellation email: {}", e.getMessage(), e);
            throw e;
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
     * Update the corresponding appointment status based on vaccination history
     */
    private void updateCorrespondingAppointmentStatus(VaccinationHistory history) {
        try {
            log.info("Updating appointment status for patient: {}, vaccine: {}, date: {}", 
                    history.getPatientId(), history.getVaccineName(), history.getVaccinationDate());

            // Find the corresponding appointment
            List<Appointment> appointments = appointmentRepository.findByPatientIdOrderByAppointmentDateDesc(history.getPatientId());
            
            for (Appointment appointment : appointments) {
                // Match by patient, vaccine, date, and time
                if (appointment.getVaccineName().equals(history.getVaccineName()) &&
                    appointment.getAppointmentDate().equals(history.getVaccinationDate()) &&
                    appointment.getTimeSlot().equals(history.getTimeSlot()) &&
                    appointment.getDoctorName().equals(history.getDoctorName())) {
                    
                    // Update appointment status based on vaccination history status
                    if ("completed".equals(history.getStatus()) && "approved".equals(history.getNurseApproval())) {
                        appointment.setStatus("completed");
                        log.info("Updated appointment {} status to completed", appointment.getId());
                    } else if ("cancelled".equals(history.getStatus()) && "rejected".equals(history.getNurseApproval())) {
                        appointment.setStatus("cancelled");
                        log.info("Updated appointment {} status to cancelled", appointment.getId());
                    }
                    
                    appointmentRepository.save(appointment);
                    log.info("Successfully updated appointment status for appointment ID: {}", appointment.getId());
                    return; // Found and updated, exit
                }
            }
            
            log.warn("No matching appointment found for vaccination history: patient={}, vaccine={}, date={}", 
                    history.getPatientId(), history.getVaccineName(), history.getVaccinationDate());
            
        } catch (Exception e) {
            log.error("Error updating appointment status: {}", e.getMessage(), e);
            throw e;
        }
    }

    /**
     * Get recent patient updates (top 5, most recent per patient)
     */
    @GetMapping("/api/nurse/recent-updates")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> getRecentUpdates() {
        try {
            log.info("Fetching recent patient updates for nurse");
            
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
                .collect(Collectors.toList());
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("recentUpdates", recentUpdatesList);
            
            log.info("Found {} recent patient updates for nurse", recentUpdatesList.size());
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            log.error("Error fetching recent updates for nurse: {}", e.getMessage(), e);
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("error", "Failed to fetch recent updates: " + e.getMessage());
            return ResponseEntity.internalServerError().body(response);
        }
    }
    
    /**
     * Test endpoint to generate and send vaccination card PDF
     */
    @GetMapping("/api/nurse/test-vaccination-card/{patientNumber}")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> testVaccinationCard(@PathVariable String patientNumber) {
        try {
            log.info("Testing vaccination card generation for patient: {}", patientNumber);
            
            // Construct full patient ID
            String patientId = "Vak P " + patientNumber.trim();
            
            // Find patient by ID
            Optional<Patient> patientOpt = patientRepository.findById(patientId);
            if (patientOpt.isEmpty()) {
                Map<String, Object> response = new HashMap<>();
                response.put("success", false);
                response.put("error", "Patient not found with ID: " + patientId);
                return ResponseEntity.badRequest().body(response);
            }
            
            Patient patient = patientOpt.get();
            
            // Get the most recent vaccination history for this patient
            List<VaccinationHistory> vaccinationHistory = vaccinationHistoryRepository.findByPatientIdOrderByVaccinationDateDesc(patientId);
            if (vaccinationHistory.isEmpty()) {
                Map<String, Object> response = new HashMap<>();
                response.put("success", false);
                response.put("error", "No vaccination history found for patient: " + patientId);
                return ResponseEntity.badRequest().body(response);
            }
            
            VaccinationHistory latestVaccination = vaccinationHistory.get(0);
            
            // Generate test vaccination card PDF
            String batchLotNo = generateBatchLotNumber(latestVaccination.getVaccineName(), latestVaccination.getVaccinationDate());
            String nurseName = "Test Nurse"; // For testing purposes
            
            byte[] vaccinationCardPdf = pdfGenerationService.generateVaccinationRecordCard(
                patient.getId(),
                patient.getPatientName(),
                patient.getDob() != null ? patient.getDob().toString() : "N/A",
                patient.getId(),
                latestVaccination.getVaccineName(),
                latestVaccination.getVaccinationDate().toString(),
                latestVaccination.getLocation(),
                batchLotNo,
                latestVaccination.getDoctorName(),
                nurseName
            );
            
            // Send test email with PDF attachment
            emailService.sendVaccinationConfirmationWithCard(
                patient.getEmail(),
                patient.getPatientName(),
                latestVaccination.getVaccineName(),
                latestVaccination.getVaccinationDate().toString(),
                latestVaccination.getLocation(),
                latestVaccination.getDoctorName(),
                nurseName,
                vaccinationCardPdf
            );
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Test vaccination card generated and sent successfully");
            response.put("patientId", patientId);
            response.put("patientName", patient.getPatientName());
            response.put("vaccineName", latestVaccination.getVaccineName());
            response.put("vaccinationDate", latestVaccination.getVaccinationDate());
            response.put("pdfSize", vaccinationCardPdf.length + " bytes");
            response.put("emailSent", true);
            
            log.info("Test vaccination card generated and sent successfully for patient: {}", patientId);
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            log.error("Error testing vaccination card generation: {}", e.getMessage(), e);
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("error", "Failed to test vaccination card: " + e.getMessage());
            return ResponseEntity.internalServerError().body(response);
        }
    }

    /**
     * Calculate time ago string
     */
    private String getTimeAgo(LocalDateTime dateTime) {
        if (dateTime == null) {
            return "Unknown";
        }
        
        LocalDateTime now = LocalDateTime.now();
        long days = java.time.Duration.between(dateTime, now).toDays();
        long hours = java.time.Duration.between(dateTime, now).toHours();
        long minutes = java.time.Duration.between(dateTime, now).toMinutes();
        
        if (days > 0) {
            return days == 1 ? "1 Day Ago" : days + " Days Ago";
        } else if (hours > 0) {
            return hours == 1 ? "1 Hour Ago" : hours + " Hours Ago";
        } else if (minutes > 0) {
            return minutes == 1 ? "1 Minute Ago" : minutes + " Minutes Ago";
        } else {
            return "Just Now";
        }
    }

    /**
     * Delete nurse account
     */
    @PostMapping("/nurse/delete-account")
    public String deleteAccount(RedirectAttributes redirectAttributes) {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            String userEmail = authentication.getName();

            log.info("Deleting account for nurse: {}", userEmail);

            // Find nurse by email
            Optional<Staff> nurseOpt = staffRepository.findByEmail(userEmail);

            if (nurseOpt.isPresent()) {
                Staff nurse = nurseOpt.get();
                Long nurseId = nurse.getId();

                // Delete from Staff table
                staffRepository.delete(nurse);

                // Delete from UserAccount table
                Optional<UserAccount> userOpt = userRepository.findByEmail(userEmail);
                if (userOpt.isPresent()) {
                    userRepository.delete(userOpt.get());
                }

                log.info("Nurse account deleted successfully: {}", nurseId);
                
                // Send account deletion email
                try {
                    emailService.sendAccountDeletionEmail(userEmail, nurse.getName(), "Nurse");
                    log.info("Account deletion email sent to nurse: {}", userEmail);
                } catch (Exception e) {
                    log.error("Failed to send account deletion email to nurse: {}", userEmail, e);
                    // Don't fail the deletion if email fails
                }
                
                redirectAttributes.addFlashAttribute("successMessage", "Your account has been deleted successfully.");
                
                // Redirect to login page after deletion
                return "redirect:/login?deleted=true";
            } else {
                log.error("Nurse not found for email: {}", userEmail);
                redirectAttributes.addFlashAttribute("errorMessage", "Account not found");
                return "redirect:/nurse/landing";
            }
        } catch (Exception e) {
            log.error("Error deleting nurse account: {}", e.getMessage(), e);
            redirectAttributes.addFlashAttribute("errorMessage", "Error deleting account. Please try again.");
            return "redirect:/nurse/landing";
        }
    }
    
    /**
     * Add user information for contact form auto-fill
     */
    private void addContactFormUserInfo(Model model, UserAccount user) {
        try {
            model.addAttribute("isLoggedIn", true);
            model.addAttribute("userEmail", user.getEmail());
            
            // Extract name from email or use nurse name if available
            String userName = extractNurseName(user.getId());
            if (userName == null || userName.isEmpty() || userName.startsWith("Nurse ")) {
                // If we have a proper nurse name, use it; otherwise extract from email
                if (userName != null && !userName.isEmpty() && !userName.startsWith("Nurse ")) {
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
