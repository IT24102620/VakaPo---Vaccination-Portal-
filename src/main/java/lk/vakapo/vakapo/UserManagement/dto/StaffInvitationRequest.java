package lk.vakapo.vakapo.UserManagement.dto;

import lombok.Data;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

@Data
public class StaffInvitationRequest {
    
    @NotBlank(message = "Email is required")
    @Email(message = "Please enter a valid email address")
    private String email;
    
    @NotBlank(message = "Name is required")
    @Size(min = 2, max = 100, message = "Name must be between 2 and 100 characters")
    private String name;
    
    @Size(max = 20, message = "Contact number must not exceed 20 characters")
    private String contact;
    
    @NotBlank(message = "Role is required")
    @Size(max = 50, message = "Role must not exceed 50 characters")
    private String role; // Doctor, Nurse, etc.
    
    @Size(max = 100, message = "Qualifications must not exceed 100 characters")
    private String qualifications;
    
    @Size(max = 50, message = "Specialization must not exceed 50 characters")
    private String specialization;
}
