package com.spring.authservice.Security.Logindto;


import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class SignupRequestDto {

    private String firstName;
    private String lastName;
    private String email;
    private String phoneNumber;
    private String password;

    private String primaryRole;




}
