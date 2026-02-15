package com.example.gateway.Security;

import com.example.gateway.Security.Logindto.LoginResponseDto;
import com.fasterxml.jackson.databind.ObjectMapper;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

@Component
@RequiredArgsConstructor
public class OAuth2SuccessHandler implements AuthenticationSuccessHandler {

    private final AuthService authService;
    private final ObjectMapper objectMapper;





    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response, Authentication authentication) throws IOException, ServletException {

        OAuth2AuthenticationToken token = (OAuth2AuthenticationToken) authentication;
        OAuth2User oAuth2User = (OAuth2User) authentication.getPrincipal();
        String registrationId = token.getAuthorizedClientRegistrationId();

       ResponseEntity<LoginResponseDto> loginResponse = authService.handleOAuth2LoginRequest(oAuth2User,registrationId);

       response.setStatus(loginResponse.getStatusCode().value());
       response.setContentType(MediaType.APPLICATION_JSON_VALUE);
       response.getWriter().write(objectMapper.writeValueAsString(loginResponse.getBody()));


        //  Build safe redirect URL (encode params!)
        //  Redirect to Angular with token + userId in query (safe & simple)
        String frontendUrl = "http://localhost:4200/oauth-callback"; // ← update in prod!
        String encodedToken = URLEncoder.encode(loginResponse.getBody().getJwt(), StandardCharsets.UTF_8);
        String redirectUrl = String.format(
                "%s?token=%s&userId=%d",
                frontendUrl,
                encodedToken,
                loginResponse.getBody().getUserId()
        );

        response.sendRedirect(redirectUrl); // 🟢 Only this — no write()



    }
}
