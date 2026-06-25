package market.example.easy.repository;

import market.example.easy.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long > {

    //pour l'inscription et la connexion
    Optional<User> findByEmail(String email);
    Optional<User> findByPhoneNumber(String phoneNumber);

    //verifier l'existence pour eviter les doublons
    boolean existsByEmail(String email);
    boolean existsByPhoneNumber(String phoneNumber);

    //recherche par email ou telephone
    Optional<User>findByEmailOrPhoneNumber(String email, String phoneNumber);

}
