package lk.vakapo.vakapo.Controller;



import lk.vakapo.vakapo.Service.FeedbackService;
import lk.vakapo.vakapo.dto.FeedbackDTO;
import lk.vakapo.vakapo.Model.FeedbackStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.*;

@RestController
@RequestMapping("/api/admin/feedback")
public class AdminFeedbackController {

    private final FeedbackService service;

    public AdminFeedbackController(FeedbackService service) {
        this.service = service;
    }

    @GetMapping("/all")
    public List<FeedbackDTO> getAllFeedbacks() {
        return service.getAllFeedbacks();
    }

    @PutMapping("/{category}/{id}/status")
    public ResponseEntity<String> updateStatus(
            @PathVariable String category,
            @PathVariable Long id,
            @RequestBody Map<String, String> body) {

        FeedbackStatus status = FeedbackStatus.valueOf(body.get("status").toUpperCase());
        service.updateStatus(category, id, status);
        return ResponseEntity.ok("Status updated successfully");
    }
}

