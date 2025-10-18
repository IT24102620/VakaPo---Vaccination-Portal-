package lk.vakapo.vakapo.UserManagement.model;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor
@Entity
@Table(schema = "dbo", name = "appointments")
public class Appointment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "appointment_id")
    private Long id;

    @Column(nullable = false, length = 20)
    private String patientId; // Patient ID from Patient table

    @Column(nullable = false, length = 150)
    private String patientEmail; // Patient email for quick reference

    @Column(nullable = false, length = 150)
    private String patientName; // Patient name for quick reference

    @Column(nullable = false, length = 150)
    private String vaccineName;

    @Column(nullable = false, length = 20)
    private String institutionType; // Hospital, Clinic

    @Column(nullable = false, length = 20)
    private String institutionId; // Hospital ID or Clinic ID

    @Column(nullable = false, length = 150)
    private String institutionName; // Hospital/Clinic name for quick reference

    @Column(nullable = false, length = 150)
    private String doctorName;

    @Column(nullable = false)
    private LocalDate appointmentDate;

    @Column(nullable = false, length = 20)
    private String timeSlot; // e.g., "08:00-08:20"

    @Column(nullable = false, length = 20)
    private String status = "scheduled"; // scheduled, completed, cancelled, no_show

    @Column(length = 20)
    private String cancelledBy; // "patient" or "hospital" - tracks who cancelled the appointment

    @Column(length = 255)
    private String notes;

    @Column(name = "created_at")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}
