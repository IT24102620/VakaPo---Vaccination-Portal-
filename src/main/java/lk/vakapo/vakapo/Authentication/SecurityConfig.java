package lk.vakapo.vakapo.Authentication;

import lk.vakapo.vakapo.UserManagement.model.User;
import lk.vakapo.vakapo.UserManagement.Repository.UserRepository;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.core.userdetails.*;
import org.springframework.security.crypto.password.NoOpPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
public class SecurityConfig {

    // Plain-text password comparison (for demo only; NOT for production)
    @Bean
    public PasswordEncoder passwordEncoder() {
        return NoOpPasswordEncoder.getInstance();
    }

    // Load users from DB by email (email acts as the username)
    @Bean
    public UserDetailsService userDetailsService(UserRepository repo) {
        return usernameEmail -> {
            User u = repo.findByEmail(usernameEmail)
                    .orElseThrow(() -> new UsernameNotFoundException("User not found: " + usernameEmail));

            String role = (u.getRole() == null || u.getRole().isBlank())
                    ? "USER"
                    : u.getRole().toUpperCase(); // e.g., PATIENT / HOSPITAL / CLINIC

            return org.springframework.security.core.userdetails.User
                    .withUsername(u.getEmail())   // principal username = email
                    .password(u.getPassword())    // plain text (per your request)
                    .roles(role)                  // Spring will prefix ROLE_
                    .build();
        };
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                // ---- AUTHZ RULES ----
                .authorizeHttpRequests(auth -> auth
                        // public pages & assets
                        .requestMatchers(
                                "/",
                                "/login",
                                "/signup",
                                "/css/**",
                                "/js/**",
                                "/images/**",
                                "/favicon.ico"
                        ).permitAll()

                        // feedback form (GET + POST)
                        .requestMatchers("/feedback").permitAll()
                        .requestMatchers(HttpMethod.POST, "/feedback").permitAll()
                        .requestMatchers("/feedback", "/feedback/**").permitAll()

                        // everything else requires auth
                        .anyRequest().authenticated()
                )

                // ---- FORM LOGIN ----
                .formLogin(form -> form
                        .loginPage("/login")              // GET /login -> your page
                        .loginProcessingUrl("/login")     // POST /login -> handled by Spring
                        .usernameParameter("email")       // your form field name
                        .passwordParameter("password")
                        .defaultSuccessUrl("/redirect", true)
                        .permitAll()
                )

                // ---- LOGOUT ----
                .logout(logout -> logout
                        .logoutUrl("/logout")
                        .logoutSuccessUrl("/login?logout")
                        .permitAll()
                );

        // CSRF stays enabled (recommended). Ensure your feedback form includes the token:
        // <input type="hidden" th:name="${_csrf.parameterName}" th:value="${_csrf.token}" />
        return http.build();
    }
}
