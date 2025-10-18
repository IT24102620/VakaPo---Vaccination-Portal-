// lk/vakapo/vakapo/UserManagement/service/CustomUserDetailsService.java
package lk.vakapo.vakapo.UserManagement.service;

import lk.vakapo.vakapo.UserManagement.model.UserAccount;
import lk.vakapo.vakapo.UserManagement.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.*;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class CustomUserDetailsService implements UserDetailsService {

    private final UserRepository users;

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        // ---------------------------------------------------------------------
        // Support a built-in administrator account. If the supplied username
        // matches our predefined admin e-mail address, we return a static
        // UserDetails instance with role ADMIN and the corresponding password.
        // ---------------------------------------------------------------------
        if (email != null && email.equalsIgnoreCase("admin@gmail.com")) {
            SimpleGrantedAuthority adminAuth = new SimpleGrantedAuthority("ROLE_ADMIN");
            return User
                    .withUsername("admin@gmail.com")
                    .password("admin123")
                    .authorities(List.of(adminAuth))
                    .accountExpired(false)
                    .accountLocked(false)
                    .credentialsExpired(false)
                    .disabled(false)
                    .build();
        }

        UserAccount u = users.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("No user with email: " + email));

        // Map role to uppercase and prefix ROLE_
        String roleUpper = u.getRole() == null ? "PATIENT" : u.getRole().toUpperCase();
        SimpleGrantedAuthority auth = new SimpleGrantedAuthority("ROLE_" + roleUpper);

        // ✅ Modified logic: Always enable the account for authentication
        // The RoleBasedAuthSuccessHandler will check admin approval and redirect accordingly
        boolean statusApproved = "approved".equalsIgnoreCase(u.getStatus());
        
        // For patients, just check status. For hospitals/clinics, we'll handle approval in the success handler
        boolean enabled = statusApproved;

        return User
                .withUsername(u.getEmail())
                .password(u.getPassword())   // plain, see PasswordEncoder below
                .authorities(List.of(auth))
                .accountExpired(false)
                .accountLocked(false)
                .credentialsExpired(false)
                .disabled(!enabled)
                .build();
    }
}
