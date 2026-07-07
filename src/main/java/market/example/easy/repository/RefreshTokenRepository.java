package market.example.easy.repository;

import market.example.easy.Entity.RefreshToken;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface RefreshTokenRepository extends JpaRepository<RefreshToken, Long> {

    Optional<RefreshToken> findByToken(String token);

    // Cette ligne était probablement en String → il faut la mettre en Long
    void deleteByUserId(Long userId);        // ← Correction ici

    Optional<RefreshToken> findByUserId(Long userId);
} 