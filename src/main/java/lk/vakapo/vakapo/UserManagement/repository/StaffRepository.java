package lk.vakapo.vakapo.UserManagement.repository;

import lk.vakapo.vakapo.UserManagement.model.Staff;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface StaffRepository extends JpaRepository<Staff, Long> {

    // Find staff by email
    Optional<Staff> findByEmail(String email);

    // Find staff by invitation token
    Optional<Staff> findByInvitationToken(String token);

    // Find all staff for a specific institution
    @Query("SELECT s FROM Staff s WHERE s.institutionId = :institutionId AND s.institutionType = :institutionType")
    List<Staff> findByInstitutionIdAndType(@Param("institutionId") String institutionId, 
                                     @Param("institutionType") String institutionType);

    // Find all staff for a specific institution by role
    @Query("SELECT s FROM Staff s WHERE s.institutionId = :institutionId AND s.institutionType = :institutionType AND s.role = :role")
    List<Staff> findByInstitutionIdAndTypeAndRole(@Param("institutionId") String institutionId, 
                                                 @Param("institutionType") String institutionType,
                                                 @Param("role") String role);

    // Find pending invitations for an institution
    @Query("SELECT s FROM Staff s WHERE s.institutionId = :institutionId AND s.institutionType = :institutionType AND s.invitationAccepted = 'not approved'")
    List<Staff> findPendingInvitations(@Param("institutionId") String institutionId, 
                                       @Param("institutionType") String institutionType);

    // Find accepted staff for an institution
    @Query("SELECT s FROM Staff s WHERE s.institutionId = :institutionId AND s.institutionType = :institutionType AND s.invitationAccepted = 'approved'")
    List<Staff> findAcceptedStaff(@Param("institutionId") String institutionId, 
                                 @Param("institutionType") String institutionType);

    // Find staff by name and institution
    @Query("SELECT s FROM Staff s WHERE s.name = :name AND s.institutionId = :institutionId AND s.institutionType = :institutionType AND s.invitationAccepted = 'approved'")
    Optional<Staff> findByNameAndInstitution(@Param("name") String name, 
                                           @Param("institutionId") String institutionId, 
                                           @Param("institutionType") String institutionType);

    // Check if email is already invited to this institution
    @Query("SELECT COUNT(s) > 0 FROM Staff s WHERE s.email = :email AND s.institutionId = :institutionId AND s.institutionType = :institutionType")
    boolean existsByEmailAndInstitution(@Param("email") String email, 
                                       @Param("institutionId") String institutionId, 
                                       @Param("institutionType") String institutionType);

    // Find all institutions (hospitals/clinics) that have invited a specific doctor
    @Query("SELECT DISTINCT s.institutionId, s.institutionType FROM Staff s WHERE s.email = :email AND s.role = 'Doctor' AND s.invitationAccepted = 'approved'")
    List<Object[]> findInstitutionsByDoctorEmail(@Param("email") String email);

    // Find all accepted staff invitations for a specific doctor
    @Query("SELECT s FROM Staff s WHERE s.email = :email AND s.role = 'Doctor' AND s.invitationAccepted = 'approved'")
    List<Staff> findAcceptedDoctorInvitations(@Param("email") String email);

    // Find staff by email and role
    @Query("SELECT s FROM Staff s WHERE s.email = :email AND s.role = :role AND s.invitationAccepted = 'approved'")
    List<Staff> findByEmailAndRole(@Param("email") String email, @Param("role") String role);
    
    // Find staff by role
    List<Staff> findByRole(String role);
    
    // Deletion methods for cascading deletes
    void deleteByEmail(String email);
    void deleteByEmailAndInstitutionTypeAndInstitutionId(String email, String institutionType, String institutionId);
    
    // Find methods for deletion service
    List<Staff> findByInstitutionTypeAndInstitutionId(String institutionType, String institutionId);
    Optional<Staff> findByEmailAndInstitutionTypeAndInstitutionId(String email, String institutionType, String institutionId);
}
