package lk.vakapo.vakapo.UserManagement.repository;

import lk.vakapo.vakapo.UserManagement.model.Feedback;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface FeedbackRepository extends JpaRepository<Feedback, Long> {
    
    /**
     * Find all approved feedback for public display
     */
    @Query("SELECT f FROM Feedback f WHERE f.isApproved = true ORDER BY f.createdAt DESC")
    List<Feedback> findApprovedFeedback();
    
    /**
     * Find all approved feedback with limit for landing pages
     */
    @Query("SELECT f FROM Feedback f WHERE f.isApproved = true ORDER BY f.createdAt DESC")
    List<Feedback> findApprovedFeedbackWithLimit();
    
    /**
     * Find feedback by user type
     */
    List<Feedback> findByUserTypeAndIsApprovedTrueOrderByCreatedAtDesc(String userType);
    
    /**
     * Find feedback by user ID
     */
    List<Feedback> findByUserIdOrderByCreatedAtDesc(String userId);
    
    /**
     * Find all feedback (for admin management)
     */
    List<Feedback> findAllByOrderByCreatedAtDesc();
    
    /**
     * Find pending feedback (for admin approval)
     */
    List<Feedback> findByIsApprovedFalseOrderByCreatedAtDesc();
    
    /**
     * Count approved feedback
     */
    long countByIsApprovedTrue();
    
    /**
     * Count pending feedback
     */
    long countByIsApprovedFalse();
}
