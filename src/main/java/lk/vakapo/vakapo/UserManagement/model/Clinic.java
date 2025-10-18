package lk.vakapo.vakapo.UserManagement.model;

import jakarta.persistence.*;
import lombok.*;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor
@Entity
@Table(schema = "dbo", name = "Clinic")
public class Clinic {

    @Id
    @Column(name = "clinic_id", length = 20)
    private String id; // e.g. "Vak C 1000"

    @Column(nullable = false, length = 150)
    private String email;

    @Column(length = 20)
    private String contact;

    @Column(length = 50)
    private String rnumber;

    @Column(length = 20)
    private String institution;

    @Column(length = 255)
    private String address;

    @Column(nullable = false, length = 10)
    private String status = "approved"; // approved | reject

    @Column(name = "admin_approval", nullable = false, length = 20)
    private String adminApproval = "not approved"; // approved | not approved

    // Save link for clinics too
    @Column(name = "certificate", length = 255)
    private String certificate;

    // NEW: username column (display/handle for the org)
    @Column(name = "user_name", length = 150)
    private String username;
}
