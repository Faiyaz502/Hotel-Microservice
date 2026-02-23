package com.user.service.UserService.controller;

import com.user.service.UserService.Exceptions.ResourceNotFoundException;
import com.user.service.UserService.service.KafkaService;
import com.user.service.UserService.service.UserService;
import com.user.service.UserService.entities.User;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.ratelimiter.RequestNotPermitted;
import io.github.resilience4j.ratelimiter.annotation.RateLimiter;
import io.github.resilience4j.retry.annotation.Retry;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.ResourceAccessException;

import java.io.IOException;
import java.util.List;


@RestController
@RequestMapping("/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;
    private final Logger logger = LoggerFactory.getLogger(UserController.class);
    private final KafkaService kafkaService;

    @PostMapping
    public ResponseEntity<User> createUser(@RequestBody User user){

        logger.info("Calling the Creating User /users Controller {} ->",user);

        User savedUser = userService.saveUser(user);


        logger.info("Calling the Kafka Publisher for Email /users Controller {} ->",user);

        kafkaService.userCreatedPublisher(savedUser);


        return ResponseEntity.status(HttpStatus.CREATED).body(savedUser);
    }

    //---------Circuit Breaker

    @GetMapping(value = "/{id}")
    @CircuitBreaker(name = "userRatingHotelBreaker",fallbackMethod = "ratingHotelFallback")
    public ResponseEntity<User> getUserById(@PathVariable String id){

        logger.info("<<<Calling the UserController : GetUserById>>>"+id);

        User user = userService.getUserById(id);
        return ResponseEntity.ok(user);
    }

    //----------Retry-----



    @GetMapping(value = "/id/{id}")
//  @CircuitBreaker(name = "userRatingHotelBreaker",fallbackMethod = "ratingHotelFallback") // retry is in service circuit Breaker + Retry
    @RateLimiter(name = "userRateLimiter",fallbackMethod = "ratingHotelFallback")
    public ResponseEntity<User> getUserByIdRetryExample(@PathVariable String id){

        try {
            logger.info("<<<Calling the UserController : GetUserById>>>"+id);



            User user = userService.getUserById(id);
            return ResponseEntity.ok(user);

        }catch (ResourceNotFoundException ex) {

        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(null);

    }


    }

    //Fallback Method Of CircuitBreaker

    public ResponseEntity<User> ratingHotelFallback(String id,Exception ex){

        if (ex instanceof RequestNotPermitted) {
            return ResponseEntity.status(HttpStatus.TOO_MANY_REQUESTS)
                    .body(User.builder()
                            .email("ratelimit@gmail.com")
                            .name("Rate Limited User")
                            .about("Too many requests, try again later")
                            .userId(id)
                            .build());
        }


        if (ex instanceof HttpServerErrorException || ex instanceof ResourceAccessException || ex instanceof IOException){

            logger.error("The Fallback is Executed because is service is down"+ex.getMessage());

            User user = User.builder()
                    .email("Dummy@gmail.com")
                    .name("Dummy User")
                    .about("Service is down")
                    .userId(id)
                    .build();

            return new ResponseEntity<>(user,HttpStatus.OK);

        }else{


            throw new RuntimeException(ex);
        }




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

