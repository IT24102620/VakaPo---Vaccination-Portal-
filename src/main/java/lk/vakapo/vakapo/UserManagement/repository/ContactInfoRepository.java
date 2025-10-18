package lk.vakapo.vakapo.UserManagement.repository;

import lk.vakapo.vakapo.UserManagement.model.ContactInfo;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface ContactInfoRepository extends JpaRepository<ContactInfo, Long> {
    
    // Find by status
    List<ContactInfo> findByStatus(String status);
    
    // Find by email
    List<ContactInfo> findByEmailAddress(String emailAddress);
    
    // Find by date range
    List<ContactInfo> findByCreatedAtBetween(LocalDateTime startDate, LocalDateTime endDate);
    
    // Find recent contacts (last 30 days)
    @Query("SELECT c FROM ContactInfo c WHERE c.createdAt >= :date ORDER BY c.createdAt DESC")
    List<ContactInfo> findRecentContacts(@Param("date") LocalDateTime date);
    
    // Count by status
    long countByStatus(String status);
    
    // Find pending contacts
    List<ContactInfo> findByStatusOrderByCreatedAtDesc(String status);
    
    
    // Paginated queries
    Page<ContactInfo> findByStatus(String status, Pageable pageable);
    
    // Search by name or email
    @Query("SELECT c FROM ContactInfo c WHERE c.fullName LIKE %:search% OR c.emailAddress LIKE %:search%")
    List<ContactInfo> searchByNameOrEmail(@Param("search") String search);
    
    // Get statistics
    @Query("SELECT COUNT(c) FROM ContactInfo c WHERE c.status = :status AND c.createdAt >= :startDate")
    long countByStatusAndDateRange(@Param("status") String status, @Param("startDate") LocalDateTime startDate);
}
