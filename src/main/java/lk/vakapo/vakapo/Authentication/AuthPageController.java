package lk.vakapo.vakapo.Authentication;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class AuthPageController {
    @GetMapping("/")
    public String landing() { return "LandingPage"; }

    @GetMapping("/login")
    public String login() { return "Login"; }

    @GetMapping("/register")
    public String register() { return "register";

    }


    @GetMapping("/adminfeedback")
    public String adminfeedback() { return "adminFeedbackPage";

    }

    @GetMapping("/feedback")
    public String feedback() { return "FeedbackPage";
    }
}
