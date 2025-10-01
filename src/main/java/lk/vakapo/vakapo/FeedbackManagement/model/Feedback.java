package lk.vakapo.vakapo.FeedbackManagement.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.OffsetDateTime;

@Entity
@Table(name = "Feedback")
public class Feedback {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank @Size(max = 100)
    @Column(nullable = false, length = 100)
    private String name;

    @NotBlank @Email @Size(max = 255)
    @Column(nullable = false, length = 255)
    private String email;

    @Size(max = 20)
    @Column(length = 20)
    private String phone;

    @Size(max = 150)
    @Column(length = 150)
    private String subject;

    @NotBlank @Size(max = 2000)
    @Column(nullable = false, length = 2000)
    private String message;

    @Min(1) @Max(5)
    @Column
    private Byte rating; // nullable allowed by DB, but UI requires it

    @Column(name = "user_id")
    private Integer userId; // optional link to Users

    @NotBlank @Size(max = 50)
    @Column(name = "source_page", nullable = false, length = 50)
    private String sourcePage = "patient"; // "landing", "patient", "hospital"

    @NotBlank @Size(max = 20)
    @Column(nullable = false, length = 20)
    private String status = "PUBLIC"; // Always PUBLIC for display

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, columnDefinition = "DATETIMEOFFSET")
    private OffsetDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false, columnDefinition = "DATETIMEOFFSET")
    private OffsetDateTime updatedAt;

    /**
     * JPA requires a no‑argument constructor. Without it the entity cannot be
     * instantiated via reflection.
     */
    public Feedback() {
        // no‑args constructor
    }

    // ----- Getter and Setter methods -----
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getSubject() {
        return subject;
    }

    public void setSubject(String subject) {
        this.subject = subject;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public Byte getRating() {
        return rating;
    }

    public void setRating(Byte rating) {
        this.rating = rating;
    }

    public Integer getUserId() {
        return userId;
    }

    public void setUserId(Integer userId) {
        this.userId = userId;
    }

    public String getSourcePage() {
        return sourcePage;
    }

    public void setSourcePage(String sourcePage) {
        this.sourcePage = sourcePage;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public OffsetDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(OffsetDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public OffsetDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(OffsetDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }
}
