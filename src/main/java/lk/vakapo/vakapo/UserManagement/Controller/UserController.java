package lk.vakapo.vakapo.UserManagement.Controller;

import lk.vakapo.vakapo.UserManagement.model.User;
import lk.vakapo.vakapo.UserManagement.Service.UserService;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.UUID;

@Controller
public class UserController {

    private final UserService userService;
    public UserController(UserService userService) { this.userService = userService; }

    // ---------- LOGIN (Sign In) ----------
    @GetMapping("/login")
    public String loginPage() { return "Login"; }

    // ---------- SIGNUP (GET + POST) ----------
    @GetMapping("/signup")
    public String signupPage(Model model) {
        model.addAttribute("user", new User());
        return "Signup";
    }

    @PostMapping("/signup")
    public String handleSignup(
            @RequestParam String email,
            @RequestParam String username,
            @RequestParam String role,

            // Patient (optional)
            @RequestParam(required = false) String pname,
            @RequestParam(required = false) String dob,
            @RequestParam(required = false) Integer age,
            @RequestParam(required = false) String gname,
            @RequestParam(required = false) String gender,
            @RequestParam(required = false) String nic,

            // Hospital / Clinic (optional)
            @RequestParam(required = false) String hname,
            @RequestParam(required = false) String rnumber,
            @RequestParam(required = false) String institution,

            // File input (same name in Hospital & Clinic sections)
            @RequestParam(required = false) MultipartFile rcertificate,

            // Common
            @RequestParam String contact,
            @RequestParam String address,
            @RequestParam String password,
            @RequestParam String retypepassword
    ) {
        if (!password.equals(retypepassword)) {
            return "redirect:/signup?error=password_mismatch";
        }

        String certPath = null;
        try {
            if (rcertificate != null && !rcertificate.isEmpty()) {
                Path uploadDir = Paths.get("uploads");
                Files.createDirectories(uploadDir);
                String fileName = UUID.randomUUID() + "_" + rcertificate.getOriginalFilename();
                Path target = uploadDir.resolve(fileName);
                rcertificate.transferTo(target.toFile());
                certPath = target.toString();
            }
        } catch (Exception e) {
            e.printStackTrace();
            return "redirect:/signup?error=file_upload";
        }

        User u = new User();
        u.setEmail(email);
        u.setUsername(username);
        u.setRole(role);                 // "Patient" / "Hospital" / "Clinic"
        u.setPname(pname);
        u.setDob(dob);
        u.setAge(age);
        u.setGname(gname);
        u.setGender(gender);
        u.setNic(nic);
        u.setHname(hname);
        u.setRnumber(rnumber);
        u.setInstitution(institution);
        u.setRcertificate(certPath);
        u.setContact(contact);
        u.setAddress(address);
        u.setPassword(password);         // plain text per your request

        userService.save(u);
        return "redirect:/login?registered";
    }

    // ---------- Post-login role-based redirect ----------
    @GetMapping("/redirect")
    public String redirectAfterLogin(@AuthenticationPrincipal UserDetails principal) {
        if (principal == null) return "redirect:/login";
        String authority = principal.getAuthorities().iterator().next().getAuthority(); // ROLE_*

        switch (authority) {
            case "ROLE_PATIENT":  return "redirect:/patient/home";
            case "ROLE_HOSPITAL": return "redirect:/hospital/home";
            case "ROLE_CLINIC":   return "redirect:/clinic/home";
            default:              return "redirect:/";
        }
    }

    // ---------- Role home pages ----------
    @GetMapping("/patient/home")
    public String patientHome() { return "patient/landingPage/PatientLandingPage"; }

    @GetMapping("/hospital/home")
    public String hospitalHome() { return "HospitalHome"; }

    @GetMapping("/clinic/home")
    public String clinicHome() { return "ClinicHome"; }
}
