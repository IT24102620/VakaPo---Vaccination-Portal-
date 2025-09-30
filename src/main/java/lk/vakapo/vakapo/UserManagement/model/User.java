package lk.vakapo.vakapo.UserManagement.model;

import jakarta.persistence.*;
import java.sql.Timestamp;

@Entity
@Table(name = "Users") // SQL Server is case-insensitive for table names by default
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(nullable = false, unique = true, length = 255)
    private String email;

    @Column(nullable = false, unique = true, length = 100)
    private String username;

    @Column(nullable = false, length = 50)
    private String role; // Patient / Hospital / Clinic

    // ---------------- Patient ----------------
    @Column(length = 255)
    private String pname;

    @Column(length = 50)
    private String dob;          // keep as string if the form posts string date

    private Integer age;

    @Column(length = 255)
    private String gname;

    @Column(length = 30)
    private String gender;

    @Column(length = 20)
    private String nic;

    // ----------- Hospital / Clinic ----------
    @Column(length = 255)
    private String hname;

    @Column(length = 100)
    private String rnumber;

    /**
     * Stores the RELATIVE file path (under your uploads root), e.g.:
     *   Hospital/sunshine_hosp/7b3b8c2f0f2c4dfbb6c0f2f8dd2b04f8.pdf
     */
    @Column(name = "rcertificate", length = 1024)
    private String rcertificate;

    @Column(length = 255)
    private String institution;

    // ---------------- Common -----------------
    @Column(length = 30)
    private String contact;

    @Column(length = 512)
    private String address;

    @Column(nullable = false, length = 255)
    private String password; // plain text per your current design

    @Column(nullable = false)
    private Timestamp created_at;

    @PrePersist
    protected void onCreate() {
        if (created_at == null) {
            created_at = new Timestamp(System.currentTimeMillis());
        }
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
