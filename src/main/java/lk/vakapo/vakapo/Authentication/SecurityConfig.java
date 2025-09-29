package lk.vakapo.vakapo.Authentication;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
public class SecurityConfig {

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                // 1) Route access rules
                .authorizeHttpRequests(auth -> auth
                        // Public pages & static files
                        .requestMatchers(
                                "/",
                                "/login",
                                "/register",
                                "/feedback",
                                "/adminfeedback",
                                "/css/**",
                                "/js/**",
                                "/Images/**",
                                "/favicon.ico"
                        ).permitAll().anyRequest().authenticated()
                )


                .formLogin(form -> form
                        .loginPage("/login")
                        .loginProcessingUrl("/login")
                        .permitAll()
                )

                .logout(logout -> logout
                        .logoutUrl("/logout")
                        .logoutSuccessUrl("/")
                )
        ;

        return http.build();
    }
}
