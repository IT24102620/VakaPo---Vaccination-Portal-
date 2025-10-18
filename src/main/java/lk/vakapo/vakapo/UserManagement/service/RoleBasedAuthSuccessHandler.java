// lk/vakapo/vakapo/UserManagement/service/RoleBasedAuthSuccessHandler.java
package lk.vakapo.vakapo.UserManagement.service;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import lk.vakapo.vakapo.UserManagement.model.Clinic;
import lk.vakapo.vakapo.UserManagement.model.Hospital;
import lk.vakapo.vakapo.UserManagement.model.UserAccount;
import lk.vakapo.vakapo.UserManagement.repository.ClinicRepository;
import lk.vakapo.vakapo.UserManagement.repository.HospitalRepository;
import lk.vakapo.vakapo.UserManagement.repository.UserRepository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
public class RoleBasedAuthSuccessHandler implements AuthenticationSuccessHandler {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private HospitalRepository hospitalRepository;

    @Autowired
    private ClinicRepository clinicRepository;

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request,
                                        HttpServletResponse response,
                                        Authentication authentication)
            throws IOException, ServletException {

        // 1) Who just logged in? Spring Security username is your email.
        String email = authentication.getName();
        UserAccount user = (email == null) ? null : userRepository.findByEmail(email).orElse(null);

        // 2) If this is a Hospital or Clinic, enforce admin approval before landing.
        if (user != null) {
            String role = user.getRole() == null ? "" : user.getRole();

            if (role.equalsIgnoreCase("Hospital")) {
                // We store the same pretty ID in Users and Hospital, so fetch by ID
                Hospital h = hospitalRepository.findById(user.getId()).orElse(null);

                // If no record or not approved -> bounce back to login with flag
                if (h == null || !"approved".equalsIgnoreCase(h.getAdminApproval())) {
                    invalidateAndRedirectToPending(request, response);
                    return;
                }
            } else if (role.equalsIgnoreCase("Clinic")) {
                Clinic c = clinicRepository.findById(user.getId()).orElse(null);
                
                if (c == null || !"approved".equalsIgnoreCase(c.getAdminApproval())) {
                    invalidateAndRedirectToPending(request, response);
                    return;
                }
            }
        }

        // 3) Role-based redirects (Spring Security authorities are ROLE_*)
        // Check for admin first; if present, send to admin landing
        if (authentication.getAuthorities().contains(new SimpleGrantedAuthority("ROLE_ADMIN"))) {
            response.sendRedirect("/admin/landing");
            return;
        }
        // Handle Doctor role
        if (authentication.getAuthorities().contains(new SimpleGrantedAuthority("ROLE_DOCTOR"))) {
            // Check if doctor needs to change password
            if (user != null && isDefaultPassword(user)) {
                response.sendRedirect("/doctor/change-password");
                return;
            }
            response.sendRedirect("/doctor/landing");
            return;
        }
        // Handle Nurse role
        if (authentication.getAuthorities().contains(new SimpleGrantedAuthority("ROLE_NURSE"))) {
            // Check if nurse needs to change password
            if (user != null && isDefaultPassword(user)) {
                response.sendRedirect("/nurse/change-password");
                return;
            }
            response.sendRedirect("/nurse/landing");
            return;
        }
        if (authentication.getAuthorities().contains(new SimpleGrantedAuthority("ROLE_PATIENT"))) {
            // Check if this is a staff member and route to appropriate landing page
            if (user != null && isStaffMember(user.getId())) {
                if (isDoctor(user.getId())) {
                    response.sendRedirect("/doctor/landing");
                    return;
                } else if (isNurse(user.getId())) {
                    response.sendRedirect("/nurse/landing");
                    return;
                } else {
                    // Fallback to generic staff landing for other staff types
                    response.sendRedirect("/staff/landing");
                    return;
                }
            }
            response.sendRedirect("/patient/landing");
            return;
        }
        if (authentication.getAuthorities().contains(new SimpleGrantedAuthority("ROLE_HOSPITAL"))) {
            response.sendRedirect("/hospital/landing");
            return;
        }
        if (authentication.getAuthorities().contains(new SimpleGrantedAuthority("ROLE_CLINIC"))) {
            response.sendRedirect("/clinic/landing");
            return;
        }
        if (authentication.getAuthorities().contains(new SimpleGrantedAuthority("ROLE_STAFF"))) {
            response.sendRedirect("/staff/landing");
            return;
        }

        // 4) Fallback
        response.sendRedirect("/");
    }

    private void invalidateAndRedirectToPending(HttpServletRequest request,
                                                HttpServletResponse response) throws IOException {
        HttpSession session = request.getSession(false);
        if (session != null) session.invalidate();
        // Login page should show an alert when this param is present
        response.sendRedirect("/login?adminApproval=pending");
    }

    /**
     * Check if user ID indicates a staff member (Doctor or Nurse only)
     */
    private boolean isStaffMember(String userId) {
        if (userId == null) return false;
        
        // Check for staff ID patterns: Vak D (Doctor), Vak N (Nurse)
        return userId.startsWith("Vak D ") || 
               userId.startsWith("Vak N ");
    }

    /**
     * Check if user ID indicates a doctor
     */
    private boolean isDoctor(String userId) {
        if (userId == null) return false;
        return userId.startsWith("Vak D ");
    }

    /**
     * Check if user ID indicates a nurse
     */
    private boolean isNurse(String userId) {
        if (userId == null) return false;
        return userId.startsWith("Vak N ");
    }

    /**
     * Check if user has default password (needs to change)
     */
    private boolean isDefaultPassword(UserAccount user) {
        if (user == null) return false;
        return "1234567890".equals(user.getPassword());
    }

}
