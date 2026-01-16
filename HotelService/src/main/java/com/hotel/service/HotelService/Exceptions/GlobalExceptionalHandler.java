package com.hotel.service.HotelService.Exceptions;


import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

@RestControllerAdvice
public class GlobalExceptionalHandler extends ResponseEntityExceptionHandler {

        @ExceptionHandler(ResourceNotFoundException.class)
        public ResponseEntity<Map<String, Object>> handleResourceNotFoundException(ResourceNotFoundException ex){

              String message  = ex.getMessage();
              Map<String, Object> response = new HashMap<>();

              response.put("message",message);
              response.put("Success",false);
              response.put("status",HttpStatus.NOT_FOUND);



              return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);

        }



}
