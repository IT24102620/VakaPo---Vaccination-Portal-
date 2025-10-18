package lk.vakapo.vakapo.UserManagement.controller;

import lk.vakapo.vakapo.UserManagement.model.Staff;
import lk.vakapo.vakapo.UserManagement.repository.StaffRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.time.LocalDateTime;
import java.util.Optional;

@Controller
@RequiredArgsConstructor
@Slf4j
public class DoctorConfirmationController {

    private final StaffRepository staffRepository;

    /**
     * Handle doctor confirmation for additional hospital
     */
    @GetMapping("/staff/doctor/confirm")
    public String confirmDoctorInvitation(@RequestParam String token, Model model) {
        try {
            log.info("Doctor confirmation request for token: {}", token);
            
            // Find staff record by token
            Optional<Staff> staffOpt = staffRepository.findByInvitationToken(token);
            if (staffOpt.isEmpty()) {
                log.error("Invalid confirmation token: {}", token);
                model.addAttribute("error", "Invalid or expired confirmation link");
                return "error/404";
            }
            
            Staff staff = staffOpt.get();
            
            // Check if it's a doctor
            if (!"Doctor".equalsIgnoreCase(staff.getRole())) {
                log.error("Token belongs to non-doctor: {}", staff.getRole());
                model.addAttribute("error", "Invalid confirmation link");
                return "error/403";
            }
            
            // Check if already confirmed
            if ("approved".equalsIgnoreCase(staff.getInvitationAccepted())) {
                log.warn("Doctor already confirmed for this hospital: {}", staff.getEmail());
                model.addAttribute("message", "You have already confirmed this invitation");
                model.addAttribute("success", true);
                return "staff/confirmation/DoctorConfirmationResult";
            }
            
            // Update staff status to approved
            staff.setInvitationAccepted("approved");
            staff.setInvitationAcceptedAt(LocalDateTime.now());
            staff.setUpdatedAt(LocalDateTime.now());
            staffRepository.save(staff);
            
            log.info("Doctor {} confirmed invitation for hospital: {}", staff.getEmail(), staff.getInstitutionId());
            
            model.addAttribute("message", "Successfully confirmed! You can now work at this hospital.");
            model.addAttribute("success", true);
            model.addAttribute("doctorName", staff.getName());
            model.addAttribute("hospitalName", staff.getInstitutionId());
            
            return "staff/confirmation/DoctorConfirmationResult";
            
        } catch (Exception e) {
            log.error("Error confirming doctor invitation: {}", e.getMessage(), e);
            model.addAttribute("error", "An error occurred while processing your confirmation");
            return "error/500";
        }
    }

    /**
     * Handle doctor rejection for additional hospital
     */
    @GetMapping("/staff/doctor/reject")
    public String rejectDoctorInvitation(@RequestParam String token, Model model) {
        try {
            log.info("Doctor rejection request for token: {}", token);
            
            // Find staff record by token
            Optional<Staff> staffOpt = staffRepository.findByInvitationToken(token);
            if (staffOpt.isEmpty()) {
                log.error("Invalid rejection token: {}", token);
                model.addAttribute("error", "Invalid or expired rejection link");
                return "error/404";
            }
            
            Staff staff = staffOpt.get();
            
            // Check if it's a doctor
            if (!"Doctor".equalsIgnoreCase(staff.getRole())) {
                log.error("Token belongs to non-doctor: {}", staff.getRole());
                model.addAttribute("error", "Invalid rejection link");
                return "error/403";
            }
            
            // Update staff status to rejected
            staff.setInvitationAccepted("rejected");
            staff.setUpdatedAt(LocalDateTime.now());
            staffRepository.save(staff);
            
            log.info("Doctor {} rejected invitation for hospital: {}", staff.getEmail(), staff.getInstitutionId());
            
            model.addAttribute("message", "You have declined the invitation to join this hospital.");
            model.addAttribute("success", true);
            model.addAttribute("doctorName", staff.getName());
            model.addAttribute("hospitalName", staff.getInstitutionId());
            
            return "staff/confirmation/DoctorConfirmationResult";
            
        } catch (Exception e) {
            log.error("Error rejecting doctor invitation: {}", e.getMessage(), e);
            model.addAttribute("error", "An error occurred while processing your rejection");
            return "error/500";
        }
    }
}
