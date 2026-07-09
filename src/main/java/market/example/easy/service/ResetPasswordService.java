package market.example.easy.service;

import com.sendgrid.SendGrid;
import com.sendgrid.helpers.mail.Mail;
import com.sendgrid.helpers.mail.Email;
import com.sendgrid.helpers.mail.Content;
import market.example.easy.Entity.ResetToken;
import market.example.easy.model.User;
import market.example.easy.repository.ResetTokenRepository;
import market.example.easy.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
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

        try {
            SendGrid sg = new SendGrid(sendGridApiKey);
            
            Email from = new Email(mailFrom);
            Email to = new Email(cleanEmail);
            String subject = "Réinitialisation de mot de passe - VISION";
            String resetLink = frontendUrl + "/reset-password?token=" + token;
            Content content = new Content("text/plain",
                    "Bonjour,\n\n" +
                    "Cliquez sur ce lien pour réinitialiser votre mot de passe :\n" +
                    resetLink + "\n\n" +
                    "Ce lien expire dans 1 heure.\n\n" +
                    "Si vous n'avez pas demandé cette réinitialisation, ignorez cet email.\n\n" +
                    "L'équipe VISION"
            );
            
            Mail mail = new Mail(from, subject, to, content);
            com.sendgrid.Request request = new com.sendgrid.Request();
            request.setMethod(com.sendgrid.Request.Method.POST);
            request.setEndpoint("mail/send");
            request.setBody(mail.build());
            
            sg.api(request);
            
            System.out.println("=====================================");
            System.out.println("EMAIL : " + cleanEmail);
            System.out.println("TOKEN GÉNÉRÉ : " + token);
            System.out.println("LIEN : " + resetLink);
            System.out.println("=====================================");
        } catch (Exception e) {
            System.err.println("Erreur lors de l'envoi de l'email : " + e.getMessage());
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