package com.smartbudget.users.controller;

import com.smartbudget.users.dto.*;
import com.smartbudget.users.model.User;
import com.smartbudget.users.repository.UserRepository;
import com.smartbudget.users.security.JwtTokenProvider;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;

@RestController
@RequestMapping("/auth")
@Tag(name = "Authentication", description = "Authentication API")
public class AuthController {

    @Autowired
    private JwtTokenProvider jwtTokenProvider;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @PostMapping("/register")
    @Operation(summary = "Register a new user")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "User registered successfully", content = @Content(mediaType = "application/json", schema = @Schema(implementation = MessageResponse.class))),
            @ApiResponse(responseCode = "400", description = "User already exists or Email already exists", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class)))
    })
    public ResponseEntity<?> register(@RequestBody RegisterRequest registerRequest) {
        if (userRepository.findByUsername(registerRequest.getUsername()) != null) {
            return ResponseEntity.badRequest().body(new ErrorResponse(400, "Bad Request", "User already exists"));
        }
        if (userRepository.findByEmail(registerRequest.getEmail()) != null) {
            return ResponseEntity.badRequest().body(new ErrorResponse(400, "Bad Request", "Email already exists"));
        }

        User user = new User();
        user.setUsername(registerRequest.getUsername());
        user.setPassword(passwordEncoder.encode(registerRequest.getPassword()));
        user.setEmail(registerRequest.getEmail());
        user.setActive(true);
        user.setCreatedAt(Instant.now());
        userRepository.save(user);
        return ResponseEntity.ok(new MessageResponse("User registered successfully"));
    }

    @PostMapping("/login")
    @Operation(summary = "Login and get a JWT token pair")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Login successful and tokens returned", content = @Content(mediaType = "application/json", schema = @Schema(implementation = TokensResponse.class))),
            @ApiResponse(responseCode = "401", description = "Invalid email or password", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class)))
    })
    public ResponseEntity<?> login(@RequestBody LoginRequest loginRequest) {
        User user = userRepository.findByEmail(loginRequest.getEmail());
        if (user == null || !passwordEncoder.matches(loginRequest.getPassword(), user.getPassword())) {
            return ResponseEntity.status(401).body(new ErrorResponse(401, "Unauthorized", "Invalid email or password"));
        }
        String accessToken = jwtTokenProvider.generateToken(user.getUsername(), 3600); // 1 hour expiration
        String refreshToken = jwtTokenProvider.generateToken(user.getUsername(), 86400); // 1 day expiration
        user.setLastLogin(Instant.now());
        userRepository.save(user);

        TokensResponse tokens = new TokensResponse();
        tokens.setAccessToken(accessToken);
        tokens.setRefreshToken(refreshToken);

        return ResponseEntity.ok(tokens);
    }

    @PostMapping("/refresh-token")
    @Operation(summary = "Refresh the access token using the refresh token")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Tokens refreshed successfully", content = @Content(mediaType = "application/json", schema = @Schema(implementation = TokensResponse.class))),
            @ApiResponse(responseCode = "401", description = "Invalid refresh token", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class)))
    })
    public ResponseEntity<?> refreshToken(@RequestBody RefreshTokenRequest refreshTokenRequest) {
        String refreshToken = refreshTokenRequest.getRefreshToken();
        if (!jwtTokenProvider.validateToken(refreshToken)) {
            return ResponseEntity.status(401).body(new ErrorResponse(401, "Unauthorized", "Invalid refresh token"));
        }
        String username = jwtTokenProvider.getUsername(refreshToken);
        String newAccessToken = jwtTokenProvider.generateToken(username, 3600);

        TokensResponse tokens = new TokensResponse();
        tokens.setAccessToken(newAccessToken);
        tokens.setRefreshToken(refreshToken);

        return ResponseEntity.ok(tokens);
    }
}
