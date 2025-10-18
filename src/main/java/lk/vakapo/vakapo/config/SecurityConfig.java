package lk.vakapo.vakapo.config;

import lk.vakapo.vakapo.UserManagement.service.CustomUserDetailsService;
import lk.vakapo.vakapo.UserManagement.service.RoleBasedAuthSuccessHandler;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
public class SecurityConfig {

    // Plain-text encoder (as you requested)
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new PasswordEncoder() {
            @Override public String encode(CharSequence raw) { return raw.toString(); }
            @Override public boolean matches(CharSequence raw, String encoded) {
                return encoded != null && encoded.contentEquals(raw);
            }
        };
    }

    // Use your @Service CustomUserDetailsService directly here
    @Bean
    public AuthenticationProvider authenticationProvider(CustomUserDetailsService uds,
                                                         PasswordEncoder pe) {
        DaoAuthenticationProvider dap = new DaoAuthenticationProvider();
        dap.setUserDetailsService(uds);
        dap.setPasswordEncoder(pe);
        return dap;
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration cfg) throws Exception {
        return cfg.getAuthenticationManager();
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http,
                                                   RoleBasedAuthSuccessHandler successHandler) throws Exception {
        http
                .csrf(csrf -> csrf.disable())
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/", "/index", "/landing",
                                "/css/**", "/js/**", "/images/**", "/uploads/**").permitAll()
                        .requestMatchers("/login", "/register", "/error", "/auth/**", "/contact").permitAll()
                        .requestMatchers("/forgot-password", "/reset-password").permitAll()
                        .requestMatchers("/api/data/**", "/favicon.ico").permitAll()
                        .requestMatchers("/api/notifications/**").authenticated()
                        // Allow public emergency notification endpoint for all authenticated users
                        .requestMatchers("/api/public/**").authenticated()
                        // Staff endpoints - allow access for users with STAFF_ prefix in ID
                        .requestMatchers("/staff/**").authenticated()
                        // Hospital endpoints - allow access for users with role HOSPITAL
                        .requestMatchers("/hospital/**").hasRole("HOSPITAL")
                        // Clinic endpoints - allow access for users with role CLINIC
                        .requestMatchers("/clinic/**").hasRole("CLINIC")
                        // Doctor endpoints - allow access for users with role DOCTOR
                        .requestMatchers("/doctor/**").hasRole("DOCTOR")
                        // Nurse endpoints - allow access for users with role NURSE
                        .requestMatchers("/nurse/**").hasRole("NURSE")
                        // Patient endpoints - allow access for users with role PATIENT
                        .requestMatchers("/patient/**").hasRole("PATIENT")
                        // Restrict admin endpoints to users with role ADMIN (must be last)
                        .requestMatchers("/admin/**").hasRole("ADMIN")
                        .anyRequest().authenticated()
                )
                .formLogin(form -> form
                        .loginPage("/login")
                        .loginProcessingUrl("/login")
                        .usernameParameter("email")
                        .passwordParameter("password")
                        .successHandler(successHandler)
                        .failureUrl("/login?error")
                        .permitAll()
                )
                .logout(l -> l.logoutUrl("/logout").logoutSuccessUrl("/").permitAll());
        return http.build();
    }
}
