package lk.vakapo.vakapo.FeedbackManagement.Service;


import lk.vakapo.vakapo.FeedbackManagement.Repository.FeedbackRepository;
import lk.vakapo.vakapo.FeedbackManagement.model.Feedback;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class FeedbackService {

    private final FeedbackRepository repo;

    public FeedbackService(FeedbackRepository repo) {
        this.repo = repo;
    }

    @Transactional
    public Feedback save(Feedback f) {
        // force PUBLIC to ensure visibility
        f.setStatus("PUBLIC");
        // default to "patient" if not set (your form sends this hidden field)
        if (f.getSourcePage() == null || f.getSourcePage().isBlank()) {
            f.setSourcePage("patient");
        }
        return repo.save(f);
    }

    // Recent for all pages combined (useful for shared widgets)
    @Transactional(readOnly = true)
    public List<Feedback> latestPublicReviews(int limit) {
        // repo method returns top 20; trim if smaller limit requested
        List<Feedback> top = repo.findTop20ByStatusOrderByCreatedAtDesc("PUBLIC");
        return top.size() > limit ? top.subList(0, limit) : top;
    }

    // Per page (landing/patient/hospital)
    @Transactional(readOnly = true)
    public List<Feedback> latestPublicReviewsByPage(String sourcePage, int limit) {
        List<Feedback> top = repo.findTop20ByStatusAndSourcePageOrderByCreatedAtDesc("PUBLIC", sourcePage);
        return top.size() > limit ? top.subList(0, limit) : top;
    }

    /**
     * Return the most recent published reviews for a given source page.
     *
     * <p>This method wraps {@link #latestPublicReviewsByPage(String, int)} but exposes a
     * simpler name for controllers. It trims the result list down to the provided limit
     * and never returns {@code null}.
     *
     * @param sourcePage the logical page identifier (e.g. "landing", "patient", "hospital", "clinic").
     * @param limit the maximum number of reviews to return; values less than 1 are treated as 1.
     * @return a list of published {@link Feedback} objects for the given source page, never {@code null}
     */
    public List<Feedback> getRecentPublishedFor(String sourcePage, int limit) {
        // normalize limit to at least 1
        int max = Math.max(1, limit);
        try {
            // Ignore the supplied sourcePage and always return the most recent public reviews across all sources.
            // This ensures that feedback posted from any page (landing, patient, hospital, clinic)
            // is visible everywhere. If you need to filter by page in the future, revert to
            // latestPublicReviewsByPage(sourcePage, max).
            return latestPublicReviews(max);
        } catch (Exception ex) {
            // fail soft: return empty list if anything goes wrong
            return List.of();
        }
    }
}
