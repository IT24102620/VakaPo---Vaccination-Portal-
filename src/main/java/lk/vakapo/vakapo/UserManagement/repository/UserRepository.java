package lk.vakapo.vakapo.UserManagement.repository;

import java.util.List;
import java.util.Optional;

import lk.vakapo.vakapo.UserManagement.model.UserAccount;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface UserRepository extends JpaRepository<UserAccount, String> {
    boolean existsByEmail(String email);
    Optional<UserAccount> findByEmail(String email);

    List<UserAccount> findByRoleInAndAdminApproval(List<String> roles, String adminApproval);
    
    // Find users by specific role
    List<UserAccount> findByRole(String role);
    
    // Delete user by email
    void deleteByEmail(String email);
}
