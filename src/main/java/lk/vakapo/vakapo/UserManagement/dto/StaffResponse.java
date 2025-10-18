package lk.vakapo.vakapo.UserManagement.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class StaffResponse {
    private Long id;
    private String email;
    private String name;
    private String contact;
    private String role;
    private String qualifications;
    private String specialization;
    private String institutionType;
    private String institutionId;
    private String invitationAccepted;
    private String createdAt;
    private String updatedAt;
}
