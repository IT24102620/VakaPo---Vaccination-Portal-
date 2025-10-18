package lk.vakapo.vakapo.UserManagement.dto;

import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDate;

@Data
public class SignupRequest {
    // Common
    private String email;
    private String password;
    private String role;         // Patient | Hospital | Clinic
    private String username;     // Patient username; can be used for orgs too
    private String contact;
    private String address;

    // Patient-specific
    private String pname;        // patientName
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    private LocalDate dob;
    private String gname;        // guardianName
    private String gender;
    private String nic;

    // Hospital/Clinic specific
    private String hname;        // org display name (fallback to username)
    private String rnumber;
    private String institution;

    // Uploaded certificate (Hospital or Clinic)
    private MultipartFile rcertificate;
}
