package com.ashish.talkactive.dtos;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class MessageDTO {

    private String content;
    private String chat;
    private String sender;
    private boolean image;
    private LocalDateTime timestamp;
}
