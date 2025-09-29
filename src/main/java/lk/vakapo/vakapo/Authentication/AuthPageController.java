package lk.vakapo.vakapo.Authentication;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class AuthPageController {

    @GetMapping("/")
    public String landing() {
        // If you don't have templates/LandingPage.html, use:
        // return "redirect:/login";
        return "LandingPage";
    }

    // IMPORTANT:
    // Do NOT put /login, /signup, /redirect or /{role}/home routes here.
}
