package lk.vakapo.vakapo.AppointmentAndBookingManagement.Controller;

import jakarta.validation.Valid;
import lk.vakapo.vakapo.AppointmentAndBookingManagement.model.AppointmentAndBooking;
import lk.vakapo.vakapo.AppointmentAndBookingManagement.Service.AppointmentAndBookingService;
import lk.vakapo.vakapo.UserManagement.Service.UserService;
import lk.vakapo.vakapo.UserManagement.model.User;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.LocalDate;

@Controller
@RequestMapping("/appointments")
public class AppointmentAndBookingController {

    private final AppointmentAndBookingService appointmentService;
    private final UserService userService;

    public AppointmentAndBookingController(AppointmentAndBookingService appointmentService, UserService userService) {
        this.appointmentService = appointmentService;
        this.userService = userService;
    }

    @GetMapping
    public String listForCurrentUser(@AuthenticationPrincipal UserDetails principal, Model model) {
        Integer userId = resolveUserId(principal);
        model.addAttribute("appointments", appointmentService.getForUser(userId));
        // Supply a list of hospitals (users with role "Hospital") for the booking form's dropdown
        model.addAttribute("hospitals", userService.findByRole("Hospital"));
        // The view name corresponds to src/main/resources/templates/patient/appointmentPage/AppointmentPage.html
        return "patient/appointmentPage/AppointmentPage";
    }

    // Handles the form on AppointmentPage.html (fields: vaccine, hospital, date, time)
    @PostMapping
    public String book(
            @AuthenticationPrincipal UserDetails principal,
            @RequestParam("vaccine") String vaccine,
            @RequestParam("hospital") String hospital,
            @RequestParam("date") String date,
            @RequestParam("time") String time,
            RedirectAttributes ra
    ) {
        Integer userId = resolveUserId(principal);

        // Create a new appointment entity and populate it from the submitted form values
        AppointmentAndBooking a = new AppointmentAndBooking();
        a.setUserId(userId);
        a.setVaccineName(vaccine);
        a.setHospitalName(hospital);
        a.setAppointmentDate(LocalDate.parse(date)); // expects yyyy-MM-dd from <input type="date">
        a.setAppointmentTime(time);

        try {
            appointmentService.create(a);
            ra.addFlashAttribute("success", "Appointment booked successfully.");
        } catch (Exception ex) {
            ra.addFlashAttribute("error", ex.getMessage());
        }
        return "redirect:/appointments";
    }

    @PostMapping("/{id}/cancel")
    public String cancel(
            @PathVariable("id") Long id,
            @AuthenticationPrincipal UserDetails principal,
            RedirectAttributes ra
    ) {
        Integer userId = resolveUserId(principal);
        boolean isAdmin = hasAdminRole(principal); // implement role check if you use roles

        try {
            appointmentService.cancel(id, userId, isAdmin);
            ra.addFlashAttribute("success", "Appointment canceled.");
        } catch (Exception ex) {
            ra.addFlashAttribute("error", ex.getMessage());
        }
        return "redirect:/appointments";
    }

    // ---------- helpers ----------

    private Integer resolveUserId(UserDetails principal) {
        if (principal == null) throw new SecurityException("Not logged in.");
        String loginId = principal.getUsername(); // could be email or username in your setup
        User u = userService.findByEmail(loginId);
        if (u == null) {
            try { u = userService.findByUsername(loginId); } catch (Exception ignore) {}
        }
        if (u == null || u.getId() == null) {
            throw new IllegalStateException("Cannot resolve current user id.");
        }
        return u.getId();  // make sure your User.getId() returns Integer
    }

    private boolean hasAdminRole(UserDetails principal) {
        if (principal == null) return false;
        return principal.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equalsIgnoreCase("ROLE_ADMIN"));
    }
}
