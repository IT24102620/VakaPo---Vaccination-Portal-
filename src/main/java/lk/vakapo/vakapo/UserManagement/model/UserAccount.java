package lk.vakapo.vakapo.UserManagement.model;

import jakarta.persistence.*;
import lombok.*;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor
@Entity
@Table(schema = "dbo", name = "Users")
public class UserAccount {

    @Id
    @Column(name = "user_id", length = 20)
    private String id;  // same as Patient/Hospital/Clinic id

    @Column(nullable = false, unique = true, length = 150)
    private String email;

    @Column(nullable = false, length = 255)
    private String password; // plain (as per your current setup)

    @Column(nullable = false, length = 20)
    private String role; // Patient | Hospital | Clinic | Doctor | Nurse

    @Column(nullable = false, length = 10)
    private String status = "approved"; // email verification

    @Column(name = "admin_approval", nullable = false, length = 20)
    private String adminApproval = "not approved"; // approved | not approved
}
