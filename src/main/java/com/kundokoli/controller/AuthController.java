package com.kundokoli.controller;

import com.kundokoli.config.JwtUtil;
import com.kundokoli.model.User;
import com.kundokoli.repository.UserRepository;
import lombok.*;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.http.*;
import org.springframework.security.authentication.*;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthenticationManager authenticationManager;
    private final JwtUtil jwtUtil;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginRequest request) {
        try {
            authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(request.getUsername(), request.getPassword())
            );
            String token = jwtUtil.generateToken(request.getUsername());
            return ResponseEntity.ok(Map.of("token", token, "username", request.getUsername()));
        } catch (AuthenticationException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("error", "Invalid credentials"));
        }
    }

    // Seeds the default admin user on startup (runs once)
    @Bean
    public CommandLineRunner seedAdmin() {
        return args -> {
            if (userRepository.findByUsername("saifnova").isEmpty()) {
                userRepository.save(User.builder()
                        .username("saifnova")
                        .password(passwordEncoder.encode("nova1234"))
                        .role("ROLE_ADMIN")
                        .build());
                System.out.println("✅ Default admin created: username=admin password=admin123");
            }
        };
    }


    @Data
    public static class LoginRequest {
        private String username;
        private String password;
    }
}


