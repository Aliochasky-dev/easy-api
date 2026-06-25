package market.example.easy.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class RegisterRequest {
    @NotBlank(message = "le nom complet est obligatoire")
    private String fullName;

    @NotBlank(message="le numero de telephone est obligatoire")
    @Size(min = 9,max = 15 ,message ="le numero de telephone doit etre valider")
    private String phoneNumber;

    @NotBlank(message = "l'email est obligatoire ")
    @Email(message = "format d'email invalide")
    private String email;

    @NotBlank(message="le mot de passe est obligatoire")
    @Size(min=10,message = "le mot de passe doit contenir au moins 6 lettres")
    private String password;

    @NotBlank(message="la confirmation de mot de passe est obligatoire")
    private String confirmPassword;
}
