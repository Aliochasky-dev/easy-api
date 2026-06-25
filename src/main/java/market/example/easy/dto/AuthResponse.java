package market.example.easy.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AuthResponse {

    private String token;
    private String refreshToken;

    @Builder.Default
    private String type = "Bearer";

    private Long userId;
    private String fullName;
    private String email;
    private String phoneNumber;
}