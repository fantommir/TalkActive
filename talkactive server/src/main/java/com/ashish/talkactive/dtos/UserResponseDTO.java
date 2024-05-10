package com.ashish.talkactive.dtos;


import com.ashish.talkactive.models.User;
import lombok.Data;

@Data
public class UserResponseDTO {

    private User user;
    private String token;
}
