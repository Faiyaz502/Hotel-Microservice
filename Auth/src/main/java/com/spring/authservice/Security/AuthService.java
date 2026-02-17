package com.spring.authservice.Security;


import com.spring.authservice.Security.Logindto.LoginRequestDto;
import com.spring.authservice.Security.Logindto.LoginResponseDto;
import com.spring.authservice.Security.Logindto.SignupRequestDto;
import com.spring.authservice.Security.Logindto.SignupResponseDto;
import com.spring.authservice.user.Entity.User;
import com.spring.authservice.user.Enums.AuthProviderType;
import com.spring.authservice.user.Enums.UserRole;
import com.spring.authservice.user.Repository.UserRepository;
import lombok.RequiredArgsConstructor;

import org.modelmapper.ModelMapper;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AuthService {


    private final AuthenticationManager authenticationManager ;
    private final AuthUtil authUtil;
    private final UserRepository userRepository;
    private final ModelMapper mapper;
    private final PasswordEncoder passwordEncoder;



    public LoginResponseDto login(LoginRequestDto loginRequestDto) {

        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(loginRequestDto.getUsername(),loginRequestDto.getPassword()));


        User user = (User) authentication.getPrincipal();

        System.out.println(user);

        String token = authUtil.genereateToken(user);

        System.out.println(token);

        return new LoginResponseDto(token,user.getId());

    }


    //OAuth signup & ID pass both go through this

    public User signup(SignupRequestDto signupRequest, AuthProviderType authProviderType, String providerId) {

        String baseUsername = (signupRequest.getFirstName() + "." + signupRequest.getLastName()).toLowerCase().replaceAll("\\s+", "");
        String username = baseUsername;
        int counter = 1;
        while(userRepository.existsUserByUsername(username)){
            username = baseUsername + counter++;
        }

        User user = User.builder()
                .firstName(signupRequest.getFirstName())
                .lastName(signupRequest.getLastName())
                .username(username)
                .email(signupRequest.getEmail())
                .phoneNumber(signupRequest.getPhoneNumber())
                .primaryRole(UserRole.valueOf(signupRequest.getPrimaryRole()))
                .providerType(authProviderType)
                .providerId(providerId)
                .build();

        if(authProviderType == AuthProviderType.EMAIL){
            user.setPassword(passwordEncoder.encode(signupRequest.getPassword()));
        }

        // Build profile based on role

        return userRepository.save(user);
    }



    // Only Normal Email password signup
    public SignupResponseDto sign(SignupRequestDto signupRequest) {



        User user = signup(signupRequest,AuthProviderType.EMAIL,null);


        SignupResponseDto s = new SignupResponseDto();

        mapper.map(user, s);

        return s;



    }


    public Boolean validateToken(String jwtToken){

       return authUtil.validateToken(jwtToken);
    }

    @Transactional
    public ResponseEntity<LoginResponseDto> handleOAuth2LoginRequest(OAuth2User oAuth2User, String registrationId) {

        // Determine provider type and provider ID
        AuthProviderType providerType = authUtil.getProviderTypeFromRegistraitonId(registrationId);
        String providerId = authUtil.determineProviderIdFromOAuth2User(oAuth2User, registrationId);

        // Search by provider ID and type
        User user = userRepository.findByProviderIdAndProviderType(providerId, providerType).orElse(null);

        String email = oAuth2User.getAttribute("email");
        String name = oAuth2User.getAttribute("name");

        // Search by email/username if exists
        User emailUser = (email != null) ? userRepository.findByUsername(email).orElse(null) : null;

        if (user == null && emailUser == null) {
            // Split name into firstName and lastName
            String firstName = name != null ? name.split(" ")[0] : "OAuth";
            String lastName = name != null && name.contains(" ") ? name.substring(name.indexOf(" ") + 1) : "User";

            // Generate username automatically from name
            String username = authUtil.determineUsernameFromOAuth2User(oAuth2User, registrationId, providerId);

            // Build signup request DTO
            SignupRequestDto signupRequest = new SignupRequestDto();
            signupRequest.setFirstName(firstName);
            signupRequest.setLastName(lastName);
            signupRequest.setEmail(email);
            signupRequest.setPrimaryRole(String.valueOf(UserRole.CUSTOMER)); // default role

            // Signup user
            user = signup(signupRequest, providerType, providerId);

        } else if (user != null) {
            // Update email/username if changed
            if (email != null && !email.isBlank() && !email.equals(user.getUsername())) {
                user.setUsername(email);
                userRepository.save(user);
            }
        } else {
            // Conflict: email already registered
            throw new BadCredentialsException("This Email is already registered with " + emailUser.getProviderType());
        }

        // Generate JWT token
        LoginResponseDto loginResponseDto = new LoginResponseDto(authUtil.genereateToken(user), user.getId());

        return ResponseEntity.ok(loginResponseDto);
    }
}
