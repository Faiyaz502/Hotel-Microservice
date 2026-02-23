package com.user.service.UserService.service;

import com.user.service.UserService.entities.User;

public interface KafkaService {

    public Boolean userCreatedPublisher(User user);


}
