//package lk.vakapo.vakapo.UserManagement.Controller;
//import lk.vakapo.vakapo.UserManagement.model.User;
//import lk.vakapo.vakapo.UserManagement.Service.UserService;
//import lk.vakapo.vakapo.Common.FileStorageService;
//import org.springframework.http.MediaType;
//import org.springframework.security.core.GrantedAuthority;
//import org.springframework.security.core.annotation.AuthenticationPrincipal;
//import org.springframework.security.core.context.SecurityContextHolder;
//import org.springframework.security.core.userdetails.UserDetails;
//import org.springframework.stereotype.Controller;
//import org.springframework.ui.Model;
//import org.springframework.web.bind.annotation.*;
//import org.springframework.web.multipart.MultipartFile;
//
//import java.util.Collection;
//import java.util.List;
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
//
//
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
//            // Your UI posts name="rcertificate" (can be multiple if input allows)
//            @RequestParam(name = "rcertificate", required = false) List<MultipartFile> rcertificate,
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
//        final String normRole = (role == null) ? "" : role.trim();
//
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
//        u.setPassword(password); // (plain in your current setup)
//
//        try {
//            if (isHospital(normRole) || isClinic(normRole)) {
//                MultipartFile chosen = firstNonEmpty(rcertificate);
//                if (chosen != null && !chosen.isEmpty()) {
//                    String relative = storage.saveUnderRoleAndUser(chosen, normRole, username);
//                    u.setRcertificate(relative);
//                }
//            }
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
//
//        Collection<? extends GrantedAuthority> auths = principal.getAuthorities();
//        if (auths == null || auths.isEmpty()) return "redirect:/";
//
//        String authority = auths.iterator().next().getAuthority();
//        switch (authority) {
//            case "ROLE_PATIENT":  return "redirect:/patient/home";
//            case "ROLE_HOSPITAL": return "redirect:/hospital/home";
//            case "ROLE_CLINIC":   return "redirect:/clinic/home";
//            default:              return "redirect:/";
//        }
//    }
//
//    // ---------- Role home pages (now add username/user to model) ----------
//    @GetMapping("/patient/home")
//    public String patientHome(@AuthenticationPrincipal UserDetails principal, Model model) {
//        addUserToModel(principal, model);
//        return "patient/landingPage/PatientLandingPage";
//    }
//
//    @GetMapping("/hospital/home")
//    public String hospitalHome(@AuthenticationPrincipal UserDetails principal, Model model) {
//        addUserToModel(principal, model);
//        return "hospital/landingPage/HospitalLandingPage";
//    }
//
//    @GetMapping("/clinic/home")
//    public String clinicHome(@AuthenticationPrincipal UserDetails principal, Model model) {
//        addUserToModel(principal, model);
//        return "clinic/landingPage/ClinicLandingPage";
//    }
//
//    @GetMapping("/patient/profile")
//    public String profile(@AuthenticationPrincipal UserDetails principal, Model model) {
//        if (principal == null) return "redirect:/login";
//
//        var dbUser = userService.findByEmail(principal.getUsername());
//        if (dbUser == null) return "redirect:/login?not_found";
//
//        model.addAttribute("user", dbUser);               // the full User entity
//        model.addAttribute("username", dbUser.getUsername()); // optional
//
//        return "patient/profilePage/ProfilePage";
//    }
//
//    @GetMapping("/hospital/profile")
//    public String hospitalProfile(@AuthenticationPrincipal UserDetails principal, Model model) {
//        addUserToModel(principal, model);
//        return "hospital/profilePage/HospitalProfilePage";
//    }
//
//    // ---------- Account (self-view + self-delete) ----------
//    @GetMapping("/account")
//    public String account(@AuthenticationPrincipal UserDetails principal, Model model) {
//        if (principal == null) return "redirect:/login";
//        addUserToModel(principal, model);
//        return "Account";
//    }
//
//    @PostMapping("/account/delete")
//    public String deleteOwnAccount(@AuthenticationPrincipal UserDetails principal) {
//        if (principal == null) return "redirect:/login";
//
//        User dbUser = userService.findByEmail(principal.getUsername());
//        if (dbUser != null) {
//            try {
//                String role = dbUser.getRole();
//                String uname = dbUser.getUsername();
//                if (role != null && uname != null) {
//                    storage.deleteUserFolder(role, uname);
//                }
//            } catch (Exception ignored) {}
//        }
//
//        userService.deleteByEmail(principal.getUsername());
//        SecurityContextHolder.clearContext();
//        return "redirect:/login?deleted";
//    }
//
//    @PostMapping("/patient/updateProfile")
//    public String updateProfile(@AuthenticationPrincipal UserDetails principal,
//                                @RequestParam String nic,
//                                @RequestParam String username,
//                                @RequestParam String email,
//                                @RequestParam String contact,
//                                @RequestParam String currentPassword,
//                                @RequestParam String newPassword,
//                                @RequestParam String confirmPassword,
//                                Model model) {
//        if (principal == null) return "redirect:/login";
//
//        var user = userService.findByEmail(principal.getUsername());
//        if (user == null) return "redirect:/login";
//
//        // Update fields
//        user.setNic(nic);
//        user.setUsername(username);
//        user.setEmail(email);
//        user.setContact(contact);
//
//        // Change password if valid
//        if (!newPassword.isEmpty() && newPassword.equals(confirmPassword)) {
//            if (userService.checkPassword(user, currentPassword)) {
//                userService.updatePassword(user, newPassword);
//            } else {
//                model.addAttribute("error", "Current password incorrect!");
//                return "patient/profilePage/ProfilePage";
//            }
//        }
//
//        userService.save(user);
//
//        return "redirect:/patient/profile";
//    }
//
//    @PostMapping("/hospital/updateProfile")
//    public String updateHospitalProfile(@AuthenticationPrincipal UserDetails principal,
//                                        @RequestParam String hname,
//                                        @RequestParam String email,
//                                        @RequestParam String contact,
//                                        @RequestParam String address,
//                                        @RequestParam(required = false) String currentPassword,
//                                        @RequestParam(required = false) String newPassword,
//                                        @RequestParam(required = false) String confirmPassword,
//                                        Model model) {
//        if (principal == null) return "redirect:/login";
//
//        var user = userService.findByEmail(principal.getUsername());
//        if (user == null) return "redirect:/login";
//
//        // ✅ Update hospital details
//        user.setHname(hname);
//        user.setEmail(email);
//        user.setContact(contact);
//        user.setAddress(address);
//
//        // ✅ Handle password change if provided
//        if (newPassword != null && !newPassword.isEmpty()) {
//            if (newPassword.equals(confirmPassword)) {
//                if (userService.checkPassword(user, currentPassword)) {
//                    userService.updatePassword(user, newPassword);
//                } else {
//                    model.addAttribute("error", "Current password is incorrect!");
//                    return "hospital/profilePage/HospitalProfilePage";
//                }
//            } else {
//                model.addAttribute("error", "Passwords do not match!");
//                return "hospital/profilePage/HospitalProfilePage";
//            }
//        }
//
//        userService.save(user);
//
//        return "redirect:/hospital/profile";
//    }
//
//
//    // ===== Helpers =====
//    private void addUserToModel(UserDetails principal, Model model) {
//        if (principal == null) return;
//        String email = principal.getUsername();        // login identifier
//        User dbUser = userService.findByEmail(email);  // load full user
//        String displayName = (dbUser != null && dbUser.getUsername() != null && !dbUser.getUsername().isBlank())
//                ? dbUser.getUsername()
//                : email;
//
//        model.addAttribute("username", displayName);
//        model.addAttribute("user", dbUser);
//    }
//
//    private boolean isHospital(String role) { return role != null && role.equalsIgnoreCase("Hospital"); }
//    private boolean isClinic(String role)   { return role != null && role.equalsIgnoreCase("Clinic"); }
//
//    private Integer safeParseInt(String s) {
//        try { if (s == null || s.isBlank()) return null; return Integer.valueOf(s.trim()); }
//        catch (Exception e) { return null; }
//    }
//
//    private MultipartFile firstNonEmpty(List<MultipartFile> files) {
//        if (files == null) return null;
//        return files.stream().filter(f -> f != null && !f.isEmpty()).findFirst().orElse(null);
//    }
//}




//
//package lk.vakapo.vakapo.UserManagement.Controller;
//
//import lk.vakapo.vakapo.UserManagement.model.User;
//import lk.vakapo.vakapo.UserManagement.Service.UserService;
//import lk.vakapo.vakapo.Common.FileStorageService;
//import org.springframework.http.MediaType;
//import org.springframework.security.core.GrantedAuthority;
//import org.springframework.security.core.annotation.AuthenticationPrincipal;
//import org.springframework.security.core.context.SecurityContextHolder;
//import org.springframework.security.core.userdetails.UserDetails;
//import org.springframework.stereotype.Controller;
//import org.springframework.ui.Model;
//import org.springframework.web.bind.annotation.*;
//import org.springframework.web.multipart.MultipartFile;
//
//import java.util.Collection;
//import java.util.List;
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
//            // Your UI posts name="rcertificate" (can be multiple if input allows)
//            @RequestParam(name = "rcertificate", required = false) List<MultipartFile> rcertificate,
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
//        final String normRole = (role == null) ? "" : role.trim();
//
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
//        u.setPassword(password); // (plain for now; service may encode)
//
//        try {
//            if (isHospital(normRole) || isClinic(normRole)) {
//                MultipartFile chosen = firstNonEmpty(rcertificate);
//                if (chosen != null && !chosen.isEmpty()) {
//                    String relative = storage.saveUnderRoleAndUser(chosen, normRole, username);
//                    u.setRcertificate(relative);
//                }
//            }
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
//
//        Collection<? extends GrantedAuthority> auths = principal.getAuthorities();
//        if (auths == null || auths.isEmpty()) return "redirect:/";
//
//        String authority = auths.iterator().next().getAuthority();
//        switch (authority) {
//            case "ROLE_PATIENT":  return "redirect:/patient/home";
//            case "ROLE_HOSPITAL": return "redirect:/hospital/home";
//            case "ROLE_CLINIC":   return "redirect:/clinic/home";
//            default:              return "redirect:/";
//        }
//    }
//
//    // ---------- Role home pages (username/user into model) ----------
//    @GetMapping("/patient/home")
//    public String patientHome(@AuthenticationPrincipal UserDetails principal, Model model) {
//        addUserToModel(principal, model);
//        return "patient/landingPage/PatientLandingPage";
//    }
//
//    @GetMapping("/hospital/home")
//    public String hospitalHome(@AuthenticationPrincipal UserDetails principal, Model model) {
//        addUserToModel(principal, model);
//        return "hospital/landingPage/HospitalLandingPage";
//    }
//
//    @GetMapping("/clinic/home")
//    public String clinicHome(@AuthenticationPrincipal UserDetails principal, Model model) {
//        addUserToModel(principal, model);
//        return "hospital/landingPage/HospitalLandingPage";
//    }
//
//    // ---------- Profiles ----------
//    @GetMapping("/patient/profile")
//    public String patientProfile(@AuthenticationPrincipal UserDetails principal, Model model) {
//        if (principal == null) return "redirect:/login";
//        var dbUser = userService.findByEmail(principal.getUsername());
//        if (dbUser == null) return "redirect:/login?not_found";
//        model.addAttribute("user", dbUser);
//        model.addAttribute("username", dbUser.getUsername());
//        return "patient/profilePage/ProfilePage";
//    }
//
//    @GetMapping("/hospital/profile")
//    public String hospitalProfile(@AuthenticationPrincipal UserDetails principal, Model model) {
//        addUserToModel(principal, model);
//        return "hospital/profilePage/HospitalProfilePage";
//    }
//
//    // ---------- Account (self-view + self-delete) ----------
//    @GetMapping("/account")
//    public String account(@AuthenticationPrincipal UserDetails principal, Model model) {
//        if (principal == null) return "redirect:/login";
//        addUserToModel(principal, model);
//        return "Account";
//    }
//
//    @PostMapping("/account/delete")
//    public String deleteOwnAccount(@AuthenticationPrincipal UserDetails principal) {
//        if (principal == null) return "redirect:/login";
//
//        User dbUser = userService.findByEmail(principal.getUsername());
//        if (dbUser != null) {
//            try {
//                String role = dbUser.getRole();
//                String uname = dbUser.getUsername();
//                if (role != null && uname != null) {
//                    storage.deleteUserFolder(role, uname);
//                }
//            } catch (Exception ignored) {}
//        }
//
//        userService.deleteByEmail(principal.getUsername());
//        SecurityContextHolder.clearContext();
//        return "redirect:/login?deleted";
//    }
//
//    // ---------- Patient: Update Profile ----------
//    @PostMapping("/patient/updateProfile")
//    public String updatePatientProfile(@AuthenticationPrincipal UserDetails principal,
//                                       @RequestParam String nic,
//                                       @RequestParam String username,
//                                       @RequestParam String email,
//                                       @RequestParam String contact,
//                                       @RequestParam(required = false) String currentPassword,
//                                       @RequestParam(required = false) String newPassword,
//                                       @RequestParam(required = false) String confirmPassword,
//                                       Model model) {
//        if (principal == null) return "redirect:/login";
//
//        var user = userService.findByEmail(principal.getUsername());
//        if (user == null) return "redirect:/login";
//
//        // Update fields
//        user.setNic(nic);
//        user.setUsername(username);
//        user.setEmail(email);
//        user.setContact(contact);
//
//        // Change password if provided and valid
//        if (newPassword != null && !newPassword.isBlank()) {
//            if (!newPassword.equals(confirmPassword)) {
//                model.addAttribute("error", "Passwords do not match!");
//                model.addAttribute("user", user);
//                return "patient/profilePage/ProfilePage";
//            }
//            if (currentPassword == null || !userService.checkPassword(user, currentPassword)) {
//                model.addAttribute("error", "Current password incorrect!");
//                model.addAttribute("user", user);
//                return "patient/profilePage/ProfilePage";
//            }
//            userService.updatePassword(user, newPassword);
//        }
//
//        userService.save(user);
//        return "redirect:/patient/profile";
//    }
//
//    // ---------- Hospital/Clinic: Update Profile ----------
//    @PostMapping("/hospital/updateProfile")
//    public String updateHospitalProfile(@AuthenticationPrincipal UserDetails principal,
//                                        @RequestParam(required = false) String hname,
//                                        @RequestParam(required = false) String email,
//                                        @RequestParam(required = false) String contact,
//                                        @RequestParam(required = false) String address,
//                                        @RequestParam(required = false) String currentPassword,
//                                        @RequestParam(required = false) String newPassword,
//                                        @RequestParam(required = false) String confirmPassword,
//                                        Model model) {
//        if (principal == null) return "redirect:/login";
//
//        var user = userService.findByEmail(principal.getUsername());
//        if (user == null) return "redirect:/login";
//
//        String oldEmail = user.getEmail();
//
//        // ✅ keep username in sync with hname for Hospital/Clinic
//        if (hname != null && !hname.isBlank()) {
//            user.setHname(hname);
//            if ("Hospital".equalsIgnoreCase(user.getRole()) || "Clinic".equalsIgnoreCase(user.getRole())) {
//                user.setUsername(hname);   // ← this line makes the header update if it uses ${username}
//            }
//        }
//        if (email != null)   user.setEmail(email);
//        if (contact != null) user.setContact(contact);
//        if (address != null) user.setAddress(address);
//
//        if (newPassword != null && !newPassword.isBlank()) {
//            if (!newPassword.equals(confirmPassword)) {
//                model.addAttribute("error", "Passwords do not match!");
//                model.addAttribute("user", user);
//                return "hospital/profilePage/HospitalProfilePage";
//            }
//            if (currentPassword == null || !userService.checkPassword(user, currentPassword)) {
//                model.addAttribute("error", "Current password is incorrect!");
//                model.addAttribute("user", user);
//                return "hospital/profilePage/HospitalProfilePage";
//            }
//            userService.updatePassword(user, newPassword);
//        }
//
//        userService.save(user);
//
//        if (email != null && !email.equalsIgnoreCase(oldEmail)) {
//            SecurityContextHolder.clearContext();
//            return "redirect:/login?relogin";
//        }
//
//        return "redirect:/hospital/profile?updated";
//    }
//
//    // ===== Helpers =====
//    private void addUserToModel(UserDetails principal, Model model) {
//        if (principal == null) return;
//
//        String email = principal.getUsername();
//        User dbUser = userService.findByEmail(email);
//
//        String displayName;
//        if (dbUser == null) {
//            displayName = email;
//        } else if ("Hospital".equalsIgnoreCase(dbUser.getRole()) || "Clinic".equalsIgnoreCase(dbUser.getRole())) {
//            displayName = (dbUser.getHname() != null && !dbUser.getHname().isBlank())
//                    ? dbUser.getHname()
//                    : (dbUser.getUsername() != null && !dbUser.getUsername().isBlank())
//                    ? dbUser.getUsername()
//                    : dbUser.getEmail();
//        } else { // Patient or others
//            displayName = (dbUser.getPname() != null && !dbUser.getPname().isBlank())
//                    ? dbUser.getPname()
//                    : (dbUser.getUsername() != null && !dbUser.getUsername().isBlank())
//                    ? dbUser.getUsername()
//                    : dbUser.getEmail();
//        }
//
//        model.addAttribute("username", displayName);
//        model.addAttribute("user", dbUser);
//    }
//
//    private boolean isHospital(String role) { return role != null && role.equalsIgnoreCase("Hospital"); }
//    private boolean isClinic(String role)   { return role != null && role.equalsIgnoreCase("Clinic"); }
//
//    private Integer safeParseInt(String s) {
//        try { if (s == null || s.isBlank()) return null; return Integer.valueOf(s.trim()); }
//        catch (Exception e) { return null; }
//    }
//
//    private MultipartFile firstNonEmpty(List<MultipartFile> files) {
//        if (files == null) return null;
//        return files.stream().filter(f -> f != null && !f.isEmpty()).findFirst().orElse(null);
//    }
//}



package lk.vakapo.vakapo.UserManagement.Controller;

import lk.vakapo.vakapo.FeedbackManagement.Service.FeedbackService;
import lk.vakapo.vakapo.UserManagement.model.User;
import lk.vakapo.vakapo.UserManagement.Service.UserService;
import lk.vakapo.vakapo.Common.FileStorageService;
import org.springframework.http.MediaType;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.Collection;
import java.util.List;

@Controller
public class UserController {

    private final UserService userService;
    private final FileStorageService storage;
    private final FeedbackService feedbackService;

    public UserController(UserService userService,
                          FileStorageService storage,
                          FeedbackService feedbackService) {
        this.userService = userService;
        this.storage = storage;
        this.feedbackService = feedbackService;
    }

    // ---------- SIGNUP (Form shown by AuthPageController) ----------
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

            // UI posts name="rcertificate" (can be multiple)
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

        final String normRole = (role == null) ? "" : role.trim();

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
        u.setPassword(password); // encode in service if needed

        try {
            if (isHospital(normRole) || isClinic(normRole)) {
                MultipartFile chosen = firstNonEmpty(rcertificate);
                if (chosen != null && !chosen.isEmpty()) {
                    String relative = storage.saveUnderRoleAndUser(chosen, normRole, username);
                    u.setRcertificate(relative);
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

        Collection<? extends GrantedAuthority> auths = principal.getAuthorities();
        if (auths == null || auths.isEmpty()) return "redirect:/";

        String authority = auths.iterator().next().getAuthority();
        switch (authority) {
            case "ROLE_PATIENT":  return "redirect:/patient/home";
            case "ROLE_HOSPITAL": return "redirect:/hospital/home";
            case "ROLE_CLINIC":   return "redirect:/clinic/home";
            default:              return "redirect:/";
        }
    }

    // ---------- Role home pages (username/user + page-specific reviews) ----------
    @GetMapping("/patient/home")
    public String patientHome(@AuthenticationPrincipal UserDetails principal, Model model) {
        addUserToModel(principal, model);
        addReviews(model, 6, "patient");
        return "patient/landingPage/PatientLandingPage";
    }

    @GetMapping("/hospital/home")
    public String hospitalHome(@AuthenticationPrincipal UserDetails principal, Model model) {
        addUserToModel(principal, model);
        addReviews(model, 6, "hospital");
        return "hospital/landingPage/HospitalLandingPage";
    }

    @GetMapping("/clinic/home")
    public String clinicHome(@AuthenticationPrincipal UserDetails principal, Model model) {
        addUserToModel(principal, model);
        addReviews(model, 6, "clinic");
        return "clinic/landingPage/ClinicLandingPage";
    }

    // ---------- Profiles ----------
    @GetMapping("/patient/profile")
    public String patientProfile(@AuthenticationPrincipal UserDetails principal, Model model) {
        if (principal == null) return "redirect:/login";
        var dbUser = userService.findByEmail(principal.getUsername());
        if (dbUser == null) return "redirect:/login?not_found";
        model.addAttribute("user", dbUser);
        model.addAttribute("username", dbUser.getUsername());
        return "patient/profilePage/ProfilePage";
    }

    @GetMapping("/hospital/profile")
    public String hospitalProfile(@AuthenticationPrincipal UserDetails principal, Model model) {
        addUserToModel(principal, model);
        return "hospital/profilePage/HospitalProfilePage";
    }

    // ---------- Account (self-view + self-delete) ----------
    @GetMapping("/account")
    public String account(@AuthenticationPrincipal UserDetails principal, Model model) {
        if (principal == null) return "redirect:/login";
        addUserToModel(principal, model);
        return "Account";
    }

    @PostMapping("/account/delete")
    public String deleteOwnAccount(@AuthenticationPrincipal UserDetails principal) {
        if (principal == null) return "redirect:/login";

        User dbUser = userService.findByEmail(principal.getUsername());
        if (dbUser != null) {
            try {
                String role = dbUser.getRole();
                String uname = dbUser.getUsername();
                if (role != null && uname != null) {
                    storage.deleteUserFolder(role, uname);
                }
            } catch (Exception ignored) {}
        }

        userService.deleteByEmail(principal.getUsername());
        SecurityContextHolder.clearContext();
        return "redirect:/login?deleted";
    }

    // ---------- Patient: Update Profile ----------
    @PostMapping("/patient/updateProfile")
    public String updatePatientProfile(@AuthenticationPrincipal UserDetails principal,
                                       @RequestParam String nic,
                                       @RequestParam String username,
                                       @RequestParam String email,
                                       @RequestParam String contact,
                                       @RequestParam(required = false) String currentPassword,
                                       @RequestParam(required = false) String newPassword,
                                       @RequestParam(required = false) String confirmPassword,
                                       Model model) {
        if (principal == null) return "redirect:/login";

        var user = userService.findByEmail(principal.getUsername());
        if (user == null) return "redirect:/login";

        // Update fields
        user.setNic(nic);
        user.setUsername(username);
        user.setEmail(email);
        user.setContact(contact);

        // Change password if provided and valid
        if (newPassword != null && !newPassword.isBlank()) {
            if (!newPassword.equals(confirmPassword)) {
                model.addAttribute("error", "Passwords do not match!");
                model.addAttribute("user", user);
                return "patient/profilePage/ProfilePage";
            }
            if (currentPassword == null || !userService.checkPassword(user, currentPassword)) {
                model.addAttribute("error", "Current password incorrect!");
                model.addAttribute("user", user);
                return "patient/profilePage/ProfilePage";
            }
            userService.updatePassword(user, newPassword);
        }

        userService.save(user);
        return "redirect:/patient/profile";
    }

    // ---------- Hospital/Clinic: Update Profile ----------
    @PostMapping("/hospital/updateProfile")
    public String updateHospitalProfile(@AuthenticationPrincipal UserDetails principal,
                                        @RequestParam(required = false) String hname,
                                        @RequestParam(required = false) String email,
                                        @RequestParam(required = false) String contact,
                                        @RequestParam(required = false) String address,
                                        @RequestParam(required = false) String currentPassword,
                                        @RequestParam(required = false) String newPassword,
                                        @RequestParam(required = false) String confirmPassword,
                                        Model model) {
        if (principal == null) return "redirect:/login";

        var user = userService.findByEmail(principal.getUsername());
        if (user == null) return "redirect:/login";

        String oldEmail = user.getEmail();

        // keep username in sync with hname for Hospital/Clinic
        if (hname != null && !hname.isBlank()) {
            user.setHname(hname);
            if ("Hospital".equalsIgnoreCase(user.getRole()) || "Clinic".equalsIgnoreCase(user.getRole())) {
                user.setUsername(hname);
            }
        }
        if (email != null)   user.setEmail(email);
        if (contact != null) user.setContact(contact);
        if (address != null) user.setAddress(address);

        if (newPassword != null && !newPassword.isBlank()) {
            if (!newPassword.equals(confirmPassword)) {
                model.addAttribute("error", "Passwords do not match!");
                model.addAttribute("user", user);
                return "hospital/profilePage/HospitalProfilePage";
            }
            if (currentPassword == null || !userService.checkPassword(user, currentPassword)) {
                model.addAttribute("error", "Current password is incorrect!");
                model.addAttribute("user", user);
                return "hospital/profilePage/HospitalProfilePage";
            }
            userService.updatePassword(user, newPassword);
        }

        userService.save(user);

        if (email != null && !email.equalsIgnoreCase(oldEmail)) {
            SecurityContextHolder.clearContext();
            return "redirect:/login?relogin";
        }

        return "redirect:/hospital/profile?updated";
    }

    // ===== Helpers =====
    private void addUserToModel(UserDetails principal, Model model) {
        if (principal == null) return;

        String email = principal.getUsername();
        User dbUser = userService.findByEmail(email);

        String displayName;
        if (dbUser == null) {
            displayName = email;
        } else if ("Hospital".equalsIgnoreCase(dbUser.getRole()) || "Clinic".equalsIgnoreCase(dbUser.getRole())) {
            displayName = (dbUser.getHname() != null && !dbUser.getHname().isBlank())
                    ? dbUser.getHname()
                    : (dbUser.getUsername() != null && !dbUser.getUsername().isBlank())
                    ? dbUser.getUsername()
                    : dbUser.getEmail();
        } else { // Patient or others
            displayName = (dbUser.getPname() != null && !dbUser.getPname().isBlank())
                    ? dbUser.getPname()
                    : (dbUser.getUsername() != null && !dbUser.getUsername().isBlank())
                    ? dbUser.getUsername()
                    : dbUser.getEmail();
        }

        model.addAttribute("username", displayName);
        model.addAttribute("user", dbUser);
    }

    // page-specific (source-tagged) reviews helper
    private void addReviews(Model model, int limit, String sourcePage) {
        try {
            model.addAttribute("reviews", feedbackService.getRecentPublishedFor(sourcePage, Math.max(1, limit)));
        } catch (Exception e) {
            model.addAttribute("reviews", List.of()); // fail-soft
        }
    }

    private boolean isHospital(String role) { return role != null && role.equalsIgnoreCase("Hospital"); }
    private boolean isClinic(String role)   { return role != null && role.equalsIgnoreCase("Clinic"); }

    private Integer safeParseInt(String s) {
        try { if (s == null || s.isBlank()) return null; return Integer.valueOf(s.trim()); }
        catch (Exception e) { return null; }
    }

    private MultipartFile firstNonEmpty(List<MultipartFile> files) {
        if (files == null) return null;
        return files.stream().filter(f -> f != null && !f.isEmpty()).findFirst().orElse(null);
    }
}
