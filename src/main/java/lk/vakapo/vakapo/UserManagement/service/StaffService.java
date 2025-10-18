package lk.vakapo.vakapo.UserManagement.service;

import lk.vakapo.vakapo.MailManagement.EmailService;
import lk.vakapo.vakapo.UserManagement.model.Clinic;
import lk.vakapo.vakapo.UserManagement.model.Hospital;
import lk.vakapo.vakapo.UserManagement.model.Staff;
import lk.vakapo.vakapo.UserManagement.model.UserAccount;
import lk.vakapo.vakapo.UserManagement.repository.ClinicRepository;
import lk.vakapo.vakapo.UserManagement.repository.HospitalRepository;
import lk.vakapo.vakapo.UserManagement.repository.StaffRepository;
import lk.vakapo.vakapo.UserManagement.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class StaffService {

    private final StaffRepository staffRepository;
    private final HospitalRepository hospitalRepository;
    private final ClinicRepository clinicRepository;
    private final UserRepository userRepository;
    private final EmailService emailService;

    /**
     * Send staff invitation
     */
    @Transactional
    public String sendStaffInvitation(String email, String name, String contact, String role, 
                                     String qualifications, String specialization, 
                                     String institutionType, String institutionId) {
        try {
            log.info("Starting staff invitation process for email: {}, name: {}, institution: {}", email, name, institutionId);
            
            // Get institution name for email first
            String institutionName = getInstitutionName(institutionType, institutionId);
            log.info("Institution name: {}", institutionName);
            
            if (institutionName == null) {
                throw new IllegalArgumentException("Institution not found");
            }

            // Check if user account already exists in the system
            boolean userExists = userRepository.existsByEmail(email);
            log.info("Email {} already exists in user table: {}", email, userExists);
            
            // Check if staff already exists in the system
            Optional<Staff> existingStaff = staffRepository.findByEmail(email);
            log.info("Email {} already exists in staff table: {}", email, existingStaff.isPresent());
            
            // If user account exists, handle accordingly
            if (userExists) {
                Optional<UserAccount> userAccount = userRepository.findByEmail(email);
                if (userAccount.isPresent()) {
                    String existingUserRole = userAccount.get().getRole();
                    log.info("Existing user role: {}", existingUserRole);
                    
                    // Check if trying to add same role
                    if (existingUserRole.equalsIgnoreCase(role)) {
                        if ("Nurse".equalsIgnoreCase(role)) {
                            // Nurses can only work in ONE hospital
                            log.warn("Nurse {} is already registered in the system", email);
                            throw new IllegalArgumentException("This nurse is already registered in the system. Nurses can only work in one hospital.");
                            
                        } else if ("Doctor".equalsIgnoreCase(role)) {
                            // Doctors can work in MULTIPLE hospitals
                            log.info("Doctor {} already has user account, sending confirmation link for additional hospital", email);
                            return sendDoctorConfirmationLink(email, name, institutionName, institutionType, institutionId);
                        }
                    } else {
                        // Trying to add different role for same email
                        log.warn("Email {} already exists with role: {}, trying to add role: {}", email, existingUserRole, role);
                        throw new IllegalArgumentException("This email address is already registered as a " + existingUserRole + ". One email address can only be either a Doctor OR a Nurse, but not both. Please use a different email address.");
                    }
                }
            }
            
            // If staff record exists but no user account, handle the staff record
            if (existingStaff.isPresent()) {
                Staff existing = existingStaff.get();
                String existingRole = existing.getRole();
                
                // Check if trying to add same role again
                if (existingRole.equalsIgnoreCase(role)) {
                    if ("Nurse".equalsIgnoreCase(role)) {
                        // Nurses can only work in ONE hospital
                        log.warn("Nurse {} is already allocated to another hospital: {}", email, existing.getInstitutionId());
                        throw new IllegalArgumentException("This nurse has already been allocated to another hospital. Nurses can only work in one hospital.");
                        
                    } else if ("Doctor".equalsIgnoreCase(role)) {
                        // Doctors can work in MULTIPLE hospitals
                        log.info("Doctor {} already exists, sending confirmation link instead of credentials", email);
                        return sendDoctorConfirmationLink(email, name, institutionName, institutionType, institutionId);
                    }
                } else {
                    // Trying to add different role for same email
                    log.warn("Email {} already exists with role: {}, trying to add role: {}", email, existingRole, role);
                    throw new IllegalArgumentException("This email address is already registered as a " + existingRole + ". One email address can only be either a Doctor OR a Nurse, but not both. Please use a different email address.");
                }
            }

            // Generate unique invitation token
            String invitationToken = UUID.randomUUID().toString();
            log.info("Generated invitation token: {}", invitationToken);

            // Create staff record
            Staff staff = new Staff();
            staff.setEmail(email);
            staff.setName(name);
            staff.setContact(contact);
            staff.setRole(role);
            staff.setQualifications(qualifications);
            staff.setSpecialization(specialization);
            staff.setInstitutionType(institutionType);
            staff.setInstitutionId(institutionId);
            staff.setInvitationAccepted("not approved");
            staff.setInvitationToken(invitationToken);
            staff.setInvitationSentAt(LocalDateTime.now());
            staff.setCreatedAt(LocalDateTime.now());
            staff.setUpdatedAt(LocalDateTime.now());

            // Save staff record
            log.info("Saving staff record to database...");
            staffRepository.save(staff);
            log.info("Staff record saved successfully");

            // Create user account for staff authentication
            log.info("Creating user account for staff: {}", email);
            createStaffUserAccount(email, staff.getId(), role, institutionType, institutionId);
            log.info("User account created successfully");

            // Send invitation email
            log.info("Sending invitation email to: {} via EmailService...", email);
            emailService.sendStaffInvitation(email, name, institutionName, institutionType, invitationToken);
            log.info("Email sent successfully via EmailService");

            log.info("Staff invitation sent successfully to: {} for {}: {}", email, institutionType, institutionName);
            return "Invitation sent successfully to " + email;

        } catch (Exception e) {
            log.error("Error sending staff invitation: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to send staff invitation: " + e.getMessage(), e);
        }
    }

    /**
     * Accept staff invitation
     */
    @Transactional
    public String acceptStaffInvitation(String token) {
        try {
            Optional<Staff> staffOpt = staffRepository.findByInvitationToken(token);
            if (staffOpt.isEmpty()) {
                throw new IllegalArgumentException("Invalid or expired invitation token");
            }

            Staff staff = staffOpt.get();
            if ("approved".equals(staff.getInvitationAccepted())) {
                throw new IllegalArgumentException("Invitation has already been accepted");
            }

            if ("rejected".equals(staff.getInvitationAccepted())) {
                throw new IllegalArgumentException("Invitation has been rejected");
            }

            // Update staff status
            staff.setInvitationAccepted("approved");
            staff.setInvitationAcceptedAt(LocalDateTime.now());
            staff.setUpdatedAt(LocalDateTime.now());

            staffRepository.save(staff);

            // Send confirmation email to institution
            String institutionName = getInstitutionName(staff.getInstitutionType(), staff.getInstitutionId());
            emailService.sendStaffAcceptanceNotification(staff.getEmail(), staff.getName(), 
                                                       institutionName, staff.getInstitutionType());

            log.info("Staff invitation accepted by: {} for {}: {}", staff.getEmail(), 
                    staff.getInstitutionType(), institutionName);
            return "Invitation accepted successfully";

        } catch (Exception e) {
            log.error("Error accepting staff invitation: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to accept invitation: " + e.getMessage(), e);
        }
    }

    /**
     * Reject staff invitation
     */
    @Transactional
    public String rejectStaffInvitation(String token) {
        try {
            Optional<Staff> staffOpt = staffRepository.findByInvitationToken(token);
            if (staffOpt.isEmpty()) {
                throw new IllegalArgumentException("Invalid or expired invitation token");
            }

            Staff staff = staffOpt.get();
            if ("approved".equals(staff.getInvitationAccepted())) {
                throw new IllegalArgumentException("Invitation has already been accepted");
            }

            // Update staff status
            staff.setInvitationAccepted("rejected");
            staff.setUpdatedAt(LocalDateTime.now());

            staffRepository.save(staff);

            log.info("Staff invitation rejected by: {} for {}: {}", staff.getEmail(), 
                    staff.getInstitutionType(), staff.getInstitutionId());
            return "Invitation rejected successfully";

        } catch (Exception e) {
            log.error("Error rejecting staff invitation: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to reject invitation: " + e.getMessage(), e);
        }
    }

    /**
     * Get all staff for an institution
     */
    public List<Staff> getStaffByInstitution(String institutionId, String institutionType) {
        return staffRepository.findByInstitutionIdAndType(institutionId, institutionType);
    }

    /**
     * Get staff by role for an institution
     */
    public List<Staff> getStaffByRole(String institutionId, String institutionType, String role) {
        return staffRepository.findByInstitutionIdAndTypeAndRole(institutionId, institutionType, role);
    }

    /**
     * Get pending invitations for an institution
     */
    public List<Staff> getPendingInvitations(String institutionId, String institutionType) {
        return staffRepository.findPendingInvitations(institutionId, institutionType);
    }

    /**
     * Get accepted staff for an institution
     */
    public List<Staff> getAcceptedStaff(String institutionId, String institutionType) {
        return staffRepository.findAcceptedStaff(institutionId, institutionType);
    }

    /**
     * Get staff by ID
     */
    public Optional<Staff> getStaffById(Long staffId) {
        return staffRepository.findById(staffId);
    }

    /**
     * Get staff by name and institution
     */
    public Optional<Staff> getStaffByNameAndInstitution(String name, String institutionId, String institutionType) {
        return staffRepository.findByNameAndInstitution(name, institutionId, institutionType);
    }

    /**
     * Remove staff member (simplified version for controllers)
     */
    @Transactional
    public void removeStaff(Long staffId) {
        try {
            Optional<Staff> staffOpt = staffRepository.findById(staffId);
            if (staffOpt.isEmpty()) {
                throw new IllegalArgumentException("Staff member not found");
            }

            Staff staff = staffOpt.get();
            
            // Get institution name for email notification
            String institutionName = getInstitutionName(staff.getInstitutionType(), staff.getInstitutionId());
            
            // Send removal notification email to staff member (with error handling)
            try {
                emailService.sendStaffRemovalNotification(staff.getEmail(), staff.getName(), 
                                                       institutionName, staff.getInstitutionType(), staff.getRole());
                log.info("Staff removal notification email sent successfully to: {}", staff.getEmail());
            } catch (Exception emailError) {
                log.warn("Failed to send removal notification email to {}: {}", staff.getEmail(), emailError.getMessage());
                // Continue with deletion even if email fails
            }

            // Delete staff record
            staffRepository.delete(staff);

            log.info("Staff member removed: {} from {}: {} - Email notification sent", 
                    staff.getEmail(), staff.getInstitutionType(), staff.getInstitutionId());

        } catch (Exception e) {
            log.error("Error removing staff member: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to remove staff member: " + e.getMessage(), e);
        }
    }

    /**
     * Remove staff member (original version with validation)
     */
    @Transactional
    public String removeStaff(Long staffId, String institutionId, String institutionType) {
        try {
            Optional<Staff> staffOpt = staffRepository.findById(staffId);
            if (staffOpt.isEmpty()) {
                throw new IllegalArgumentException("Staff member not found");
            }

            Staff staff = staffOpt.get();
            if (!staff.getInstitutionId().equals(institutionId) || !staff.getInstitutionType().equals(institutionType)) {
                throw new IllegalArgumentException("Unauthorized to remove this staff member");
            }

            // Get institution name for email notification
            String institutionName = getInstitutionName(institutionType, institutionId);
            
            // Send removal notification email to staff member
            emailService.sendStaffRemovalNotification(staff.getEmail(), staff.getName(), 
                                                   institutionName, institutionType, staff.getRole());

            // Delete staff record
            staffRepository.delete(staff);

            log.info("Staff member removed: {} from {}: {} - Email notification sent", 
                    staff.getEmail(), institutionType, institutionId);
            return "Staff member removed successfully and notified via email";

        } catch (Exception e) {
            log.error("Error removing staff member: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to remove staff member: " + e.getMessage(), e);
        }
    }

    /**
     * Cancel staff connection - removes hospital/clinic connection but keeps doctor account
     * This method only removes the connection between staff and institution, 
     * but preserves the staff member's account for potential future connections
     */
    @Transactional
    public void cancelStaffConnection(Long staffId) {
        try {
            Optional<Staff> staffOpt = staffRepository.findById(staffId);
            if (staffOpt.isEmpty()) {
                throw new IllegalArgumentException("Staff member not found");
            }

            Staff staff = staffOpt.get();
            
            // Get institution name for email notification
            String institutionName = getInstitutionName(staff.getInstitutionType(), staff.getInstitutionId());
            
            // Send cancellation notification email to staff member (with error handling)
            try {
                emailService.sendStaffCancellationNotification(staff.getEmail(), staff.getName(), 
                                                           institutionName, staff.getInstitutionType(), staff.getRole());
                log.info("Staff cancellation notification email sent successfully to: {}", staff.getEmail());
            } catch (Exception emailError) {
                log.warn("Failed to send cancellation notification email to {}: {}", staff.getEmail(), emailError.getMessage());
                // Continue with cancellation even if email fails
            }

            // Remove the staff record (this removes the connection to the institution)
            // The staff member's user account remains intact for potential future connections
            staffRepository.delete(staff);

            log.info("Staff connection cancelled: {} from {}: {} - Account preserved, email notification sent", 
                    staff.getEmail(), staff.getInstitutionType(), staff.getInstitutionId());

        } catch (Exception e) {
            log.error("Error cancelling staff connection: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to cancel staff connection: " + e.getMessage(), e);
        }
    }


    /**
     * Create user account for staff authentication
     */
    private void createStaffUserAccount(String email, Long staffId, String role, String institutionType, String institutionId) {
        try {
            // Check if user already exists
            if (userRepository.existsByEmail(email)) {
                log.info("User account already exists for email: {}, skipping user account creation", email);
                return;
            }

            // Generate staff ID based on role
            String staffUserId = generateStaffUserId(role, staffId);
            log.info("Generated staff user ID: {} for role: {}", staffUserId, role);

            // Create user account for staff
            UserAccount staffUser = new UserAccount();
            staffUser.setId(staffUserId); // Use role-based staff ID
            staffUser.setEmail(email);
            staffUser.setPassword("1234567890"); // Default temporary password
            staffUser.setRole(role); // Use actual role (Doctor or Nurse)
            staffUser.setStatus("approved"); // Approved status
            staffUser.setAdminApproval("approved"); // Auto-approved for staff

            // Save user account
            userRepository.save(staffUser);
            log.info("User account created for staff: {} with ID: {}", email, staffUser.getId());

        } catch (Exception e) {
            log.error("Error creating user account for staff: {}", email, e);
            if (e.getMessage().contains("CHECK constraint")) {
                throw new RuntimeException("Database constraint error: Doctor and Nurse roles are not allowed. Please run the SQL script to fix the database constraint.", e);
            }
            throw new RuntimeException("Failed to create user account: " + e.getMessage(), e);
        }
    }

    /**
     * Send confirmation link to existing doctor for new hospital
     */
    private String sendDoctorConfirmationLink(String email, String name, String institutionName, 
                                            String institutionType, String institutionId) {
        try {
            // Generate confirmation token
            String confirmationToken = UUID.randomUUID().toString();
            
            // Create new staff record for the additional hospital
            Staff additionalStaff = new Staff();
            additionalStaff.setEmail(email);
            additionalStaff.setName(name);
            additionalStaff.setRole("Doctor");
            additionalStaff.setInstitutionType(institutionType);
            additionalStaff.setInstitutionId(institutionId);
            additionalStaff.setInvitationAccepted("pending_confirmation");
            additionalStaff.setInvitationToken(confirmationToken);
            additionalStaff.setInvitationSentAt(LocalDateTime.now());
            additionalStaff.setCreatedAt(LocalDateTime.now());
            additionalStaff.setUpdatedAt(LocalDateTime.now());
            
            // Save the additional staff record
            staffRepository.save(additionalStaff);
            
            // Send confirmation email
            emailService.sendDoctorConfirmationLink(email, name, institutionName, institutionType, confirmationToken);
            
            log.info("Doctor confirmation link sent to: {} for {}: {}", email, institutionType, institutionName);
            return "Confirmation link sent to existing doctor " + email + ". Doctor can confirm to join this hospital.";
            
        } catch (Exception e) {
            log.error("Error sending doctor confirmation link: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to send doctor confirmation link: " + e.getMessage(), e);
        }
    }

    /**
     * Generate staff user ID based on role
     */
    private String generateStaffUserId(String role, Long staffId) {
        // Map role to prefix - only Doctor and Nurse supported
        String prefix;
        switch (role.toLowerCase()) {
            case "doctor":
                prefix = "Vak D";
                break;
            case "nurse":
                prefix = "Vak N";
                break;
            default:
                // Default to Doctor if unknown role
                prefix = "Vak D";
                break;
        }
        
        // Format: Vak D 1000, Vak N 1000, etc. (both start from 1000)
        // Add 999 to staffId to start from 1000
        long adjustedId = staffId + 999;
        return prefix + " " + String.format("%04d", adjustedId);
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
}
