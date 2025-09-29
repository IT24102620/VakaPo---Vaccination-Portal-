package lk.vakapo.vakapo.UserManagement.Service;

import lk.vakapo.vakapo.UserManagement.model.User;
import lk.vakapo.vakapo.UserManagement.Repository.UserRepository;
import org.springframework.stereotype.Service;

@Service
public class UserService {
    private final UserRepository repo;

    public UserService(UserRepository repo) { this.repo = repo; }

    public User save(User user) { return repo.save(user); }

    public User findByEmail(String email) { return repo.findByEmail(email).orElse(null); }
}
