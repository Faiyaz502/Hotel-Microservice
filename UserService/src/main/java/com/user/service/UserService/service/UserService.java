package com.user.service.UserService.service;

import com.user.service.UserService.Payload.PaginatedResponse;
import com.user.service.UserService.Payload.UserProjection;
import com.user.service.UserService.entities.User;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface UserService {

    User saveUser(User user);

    PaginatedResponse<UserProjection> getAllUsers(
            String name, String userId, String phone, String email, String lastId, int size);

    User getUserById(String id);

    User updateUser(String id ,User user);

    String deleteUser(String userId);


}
