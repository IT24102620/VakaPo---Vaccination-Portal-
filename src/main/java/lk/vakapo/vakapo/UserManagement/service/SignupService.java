package lk.vakapo.vakapo.UserManagement.service;

import jakarta.transaction.Transactional;
import lk.vakapo.vakapo.UserManagement.dto.SignupRequest;
import lk.vakapo.vakapo.UserManagement.model.*;
import lk.vakapo.vakapo.UserManagement.repository.*;
import lk.vakapo.vakapo.PDFManagement.PDFGenerationService;
import lk.vakapo.vakapo.MailManagement.PDFEmailService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.io.InputStream;
import java.nio.file.*;
import java.util.Objects;

@Service
@RequiredArgsConstructor
@Slf4j
public class SignupService {

    private final UserRepository userRepo;
    private final PatientRepository patientRepo;
    private final HospitalRepository hospitalRepo;
    private final ClinicRepository clinicRepo;
    private final IdSequenceDao seqDao;
    private final PDFGenerationService pdfGenerationService;
    private final PDFEmailService pdfEmailService;

    private static String pad4(long n) { return String.format("%04d", n); }

    public boolean emailExists(String email) {
        return userRepo.existsByEmail(email);
    }

    @Transactional
    public String signup(SignupRequest req) {
        if (userRepo.existsByEmail(req.getEmail())) {
            throw new IllegalArgumentException("Email already registered");
        }
        String role = req.getRole();
        if (role == null || role.isBlank()) {
            throw new IllegalArgumentException("Role is required");
        }

        String id;
        switch (role) {
            case "Patient" -> {
                // Use timestamp-based ID generation as fallback
                long next = System.currentTimeMillis() % 10000; // Use last 4 digits of timestamp
                id = "Vak P " + pad4(next);

                Patient p = new Patient();
                p.setId(id);
                p.setEmail(req.getEmail());
                p.setContact(req.getContact());
                p.setAddress(req.getAddress());
                p.setStatus("approved");

                // Additional fields
                p.setUsername(req.getUsername());
                p.setPatientName(req.getPname());
                p.setDob(req.getDob());
                p.setGuardianName(req.getGname());
                p.setGender(req.getGender());
                p.setNic(req.getNic());

                patientRepo.save(p);

                // Create a login user right away for patients
                UserAccount u = new UserAccount();
                u.setId(id);
                u.setEmail(req.getEmail());
                u.setPassword(req.getPassword());
                u.setRole("Patient");
                u.setStatus("approved");
                u.setAdminApproval("approved");
                userRepo.save(u);

                // Send patient registration card PDF via email
                try {
                    log.info("Sending patient registration card PDF to: {}", req.getEmail());
                    
                    // Generate PDF registration card
                    byte[] pdfBytes = pdfGenerationService.generatePatientRegistrationCardHTML(
                        id,                                    // Patient ID
                        req.getPname(),                       // Patient Name
                        req.getEmail(),                       // Email
                        req.getContact(),                     // Contact
                        req.getDob() != null ? req.getDob().toString() : "N/A", // Date of Birth
                        req.getGender() != null ? req.getGender() : "N/A",      // Gender
                        req.getAddress()                      // Address
                    );
                    
                    // Send PDF via email
                    pdfEmailService.sendPatientRegistrationCard(
                        req.getEmail(), 
                        req.getPname(), 
                        id, 
                        pdfBytes
                    );
                    
                    log.info("Patient registration card PDF sent successfully to: {}", req.getEmail());
                } catch (Exception e) {
                    log.error("Failed to send patient registration card PDF to: {}. Error: {}", 
                             req.getEmail(), e.getMessage(), e);
                    // Don't fail the signup process if email fails
                    log.info("Signup completed successfully despite PDF email failure for: {}", req.getEmail());
                }
            }
            case "Hospital" -> {
                // Use timestamp-based ID generation as fallback
                long next = System.currentTimeMillis() % 10000; // Use last 4 digits of timestamp
                id = "Vak H " + pad4(next);

                Hospital h = new Hospital();
                h.setId(id);
                h.setEmail(req.getEmail());
                h.setContact(req.getContact());
                h.setRnumber(req.getRnumber());
                h.setInstitution(req.getInstitution());
                h.setAddress(req.getAddress());
                h.setStatus("approved");
                h.setAdminApproval("not approved");
                String orgUserName = (req.getUsername() != null && !req.getUsername().isBlank())
                        ? req.getUsername() : req.getHname();
                h.setUsername(orgUserName);
                if (req.getRcertificate() != null && !req.getRcertificate().isEmpty()) {
                    h.setCertificate(saveCertificateFile(id, req.getRcertificate()));
                }
                hospitalRepo.save(h);

                // Create locked login user (blocked by adminApproval)
                UserAccount u = new UserAccount();
                u.setId(id);
                u.setEmail(req.getEmail());
                u.setPassword(req.getPassword());
                u.setRole("Hospital");
                u.setStatus("approved");
                u.setAdminApproval("not approved");
                userRepo.save(u);
            }
            case "Clinic" -> {
                // Use timestamp-based ID generation as fallback
                long next = System.currentTimeMillis() % 10000; // Use last 4 digits of timestamp
                id = "Vak C " + pad4(next);

                Clinic c = new Clinic();
                c.setId(id);
                c.setEmail(req.getEmail());
                c.setContact(req.getContact());
                c.setRnumber(req.getRnumber());
                c.setInstitution(req.getInstitution());
                c.setAddress(req.getAddress());
                c.setStatus("approved");
                c.setAdminApproval("not approved");
                String orgUserName = (req.getUsername() != null && !req.getUsername().isBlank())
                        ? req.getUsername() : req.getHname();
                c.setUsername(orgUserName);
                if (req.getRcertificate() != null && !req.getRcertificate().isEmpty()) {
                    c.setCertificate(saveCertificateFile(id, req.getRcertificate()));
                }
                clinicRepo.save(c);

                // Create locked login user (blocked by adminApproval)
                UserAccount u = new UserAccount();
                u.setId(id);
                u.setEmail(req.getEmail());
                u.setPassword(req.getPassword());
                u.setRole("Clinic");
                u.setStatus("approved");
                u.setAdminApproval("not approved");
                userRepo.save(u);
            }
            default -> throw new IllegalArgumentException("Invalid role: " + role);
        }
        return id;
    }

        // Save under static/uploads and return web path "/uploads/<file>"
    private String saveCertificateFile(String id, org.springframework.web.multipart.MultipartFile file) {
        try {
            Path uploadDir = Paths.get("src/main/resources/static/uploads");
            Files.createDirectories(uploadDir);

            String original = Objects.toString(file.getOriginalFilename(), "certificate");
            String safeName = original.replaceAll("[^a-zA-Z0-9._-]", "_");
            String fileName = id.replace(' ', '_') + "_" + safeName;

            Path target = uploadDir.resolve(fileName);
            try (InputStream in = file.getInputStream()) {
                Files.copy(in, target, StandardCopyOption.REPLACE_EXISTING);
            }
            return "/uploads/" + fileName;
        } catch (Exception ex) {
            throw new IllegalArgumentException("Failed to save certificate: " + ex.getMessage(), ex);
        }
    }
}
