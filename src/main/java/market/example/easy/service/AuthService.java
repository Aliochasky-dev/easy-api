package market.example.easy.service;


import jakarta.servlet.http.HttpServletRequest;
import market.example.easy.Entity.RefreshToken;
import market.example.easy.dto.AuthResponse;
import market.example.easy.dto.LoginRequest;
import market.example.easy.dto.RegisterRequest;
import market.example.easy.security.JwtUtil;
import market.example.easy.model.User;
import market.example.easy.repository.UserRepository;

import org.springframework.security.authentication.BadCredentialsException;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

@Service
@RequiredArgsConstructor


    @Transactional
    public class AuthService {

        private final UserRepository         userRepository;
        private final PasswordEncoder        passwordEncoder;
        private final JwtUtil                jwtUtil;
        private final AuthenticationManager  authenticationManager;
        private final RefreshTokenService    refreshTokenService;
        private final LoginAttemptService    loginAttemptService;  // ← nouveau

        @Transactional
        public AuthResponse register(RegisterRequest request) {

            if (userRepository.existsByEmail(request.getEmail())) {
                throw new RuntimeException("Cet email est déjà utilisé");
            }
            if (userRepository.existsByPhoneNumber(request.getPhoneNumber())) {
                throw new RuntimeException("Ce numéro de téléphone est déjà utilisé");
            }
            if (!request.getPassword().equals(request.getConfirmPassword())) {
                throw new RuntimeException("Les mots de passe ne correspondent pas");
            }

            User user = User.builder()
                    .fullName(request.getFullName())
                    .phoneNumber(request.getPhoneNumber())
                    .email(request.getEmail())
                    .password(passwordEncoder.encode(request.getPassword()))
                    .enabled(true)
                    .build();

            User savedUser = userRepository.save(user);
            String token = jwtUtil.generateToken(savedUser);

            return AuthResponse.builder()
                    .token(token)
                    .userId(savedUser.getId())
                    .fullName(savedUser.getFullName())
                    .email(savedUser.getEmail())
                    .phoneNumber(savedUser.getPhoneNumber())
                    .build();
        }
    public AuthResponse login(LoginRequest request) {

        String login = request.getLogin();
        String ip = getClientIp();

        // 1. Vérifier si l'identifiant est bloqué
        loginAttemptService.verifierBlocage(login);

        // 2. Chercher l'utilisateur
        User user = userRepository.findByEmailOrPhoneNumber(login, login)
                .orElseThrow(() -> {
                    System.out.println(">>> Utilisateur introuvable : " + login);
                    loginAttemptService.enregistrerEchec(login, ip);
                    return new RuntimeException("Identifiant ou mot de passe incorrect");
                });

        // 3. Authentification
        try {

            authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(
                            user.getEmail(),
                            request.getPassword()
                    )
            );

            System.out.println(">>> Authentification réussie");

        } catch (Exception e) {

            System.out.println("================================");
            System.out.println("Exception capturée : " + e.getClass().getName());
            System.out.println("Message : " + e.getMessage());
            System.out.println("================================");

            loginAttemptService.enregistrerEchec(login, ip);

            try {
                loginAttemptService.verifierBlocage(login);
            } catch (Exception blocageException) {
                throw blocageException;
            }

            throw new RuntimeException("Identifiant ou mot de passe incorrect");
        }

        // 4. Réinitialiser les tentatives après succès
        loginAttemptService.reinitialiserEchecs(login);

        // 5. Générer les tokens
        String token = jwtUtil.generateToken(user);

        RefreshToken refreshTokenEntity =
                refreshTokenService.createRefreshToken(user);

        return AuthResponse.builder()
                .token(token)
                .refreshToken(refreshTokenEntity.getToken())
                .type("Bearer")
                .userId(user.getId())
                .fullName(user.getFullName())
                .email(user.getEmail())
                .phoneNumber(user.getPhoneNumber())
                .build();
    }

        // Récupère l'IP réelle du client (proxy-aware)
        private String getClientIp() {
            try {
                HttpServletRequest req = ((ServletRequestAttributes)
                        RequestContextHolder.getRequestAttributes()).getRequest();
                String forwarded = req.getHeader("X-Forwarded-For");
                if (forwarded != null && !forwarded.isBlank()) {
                    return forwarded.split(",")[0].trim();
                }
                return req.getRemoteAddr();
            } catch (Exception e) {
                return "unknown";
            }
        }
    public AuthResponse refreshToken(String refreshTokenValue) {

        RefreshToken refreshToken =
                refreshTokenService.verifyRefreshToken(refreshTokenValue);

        User user = refreshToken.getUser();

        String newAccessToken =
                jwtUtil.generateToken(user);

        return AuthResponse.builder()
                .token(newAccessToken)
                .refreshToken(refreshTokenValue)
                .userId(user.getId())
                .fullName(user.getFullName())
                .email(user.getEmail())
                .phoneNumber(user.getPhoneNumber())
                .build();
    }
}