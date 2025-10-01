package lk.vakapo.vakapo.UserManagement.Service;

import lk.vakapo.vakapo.UserManagement.Repository.UserRepository;
import lk.vakapo.vakapo.UserManagement.model.User;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
public class UserService {

    private final UserRepository repo;
    private final PasswordEncoder passwordEncoder;

    public UserService(UserRepository repo, PasswordEncoder passwordEncoder) {
        this.repo = repo;
        this.passwordEncoder = passwordEncoder;
    }

    // ---------- SAVE / REGISTER ----------
    @Transactional
    public User save(User user) {
        normalizeUser(user);
        return repo.save(user);
    }

    /**
     * Registers a user, encoding the password if it isn't already encoded.
     * Supports BCrypt ("$2a/$2b/$2y...") and DelegatingPasswordEncoder ("{id}...").
     */
    @Transactional
    public User register(User user) {
        normalizeUser(user);
        String pwd = user.getPassword();
        if (pwd != null && !looksEncoded(pwd)) {
            user.setPassword(passwordEncoder.encode(pwd));
        }
        return repo.save(user);
    }

    private boolean looksEncoded(String pwd) {
        return pwd.startsWith("$2a") || pwd.startsWith("$2b") || pwd.startsWith("$2y") || pwd.startsWith("{");
    }

    private void normalizeUser(User u) {
        if (u == null) return;
        if (u.getEmail() != null)   u.setEmail(u.getEmail().trim());
        if (u.getUsername() != null)u.setUsername(u.getUsername().trim());
        if (u.getContact() != null) u.setContact(u.getContact().trim());
        if (u.getAddress() != null) u.setAddress(u.getAddress().trim());
        if (u.getRnumber() != null) u.setRnumber(u.getRnumber().trim());
        if (u.getInstitution() != null) u.setInstitution(u.getInstitution().trim());
        if (u.getNic() != null)     u.setNic(u.getNic().trim());
        if (u.getHname() != null)   u.setHname(u.getHname().trim());
    }

    // ---------- FIND ----------
    public User findByEmail(String email) {
        if (email == null) return null;
        return repo.findByEmail(email.trim()).orElse(null);
    }

    public User findByUsername(String username) {
        if (username == null) return null;
        return repo.findByUsername(username.trim()).orElse(null);
    }

    /** Try email first, then username — useful when principal could be either. */
    public User findByEmailOrUsername(String loginId) {
        if (loginId == null) return null;
        String key = loginId.trim();
        User byEmail = repo.findByEmail(key).orElse(null);
        if (byEmail != null) return byEmail;
        return repo.findByUsername(key).orElse(null);
    }

    public Optional<User> findById(Integer id) {
        return repo.findById(id);
    }

    public boolean emailExists(String email) {
        if (email == null) return false;
        return repo.findByEmail(email.trim()).isPresent();
    }

    /** Check if an email is used by someone else (exclude a specific user id). */
    public boolean emailExistsOtherThan(String email, Integer excludeId) {
        if (email == null || excludeId == null) return false;
        return repo.existsByEmailAndIdNot(email.trim(), excludeId);
    }

    // ---------- UPDATE (file path) ----------
    @Transactional
    public User updateCertificatePath(Integer userId, String relativePath) {
        Optional<User> existing = repo.findById(userId);
        if (existing.isEmpty()) return null;

        User user = existing.get();
        user.setRcertificate(relativePath);
        return repo.save(user);
    }

    // ---------- UPDATE (patient profile fields) ----------
    @Transactional
    public User updatePatientProfileFields(User user,
                                           String nic,
                                           String username,
                                           String email,
                                           String contact) {
        if (nic != null) user.setNic(nic.trim());
        if (username != null) user.setUsername(username.trim());
        if (email != null) user.setEmail(email.trim());
        if (contact != null) user.setContact(contact.trim());
        normalizeUser(user);
        return repo.save(user);
    }

    // ---------- UPDATE (hospital profile fields) ----------
    @Transactional
    public User updateHospitalProfileFields(User user,
                                            String hname,
                                            String email,
                                            String contact,
                                            String address,
                                            String rnumber,
                                            String institution) {
        if (hname != null) user.setHname(hname.trim());        // or setUsername if that's your design
        if (email != null) user.setEmail(email.trim());
        if (contact != null) user.setContact(contact.trim());
        if (address != null) user.setAddress(address.trim());
        if (rnumber != null) user.setRnumber(rnumber.trim());
        if (institution != null) user.setInstitution(institution.trim());
        normalizeUser(user);
        return repo.save(user);
    }

    // ---------- PASSWORD HELPERS ----------
    /** Returns true if rawPassword matches user's stored (hashed) password */
    public boolean checkPassword(User user, String rawPassword) {
        if (user == null || rawPassword == null) return false;
        return passwordEncoder.matches(rawPassword, user.getPassword());
    }

    /** Encodes and updates the user's password */
    @Transactional
    public void updatePassword(User user, String newRawPassword) {
        if (user == null || newRawPassword == null) return;
        user.setPassword(passwordEncoder.encode(newRawPassword));
        repo.save(user);
    }

    /** Encodes and updates password by id (convenience) */
    @Transactional
    public boolean updatePassword(Integer userId, String newRawPassword) {
        if (userId == null || newRawPassword == null) return false;
        Optional<User> opt = repo.findById(userId);
        if (opt.isEmpty()) return false;
        User u = opt.get();
        u.setPassword(passwordEncoder.encode(newRawPassword));
        repo.save(u);
        return true;
    }

    // ---------- DELETE ----------
    @Transactional
    public boolean deleteByEmail(String email) {
        if (email == null) return false;
        Optional<User> existing = repo.findByEmail(email.trim());
        if (existing.isPresent()) {
            repo.delete(existing.get());
            return true;
        }
        return false;
    }

    @Transactional
    public boolean deleteById(Integer id) {
        if (id == null) return false;
        if (repo.existsById(id)) {
            repo.deleteById(id);
            return true;
        }
        return false;
    }
}
