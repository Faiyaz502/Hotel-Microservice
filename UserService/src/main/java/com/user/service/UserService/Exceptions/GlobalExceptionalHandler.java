package com.user.service.UserService.Exceptions;

import com.user.service.UserService.Payload.ApiResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

@RestControllerAdvice
public class GlobalExceptionalHandler extends ResponseEntityExceptionHandler {

        @ExceptionHandler(ResourceNotFoundException.class)
        public ResponseEntity<ApiResponse> handleResourceNotFoundException(ResourceNotFoundException ex){

              String message  = ex.getMessage();
              ApiResponse response = ApiResponse.builder()
                      .message(message)
                      .success(true)
                      .status(HttpStatus.NOT_FOUND.value()).build();


              return new ResponseEntity<ApiResponse>(response,HttpStatus.NOT_FOUND);

        }



}
