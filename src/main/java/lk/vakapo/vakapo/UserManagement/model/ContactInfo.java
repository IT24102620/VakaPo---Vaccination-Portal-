package lk.vakapo.vakapo.UserManagement.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "contact")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ContactInfo {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(name = "full_name", nullable = false, length = 100)
    private String fullName;
    
    @Column(name = "email_address", nullable = false, length = 100)
    private String emailAddress;
    
    @Column(name = "phone_number", nullable = false, length = 20)
    private String phoneNumber;
    
    @Column(name = "message", columnDefinition = "TEXT")
    private String message;
    
    @Column(name = "status", length = 20, nullable = false)
    private String status = "pending"; // pending, reviewed, responded
    
    @Column(name = "response", columnDefinition = "TEXT")
    private String response;
    
    @Column(name = "responded_by", length = 100)
    private String respondedBy;
    
    @Column(name = "responded_at")
    private LocalDateTime respondedAt;
    
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt = LocalDateTime.now();
    
    // Constructor for creating new contact submissions
    public ContactInfo(String fullName, String emailAddress, String phoneNumber, String message) {
        this.fullName = fullName;
        this.emailAddress = emailAddress;
        this.phoneNumber = phoneNumber;
        this.message = message;
        this.status = "pending";
        this.createdAt = LocalDateTime.now();
    }
}
