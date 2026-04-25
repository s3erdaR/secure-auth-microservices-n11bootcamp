package com.n11bootcamp.jwtornek.controller;

import com.n11bootcamp.jwtornek.auth.TokenManager;
import com.n11bootcamp.jwtornek.entity.RefreshToken;
import com.n11bootcamp.jwtornek.entity.Role;
import com.n11bootcamp.jwtornek.entity.User;
import com.n11bootcamp.jwtornek.event.UserRegisteredEvent;
import com.n11bootcamp.jwtornek.producer.UserEventProducer;
import com.n11bootcamp.jwtornek.repository.RefreshTokenRepository;
import com.n11bootcamp.jwtornek.repository.RoleRepository;
import com.n11bootcamp.jwtornek.repository.UserRepository;
import com.n11bootcamp.jwtornek.request.LoginRequest;
import com.n11bootcamp.jwtornek.request.LogoutRequest;
import com.n11bootcamp.jwtornek.request.RefreshTokenRequest;
import com.n11bootcamp.jwtornek.request.RegisterRequest;
import com.n11bootcamp.jwtornek.response.AuthResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;


@RestController
@RequestMapping("/login")
public class AuthController {

    private final TokenManager tokenManager;
    private final AuthenticationManager authenticationManager;
    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;
    private final RefreshTokenRepository refreshTokenRepository;
    private final UserEventProducer userEventProducer;

    public AuthController(TokenManager tokenManager, AuthenticationManager authenticationManager, UserRepository userRepository, RoleRepository roleRepository, PasswordEncoder passwordEncoder, RefreshTokenRepository refreshTokenRepository, UserEventProducer userEventProducer) {
        this.tokenManager = tokenManager;
        this.authenticationManager = authenticationManager;
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
        this.passwordEncoder = passwordEncoder;
        this.refreshTokenRepository = refreshTokenRepository;
        this.userEventProducer = userEventProducer;
    }

    @PostMapping
    public ResponseEntity<AuthResponse> login(@RequestBody LoginRequest loginRequest) {

        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        loginRequest.getUsername(),
                        loginRequest.getPassword()
                )
        );

        User user = userRepository.findByUsername(loginRequest.getUsername())
                .orElseThrow();

        String accessToken = tokenManager.generateAccessToken(loginRequest.getUsername());
        String refreshTokenValue = tokenManager.generateRefreshToken(loginRequest.getUsername());

        RefreshToken refreshToken = new RefreshToken();
        refreshToken.setToken(refreshTokenValue);
        refreshToken.setUser(user);
        refreshToken.setExpiryDate(LocalDateTime.now().plusDays(7));
        refreshToken.setRevoked(false);

        refreshTokenRepository.save(refreshToken);

        return ResponseEntity.ok(new AuthResponse(accessToken, refreshTokenValue));
    }

    @PostMapping("/refresh-token")
    public ResponseEntity<AuthResponse> refreshToken(@RequestBody RefreshTokenRequest request) {

        String refreshTokenValue = request.getRefreshToken();

        RefreshToken storedToken = refreshTokenRepository.findByToken(refreshTokenValue)
                .orElse(null);

        if (storedToken == null) {
            return ResponseEntity.status(401).build();
        }

        if (storedToken.isRevoked()) {
            return ResponseEntity.status(401).build();
        }

        if (storedToken.getExpiryDate().isBefore(LocalDateTime.now())) {
            return ResponseEntity.status(401).build();
        }

        if (!tokenManager.tokenValidate(refreshTokenValue) || !tokenManager.isRefreshToken(refreshTokenValue)) {
            return ResponseEntity.status(401).build();
        }

        String username = tokenManager.getUsernameToken(refreshTokenValue);

        String newAccessToken = tokenManager.generateAccessToken(username);
        String newRefreshTokenValue = tokenManager.generateRefreshToken(username);

        storedToken.setRevoked(true);
        refreshTokenRepository.save(storedToken);

        RefreshToken newRefreshToken = new RefreshToken();
        newRefreshToken.setToken(newRefreshTokenValue);
        newRefreshToken.setUser(storedToken.getUser());
        newRefreshToken.setExpiryDate(LocalDateTime.now().plusDays(7));
        newRefreshToken.setRevoked(false);

        refreshTokenRepository.save(newRefreshToken);

        return ResponseEntity.ok(new AuthResponse(newAccessToken, newRefreshTokenValue));
    }

    @PostMapping("/register")
    public ResponseEntity<?> register(@RequestBody RegisterRequest request) {

        if (userRepository.existsByUsername(request.getUsername())) {
            return ResponseEntity.badRequest().body("Username already exists");
        }

        if (userRepository.existsByEmail(request.getEmail())) {
            return ResponseEntity.badRequest().body("Email already exists");
        }

        Role userRole = roleRepository.findByName("ROLE_USER")
                .orElseGet(() -> roleRepository.save(new Role("ROLE_USER")));

        User user = new User();
        user.setUsername(request.getUsername());
        user.setEmail(request.getEmail());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.getRoles().add(userRole);

        userRepository.save(user);

        userEventProducer.sendUserRegisteredEvent(
                new UserRegisteredEvent(user.getUsername(), user.getEmail())
        );

        return ResponseEntity.ok("User registered successfully");
    }

    @PostMapping("/logout")
    public ResponseEntity<?> logout(@RequestBody LogoutRequest request) {

        RefreshToken storedToken = refreshTokenRepository.findByToken(request.getRefreshToken())
                .orElse(null);

        if (storedToken == null) {
            return ResponseEntity.badRequest().body("Refresh token not found");
        }

        storedToken.setRevoked(true);
        refreshTokenRepository.save(storedToken);

        return ResponseEntity.ok("Logout successful");
    }

}
