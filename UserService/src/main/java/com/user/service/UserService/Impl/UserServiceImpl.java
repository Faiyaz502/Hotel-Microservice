package com.user.service.UserService.Impl;

import com.user.service.UserService.Exceptions.ResourceNotFoundException;
import com.user.service.UserService.Repositories.UserRepository;
import com.user.service.UserService.entities.Hotel;
import com.user.service.UserService.entities.Ratings;
import com.user.service.UserService.externalService.HotelService;
import com.user.service.UserService.service.UserService;
import com.user.service.UserService.entities.User;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;


@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final RestTemplate restTemplate;
    private static final Logger log = LoggerFactory.getLogger(UserServiceImpl.class);
    private final HotelService hotelService;


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

        //Fetching from database
        User user = userRepository.findById(id).orElseThrow(() ->
                new ResourceNotFoundException("ID is not found in the DB"));

        ///  Fetch rating from Rating service

        //==== http://localhost:8083/ratings/users/3277b3b6-4af9-480c-b2e1-64883eb737bf example call in postman


        Ratings[] ratingsArr = restTemplate.getForObject("http://RATINGSERVICE/ratings/users/"+user.getUserId(), Ratings[].class);

       log.info(ratingsArr.toString());



       List<Ratings> ratings = Arrays.stream(ratingsArr).toList();

        ratings.stream().map(ratings1 -> {

            log.info("Hotel ID : --------"+ ratings1.getHotelId());

            //Fetch the hotels from the hotel service api

            ResponseEntity<Hotel> res = restTemplate.getForEntity("http://HOTELSERVICE/hotels/"+ratings1.getHotelId(), Hotel.class);

             Hotel hotel = res.getBody();


             //With Feign Client


            Hotel hotelFeign = hotelService.getHotel(ratings1.getHotelId());

            log.info("------------- Feign Client : "+ hotelFeign.getName(),hotelFeign.getAbout(),hotelFeign.getLocation());





             /// we can get status code and header if we call for entity
            log.info(res.getBody().toString());
             log.info(res.getStatusCode().toString());
             log.info(res.getHeaders().toString());

            //Set the hotel to rating

            ratings1.setHotel(hotel);


            return ratings1;
        }).toList();



       user.setRatings(ratings);



    return user;

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
