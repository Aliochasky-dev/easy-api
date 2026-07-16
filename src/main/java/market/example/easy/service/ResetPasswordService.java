package market.example.easy.service;

import market.example.easy.Entity.ResetToken;
import market.example.easy.model.User;
import market.example.easy.repository.ResetTokenRepository;
import market.example.easy.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
public class ResetPasswordService {

    @Autowired private UserRepository userRepository;
    @Autowired private ResetTokenRepository resetTokenRepository;
    @Autowired private PasswordEncoder passwordEncoder;
    @Autowired private JavaMailSender mailSender;   // Injection du mail sender

    @Value("${app.frontend.url:http://localhost:8080}")
    private String frontendUrl;

    @Value("${spring.mail.from:noreply@easy-api.com}")
    private String mailFrom;

    public void sendResetToken(String email) {
        String cleanEmail = email.trim();

        User user = userRepository.findByEmail(cleanEmail)
                .orElseThrow(() -> new RuntimeException("Email non trouvé"));

        String token = UUID.randomUUID().toString();
        ResetToken resetToken = new ResetToken(token, cleanEmail);
        resetTokenRepository.save(resetToken);

        try {
            String resetLink = frontendUrl + "/reset-password?token=" + token;

            String emailBody = "Bonjour,\n\n" +
                    "Cliquez sur ce lien pour réinitialiser votre mot de passe :\n" +
                    resetLink + "\n\n" +
                    "Ce lien expire dans 1 heure.\n\n" +
                    "Si vous n'avez pas demandé cette réinitialisation, ignorez cet email.\n\n" +
                    "L'équipe VISION";

            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(mailFrom);
            message.setTo(cleanEmail);
            message.setSubject("Réinitialisation de mot de passe - VISION");
            message.setText(emailBody);

            mailSender.send(message);

            System.out.println("=====================================");
            System.out.println("EMAIL : " + cleanEmail);
            System.out.println("TOKEN GÉNÉRÉ : " + token);
            System.out.println("LIEN : " + resetLink);
            System.out.println("Email envoyé avec succès via Gmail !");
            System.out.println("=====================================");

        } catch (Exception e) {
            System.err.println("=== ERREUR ENVOI EMAIL ===");
            e.printStackTrace();
            throw new RuntimeException("Erreur lors de l'envoi de l'email", e);
        }
    }

    public void resetPassword(String token, String newPassword) {
        String cleanToken = token.trim();

        ResetToken resetToken = resetTokenRepository.findByToken(cleanToken)
                .orElseThrow(() -> new RuntimeException("Token invalide ou expiré"));

        if (resetToken.getExpiryDate().isBefore(LocalDateTime.now())) {
            resetTokenRepository.delete(resetToken);
            throw new RuntimeException("Token expiré");
        }

        User user = userRepository.findByEmail(resetToken.getEmail())
                .orElseThrow(() -> new RuntimeException("Utilisateur non trouvé"));

        user.setPassword(passwordEncoder.encode(newPassword.trim()));
        userRepository.save(user);

        resetTokenRepository.delete(resetToken);
    }
}