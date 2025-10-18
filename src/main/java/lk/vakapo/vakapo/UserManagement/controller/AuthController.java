package lk.vakapo.vakapo.UserManagement.controller;

import jakarta.validation.Valid;
import lk.vakapo.vakapo.UserManagement.dto.SignupRequest;
import lk.vakapo.vakapo.UserManagement.service.SignupService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@Slf4j
@RequestMapping("/auth")
public class AuthController {

    private final SignupService signupService;

    // consumes = multipart/form-data
    @PostMapping(value = "/signup", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<?> signup(@Valid @ModelAttribute SignupRequest req) {
        try {
            String userId = signupService.signup(req);
            return ResponseEntity.ok().body("{\"success\": true, \"userId\": \"" + userId + "\", \"message\": \"Registration successful!\"}");
        } catch (IllegalArgumentException e) {
            log.error("Signup validation error: {}", e.getMessage());
            return ResponseEntity.badRequest().body("{\"success\": false, \"error\": \"" + e.getMessage() + "\"}");
        } catch (Exception e) {
            log.error("Unexpected error during signup: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("{\"success\": false, \"error\": \"An unexpected error occurred. Please try again.\"}");
        }
    }

    // Check if email exists
    @GetMapping("/check-email")
    public ResponseEntity<?> checkEmail(@RequestParam String email) {
        try {
            boolean exists = signupService.emailExists(email);
            return ResponseEntity.ok().body("{\"exists\": " + exists + "}");
        } catch (Exception e) {
            log.error("Error checking email: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("{\"error\": \"Failed to check email availability\"}");
        }
    }


}
