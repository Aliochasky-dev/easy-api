package market.example.easy.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/test")
@SecurityRequirement(name = "Bearer Authentication")
public class TestController {

    @Operation(summary = "Route protégée - Test JWT")
    @GetMapping
    public ResponseEntity<String> testProtectedRoute() {
        return ResponseEntity.ok("✅ Vous êtes bien authentifié ! Le JWT fonctionne correctement.");
    }
}