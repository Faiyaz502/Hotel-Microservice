package com.spring.authservice.Security.Controller;


import com.spring.authservice.Security.AuthService;
import com.spring.authservice.Security.Logindto.LoginRequestDto;
import com.spring.authservice.Security.Logindto.LoginResponseDto;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/login")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @PostMapping
    public ResponseEntity<LoginResponseDto> login(@RequestBody LoginRequestDto loginRequestDto) {
        try {
            LoginResponseDto response = authService.login(loginRequestDto);
            return ResponseEntity.ok(response);
        } catch (BadCredentialsException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(null);
        }
    }
    @GetMapping("/validate")
    public ResponseEntity<Boolean> validateToken(@RequestParam("token") String token){

        return  ResponseEntity.ok().body(authService.validateToken(token));

    }
}
