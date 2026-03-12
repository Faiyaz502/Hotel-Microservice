package com.user.service.UserService.Impl;

import com.user.service.UserService.Exceptions.ResourceNotFoundException;
import com.user.service.UserService.Payload.HotelSummaryDto;
import com.user.service.UserService.Payload.PaginatedResponse;
import com.user.service.UserService.Payload.UserProjection;
import com.user.service.UserService.Payload.UserResponse;
import com.user.service.UserService.Repositories.UserRepository;
import com.user.service.UserService.entities.Hotel;
import com.user.service.UserService.entities.Ratings;
import com.user.service.UserService.externalService.HotelService;
import com.user.service.UserService.service.UserService;
import com.user.service.UserService.entities.User;
import io.github.resilience4j.retry.annotation.Retry;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.Caching;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestTemplate;

import java.io.IOException;
import java.util.*;


@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final RestTemplate restTemplate;
    private static final Logger log = LoggerFactory.getLogger(UserServiceImpl.class);
    private final HotelService hotelService;


    @Override
    @CacheEvict(value = "users", allEntries = true)
    public User saveUser(User user) {

      String userID = UUID.randomUUID().toString();


            user.setUserId(userID);

        return userRepository.save(user);
    }


    //Redis Caching

    @Override
    @Cacheable(value = "user_search",
            key = "{#name, #userId, #phone, #email, #lastId, #size}",
            unless = "#result.content.isEmpty()")
    public PaginatedResponse<UserResponse> getAllUsers(
            String name, String userId, String phone, String email, String lastId, int size) {

        //---- Specification
        Specification<User> spec = UserSpecification.build(name, userId, phone, email, lastId);

        // ----- Ensuring the size limit

        int fetchSize = Math.min(size,100);


        //---- Fetch from Repository

        List<UserProjection> projections = userRepository.findBy(spec,q-> q
                .as(UserProjection.class)
                .sortBy(Sort.by("userId").ascending())
                .limit(fetchSize+1)
                .all());

        // --Handle Empty Results early
        if (projections.isEmpty()) {
            return new PaginatedResponse<>(Collections.emptyList(), null);
        }



        List<UserResponse> responses = projections.stream()
                .limit(fetchSize)
                .map(this::toResponse).toList();



        String nextCursor = null;


        //== Determine the Next Cursor

        if (projections.size() > fetchSize) {
            // The last item (index fetchSize) is the starting point for the next request
            nextCursor = projections.get(fetchSize).getUserId();

        }


        log.info("Hotel Search: name={}, email={}, results={}, hasNext={}",
                name, email, responses.size(), nextCursor != null);

        return new PaginatedResponse<>(responses, nextCursor);
    }


    int retryCount = 1 ;

    @Override
    @Cacheable(value = "user", key = "#id")
    @Retry(name = "ratingHotelRetry" , fallbackMethod = "RetryFallbackForFetchingUserById")
    public User getUserById(String id) {

        //Fetching from database
        User user = userRepository.findById(id).orElseThrow(() ->
                new ResourceNotFoundException("ID is not found in the DB"));

        retryCount++;

        log.info("<<<Retry Value >>>  "+retryCount);

        ///  Fetch rating from Rating service
            //==== http://localhost:8083/ratings/users/3277b3b6-4af9-480c-b2e1-64883eb737bf example call in postman

        log.info("------------Rating Service MicroService Calling ");


        Ratings[] ratingsArr = restTemplate.getForObject("http://RATINGSERVICE/api/v1/ratings/users/"+user.getUserId(), Ratings[].class);

       log.info("Rating MicroService Response {} ->",ratingsArr.toString());



       List<Ratings> ratings = Arrays.stream(ratingsArr).toList();

        ratings.forEach(rating -> {
            try {
                // Call using the correct DTO
                ResponseEntity<HotelSummaryDto> hotelRes = hotelService.getHotel(rating.getHotelId());
                HotelSummaryDto dto = hotelRes.getBody();

                if (dto != null) {
                    // Map DTO to Entity so you can set it in the Rating
                    Hotel hotel = Hotel.builder()
                            .id(dto.getId())
                            .name(dto.getName())
                            .location(dto.getLocation())
                            .about(dto.getAbout())
                            .contact(dto.getContact())
                            .build();

                    rating.setHotel(hotel);
                }
            } catch (feign.FeignException.NotFound e) {
                log.error("Hotel ID {} truly not found in HotelService", rating.getHotelId());
            }
        });






       user.setRatings(ratings);



    return user;

    }


    //Fallback Method Of Retry

    public User RetryFallbackForFetchingUserById(String id, Exception ex) {
        // Only handle network / server errors
        if (ex instanceof HttpServerErrorException || ex instanceof ResourceAccessException || ex instanceof IOException) {

            log.error("Retry fallback executed: " + ex.getMessage());

            return User.builder()
                    .userId(id)
                    .name("Dummy User (Retry)")
                    .email("dummyretry@gmail.com")
                    .about("Service temporarily unavailable")
                    .ratings(new ArrayList<>())
                    .build();
        } else {
            // For ignored exceptions, rethrow to propagate to controller / exception handler
            throw new RuntimeException(ex);
        }
    }

    @Override
    @CachePut(value = "user", key = "#id")
    @CacheEvict(value = "users", allEntries = true)
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
    @Caching(evict = {
            @CacheEvict(value = "user", key = "#id"),
            @CacheEvict(value = "users", allEntries = true)
    })
    public String deleteUser(String userId) {
          try {
              userRepository.deleteById(userId);

          }catch (Exception e){
              return e.getMessage();
          }

        return "Successfully Deleted";
    }


    private UserResponse toResponse(UserProjection projection){


        return new UserResponse(
                projection.getUserId(), projection.getName(), projection.getPhone(), projection.getEmail(),
                projection.getAbout()
        );

    }





}
