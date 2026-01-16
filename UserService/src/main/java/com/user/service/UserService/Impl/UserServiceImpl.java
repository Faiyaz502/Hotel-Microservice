package com.user.service.UserService.Impl;

import com.user.service.UserService.Exceptions.ResourceNotFoundException;
import com.user.service.UserService.Repositories.UserRepository;
import com.user.service.UserService.service.UserService;
import com.user.service.UserService.entities.User;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;

    @Override
    public User saveUser(User user) {

      String userID = UUID.randomUUID().toString();


            user.setUserId(userID);

        return userRepository.save(user);
    }

    @Override
    public List<User> getAllUsers() {
        return userRepository.findAll();
    }

    @Override
    public User getUserById(String id) {
        return userRepository.findById(id).orElseThrow(()->
                new ResourceNotFoundException("ID is not found in the DB"));
    }

    @Override
    public User updateUser(String id, User userDetails) {
        //  Find existing user
        User existingUser = userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("User not found"));

        // Update fields
        existingUser.setName(userDetails.getName());
        existingUser.setEmail(userDetails.getEmail());
        existingUser.setPhone(userDetails.getPhone());
        existingUser.setAbout(userDetails.getAbout());


        return userRepository.save(existingUser);
    }

    @Override
    public String deleteUser(String userId) {
          try {
              userRepository.deleteById(userId);

          }catch (Exception e){
              return e.getMessage();
          }

        return "Successfully Deleted";
    }
}
