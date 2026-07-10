package market.example.easy.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.web.client.RestTemplate;
import org.springframework.util.StringUtils;

import java.security.SecureRandom;
import java.time.Duration;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class EmailOtpService {

    private final RedisTemplate<String, String> redisTemplate;

    @Value("${spring.mail.from:noreply@easy-api.com}")
    private String mailFrom;

    @Value("${SENDGRID_API_KEY:}")
    private String sendGridApiKey;

    private static final int OTP_LENGTH = 6;
    private static final Duration OTP_EXPIRY = Duration.ofMinutes(10);

    /**
     * Génère un OTP et l'envoie par email via SendGrid API (obligatoire)
     */
    public String generateAndSendOtp(String email) {
        String otp = generateOtp();
        String redisKey = "otp:email:" + email;

        // Sauvegarde dans Redis
        redisTemplate.opsForValue().set(redisKey, otp, OTP_EXPIRY);

        // Envoi de l'email
        sendOtpEmail(email, otp);

        System.out.println("📧 OTP généré pour " + email + " → " + otp);

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
     * Envoi de l'email OTP via SendGrid API (REQUIS). Si SENDGRID_API_KEY manquant, l'envoi échoue avec message explicite.
     */
    public void sendOtpEmail(String toEmail, String otp) {
        if (!StringUtils.hasText(sendGridApiKey)) {
            throw new RuntimeException("SENDGRID_API_KEY non configuré. Configurez la variable d'environnement SENDGRID_API_KEY pour activer l'envoi d'emails via SendGrid.");
        }

        String emailBody = "Bonjour,\n\n" +
                "Votre code OTP pour VISION est : " + otp + "\n\n" +
                "Ce code est valable pendant 10 minutes.\n\n" +
                "Cordialement,\n" +
                "L'équipe VISION";

        try {
            Map<String, Object> emailPayload = new LinkedHashMap<>();
            emailPayload.put("personalizations", Arrays.asList(
                    Map.of("to", Arrays.asList(Map.of("email", toEmail)))
            ));
            emailPayload.put("from", Map.of("email", mailFrom));
            emailPayload.put("subject", "Votre code de vérification VISION");
            emailPayload.put("content", Arrays.asList(
                    Map.of("type", "text/plain", "value", emailBody)
            ));

            RestTemplate restTemplate = new RestTemplate();
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.set("Authorization", "Bearer " + sendGridApiKey);

            ObjectMapper mapper = new ObjectMapper();
            String jsonPayload = mapper.writeValueAsString(emailPayload);
            HttpEntity<String> request = new HttpEntity<>(jsonPayload, headers);

            restTemplate.postForObject("https://api.sendgrid.com/v3/mail/send", request, String.class);

            System.out.println("OTP envoyé via SendGrid à " + toEmail);
        } catch (Exception e) {
            System.err.println("Envoi via SendGrid échoué: " + e.getMessage());
            throw new RuntimeException("Erreur lors de l'envoi de l'email via SendGrid", e);
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
