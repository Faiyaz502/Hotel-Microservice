package com.user.service.UserService.service;

public interface RedisService {


    public void saveValue(String key, String value);

    public String getValue(String key);

}
