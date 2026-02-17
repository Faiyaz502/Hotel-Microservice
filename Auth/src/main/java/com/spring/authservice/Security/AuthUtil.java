package com.spring.authservice.Security;



import com.spring.authservice.user.Entity.User;
import com.spring.authservice.user.Enums.AuthProviderType;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;

import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Component
public class AuthUtil {

    @Value("${jwt.secretKey}")
    private String secretKey;

    public SecretKey getSecretKey(){

        return Keys.hmacShaKeyFor(secretKey.getBytes(StandardCharsets.UTF_8));

    }

    public String genereateToken(User user){

        System.out.println(user.getAuthorities());

        return Jwts.builder()
                .setSubject(user.getUsername())
                .claim("userId", user.getId().toString())
                .claim("roles", user.getAuthorities()
                        .stream()
                        .map(a -> a.getAuthority()) // e.g. ROLE_ADMIN, ROLE_CUSTOMER
                        .collect(Collectors.toList()))
                .issuedAt(new Date())
                .signWith(getSecretKey())
                .compact();


    }


    public String getUsernameFromToken(String token) {


        Claims claims = Jwts.parser()
                .verifyWith(getSecretKey())
                .build()
                .parseSignedClaims(token)
                .getPayload();


        return claims.getSubject();


    }

    // Validate token
    public boolean validateToken(String token) {
        try {
            Jwts.parser()
                    .setSigningKey(getSecretKey())
                    .build()
                    .parseClaimsJws(token);
            return true;
        } catch (ExpiredJwtException e) {
            log.error("Token expired");
            return false;
        } catch (JwtException | IllegalArgumentException e) {
            log.error("Invalid token: {}", e.getMessage());
            return false;
        }
    }

    // Extract token from Bearer format
    public String extractTokenFromBearer(String bearerToken) {
        if (bearerToken != null && bearerToken.startsWith("Bearer ")) {
            return bearerToken.substring(7);
        }
        return bearerToken;
    }


    // Get User ID from Token
    public Long getUserIdFromToken(String token) {
        try {
            Claims claims = Jwts.parser()
                    .setSigningKey(getSecretKey())
                    .build()
                    .parseClaimsJws(token)
                    .getBody();
            return Long.parseLong(claims.getSubject());
        } catch (ExpiredJwtException e) {
            log.error("Token expired: {}", e.getMessage());
            throw new RuntimeException("Token expired");
        } catch (JwtException | NumberFormatException e) {
            log.error("Invalid token: {}", e.getMessage());
            throw new RuntimeException("Invalid token");
        }
    }

    // Get Email from Token
    public String getEmailFromToken(String token) {
        try {
            Claims claims = Jwts.parser()
                    .setSigningKey(getSecretKey())
                    .build()
                    .parseClaimsJws(token)
                    .getBody();
            return claims.get("email", String.class);
        } catch (JwtException e) {
            log.error("Error extracting email from token: {}", e.getMessage());
            throw new RuntimeException("Invalid token");
        }
    }

    //Roles

    public List<String> getRolesFromToken(String token) {
        Claims claims = Jwts.parser()
                .verifyWith(getSecretKey())
                .build()
                .parseSignedClaims(token)
                .getPayload();

        return claims.get("roles", List.class);
    }



    //--------------OAUTH2----------------------

    public AuthProviderType getProviderTypeFromRegistraitonId(String registrationId){


        return switch (registrationId.toLowerCase()){

            case "google" -> AuthProviderType.GOOGLE ;
            case "github" -> AuthProviderType.GITHUB ;
            case "facebook" -> AuthProviderType.FACEBOOK ;
            default -> throw new IllegalArgumentException("Unsupported OAuth2 provider"+registrationId);


        };

    }


    public String determineProviderIdFromOAuth2User(OAuth2User oAuth2User, String registrationId ){

        String providerId = switch (registrationId.toLowerCase()){

            case "google" -> oAuth2User.getAttribute("sub");
            case "github" -> oAuth2User.getAttribute("id").toString();
            default -> {
                log.error("Unsupported OAuth2 provider"+registrationId);
                throw new IllegalArgumentException("Unsupported OAuth2 provider"+registrationId);
            }


        };

        if (providerId == null || providerId.isBlank()){

            log.error("Unable to detemine provideId for the provider : {}"+registrationId);
            throw new IllegalArgumentException("Unable to detemine provideId for OAuth2 Login");

        }

    return providerId;

    }

    public String determineUsernameFromOAuth2User(OAuth2User oAuth2User,String registerId, String providerId){


        String email = oAuth2User.getAttribute("email");

        if(email != null && !email.isBlank()){

            return email;


        }


        return switch (registerId.toLowerCase()){

            case "google" -> oAuth2User.getAttribute("sub");
            case "github" -> oAuth2User.getAttribute("login");
                     default -> providerId;


        };


    }



}
