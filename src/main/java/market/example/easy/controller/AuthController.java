package market.example.easy.controller;

import market.example.easy.dto.*;
import market.example.easy.service.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import market.example.easy.service.ResetPasswordService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")   // À ajuster en production
public class AuthController {

    private final AuthService authService;
    private final ResetPasswordService resetPasswordService;

    @PostMapping("/register")
    public ResponseEntity<AuthResponse> register(@Valid @RequestBody RegisterRequest request) {
        AuthResponse response = authService.register(request);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@Valid @RequestBody LoginRequest request) {
        AuthResponse response = authService.login(request);
        return ResponseEntity.ok(response);
    }
    @PostMapping("/refresh")
    public ResponseEntity<AuthResponse> refreshToken(
            @RequestParam String refreshToken) {

        AuthResponse response =
                authService.refreshToken(refreshToken);

        return ResponseEntity.ok(response);
    }
    /**
     * POST /api/auth/forgot-password
     * Body : { email }
     */
    @PostMapping("/forgot-password")
    public ResponseEntity<ApiResponse<Void>> forgotPassword(
            @Valid @RequestBody ForgotPasswordRequestDto req) {
        resetPasswordService.sendResetToken(req.getEmail());
        return ResponseEntity.ok(ApiResponse.ok("Email de réinitialisation envoyé", null));
    }

    /**
     * POST /api/auth/reset-password
     * Body : { token, newPassword }
     */
    @PostMapping("/reset-password")
    public ResponseEntity<ApiResponse<Void>> resetPassword(
            @Valid @RequestBody ResetPasswordDto req) {
        resetPasswordService.resetPassword(req.getToken(), req.getNewPassword());
        return ResponseEntity.ok(ApiResponse.ok("Mot de passe réinitialisé avec succès", null));
    }
}