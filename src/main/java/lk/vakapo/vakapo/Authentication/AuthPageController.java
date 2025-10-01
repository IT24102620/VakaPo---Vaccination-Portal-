//package lk.vakapo.vakapo.Authentication;
//
//import org.springframework.stereotype.Controller;
//import org.springframework.web.bind.annotation.GetMapping;
//
//@Controller
//public class AuthPageController {
//
//    @GetMapping("/")
//    public String landing() {
//        // If you don't have templates/LandingPage.html, use:
//        // return "redirect:/login";
//        return "LandingPage";
//    }
//
//    // IMPORTANT:
//    // Do NOT put /login, /signup, /redirect or /{role}/home routes here.
//}



package lk.vakapo.vakapo.Authentication;

import lk.vakapo.vakapo.FeedbackManagement.Service.FeedbackService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class AuthPageController {

    private final FeedbackService feedbackService;

    public AuthPageController(FeedbackService feedbackService) {
        this.feedbackService = feedbackService;
    }

    @GetMapping("/")
    public String landing(Model model) {
        // 6 most recent public reviews for the landing page
        model.addAttribute("reviews", feedbackService.getRecentPublishedFor("landing", 6));
        return "LandingPage";
    }

    @GetMapping("/login")
    public String login() {
        return "Login";
    }

    @GetMapping("/signup")
    public String signup() {
        return "Signup";
    }
}
