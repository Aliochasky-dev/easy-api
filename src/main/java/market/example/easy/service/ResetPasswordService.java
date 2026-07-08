package market.example.easy.service;

import market.example.easy.Entity.ResetToken;
import market.example.easy.model.User;
import market.example.easy.repository.ResetTokenRepository;
import market.example.easy.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
public class ResetPasswordService {

    @Autowired
    private UserRepository userRepository;
    @Autowired private ResetTokenRepository resetTokenRepository;
    @Autowired private PasswordEncoder passwordEncoder;
    @Autowired private JavaMailSender mailSender;

    public void sendResetToken(String email) {
        // ← .trim() pour éviter les espaces dans l'email
        String cleanEmail = email.trim();

        User user = userRepository.findByEmail(cleanEmail)
                .orElseThrow(() -> new RuntimeException("Email non trouvé"));

        String token = UUID.randomUUID().toString();
        ResetToken resetToken = new ResetToken(token, cleanEmail);
        resetTokenRepository.save(resetToken);

        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(cleanEmail);
        message.setSubject("Réinitialisation de mot de passe - VISION");
        message.setText(
                "Bonjour,\n\n" +
                        "Cliquez sur ce lien pour réinitialiser votre mot de passe :\n" +
                        "http://localhost:8080/reset-password?token=" + token + "\n\n" +
                        "Ce lien expire dans 1 heure.\n\n" +
                        "Si vous n'avez pas demandé cette réinitialisation, ignorez cet email.\n\n" +
                        "L'équipe VISION"
        );
        mailSender.send(message);

        System.out.println("=====================================");
        System.out.println("EMAIL : " + cleanEmail);
        System.out.println("TOKEN GÉNÉRÉ : " + token);
        System.out.println("LIEN : http://localhost:8080/reset-password?token=" + token);
        System.out.println("=====================================");
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