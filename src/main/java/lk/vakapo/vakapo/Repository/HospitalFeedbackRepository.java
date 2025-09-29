package lk.vakapo.vakapo.Repository;


import lk.vakapo.vakapo.Model.*;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface HospitalFeedbackRepository extends JpaRepository<HospitalFeedback, Long> {
    List<HospitalFeedback> findByStatus(FeedbackStatus status);
}
