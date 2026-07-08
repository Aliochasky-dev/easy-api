package market.example.easy.Entity;

import jakarta.persistence.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "reset_tokens")
public class ResetToken {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String token;

    @Column(nullable = false)
    private String email;

    @Column(nullable = false)
    private LocalDateTime expiryDate;



    // CONSTRUCTEUR VIDE OBLIGATOIRE POUR HIBERNATE
    public ResetToken() {
        // Laisser vide ou initialiser les champs si besoin
    }

    // Constructeur
    public ResetToken(String token, String email) {
        this.token = token;
        this.email = email;
        this.expiryDate = LocalDateTime.now().plusMinutes(1);
    }

    public ResetToken(String token, String email, LocalDateTime expiryDate){
        this.token = token;
    }
    public Long getId(){
        return id;
    }
    public void setId(Long id){
        this.id = id;
    }
    public String getToken(){
        return token;
    }
    public void setToken(String token){
        this.token = token;
    }
    public String getEmail(){
        return email;
    }
    public void setEmail(String email){
        this.email = email;
    }
    public LocalDateTime getExpiryDate(){
        return expiryDate;
    }
    public void setExpiryDate(LocalDateTime expiryDate){
        this.expiryDate = expiryDate;
    }


}