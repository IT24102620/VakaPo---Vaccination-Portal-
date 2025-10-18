package lk.vakapo.vakapo.UserManagement.model;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDate;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor
@Entity
@Table(name = "vaccination_history")
public class VaccinationHistorySimple {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String patientId;

    @Column(nullable = false)
    private String patientName;

    @Column(nullable = false)
    private String vaccineName;

    @Column(nullable = false)
    private LocalDate vaccinationDate;

    @Column(nullable = false)
    private String timeSlot;

    @Column(nullable = false)
    private String location;

    @Column(nullable = false)
    private String doctorName;

    @Column
    private String dosageLevel = "0ml";

    @Column
    private String additionalNotes = "";

    @Column(nullable = false)
    private String status = "pending";
}
