package lk.vakapo.vakapo.UserManagement.service;

import lk.vakapo.vakapo.UserManagement.model.Staff;
import lk.vakapo.vakapo.UserManagement.repository.StaffRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class StaffUserDetailsService implements UserDetailsService {

    private final StaffRepository staffRepository;

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        log.info("Loading staff user by email: {}", email);

        // Find staff member by email
        Staff staff = staffRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("No staff member found with email: " + email));

        log.info("Staff found: {} - Role: {}, Status: {}", email, staff.getRole(), staff.getInvitationAccepted());

        // Check if invitation is accepted
        if (!"approved".equalsIgnoreCase(staff.getInvitationAccepted())) {
            log.warn("Staff invitation not approved for: {}", email);
            throw new UsernameNotFoundException("Staff invitation not approved for: " + email);
        }

        // Map role to Spring Security authority
        String roleUpper = staff.getRole() == null ? "STAFF" : staff.getRole().toUpperCase();
        SimpleGrantedAuthority auth = new SimpleGrantedAuthority("ROLE_" + roleUpper);

        log.info("Staff authentication successful for: {} with role: {}", email, roleUpper);

        return User.builder()
                .username(staff.getEmail())
                .password("1234567890") // Default password for staff
                .authorities(List.of(auth))
                .accountExpired(false)
                .accountLocked(false)
                .credentialsExpired(false)
                .disabled(false) // Staff are enabled once invitation is accepted
                .build();
    }
}
