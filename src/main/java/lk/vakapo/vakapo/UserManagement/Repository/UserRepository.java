package lk.vakapo.vakapo.UserManagement.Repository;

import lk.vakapo.vakapo.UserManagement.model.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Integer> {

    Optional<User> findByEmail(String email);

    Optional<User> findByUsername(String username);   // <--- ADD THIS

    boolean existsByEmailAndIdNot(String email, Integer id);
}
