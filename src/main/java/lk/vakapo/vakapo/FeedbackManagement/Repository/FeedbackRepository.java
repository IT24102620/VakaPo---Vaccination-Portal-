package lk.vakapo.vakapo.FeedbackManagement.Repository;


import lk.vakapo.vakapo.FeedbackManagement.model.Feedback;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface FeedbackRepository extends JpaRepository<Feedback, Long> {

    // Latest public reviews (any page)
    List<Feedback> findTop20ByStatusOrderByCreatedAtDesc(String status);

    // Latest public reviews per page
    List<Feedback> findTop20ByStatusAndSourcePageOrderByCreatedAtDesc(String status, String sourcePage);
}
