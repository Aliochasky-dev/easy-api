package market.example.easy.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;

/**
 * Vérifie la présence de la variable d'environnement SENDGRID_API_KEY au démarrage.
 * Si absente, l'application échoue rapidement pour éviter des erreurs SMTP/runtime.
 */
@Component
public class StartupValidator {

    @Value("${SENDGRID_API_KEY:}")
    private String sendGridApiKey;

    @PostConstruct
    public void validate() {
        if (sendGridApiKey == null || sendGridApiKey.isBlank()) {
            throw new IllegalStateException("SENDGRID_API_KEY non configurée. Définissez la variable d'environnement SENDGRID_API_KEY pour activer l'envoi d'emails via SendGrid.");
        }
    }
}
