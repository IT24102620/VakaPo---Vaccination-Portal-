//package lk.vakapo.vakapo.UserManagement.Controller;
//
//import lk.vakapo.vakapo.UserManagement.model.User;
//import lk.vakapo.vakapo.UserManagement.Service.UserService;
//import lk.vakapo.vakapo.Common.FileStorageService; // <-- your service package
//import org.springframework.http.MediaType;
//import org.springframework.security.core.annotation.AuthenticationPrincipal;
//import org.springframework.security.core.userdetails.UserDetails;
//import org.springframework.stereotype.Controller;
//import org.springframework.ui.Model;
//import org.springframework.web.bind.annotation.*;
//import org.springframework.web.multipart.MultipartFile;
//
//@Controller
//public class UserController {
//
//    private final UserService userService;
//    private final FileStorageService storage;
//
//    public UserController(UserService userService, FileStorageService storage) {
//        this.userService = userService;
//        this.storage = storage;
//    }
//
//    // ---------- LOGIN ----------
//    @GetMapping("/login")
//    public String loginPage() { return "Login"; }
//
//    // ---------- SIGNUP ----------
//    @GetMapping("/signup")
//    public String signupPage(Model model) {
//        model.addAttribute("user", new User());
//        return "Signup";
//    }
//
//    @PostMapping(value = "/signup", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
//    public String handleSignup(
//            @RequestParam String email,
//            @RequestParam String username,
//            @RequestParam String role,
//
//            // Patient (optional)
//            @RequestParam(required = false) String pname,
//            @RequestParam(required = false) String dob,
//            @RequestParam(required = false, name = "age") String ageStr,
//            @RequestParam(required = false) String gname,
//            @RequestParam(required = false) String gender,
//            @RequestParam(required = false) String nic,
//
//            // Hospital / Clinic (optional)
//            @RequestParam(required = false) String hname,
//            @RequestParam(required = false) String rnumber,
//            @RequestParam(required = false) String institution,
//
//            // EXACTLY ONE file for Hospital, EXACTLY ONE for Clinic
//            @RequestParam(name = "rcertificate", required = false) MultipartFile rcertificate,
//            @RequestParam(name = "ccertificate", required = false) MultipartFile ccertificate,
//
//            // Common
//            @RequestParam String contact,
//            @RequestParam String address,
//            @RequestParam String password,
//            @RequestParam String retypepassword
//    ) {
//        if (!password.equals(retypepassword)) {
//            return "redirect:/signup?error=password_mismatch";
//        }
//
//        final String normRole = role == null ? "" : role.trim();
//
//        // Build user
//        User u = new User();
//        u.setEmail(email);
//        u.setUsername(username);
//        u.setRole(normRole);
//
//        // Patient
//        u.setPname(pname);
//        u.setDob(dob);
//        u.setAge(safeParseInt(ageStr));
//        u.setGname(gname);
//        u.setGender(gender);
//        u.setNic(nic);
//
//        // Hospital / Clinic
//        u.setHname(hname);
//        u.setRnumber(rnumber);
//        u.setInstitution(institution);
//
//        // Common
//        u.setContact(contact);
//        u.setAddress(address);
//        u.setPassword(password); // plain text per your current setup
//
//        // ---- File upload rules ----
//        try {
//            if (isHospital(normRole)) {
//                // Hospital expects name="rcertificate"
//                if (rcertificate != null && !rcertificate.isEmpty()) {
//                    String relative = storage.saveUnderRoleAndUser(rcertificate, "Hospital", username);
//                    u.setRcertificate(relative);
//                }
//            } else if (isClinic(normRole)) {
//                // Clinic prefers name="ccertificate", but fall back to rcertificate if your form uses that
//                MultipartFile chosen = (ccertificate != null && !ccertificate.isEmpty())
//                        ? ccertificate
//                        : (rcertificate != null && !rcertificate.isEmpty() ? rcertificate : null);
//                if (chosen != null) {
//                    String relative = storage.saveUnderRoleAndUser(chosen, "Clinic", username);
//                    u.setRcertificate(relative);
//                }
//            }
//            // Patient: no file upload → leave rcertificate as null
//        } catch (Exception e) {
//            e.printStackTrace();
//            return "redirect:/signup?error=file_upload";
//        }
//
//        userService.save(u);
//        return "redirect:/login?registered";
//    }
//
//    // ---------- Post-login role-based redirect ----------
//    @GetMapping("/redirect")
//    public String redirectAfterLogin(@AuthenticationPrincipal UserDetails principal) {
//        if (principal == null) return "redirect:/login";
//        String authority = principal.getAuthorities().iterator().next().getAuthority(); // ROLE_*
//        switch (authority) {
//            case "ROLE_PATIENT":  return "redirect:/patient/home";
//            case "ROLE_HOSPITAL": return "redirect:/hospital/home";
//            case "ROLE_CLINIC":   return "redirect:/clinic/home";
//            default:              return "redirect:/";
//        }
//    }
//
//    // ---------- Role home pages ----------
//    @GetMapping("/patient/home")
//    public String patientHome() { return "patient/landingPage/PatientLandingPage"; }
//
//    @GetMapping("/hospital/home")
//    public String hospitalHome() { return "hospital/landingPage/HospitalLandingPage"; }
//
//    @GetMapping("/clinic/home")
//    public String clinicHome() { return "clinic/landingPage/ClinicLandingPage"; }
//
//    // ===== Helpers =====
//    private boolean isHospital(String role) {
//        return role != null && role.equalsIgnoreCase("Hospital");
//    }
//    private boolean isClinic(String role) {
//        return role != null && role.equalsIgnoreCase("Clinic");
//    }
//    private Integer safeParseInt(String s) {
//        try { if (s == null || s.isBlank()) return null; return Integer.valueOf(s.trim()); }
//        catch (Exception e) { return null; }
//    }
//}







package lk.vakapo.vakapo.UserManagement.Controller;

import lk.vakapo.vakapo.UserManagement.model.User;
import lk.vakapo.vakapo.UserManagement.Service.UserService;
import lk.vakapo.vakapo.Common.FileStorageService;
import org.springframework.http.MediaType;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@Controller
public class UserController {

    private final UserService userService;
    private final FileStorageService storage;

    public UserController(UserService userService, FileStorageService storage) {
        this.userService = userService;
        this.storage = storage;
    }

    // ---------- LOGIN ----------
    @GetMapping("/login")
    public String loginPage() { return "Login"; }

    // ---------- SIGNUP ----------
    @GetMapping("/signup")
    public String signupPage(Model model) {
        model.addAttribute("user", new User());
        return "Signup";
    }

    @PostMapping(value = "/signup", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public String handleSignup(
            @RequestParam String email,
            @RequestParam String username,
            @RequestParam String role,

            // Patient (optional)
            @RequestParam(required = false) String pname,
            @RequestParam(required = false) String dob,
            @RequestParam(required = false, name = "age") String ageStr,
            @RequestParam(required = false) String gname,
            @RequestParam(required = false) String gender,
            @RequestParam(required = false) String nic,

            // Hospital / Clinic (optional)
            @RequestParam(required = false) String hname,
            @RequestParam(required = false) String rnumber,
            @RequestParam(required = false) String institution,

            // IMPORTANT: both Hospital and Clinic use name="rcertificate" in your UI
            @RequestParam(name = "rcertificate", required = false) List<MultipartFile> rcertificate,

            // Common
            @RequestParam String contact,
            @RequestParam String address,
            @RequestParam String password,
            @RequestParam String retypepassword
    ) {
        if (!password.equals(retypepassword)) {
            return "redirect:/signup?error=password_mismatch";
        }

        final String normRole = role == null ? "" : role.trim();

        User u = new User();
        u.setEmail(email);
        u.setUsername(username);
        u.setRole(normRole);

        // Patient
        u.setPname(pname);
        u.setDob(dob);
        u.setAge(safeParseInt(ageStr));
        u.setGname(gname);
        u.setGender(gender);
        u.setNic(nic);

        // Hospital / Clinic
        u.setHname(hname);
        u.setRnumber(rnumber);
        u.setInstitution(institution);

        // Common
        u.setContact(contact);
        u.setAddress(address);
        u.setPassword(password); // (plain text per your current setup)

        try {
            // Only Hospital/Clinic upload a file
            if (isHospital(normRole) || isClinic(normRole)) {
                MultipartFile chosen = firstNonEmpty(rcertificate);
                if (chosen != null && !chosen.isEmpty()) {
                    String relative = storage.saveUnderRoleAndUser(chosen, normRole, username);
                    u.setRcertificate(relative); // e.g., "Clinic/MyClinic/abcd1234.pdf"
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            return "redirect:/signup?error=file_upload";
        }

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
    public String hospitalHome() { return "hospital/landingPage/HospitalLandingPage"; }

    @GetMapping("/clinic/home")
    public String clinicHome() { return "clinic/landingPage/ClinicLandingPage"; }

    // ===== Helpers =====
    private boolean isHospital(String role) { return role != null && role.equalsIgnoreCase("Hospital"); }
    private boolean isClinic(String role) { return role != null && role.equalsIgnoreCase("Clinic"); }

    private Integer safeParseInt(String s) {
        try { if (s == null || s.isBlank()) return null; return Integer.valueOf(s.trim()); }
        catch (Exception e) { return null; }
    }

    private MultipartFile firstNonEmpty(List<MultipartFile> files) {
        if (files == null) return null;
        return files.stream().filter(f -> f != null && !f.isEmpty()).findFirst().orElse(null);
    }
}
