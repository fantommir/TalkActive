package com.ashish.talkactive.models;


import jakarta.validation.constraints.Email;
import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.ArrayList;
import java.util.List;

@Data
@Document (collection = "users")
public class User {

    @Id
    private String id;

    @Email
    private String email;

    private String name;

    private String password;

    private Status status;

    private String profileImage;

    private List<String> friendRequests = new ArrayList<>();

    private List<String> friends = new ArrayList<>();

    private List<String> sentFriendRequests = new ArrayList<>();

    public User(String name, String email, String password, String profileImage) {
        this.name = name;
        this.email = email;
        this.password = password;
        this.profileImage = profileImage;
    }
}


