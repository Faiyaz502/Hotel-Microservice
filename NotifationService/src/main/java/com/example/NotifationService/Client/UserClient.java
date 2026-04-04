package com.example.NotifationService.Client;

import com.example.NotifationService.Config.FeignConfig;
import com.example.NotifationService.payload.User;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

@FeignClient(name = "USERSERVICE",configuration = FeignConfig.class)
public interface UserClient {

    @GetMapping("api/v1/users/id/{id}")
    User getUserDetails(@RequestParam String id);
}
