package lk.vakapo.vakapo.UserManagement.service;

import lk.vakapo.vakapo.UserManagement.model.Feedback;
import lk.vakapo.vakapo.UserManagement.repository.FeedbackRepository;
import lk.vakapo.vakapo.MailManagement.EmailService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class FeedbackService {
    
    private final FeedbackRepository feedbackRepository;
    private final EmailService emailService;
    
    /**
     * Submit new feedback
     */
    public Feedback submitFeedback(Feedback feedback) {
        try {
            log.info("Submitting feedback from user: {} ({})", feedback.getName(), feedback.getUserType());
            log.info("Feedback details - Name: {}, Email: {}, Rating: {}, Message: {}", 
                feedback.getName(), feedback.getEmail(), feedback.getRating(), feedback.getMessage());
            
            // Set default values
            feedback.setIsApproved(false); // Requires admin approval
            feedback.setCreatedAt(LocalDateTime.now());
            
            log.info("About to save feedback to database...");
            Feedback savedFeedback = feedbackRepository.save(feedback);
            log.info("Feedback saved successfully with ID: {}", savedFeedback.getId());
            
            // Verify the save by trying to retrieve it
            Feedback retrievedFeedback = feedbackRepository.findById(savedFeedback.getId()).orElse(null);
            if (retrievedFeedback != null) {
                log.info("Verification successful - feedback retrieved from database: {}", retrievedFeedback.getId());
            } else {
                log.error("Verification failed - could not retrieve saved feedback");
            }
            
            return savedFeedback;
        } catch (Exception e) {
            log.error("Error submitting feedback: {}", e.getMessage(), e);
            log.error("Exception type: {}", e.getClass().getSimpleName());
            e.printStackTrace();
            throw new RuntimeException("Failed to submit feedback", e);
        }
    }
    
    /**
     * Get approved feedback for landing pages (limited to recent ones)
     */
    public List<Feedback> getApprovedFeedbackForLanding() {
        try {
            // Get the most recent 10 approved feedback
            List<Feedback> feedback = feedbackRepository.findApprovedFeedback();
            
            // Limit to 10 most recent
            if (feedback.size() > 10) {
                feedback = feedback.subList(0, 10);
            }
            
            log.info("Retrieved {} approved feedback for landing pages", feedback.size());
            return feedback;
        } catch (Exception e) {
            log.error("Error retrieving approved feedback: {}", e.getMessage(), e);
            return List.of(); // Return empty list on error
        }
    }
    
    /**
     * Get approved feedback by user type
     */
    public List<Feedback> getApprovedFeedbackByUserType(String userType) {
        try {
            List<Feedback> feedback = feedbackRepository.findByUserTypeAndIsApprovedTrueOrderByCreatedAtDesc(userType);
            log.info("Retrieved {} approved feedback for user type: {}", feedback.size(), userType);
            return feedback;
        } catch (Exception e) {
            log.error("Error retrieving feedback by user type {}: {}", userType, e.getMessage(), e);
            return List.of();
        }
    }
    
    /**
     * Get feedback by user ID
     */
    public List<Feedback> getFeedbackByUserId(String userId) {
        try {
            List<Feedback> feedback = feedbackRepository.findByUserIdOrderByCreatedAtDesc(userId);
            log.info("Retrieved {} feedback for user ID: {}", feedback.size(), userId);
            return feedback;
        } catch (Exception e) {
            log.error("Error retrieving feedback for user ID {}: {}", userId, e.getMessage(), e);
            return List.of();
        }
    }
    
    /**
     * Get all feedback (for admin)
     */
    public List<Feedback> getAllFeedback() {
        try {
            List<Feedback> feedback = feedbackRepository.findAllByOrderByCreatedAtDesc();
            log.info("Retrieved {} total feedback", feedback.size());
            return feedback;
        } catch (Exception e) {
            log.error("Error retrieving all feedback: {}", e.getMessage(), e);
            return List.of();
        }
    }
    
    /**
     * Get pending feedback (for admin approval)
     */
    public List<Feedback> getPendingFeedback() {
        try {
            List<Feedback> feedback = feedbackRepository.findByIsApprovedFalseOrderByCreatedAtDesc();
            log.info("Retrieved {} pending feedback", feedback.size());
            return feedback;
        } catch (Exception e) {
            log.error("Error retrieving pending feedback: {}", e.getMessage(), e);
            return List.of();
        }
    }
    
    /**
     * Approve feedback
     */
    public boolean approveFeedback(Long feedbackId, String approvedBy) {
        try {
            Optional<Feedback> feedbackOpt = feedbackRepository.findById(feedbackId);
            
            if (feedbackOpt.isPresent()) {
                Feedback feedback = feedbackOpt.get();
                feedback.setIsApproved(true);
                feedback.setApprovedAt(LocalDateTime.now());
                feedback.setApprovedBy(approvedBy);
                
                feedbackRepository.save(feedback);
                log.info("Feedback {} approved by {}", feedbackId, approvedBy);
                
                // Send email notification to the user
                try {
                    emailService.sendFeedbackApprovalNotification(
                        feedback.getEmail(),
                        feedback.getName(),
                        feedback.getUserType(),
                        feedback.getMessage()
                    );
                    log.info("Feedback approval email sent to: {}", feedback.getEmail());
                } catch (Exception emailException) {
                    log.error("Failed to send feedback approval email to {}: {}", 
                        feedback.getEmail(), emailException.getMessage(), emailException);
                    // Don't fail the approval process if email fails
                }
                
                return true;
            } else {
                log.warn("Feedback with ID {} not found for approval", feedbackId);
                return false;
            }
        } catch (Exception e) {
            log.error("Error approving feedback {}: {}", feedbackId, e.getMessage(), e);
            return false;
        }
    }
    
    /**
     * Reject feedback (delete it)
     */
    public boolean rejectFeedback(Long feedbackId) {
        return rejectFeedback(feedbackId, null);
    }
    
    /**
     * Reject feedback (delete it) with optional admin response
     */
    public boolean rejectFeedback(Long feedbackId, String adminResponse) {
        try {
            Optional<Feedback> feedbackOpt = feedbackRepository.findById(feedbackId);
            
            if (feedbackOpt.isPresent()) {
                Feedback feedback = feedbackOpt.get();
                
                // Send rejection email if admin provided a response
                if (adminResponse != null && !adminResponse.trim().isEmpty()) {
                    try {
                        emailService.sendFeedbackRejectionNotification(
                            feedback.getEmail(),
                            feedback.getName(),
                            feedback.getUserType(),
                            feedback.getMessage(),
                            adminResponse
                        );
                        log.info("Feedback rejection email sent to: {}", feedback.getEmail());
                    } catch (Exception emailException) {
                        log.error("Failed to send feedback rejection email to {}: {}", 
                            feedback.getEmail(), emailException.getMessage(), emailException);
                        // Don't fail the rejection process if email fails
                    }
                }
                
                feedbackRepository.delete(feedback);
                log.info("Feedback {} rejected and deleted", feedbackId);
                return true;
            } else {
                log.warn("Feedback with ID {} not found for rejection", feedbackId);
                return false;
            }
        } catch (Exception e) {
            log.error("Error rejecting feedback {}: {}", feedbackId, e.getMessage(), e);
            return false;
        }
    }
    
    /**
     * Get feedback statistics
     */
    public FeedbackStats getFeedbackStats() {
        try {
            long approvedCount = feedbackRepository.countByIsApprovedTrue();
            long pendingCount = feedbackRepository.countByIsApprovedFalse();
            
            // Calculate average rating from all approved feedback
            List<Feedback> approvedFeedback = feedbackRepository.findApprovedFeedback();
            double averageRating = 0.0;
            
            if (!approvedFeedback.isEmpty()) {
                double totalRating = approvedFeedback.stream()
                    .mapToInt(feedback -> feedback.getRating() != null ? feedback.getRating() : 0)
                    .sum();
                averageRating = totalRating / approvedFeedback.size();
            }
            
            return new FeedbackStats(approvedCount, pendingCount, averageRating);
        } catch (Exception e) {
            log.error("Error getting feedback statistics: {}", e.getMessage(), e);
            return new FeedbackStats(0, 0, 0.0);
        }
    }
    
    /**
     * Get feedback by ID
     */
    public Optional<Feedback> getFeedbackById(Long id) {
        try {
            return feedbackRepository.findById(id);
        } catch (Exception e) {
            log.error("Error retrieving feedback by ID {}: {}", id, e.getMessage(), e);
            return Optional.empty();
        }
    }
    
    /**
     * Get feedback list with pagination and filtering
     */
    public List<Feedback> getFeedbackList(int page, int size, String status, String search, String sortBy, String sortDir) {
        try {
            List<Feedback> allFeedback = feedbackRepository.findAllByOrderByCreatedAtDesc();
            
            // Apply status filter
            if (status != null && !status.isEmpty()) {
                if ("PENDING".equals(status)) {
                    allFeedback = allFeedback.stream()
                            .filter(f -> !f.getIsApproved())
                            .collect(java.util.stream.Collectors.toList());
                } else if ("APPROVED".equals(status)) {
                    allFeedback = allFeedback.stream()
                            .filter(f -> f.getIsApproved())
                            .collect(java.util.stream.Collectors.toList());
                }
            }
            
            // Apply search filter
            if (search != null && !search.isEmpty()) {
                String searchLower = search.toLowerCase();
                allFeedback = allFeedback.stream()
                        .filter(f -> (f.getName() != null && f.getName().toLowerCase().contains(searchLower)) ||
                                   (f.getEmail() != null && f.getEmail().toLowerCase().contains(searchLower)) ||
                                   (f.getMessage() != null && f.getMessage().toLowerCase().contains(searchLower)))
                        .collect(java.util.stream.Collectors.toList());
            }
            
            // Apply pagination
            int start = page * size;
            int end = Math.min(start + size, allFeedback.size());
            
            if (start >= allFeedback.size()) {
                return List.of();
            }
            
            return allFeedback.subList(start, end);
        } catch (Exception e) {
            log.error("Error retrieving feedback list: {}", e.getMessage(), e);
            return List.of();
        }
    }
    
    /**
     * Update existing feedback
     */
    public boolean updateFeedback(Long feedbackId, String userId, String name, String email, String contactNo, 
                                 Integer rating, String message) {
        try {
            Optional<Feedback> feedbackOpt = feedbackRepository.findById(feedbackId);
            
            if (feedbackOpt.isPresent()) {
                Feedback feedback = feedbackOpt.get();
                
                // Verify the user owns this feedback
                if (!userId.equals(feedback.getUserId())) {
                    log.warn("User {} attempted to update feedback {} they don't own", userId, feedbackId);
                    return false;
                }
                
                // Update feedback fields
                feedback.setName(name);
                feedback.setEmail(email);
                feedback.setContactNo(contactNo);
                feedback.setRating(rating);
                feedback.setMessage(message);
                feedback.setIsApproved(false); // Reset approval status when edited
                feedback.setApprovedAt(null);
                feedback.setApprovedBy(null);
                
                feedbackRepository.save(feedback);
                log.info("Feedback {} updated by user {}", feedbackId, userId);
                return true;
            } else {
                log.warn("Feedback with ID {} not found for update", feedbackId);
                return false;
            }
        } catch (Exception e) {
            log.error("Error updating feedback {}: {}", feedbackId, e.getMessage(), e);
            return false;
        }
    }
    
    /**
     * Delete feedback by user (only if they own it)
     */
    public boolean deleteFeedbackByUser(Long feedbackId, String userId) {
        try {
            Optional<Feedback> feedbackOpt = feedbackRepository.findById(feedbackId);
            
            if (feedbackOpt.isPresent()) {
                Feedback feedback = feedbackOpt.get();
                
                // Verify the user owns this feedback
                if (!userId.equals(feedback.getUserId())) {
                    log.warn("User {} attempted to delete feedback {} they don't own", userId, feedbackId);
                    return false;
                }
                
                feedbackRepository.delete(feedback);
                log.info("Feedback {} deleted by user {}", feedbackId, userId);
                return true;
            } else {
                log.warn("Feedback with ID {} not found for deletion", feedbackId);
                return false;
            }
        } catch (Exception e) {
            log.error("Error deleting feedback {}: {}", feedbackId, e.getMessage(), e);
            return false;
        }
    }
    
    /**
     * Inner class for feedback statistics
     */
    public static class FeedbackStats {
        private final long approvedCount;
        private final long pendingCount;
        private final double averageRating;
        
        public FeedbackStats(long approvedCount, long pendingCount, double averageRating) {
            this.approvedCount = approvedCount;
            this.pendingCount = pendingCount;
            this.averageRating = averageRating;
        }
        
        public long getApprovedCount() { return approvedCount; }
        public long getPendingCount() { return pendingCount; }
        public double getAverageRating() { return averageRating; }
    }
}
