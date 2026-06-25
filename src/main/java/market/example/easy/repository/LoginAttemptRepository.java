package market.example.easy.repository;

import market.example.easy.Entity.LoginAttempt;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.Optional;

@Repository
public interface LoginAttemptRepository extends JpaRepository<LoginAttempt, Long> {

    Optional<LoginAttempt> findByIdentifier(String identifier);

    // Nettoyer les anciens enregistrements (job de nettoyage)
    @Modifying
    @Query("DELETE FROM LoginAttempt l WHERE l.dernierEchec < :before")
    void deleteOldAttempts(LocalDateTime before);
}