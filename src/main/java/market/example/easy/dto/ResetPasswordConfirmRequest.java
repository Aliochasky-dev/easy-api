package market.example.easy.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class ResetPasswordConfirmRequest {
    @NotBlank(message = "Le téléphone est obligatoire")
    private String telephone;

    @NotBlank(message = "Le code OTP est obligatoire")
    private String otpCode;

    @NotBlank(message = "Le nouveau mot de passe est obligatoire")
    @Size(min = 8, message = "8 caractères minimum")
    private String newPassword;
}