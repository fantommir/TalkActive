package com.ashish.talkactive.models;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;

@Data
@Document(collection = "messages")
public class Message {

    @Id
    private String id;

    private String sender;

    private String chat;

    private String content;  // text or image in base64 string

    private boolean image;

    private LocalDateTime timestamp;

    public Message(String chat, String sender, String content, boolean image,  LocalDateTime timestamp){
        this.chat = chat;
        this.sender = sender;
        this.content = content;
        this.image = image;
        this.timestamp = timestamp;
    }

}
