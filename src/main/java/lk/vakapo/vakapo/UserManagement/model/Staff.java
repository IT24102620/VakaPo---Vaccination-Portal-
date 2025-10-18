package lk.vakapo.vakapo.UserManagement.model;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor
@Entity
@Table(schema = "dbo", name = "Staff")
public class Staff {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "staff_id")
    private Long id;

    @Column(nullable = false, length = 150)
    private String email;

    @Column(nullable = false, length = 100)
    private String name;

    @Column(length = 20)
    private String contact;

    @Column(nullable = false, length = 50)
    private String role; // Doctor, Nurse, etc.

    @Column(length = 100)
    private String qualifications;

    @Column(length = 50)
    private String specialization;

    @Column(nullable = false, length = 20)
    private String institutionType; // Hospital, Clinic

    @Column(nullable = false, length = 20)
    private String institutionId; // Hospital ID or Clinic ID

    @Column(nullable = false, length = 20)
    private String invitationAccepted = "not approved"; // approved, not approved, rejected

    @Column(name = "invitation_token", length = 255)
    @JsonIgnore
    private String invitationToken;

    @Column(name = "invitation_sent_at")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime invitationSentAt;

    @Column(name = "invitation_accepted_at")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime invitationAcceptedAt;

    @Column(name = "created_at")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime updatedAt;
}
