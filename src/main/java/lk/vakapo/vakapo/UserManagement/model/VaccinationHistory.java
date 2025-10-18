package lk.vakapo.vakapo.UserManagement.model;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor
@Entity
@Table(schema = "dbo", name = "vaccination_history")
public class VaccinationHistory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "history_id")
    private Long id;

    @Column(nullable = false, length = 20)
    private String patientId; // Patient ID from Patient table

    @Column(nullable = false, length = 100)
    private String patientEmail; // Patient email for quick reference

    @Column(nullable = false, length = 100)
    private String patientName; // Patient name for quick reference

    @Column(nullable = false, length = 100)
    private String vaccineName;

    @Column(nullable = false)
    private LocalDate vaccinationDate;

    @Column(nullable = false, length = 20)
    private String timeSlot; // e.g., "10:04-10:24"

    @Column(nullable = false, length = 100)
    private String location; // Hospital/Clinic name

    @Column(nullable = false, length = 20)
    private String institutionType; // Hospital, Clinic

    @Column(nullable = false, length = 20)
    private String institutionId; // Hospital ID or Clinic ID

    @Column(nullable = false, length = 100)
    private String doctorName;

    @Column(length = 20)
    private String dosageLevel = "0ml"; // Default dosage level

    @Column(length = 200)
    private String additionalNotes; // Additional notes from doctor

    @Column(name = "next_vaccine_date")
    private LocalDate nextVaccineDate; // Date for next vaccination, null if no next shot needed

    @Column(name = "nurse_approval", nullable = false, length = 20)
    private String nurseApproval = "pending"; // pending, approved, rejected

    @Column(nullable = false, length = 20)
    private String status = "pending"; // pending, completed, cancelled

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
