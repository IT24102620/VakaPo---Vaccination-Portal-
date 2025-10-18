package lk.vakapo.vakapo.AdminManagement.controller;


import jakarta.transaction.Transactional;
import lk.vakapo.vakapo.MailManagement.EmailService;
import lk.vakapo.vakapo.MailManagement.EmailTemplateService;
import lk.vakapo.vakapo.UserManagement.model.Clinic;
import lk.vakapo.vakapo.UserManagement.model.Hospital;
import lk.vakapo.vakapo.UserManagement.model.UserAccount;
import lk.vakapo.vakapo.UserManagement.repository.ClinicRepository;
import lk.vakapo.vakapo.UserManagement.repository.HospitalRepository;
import lk.vakapo.vakapo.UserManagement.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Optional;

@Controller
@RequiredArgsConstructor
@Slf4j
@RequestMapping("/admin/sub-admin-request")
public class SubAdminManagerController {

    private final HospitalRepository hospitalRepo;
    private final ClinicRepository clinicRepo;
    private final UserRepository userRepo;
    private final EmailService emailService;
    private final EmailTemplateService emailTemplateService;

    /** Page with pending Hospital/Clinic requests */
    @GetMapping("/requests")
    public String pending(Model model) {
        // Pending requests (not approved)
        model.addAttribute("hospitals", hospitalRepo.findByAdminApproval("not approved"));
        model.addAttribute("clinics", clinicRepo.findByAdminApproval("not approved"));
        
        // Approved institutions
        model.addAttribute("approvedHospitals", hospitalRepo.findByAdminApproval("approved"));
        model.addAttribute("approvedClinics", clinicRepo.findByAdminApproval("approved"));
        
        return "admin/subAdmin/Allowed_Sub_Admin";
    }

    /** Stream certificate file (it was saved under /static/uploads, we serve it via classpath) */
    @GetMapping("/certificate")
    @ResponseBody
    public org.springframework.http.ResponseEntity<byte[]> viewCertificate(@RequestParam("path") String webPath) {
        try {
            if (!StringUtils.hasText(webPath) || !webPath.startsWith("/uploads/")) {
                return org.springframework.http.ResponseEntity.badRequest().build();
            }
            // Handle both development and production paths
            Path file = Paths.get("src/main/resources/static" + webPath);
            if (!Files.exists(file)) {
                // Try alternative path for production
                file = Paths.get("static" + webPath);
            }
            byte[] bytes = Files.readAllBytes(file);

            String filename = file.getFileName().toString();
            
            // Determine content type based on file extension
            String contentType = MediaType.APPLICATION_OCTET_STREAM_VALUE;
            if (filename.toLowerCase().endsWith(".pdf")) {
                contentType = "application/pdf";
            } else if (filename.toLowerCase().endsWith(".jpg") || filename.toLowerCase().endsWith(".jpeg")) {
                contentType = "image/jpeg";
            } else if (filename.toLowerCase().endsWith(".png")) {
                contentType = "image/png";
            }
            
            return org.springframework.http.ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=\"" + filename + "\"")
                    .contentType(MediaType.parseMediaType(contentType))
                    .body(bytes);
        } catch (Exception e) {
            // Log the error for debugging
            System.err.println("Certificate viewing error: " + e.getMessage());
            e.printStackTrace();
            return org.springframework.http.ResponseEntity.notFound().build();
        }
    }

    /** Approve a hospital */
    @PostMapping("/approve/hospital/{id}")
    @Transactional
    public String approveHospital(@PathVariable String id) {
        try {
            Optional<Hospital> opt = hospitalRepo.findById(id);
            if (opt.isPresent()) {
                Hospital h = opt.get();
                h.setAdminApproval("approved");
                hospitalRepo.save(h);
                
                Optional<UserAccount> userOpt = userRepo.findById(id);
                if (userOpt.isPresent()) {
                    UserAccount u = userOpt.get();
                    u.setAdminApproval("approved");
                    userRepo.save(u);
                    
                    // Send approval notification email
                    try {
                        String institutionName = h.getUsername() != null ? h.getUsername() : "Hospital";
                        emailTemplateService.sendHtmlApprovalNotification(h.getEmail(), institutionName, "Hospital");
                        log.info("Approval email sent to hospital: {}", h.getEmail());
                    } catch (Exception emailException) {
                        log.error("Failed to send approval email to hospital {}: {}", h.getEmail(), emailException.getMessage());
                        // Don't fail the approval process if email fails
                    }
                    
                    log.info("Hospital {} approved successfully", id);
                } else {
                    log.error("UserAccount not found for hospital {}", id);
                }
            } else {
                log.error("Hospital not found with id: {}", id);
            }
        } catch (Exception e) {
            log.error("Error approving hospital {}: {}", id, e.getMessage(), e);
        }
        return "redirect:/admin/sub-admin-request/requests?ok=1";
    }

    /** Approve a clinic */
    @PostMapping("/approve/clinic/{id}")
    @Transactional
    public String approveClinic(@PathVariable String id) {
        try {
            Optional<Clinic> opt = clinicRepo.findById(id);
            if (opt.isPresent()) {
                Clinic c = opt.get();
                c.setAdminApproval("approved");
                clinicRepo.save(c);
                
                Optional<UserAccount> userOpt = userRepo.findById(id);
                if (userOpt.isPresent()) {
                    UserAccount u = userOpt.get();
                    u.setAdminApproval("approved");
                    userRepo.save(u);
                    
                    // Send approval notification email
                    try {
                        String institutionName = c.getUsername() != null ? c.getUsername() : "Clinic";
                        emailTemplateService.sendHtmlApprovalNotification(c.getEmail(), institutionName, "Clinic");
                        log.info("Approval email sent to clinic: {}", c.getEmail());
                    } catch (Exception emailException) {
                        log.error("Failed to send approval email to clinic {}: {}", c.getEmail(), emailException.getMessage());
                        // Don't fail the approval process if email fails
                    }
                    
                    log.info("Clinic {} approved successfully", id);
                } else {
                    log.error("UserAccount not found for clinic {}", id);
                }
            } else {
                log.error("Clinic not found with id: {}", id);
            }
        } catch (Exception e) {
            log.error("Error approving clinic {}: {}", id, e.getMessage(), e);
        }
        return "redirect:/admin/sub-admin-request/requests?ok=1";
    }

    /** Delete request (removes org + user record) */
    @PostMapping("/delete/hospital/{id}")
    @Transactional
    public String deleteHospital(@PathVariable String id) {
        hospitalRepo.deleteById(id);
        userRepo.findById(id).ifPresent(userRepo::delete);
        return "redirect:/admin/sub-admin-request/requests?deleted=1";
    }

    @PostMapping("/delete/clinic/{id}")
    @Transactional
    public String deleteClinic(@PathVariable String id) {
        clinicRepo.deleteById(id);
        userRepo.findById(id).ifPresent(userRepo::delete);
        return "redirect:/admin/sub-admin-request/requests?deleted=1";
    }

    /** Un-approve a hospital */
    @PostMapping("/unapprove/hospital/{id}")
    @Transactional
    public String unapproveHospital(@PathVariable String id) {
        try {
            Optional<Hospital> opt = hospitalRepo.findById(id);
            if (opt.isPresent()) {
                Hospital h = opt.get();
                h.setAdminApproval("not approved");
                hospitalRepo.save(h);
                
                Optional<UserAccount> userOpt = userRepo.findById(id);
                if (userOpt.isPresent()) {
                    UserAccount u = userOpt.get();
                    u.setAdminApproval("not approved");
                    userRepo.save(u);
                    
                    // Send unapproval notification email
                    try {
                        String institutionName = h.getUsername() != null ? h.getUsername() : "Hospital";
                        emailTemplateService.sendHtmlUnapprovalNotification(h.getEmail(), institutionName, "Hospital");
                        log.info("Unapproval email sent to hospital: {}", h.getEmail());
                    } catch (Exception emailException) {
                        log.error("Failed to send unapproval email to hospital {}: {}", h.getEmail(), emailException.getMessage());
                        // Don't fail the unapproval process if email fails
                    }
                    
                    log.info("Hospital {} unapproved successfully", id);
                } else {
                    log.error("UserAccount not found for hospital {}", id);
                }
            } else {
                log.error("Hospital not found with id: {}", id);
            }
        } catch (Exception e) {
            log.error("Error unapproving hospital {}: {}", id, e.getMessage(), e);
        }
        return "redirect:/admin/sub-admin-request/requests?unapproved=1";
    }

    /** Un-approve a clinic */
    @PostMapping("/unapprove/clinic/{id}")
    @Transactional
    public String unapproveClinic(@PathVariable String id) {
        try {
            Optional<Clinic> opt = clinicRepo.findById(id);
            if (opt.isPresent()) {
                Clinic c = opt.get();
                c.setAdminApproval("not approved");
                clinicRepo.save(c);
                
                Optional<UserAccount> userOpt = userRepo.findById(id);
                if (userOpt.isPresent()) {
                    UserAccount u = userOpt.get();
                    u.setAdminApproval("not approved");
                    userRepo.save(u);
                    
                    // Send unapproval notification email
                    try {
                        String institutionName = c.getUsername() != null ? c.getUsername() : "Clinic";
                        emailTemplateService.sendHtmlUnapprovalNotification(c.getEmail(), institutionName, "Clinic");
                        log.info("Unapproval email sent to clinic: {}", c.getEmail());
                    } catch (Exception emailException) {
                        log.error("Failed to send unapproval email to clinic {}: {}", c.getEmail(), emailException.getMessage());
                        // Don't fail the unapproval process if email fails
                    }
                    
                    log.info("Clinic {} unapproved successfully", id);
                } else {
                    log.error("UserAccount not found for clinic {}", id);
                }
            } else {
                log.error("Clinic not found with id: {}", id);
            }
        } catch (Exception e) {
            log.error("Error unapproving clinic {}: {}", id, e.getMessage(), e);
        }
        return "redirect:/admin/sub-admin-request/requests?unapproved=1";
    }
}
