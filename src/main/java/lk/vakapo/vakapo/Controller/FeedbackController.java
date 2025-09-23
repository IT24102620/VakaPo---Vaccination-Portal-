package lk.vakapo.vakapo.Controller;

import lk.vakapo.vakapo.Model.Feedback;
import lk.vakapo.vakapo.Service.FeedbackService;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/feedback")
@CrossOrigin(origins = "*")
public class FeedbackController {

    private final FeedbackService service;

    public FeedbackController(FeedbackService service) {
        this.service = service;
    }

    @PostMapping("/submit")
    public Feedback submitFeedback(@RequestBody Feedback feedback) {
        return service.saveFeedback(feedback);
    }

    @GetMapping("/all")
    public List<Feedback> getAllFeedbacks() {
        return service.getAllFeedbacks();
    }

    @PostMapping("/status")
    public Feedback updateStatus(@RequestParam Long id, @RequestParam Feedback.Status status) {
        return service.updateStatus(id, status);
    }
}
