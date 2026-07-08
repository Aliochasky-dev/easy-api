package market.example.easy.Entity;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@NoArgsConstructor
@Entity
@Setter
@Getter
@Table(name ="token")
public class Token {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String token;

    @Column(nullable = false)
    private String email;

    @Column(nullable = false)
    private LocalDateTime expiryDate;

    public Token(String token, String email){
        this.token = token;
        this.email = email;
        this.expiryDate = expiryDate;
        this.expiryDate = LocalDateTime.now().plusMinutes(30);


    }

    public Token(String token, String email, LocalDateTime expiryDate){
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
