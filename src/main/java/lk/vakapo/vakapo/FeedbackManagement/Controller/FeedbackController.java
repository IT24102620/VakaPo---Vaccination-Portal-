package lk.vakapo.vakapo.FeedbackManagement.Controller;

import jakarta.validation.Valid;
import lk.vakapo.vakapo.FeedbackManagement.Service.FeedbackService;
import lk.vakapo.vakapo.FeedbackManagement.model.Feedback;
import lk.vakapo.vakapo.UserManagement.Service.UserService;
import lk.vakapo.vakapo.UserManagement.model.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

@Controller
public class FeedbackController {

    private static final Logger log = LoggerFactory.getLogger(FeedbackController.class);

    private final FeedbackService feedbackService;
    private final UserService userService;

    public FeedbackController(FeedbackService feedbackService, UserService userService) {
        this.feedbackService = feedbackService;
        this.userService = userService;
    }

    // ---- GET: show the form (prefill if logged in)
    @GetMapping("/feedback")
    public String feedbackForm(@AuthenticationPrincipal UserDetails principal, Model model) {
        if (!model.containsAttribute("feedback")) {
            Feedback fb = new Feedback();
            boolean prefill = false;

            if (principal != null) {
                String loginId = principal.getUsername(); // email or username depending on your setup
                User u = userService.findByEmail(loginId);
                if (u == null) {
                    try { u = userService.findByUsername(loginId); } catch (Exception ignore) {}
                }
                if (u != null) {
                    // map user → feedback fields
                    fb.setName(u.getUsername());    // or u.getFullName() if you have it
                    fb.setEmail(u.getEmail());
                    fb.setPhone(u.getPhone());      // adjust getter if different
                    fb.setUserId(u.getId());        // persist link
                    prefill = true;
                }
            }

            model.addAttribute("feedback", fb);
            model.addAttribute("prefill", prefill);
        }
        // This view name should match where your uploaded file is resolved.
        // If you've placed it under templates/common/feedbackPage/FeedbackPage.html, use that path:
        return "common/feedbackPage/FeedbackPage";
    }

    // ---- POST: handle submit
    @PostMapping("/feedback")
    public String submitFeedback(
            @AuthenticationPrincipal UserDetails principal,
            @Valid @ModelAttribute("feedback") Feedback feedback,
            BindingResult errors,
            Model model
    ) {
        if (errors.hasErrors()) {
            model.addAttribute("prefill", false); // fields are bound already
            return "common/feedbackPage/FeedbackPage";
        }

        // Ensure user link if logged in
        if (principal != null) {
            String loginId = principal.getUsername();
            User u = userService.findByEmail(loginId);
            if (u == null) { try { u = userService.findByUsername(loginId); } catch (Exception ignore) {} }
            if (u != null) {
                feedback.setUserId(u.getId());
                // If user changed the read-only fields somehow, you can re-enforce:
                if (feedback.getName() == null || feedback.getName().isBlank())  feedback.setName(u.getUsername());
                if (feedback.getEmail() == null || feedback.getEmail().isBlank()) feedback.setEmail(u.getEmail());
                if (feedback.getPhone() == null || feedback.getPhone().isBlank()) feedback.setPhone(u.getPhone());
            }
        }

        try {
            feedbackService.save(feedback);
            model.addAttribute("success", "Thank you! Your review has been posted.");
            // re-render a fresh form with prefill if available
            return feedbackForm(principal, model);
        } catch (Exception ex) {
            log.error("Failed to save feedback", ex);
            model.addAttribute("success", null);
            errors.reject("save.failed", "Sorry, something went wrong while saving your feedback.");
            return "common/feedbackPage/FeedbackPage";
        }
    }
}
