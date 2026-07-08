package market.example.easy.service;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

import java.security.SecureRandom;
import java.time.Duration;

@Service
@RequiredArgsConstructor
public class EmailOtpService {

    private final JavaMailSender mailSender;
    private final RedisTemplate<String, String> redisTemplate;

    @Value("${spring.mail.username}")
    private String fromEmail;

    private static final int OTP_LENGTH = 6;
    private static final Duration OTP_EXPIRY = Duration.ofMinutes(10);

    /**
     * Génère un OTP et l'envoie par email
     */
    public String generateAndSendOtp(String email) {
        String otp = generateOtp();
        String redisKey = "otp:email:" + email;

        // Sauvegarde dans Redis
        redisTemplate.opsForValue().set(redisKey, otp, OTP_EXPIRY);

        // Envoi de l'email
        sendOtpEmail(email, otp);

        System.out.println("📧 OTP envoyé par email à " + email + " → " + otp);

        return otp; // Pour test seulement
    }

    /**
     * Vérifie l'OTP
     */
    public boolean verifyOtp(String email, String code) {
        String redisKey = "otp:email:" + email;
        String storedOtp = redisTemplate.opsForValue().get(redisKey);

        if (storedOtp == null) {
            throw new RuntimeException("Le code OTP a expiré ou n'existe pas");
        }

        if (!storedOtp.equals(code)) {
            return false;
        }

        redisTemplate.delete(redisKey);
        return true;
    }

    /**
     * Envoi de l'email OTP
     */
    public void sendOtpEmail(String toEmail, String otp) {
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(fromEmail);
            message.setTo(toEmail);
            message.setSubject("Votre code de vérification VISION");
            message.setText("""
                Bonjour,

                Votre code OTP pour VISION est : %s

                Ce code est valable pendant 10 minutes.

                Cordialement,
                L'équipe VISION
                """.formatted(otp));

            mailSender.send(message);
        } catch (Exception e) {
            System.err.println("Erreur lors de l'envoi de l'email OTP : " + e.getMessage());
            throw new RuntimeException("Impossible d'envoyer l'email OTP");
        }
    }

    private String generateOtp() {
        SecureRandom random = new SecureRandom();
        StringBuilder otp = new StringBuilder();
        for (int i = 0; i < OTP_LENGTH; i++) {
            otp.append(random.nextInt(10));
        }
        return otp.toString();
    }
}