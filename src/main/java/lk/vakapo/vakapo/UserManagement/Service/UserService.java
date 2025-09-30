package lk.vakapo.vakapo.UserManagement.Service;

import lk.vakapo.vakapo.UserManagement.Repository.UserRepository;
import lk.vakapo.vakapo.UserManagement.model.User;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class UserService {

    private final UserRepository repo;

    public UserService(UserRepository repo) {
        this.repo = repo;
    }

    /** Create or update a user (plain-text password per your current design). */
    public User save(User user) {
        return repo.save(user);
    }

    /** Convenience for registration flows (same as save, but clearer name). */
    public User register(User user) {
        return repo.save(user);
    }

    /** Finders */
    public User findByEmail(String email) {
        return repo.findByEmail(email).orElse(null);
    }


    /** Existence checks (useful to show “email/username already taken”). */
    public boolean emailExists(String email) {
        return repo.findByEmail(email).isPresent();
    }


    /**
     * Update only the rcertificate path for an existing user.
     * Returns the updated user or null if not found.
     */
    public User updateCertificatePath(Integer userId, String relativePath) {
        Optional<User> existing = repo.findById(userId);
        if (existing.isEmpty()) return null;

        User user = existing.get();
        user.setRcertificate(relativePath);
        return repo.save(user);
    }
}
