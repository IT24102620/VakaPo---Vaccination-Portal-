package lk.vakapo.vakapo.UserManagement.Repository;

import lk.vakapo.vakapo.UserManagement.model.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Integer> {

    Optional<User> findByEmail(String email);

    Optional<User> findByUsername(String username);   // <--- ADD THIS

    boolean existsByEmailAndIdNot(String email, Integer id);

    /**
     * Find all users with a specific role, ignoring case.  This is useful for
     * retrieving all hospitals or clinics when building appointment booking
     * functionality.  Spring Data automatically provides the implementation
     * based on the method name.
     *
     * @param role the role name (e.g. "Hospital", "Clinic", "Patient")
     * @return a list of users matching the given role
     */
    java.util.List<User> findByRoleIgnoreCase(String role);
}
