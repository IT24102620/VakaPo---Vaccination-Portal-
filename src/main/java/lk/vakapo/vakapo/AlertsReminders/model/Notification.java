package lk.vakapo.vakapo.AlertsReminders.model;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;

@Entity
@Table(name = "notifications")
@Data
public class Notification {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String title;

    @Column(nullable = false, columnDefinition = "NVARCHAR(MAX)")
    private String message;

    @Column(nullable = false)
    private String audience;

    @Column(name = "delivery_method", nullable = false)
    private String deliveryMethod;

    @Column(name = "schedule_date")
    private LocalDateTime scheduleDate;

    @Column(name = "is_read")
    private boolean isRead = false;
}