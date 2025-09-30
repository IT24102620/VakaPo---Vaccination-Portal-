//package lk.vakapo.vakapo.UserManagement.Service;
//
//import lk.vakapo.vakapo.UserManagement.Repository.UserRepository;
//import lk.vakapo.vakapo.UserManagement.model.User;
//import org.springframework.stereotype.Service;
//import org.springframework.transaction.annotation.Transactional;
//
//import java.util.Optional;
//
//@Service
//public class UserService {
//
//    private final UserRepository repo;
//
//    public UserService(UserRepository repo) {
//        this.repo = repo;
//    }
//
//    // ---------- SAVE / REGISTER ----------
//    public User save(User user) {
//        return repo.save(user);
//    }
//
//    public User register(User user) {
//        return repo.save(user);
//    }
//
//    // ---------- FIND ----------
//    public User findByEmail(String email) {
//        return repo.findByEmail(email).orElse(null);
//    }
//
//    public boolean emailExists(String email) {
//        return repo.findByEmail(email).isPresent();
//    }
//
//    // ---------- UPDATE ----------
//    public User updateCertificatePath(Integer userId, String relativePath) {
//        Optional<User> existing = repo.findById(userId);
//        if (existing.isEmpty()) return null;
//
//        User user = existing.get();
//        user.setRcertificate(relativePath);
//        return repo.save(user);
//    }
//
//    // ---------- DELETE ----------
//    @Transactional
//    public boolean deleteByEmail(String email) {
//        Optional<User> existing = repo.findByEmail(email);
//        if (existing.isPresent()) {
//            repo.delete(existing.get());
//            return true;
//        }
//        return false;
//    }
//
//    @Transactional
//    public boolean deleteById(Integer id) {
//        if (repo.existsById(id)) {
//            repo.deleteById(id);
//            return true;
//        }
//        return false;
//    }
//}






//package lk.vakapo.vakapo.UserManagement.Service;
//
//import lk.vakapo.vakapo.UserManagement.Repository.UserRepository;
//import lk.vakapo.vakapo.UserManagement.model.User;
//import org.springframework.security.crypto.password.PasswordEncoder;
//import org.springframework.stereotype.Service;
//import org.springframework.transaction.annotation.Transactional;
//
//import java.util.Optional;
//
//@Service
//public class UserService {
//
//    private final UserRepository repo;
//    private final PasswordEncoder passwordEncoder;
//
//    public UserService(UserRepository repo, PasswordEncoder passwordEncoder) {
//        this.repo = repo;
//        this.passwordEncoder = passwordEncoder;
//    }
//
//    // ---------- SAVE / REGISTER ----------
//    @Transactional
//    public User save(User user) {
//        return repo.save(user);
//    }
//
//    @Transactional
//    public User register(User user) {
//        // ensure password is encoded if not already
//        if (user.getPassword() != null && !user.getPassword().startsWith("$2a")) {
//            user.setPassword(passwordEncoder.encode(user.getPassword()));
//        }
//        return repo.save(user);
//    }
//
//    // ---------- FIND ----------
//    public User findByEmail(String email) {
//        return repo.findByEmail(email).orElse(null);
//    }
//
//    public Optional<User> findById(Integer id) {
//        return repo.findById(id);
//    }
//
//    public boolean emailExists(String email) {
//        return repo.findByEmail(email).isPresent();
//    }
//
//    // ---------- UPDATE (file path) ----------
//    @Transactional
//    public User updateCertificatePath(Integer userId, String relativePath) {
//        Optional<User> existing = repo.findById(userId);
//        if (existing.isEmpty()) return null;
//
//        User user = existing.get();
//        user.setRcertificate(relativePath);
//        return repo.save(user);
//    }
//
//    // ---------- UPDATE (profile fields) ----------
//    @Transactional
//    public User updateProfileFields(User user,
//                                    String nic,
//                                    String username,
//                                    String email,
//                                    String contact) {
//
//        if (nic != null) user.setNic(nic);
//        if (username != null) user.setUsername(username);
//        if (email != null) user.setEmail(email);
//        if (contact != null) user.setContact(contact);
//
//        return repo.save(user);
//    }
//
//    // ---------- PASSWORD HELPERS ----------
//    /** Returns true if rawPassword matches user's stored (hashed) password */
//    public boolean checkPassword(User user, String rawPassword) {
//        if (user == null || rawPassword == null) return false;
//        return passwordEncoder.matches(rawPassword, user.getPassword());
//    }
//
//    /** Encodes and updates the user's password */
//    @Transactional
//    public void updatePassword(User user, String newRawPassword) {
//        user.setPassword(passwordEncoder.encode(newRawPassword));
//        repo.save(user);
//    }
//
//    // ---------- DELETE ----------
//    @Transactional
//    public boolean deleteByEmail(String email) {
//        Optional<User> existing = repo.findByEmail(email);
//        if (existing.isPresent()) {
//            repo.delete(existing.get());
//            return true;
//        }
//        return false;
//    }
//
//    @Transactional
//    public boolean deleteById(Integer id) {
//        if (repo.existsById(id)) {
//            repo.deleteById(id);
//            return true;
//        }
//        return false;
//    }
//}






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
        return repo.save(user);
    }

    /**
     * Registers a user, encoding the password if it isn't already encoded.
     * (Uses a simple guard: if it doesn't look encoded, encode.)
     */
    @Transactional
    public User register(User user) {
        String pwd = user.getPassword();
        if (pwd != null && !looksEncoded(pwd)) {
            user.setPassword(passwordEncoder.encode(pwd));
        }
        return repo.save(user);
    }

    private boolean looksEncoded(String pwd) {
        // Works for BCrypt and DelegatingPasswordEncoder ("{id}...")
        return pwd.startsWith("$2a") || pwd.startsWith("$2b") || pwd.startsWith("$2y") || pwd.startsWith("{");
    }

    // ---------- FIND ----------
    public User findByEmail(String email) {
        return repo.findByEmail(email).orElse(null);
    }

    public Optional<User> findById(Integer id) {
        return repo.findById(id);
    }

    public boolean emailExists(String email) {
        return repo.findByEmail(email).isPresent();
    }

    /** Check if an email is used by someone else (exclude a specific user id). */
    public boolean emailExistsOtherThan(String email, Integer excludeId) {
        // Ensure UserRepository has: boolean existsByEmailAndIdNot(String email, Integer id);
        return repo.existsByEmailAndIdNot(email, excludeId);
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
        if (nic != null) user.setNic(nic);
        if (username != null) user.setUsername(username);
        if (email != null) user.setEmail(email);
        if (contact != null) user.setContact(contact);
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
        if (hname != null) user.setHname(hname);        // or setUsername(...) if you store hosp name there
        if (email != null) user.setEmail(email);
        if (contact != null) user.setContact(contact);
        if (address != null) user.setAddress(address);
        if (rnumber != null) user.setRnumber(rnumber);
        if (institution != null) user.setInstitution(institution);
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
        user.setPassword(passwordEncoder.encode(newRawPassword));
        repo.save(user);
    }

    /** Encodes and updates password by id (convenience) */
    @Transactional
    public boolean updatePassword(Integer userId, String newRawPassword) {
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
        Optional<User> existing = repo.findByEmail(email);
        if (existing.isPresent()) {
            repo.delete(existing.get());
            return true;
        }
        return false;
    }

    @Transactional
    public boolean deleteById(Integer id) {
        if (repo.existsById(id)) {
            repo.deleteById(id);
            return true;
        }
        return false;
    }
}

