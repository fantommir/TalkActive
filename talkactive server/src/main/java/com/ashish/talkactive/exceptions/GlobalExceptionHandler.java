package com.ashish.talkactive.exceptions;


import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import java.time.LocalDateTime;

@ControllerAdvice
public class GlobalExceptionHandler {


    @ExceptionHandler(UserNotFoundException.class)
    public ResponseEntity<ApiError> resultNotFoundExceptionHandler(UserNotFoundException ex) {

        ApiError message = new ApiError(HttpStatus.NOT_FOUND.value(), LocalDateTime.now(), ex.getLocalizedMessage());

        return new ResponseEntity<>(message, HttpStatus.NOT_FOUND);
    }


    @ExceptionHandler
    public ResponseEntity<ApiError> methodArgumentNotValidExceptionHandler(MethodArgumentNotValidException ex) {

        FieldError fieldError = ex.getBindingResult().getFieldErrors().get(0);

        ApiError message = new ApiError(HttpStatus.NOT_FOUND.value(), LocalDateTime.now(), fieldError.getField() + ": " + fieldError.getDefaultMessage());

        return new ResponseEntity<>(message, HttpStatus.BAD_REQUEST);
    }



    // To handle all other types of exception
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiError> globalExceptionHandler(Exception ex) {

        ApiError message = new ApiError(HttpStatus.INTERNAL_SERVER_ERROR.value(), LocalDateTime.now(), ex.getLocalizedMessage());

        return new ResponseEntity<>(message, HttpStatus.INTERNAL_SERVER_ERROR);
    }

}
