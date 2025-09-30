package lk.vakapo.vakapo.UserManagement.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import org.hibernate.annotations.CreationTimestamp;

import java.sql.Timestamp;

@Entity
@Table(
        name = "Users",
        indexes = {
                @Index(name = "idx_users_email", columnList = "email"),
                @Index(name = "idx_users_username", columnList = "username")
        }
)
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    // ---------- Auth / Identity ----------
    @NotBlank(message = "Email is required")
    @Email(message = "Please provide a valid email")
    @Size(max = 255)
    @Column(nullable = false, unique = true, length = 255)
    private String email;

    @NotBlank(message = "Username is required")
    @Size(max = 100)
    @Column(nullable = false, unique = true, length = 100)
    private String username;

    @NotBlank(message = "Role is required") // Patient / Hospital / Clinic
    @Size(max = 50)
    @Column(nullable = false, length = 50)
    private String role;

    // ---------------- Patient ----------------
    @Size(max = 255)
    @Column(length = 255)
    private String pname;

    @Size(max = 50)
    @Column(length = 50)
    private String dob; // stored as string as per current form

    private Integer age;

    @Size(max = 255)
    @Column(length = 255)
    private String gname;

    @Size(max = 30)
    @Column(length = 30)
    private String gender;

    @Size(max = 20)
    @Column(length = 20)
    private String nic;

    // ----------- Hospital / Clinic ----------
    @Size(max = 255)
    @Column(length = 255)
    private String hname;

    @Size(max = 100)
    @Column(length = 100)
    private String rnumber;

    /**
     * Stores the RELATIVE file path (under your uploads root), e.g.:
     *   Hospital/sunshine_hosp/7b3b8c2f0f2c4dfb....pdf
     */
    @Size(max = 1024)
    @Column(name = "rcertificate", length = 1024)
    private String rcertificate;

    @Size(max = 255)
    @Column(length = 255)
    private String institution;

    // ---------------- Common -----------------
    @Size(max = 30)
    @Column(length = 30)
    private String contact;

    @Size(max = 512)
    @Column(length = 512)
    private String address;

    @NotBlank(message = "Password is required")
    @Size(max = 255)
    @Column(nullable = false, length = 255)
    private String password; // (plain in your current setup)

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private Timestamp created_at;

    // ---------------- Convenience ----------------
    /** For views: prefer username; fall back to email if blank. */
    @Transient
    public String getDisplayName() {
        return (username != null && !username.isBlank()) ? username : email;
    }

    // ---------------- Getters & Setters ----------------
    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }

    public String getRole() { return role; }
    public void setRole(String role) { this.role = role; }

    public String getPname() { return pname; }
    public void setPname(String pname) { this.pname = pname; }

    public String getDob() { return dob; }
    public void setDob(String dob) { this.dob = dob; }

    public Integer getAge() { return age; }
    public void setAge(Integer age) { this.age = age; }

    public String getGname() { return gname; }
    public void setGname(String gname) { this.gname = gname; }

    public String getGender() { return gender; }
    public void setGender(String gender) { this.gender = gender; }

    public String getNic() { return nic; }
    public void setNic(String nic) { this.nic = nic; }

    public String getHname() { return hname; }
    public void setHname(String hname) { this.hname = hname; }

    public String getRnumber() { return rnumber; }
    public void setRnumber(String rnumber) { this.rnumber = rnumber; }

    public String getRcertificate() { return rcertificate; }
    public void setRcertificate(String rcertificate) { this.rcertificate = rcertificate; }

    public String getInstitution() { return institution; }
    public void setInstitution(String institution) { this.institution = institution; }

    public String getContact() { return contact; }
    public void setContact(String contact) { this.contact = contact; }

    public String getAddress() { return address; }
    public void setAddress(String address) { this.address = address; }

    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }

    public Timestamp getCreated_at() { return created_at; }
    public void setCreated_at(Timestamp created_at) { this.created_at = created_at; }
}
