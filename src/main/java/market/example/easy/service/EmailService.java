package market.example.easy.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class EmailService {

    // JavaMailSender est fourni automatiquement par Spring
    // grâce à la dépendance spring-boot-starter-mail
    private final JavaMailSender mailSender;

    // Envoie un email simple avec le code OTP
    public void sendOtpEmail(String destinataire, String otp) {

        // SimpleMailMessage = email simple sans HTML
        SimpleMailMessage message = new SimpleMailMessage();

        // L'adresse qui recevra l'email
        message.setTo(destinataire);

        // Sujet de l'email
        message.setSubject("Votre code de vérification WifiZone");

        // Corps du message avec le code OTP
        message.setText(
                "Bonjour,\n\n" +
                        "Votre code de vérification est : " + otp + "\n\n" +
                        "Ce code expire dans 5 minutes.\n\n" +
                        "L'équipe WifiZone"
        );

        // Envoi de l'email
        mailSender.send(message);

        log.info("OTP envoyé à : {}", destinataire);
    }
}