package lk.vakapo.vakapo.UserManagement.model;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor
@Entity
@Table(schema = "dbo", name = "vaccination_schedule")
public class VaccinationSchedule {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "schedule_id")
    private Long id;

    @Column(nullable = false, length = 100)
    private String doctorName;

    @Column(nullable = false, length = 100)
    private String vaccineName;

    @Column(nullable = false, length = 20)
    private String institutionType; // Hospital, Clinic

    @Column(nullable = false, length = 20)
    private String institutionId; // Hospital ID or Clinic ID

    @Column(nullable = false, length = 10)
    private String timeFrom; // e.g., "09:00"

    @Column(nullable = false, length = 10)
    private String timeTo; // e.g., "10:00"

    @Column(nullable = false, length = 100)
    private String days; // e.g., "Monday,Tuesday,Friday"

    @Column(nullable = false, length = 20)
    private String status = "scheduled"; // scheduled, completed, cancelled

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
