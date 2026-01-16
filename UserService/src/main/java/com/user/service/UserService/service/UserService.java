package com.user.service.UserService.service;

import com.user.service.UserService.entities.User;

import java.util.List;

public interface UserService {

    User saveUser(User user);

    List<User> getAllUsers();

    User getUserById(String id);

    User updateUser(String id ,User user);

    String deleteUser(String userId);


}
