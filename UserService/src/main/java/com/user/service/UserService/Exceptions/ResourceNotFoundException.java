package com.user.service.UserService.Exceptions;

public class ResourceNotFoundException extends RuntimeException {

        public ResourceNotFoundException(){
            super("Resource not found in the server!!!!!");
        }

    public ResourceNotFoundException(String messsage){
        super(messsage);
    }





}
