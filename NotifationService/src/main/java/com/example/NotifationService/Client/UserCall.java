package com.example.NotifationService.Client;

import com.example.NotifationService.payload.User;
import jakarta.ws.rs.GET;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/UserGet")
@RequiredArgsConstructor
public class UserCall {

    private final UserClient userClient;

    @GetMapping
    public User getUser(@RequestParam String userId){


        return userClient.getUserDetails(userId);
    }
}
