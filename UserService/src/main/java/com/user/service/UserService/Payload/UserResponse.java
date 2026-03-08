package com.user.service.UserService.Payload;

import java.io.Serializable;

public record UserResponse(String userId, String name, String phone, String email, String about) implements Serializable {
}
