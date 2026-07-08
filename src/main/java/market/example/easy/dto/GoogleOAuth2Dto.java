package market.example.easy.dto;


import lombok.Builder;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

// @Builder a besoin de @AllArgsConstructor
// Jackson a besoin de @NoArgsConstructor
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class GoogleOAuth2Dto {

    private String id;
    private String email;
    private String nom;
    private String prenom;
    private String picture;

    public String getId()       { return id; }
    public void setId(String id) { this.id = id; }

    public String getEmail()         { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getNom()         { return nom; }
    public void setNom(String nom) { this.nom = nom; }

    public String getPrenom()            { return prenom; }
    public void setPrenom(String prenom) { this.prenom = prenom; }

    public String getPicture()             { return picture; }
    public void setPicture(String picture) { this.picture = picture; }
}