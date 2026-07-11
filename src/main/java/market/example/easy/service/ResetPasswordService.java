package market.example.easy.service;

import market.example.easy.Entity.ResetToken;
import market.example.easy.model.User;
import market.example.easy.repository.ResetTokenRepository;
import market.example.easy.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDateTime;
import java.util.*;

import com.fasterxml.jackson.databind.ObjectMapper;

@Service
public class ResetPasswordService {

    @Autowired
    private UserRepository userRepository;
    @Autowired private ResetTokenRepository resetTokenRepository;
    @Autowired private PasswordEncoder passwordEncoder;
    
    @Value("${app.frontend.url:http://localhost:8080}")
    private String frontendUrl;
    
    @Value("${spring.mail.from:noreply@easy-api.com}")
    private String mailFrom;
    
    @Value("${SENDGRID_API_KEY:}")
    private String sendGridApiKey;

    public void sendResetToken(String email) {
        String cleanEmail = email.trim();

        User user = userRepository.findByEmail(cleanEmail)
                .orElseThrow(() -> new RuntimeException("Email non trouvé"));

        String token = UUID.randomUUID().toString();
        ResetToken resetToken = new ResetToken(token, cleanEmail);
        resetTokenRepository.save(resetToken);
        if (!StringUtils.hasText(sendGridApiKey)) {
            throw new RuntimeException("SENDGRID_API_KEY manquante dans les variables Railway");
        }

        try {
            String resetLink = frontendUrl + "/reset-password?token=" + token;
            String emailBody = "Bonjour,\n\n" +
                    "Cliquez sur ce lien pour réinitialiser votre mot de passe :\n" +
                    resetLink + "\n\n" +
                    "Ce lien expire dans 1 heure.\n\n" +
                    "Si vous n'avez pas demandé cette réinitialisation, ignorez cet email.\n\n" +
                    "L'équipe VISION";

            // Construire le payload JSON pour SendGrid
            Map<String, Object> emailPayload = new LinkedHashMap<>();
            emailPayload.put("personalizations", Arrays.asList(
                    Map.of("to", Arrays.asList(Map.of("email", cleanEmail)))
            ));
            emailPayload.put("from", Map.of("email", mailFrom));
            emailPayload.put("subject", "Réinitialisation de mot de passe - VISION");
            emailPayload.put("content", Arrays.asList(
                    Map.of("type", "text/plain", "value", emailBody)
            ));

            // Appeler l'API SendGrid
            RestTemplate restTemplate = new RestTemplate();
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.set("Authorization", "Bearer " + sendGridApiKey);

            ObjectMapper mapper = new ObjectMapper();
            String jsonPayload = mapper.writeValueAsString(emailPayload);
            HttpEntity<String> request = new HttpEntity<>(jsonPayload, headers);

            restTemplate.postForObject("https://api.sendgrid.com/v3/mail/send", request, String.class);

            System.out.println("=====================================");
            System.out.println("EMAIL : " + cleanEmail);
            System.out.println("TOKEN GÉNÉRÉ : " + token);
            System.out.println("LIEN : " + resetLink);
            System.out.println("Email envoyé avec succès !");
            System.out.println("=====================================");
        } catch (Exception e) {
        System.err.println("=== ERREUR ENVOI EMAIL ===");
        System.err.println("SENDGRID_API_KEY vide ? " + !StringUtils.hasText(sendGridApiKey));
        System.err.println("Message: " + e.getMessage());
        e.printStackTrace();   // pour voir le stack complet
        throw new RuntimeException("Erreur lors de l'envoi de l'email", e);
    }
    }

    public void resetPassword(String token, String newPassword) {
        // ← .trim() sur le token pour éviter les espaces parasites
        String cleanToken = token.trim();

        ResetToken resetToken = resetTokenRepository.findByToken(cleanToken)
                .orElseThrow(() -> new RuntimeException("Token invalide ou expiré"));

        if (resetToken.getExpiryDate().isBefore(LocalDateTime.now())) {
            resetTokenRepository.delete(resetToken); // nettoyage auto
            throw new RuntimeException("Token expiré");
        }

        User user = userRepository.findByEmail(resetToken.getEmail())
                .orElseThrow(() -> new RuntimeException("Utilisateur non trouvé"));

        // ← .trim() sur le nouveau mot de passe aussi
        user.setPassword(passwordEncoder.encode(newPassword.trim()));
        userRepository.save(user);

        resetTokenRepository.delete(resetToken);
    }
}