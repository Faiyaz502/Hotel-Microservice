package com.hotel.service.HotelService.Exceptions;

public class ResourceNotFoundException extends RuntimeException {

        public ResourceNotFoundException(){
            super("Resource not found in the server!!!!!");
        }

    public ResourceNotFoundException(String messsage){
        super(messsage);
    }





}
