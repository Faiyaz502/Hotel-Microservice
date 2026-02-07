package com.user.service.UserService.controller;

import com.user.service.UserService.service.UserService;
import com.user.service.UserService.entities.User;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import org.springframework.web.bind.annotation.*;

import java.util.List;


@RestController
@RequestMapping("/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;
    private final Logger logger = LoggerFactory.getLogger(UserController.class);

    @PostMapping
    public ResponseEntity<User> createUser(@RequestBody User user){
        User savedUser = userService.saveUser(user);
        return ResponseEntity.status(HttpStatus.CREATED).body(savedUser);
    }

    @GetMapping(value = "/{id}")
    @CircuitBreaker(name = "userRatingHotelBreaker",fallbackMethod = "ratingHotelFallback")
    public ResponseEntity<User> getUserById(@PathVariable String id){

        logger.info("<<<Calling the UserController : GetUserById>>>"+id);

        User user = userService.getUserById(id);
        return ResponseEntity.ok(user);
    }

    //Fallback Method Of CircuitBreaker

    public ResponseEntity<User> ratingHotelFallback(String id,Exception ex){

        logger.error("The Fallback is Executed because is service is down"+ex.getMessage());

        User user = User.builder()
                .email("Dummy@gmail.com")
                .name("Dummy User")
                .about("Service is down")
                .userId(id)
                .build();

        return new ResponseEntity<>(user,HttpStatus.OK);


    }

    @GetMapping
    public ResponseEntity<List<User>> getAllUsers(){
        List<User> users = userService.getAllUsers();
        return ResponseEntity.ok(users);
    }

    @PutMapping("/{id}")
    public ResponseEntity<User> updateUser(@PathVariable String id, @RequestBody User user){
        User updatedUser = userService.updateUser(id, user);
        return ResponseEntity.ok(updatedUser);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<String> deleteUser(@PathVariable String id){
        String message = userService.deleteUser(id);
        return ResponseEntity.ok(message);
    }
}

