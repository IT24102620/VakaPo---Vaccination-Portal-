
package lk.vakapo.vakapo.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class RoleLandingController {



    @GetMapping("/admin/landing")
    public String adminLanding() {
        return "admin/landingPage/AdminLandingPage";
    }

    @GetMapping("/admin/sub-admin-request")
    public String subAdmin() {
        return "redirect:/admin/sub-admin-request/requests";
    }



    @GetMapping("/admin/notifications")
    public String notifications() {
        return "redirect:/admin/delegate/notify/page";
    }
}
