package market.example.easy.Entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "login_attempts")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class LoginAttempt {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Identifiant utilisé pour tenter la connexion (email ou téléphone)
    @Column(nullable = false)
    private String identifier;

    // Nombre de tentatives échouées consécutives
    @Column(name = "nb_echecs", nullable = false)
    private int nbEchecs = 0;

    // Date du dernier échec
    @Column(name = "dernier_echec")
    private LocalDateTime dernierEchec;

    // Compte bloqué jusqu'à cette date
    @Column(name = "bloque_jusqu_a")
    private LocalDateTime bloqueJusquA;

    // Adresse IP de la tentative
    @Column(name = "ip_address")
    private String ipAddress;
}