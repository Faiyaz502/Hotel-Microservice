package com.example.gateway.Security.Logindto;

import com.spring.fooddelivery.domain.enums.UserRole;
import com.spring.fooddelivery.domain.user.Dto.AdminProfileCreateDTO;
import com.spring.fooddelivery.domain.user.Dto.DeliveryPersonCreateDTO;
import com.spring.fooddelivery.domain.user.Dto.RestaurantOwnerCreateDTO;
import com.spring.fooddelivery.domain.user.Dto.UserProfileCreateDTO;
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
