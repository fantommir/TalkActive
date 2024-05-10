package com.ashish.talkactive.exceptions;

import lombok.Data;

import java.time.LocalDateTime;


@Data
public class ApiError {

    private boolean success = false;

    private int statusCode;

    private LocalDateTime timestamp;

    private String message;


    public ApiError(int statusCode, LocalDateTime timestamp, String message){
        this.statusCode =statusCode;
        this.timestamp =timestamp;
        this.message = message;
    }
}
