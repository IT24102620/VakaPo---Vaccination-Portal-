package lk.vakapo.vakapo.Controller;

import lk.vakapo.vakapo.Model.Feedback;
import lk.vakapo.vakapo.Service.FeedbackService;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/landing")
@CrossOrigin(origins = "*")
public class LandingController {

    private final FeedbackService service;

    public LandingController(FeedbackService service) {
        this.service = service;
    }

    @GetMapping("/reviews")
    public List<Feedback> getApprovedReviews() {
        return service.getApprovedFeedbacks();
    }
}
