package lk.vakapo.vakapo.Service;

import lk.vakapo.vakapo.Model.Feedback;
import lk.vakapo.vakapo.Repository.FeedbackRepository;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.Optional;

@Service
public class FeedbackService {

    private final FeedbackRepository repository;

    public FeedbackService(FeedbackRepository repository) {
        this.repository = repository;
    }

    public Feedback saveFeedback(Feedback feedback) {
        feedback.setStatus(Feedback.Status.PENDING);
        return repository.save(feedback);
    }

    public List<Feedback> getAllFeedbacks() {
        return repository.findAll();
    }

    public List<Feedback> getApprovedFeedbacks() {
        return repository.findByStatus(Feedback.Status.APPROVED);
    }

    public Feedback updateStatus(Long id, Feedback.Status status) {
        Optional<Feedback> opt = repository.findById(id);
        if(opt.isPresent()){
            Feedback f = opt.get();
            f.setStatus(status);
            return repository.save(f);
        }
        return null;
    }
}
