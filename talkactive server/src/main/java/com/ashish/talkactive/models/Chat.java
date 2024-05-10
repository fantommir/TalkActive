package com.ashish.talkactive.models;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.DBRef;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.ArrayList;
import java.util.List;

@Data
@Document(collection = "chats")
public class Chat {

    @Id
    private String id;

    @DBRef
    private List<User> users = new ArrayList<>();

    @DBRef
    private List<Message> messages = new ArrayList<>();

    public Chat (List<User> users){
        this.users = users;
    }
}
