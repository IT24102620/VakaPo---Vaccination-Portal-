package lk.vakapo.vakapo.UserManagement.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "feedback")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Feedback {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(name = "name", nullable = false)
    private String name;
    
    @Column(name = "email", nullable = false)
    private String email;
    
    @Column(name = "contact_no")
    private String contactNo;
    
    @Column(name = "rating", nullable = false)
    private Integer rating;
    
    @Column(name = "message", columnDefinition = "TEXT")
    private String message;
    
    @Column(name = "user_type", nullable = false)
    private String userType; // PATIENT, DOCTOR, NURSE, HOSPITAL, CLINIC
    
    @Column(name = "user_id")
    private String userId; // ID of the user who submitted feedback
    
    @Column(name = "is_approved", nullable = false)
    private Boolean isApproved = false; // Admin approval for public display
    
    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;
    
    @Column(name = "approved_at")
    private LocalDateTime approvedAt;
    
    @Column(name = "approved_by")
    private String approvedBy; // Admin who approved the feedback
}
