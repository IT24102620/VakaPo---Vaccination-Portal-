package lk.vakapo.vakapo.Controller;


import lk.vakapo.vakapo.Service.FeedbackService;
import lk.vakapo.vakapo.dto.FeedbackDTO;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/feedback")
public class FeedbackController {

    private final FeedbackService service;

    public FeedbackController(FeedbackService service) {
        this.service = service;
    }

    @PostMapping
    public ResponseEntity<String> submitFeedback(@RequestBody FeedbackDTO dto) {
        service.saveFeedback(dto);
        return ResponseEntity.ok("Feedback submitted successfully (Pending review)");
    }

    @GetMapping("/approved")
    public List<FeedbackDTO> getApprovedFeedbacks() {
        return service.getApprovedFeedbacks();
    }
}
