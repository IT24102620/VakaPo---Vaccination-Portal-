package lk.vakapo.vakapo.UserManagement.model;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDate;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor
@Entity
@Table(schema = "dbo", name = "Patient")
public class Patient {

    @Id
    @Column(name = "patient_id", length = 20)
    private String id; // e.g. "Vak P 1000"

    @Column(nullable = false, length = 150)
    private String email;

    @Column(length = 20)
    private String contact;

    @Column(length = 255)
    private String address;

    @Column(nullable = false, length = 10)
    private String status = "approved"; // approved | reject

    // NEW FIELDS
    @Column(name = "user_name", length = 150)
    private String username;

    @Column(name = "patient_name", length = 150)
    private String patientName;

    @Column(name = "date_of_birth")
    private LocalDate dob;

    @Column(name = "guardian_name", length = 150)
    private String guardianName;

    @Column(name = "gender", length = 10)
    private String gender;

    @Column(name = "nic", length = 20)
    private String nic;
}
