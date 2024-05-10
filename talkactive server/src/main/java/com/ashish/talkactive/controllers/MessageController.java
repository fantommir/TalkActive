package com.ashish.talkactive.controllers;



import com.ashish.talkactive.dtos.MessageDTO;
import com.ashish.talkactive.models.Chat;
import com.ashish.talkactive.models.Message;
import com.ashish.talkactive.repositories.ChatRepository;
import com.ashish.talkactive.repositories.MessageRepository;
import com.ashish.talkactive.repositories.UserRepository;
import com.ashish.talkactive.services.MessageService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.web.bind.annotation.*;

import java.util.Map;


@RestController
@CrossOrigin
public class MessageController {

    @Autowired
    private SimpMessagingTemplate messagingTemplate;


    @Autowired
    private MessageService messageService;

    @Autowired
    private ChatRepository chatRepository;

    @Autowired
    private UserRepository userRepository;


    @MessageMapping("/private-chat")
    public void processMessage(@Payload MessageDTO messageDTO) {
        Chat chat = chatRepository.findById(messageDTO.getChat()).get();

        Message newMessage = null;
        if (messageDTO.isImage()){
            String image = messageService.processImage(messageDTO.getContent(), "Message_Images");
            newMessage = new Message(messageDTO.getChat(), messageDTO.getSender(), image, messageDTO.isImage(), messageDTO.getTimestamp());
        }
        else {
            newMessage = new Message(messageDTO.getChat(), messageDTO.getSender(), messageDTO.getContent(), messageDTO.isImage(), messageDTO.getTimestamp());
        }
        Message savedMsg = messageService.save(newMessage);
        chat.getMessages().add(savedMsg);
        chatRepository.save(chat);

        messagingTemplate.convertAndSend("/group/" + messageDTO.getChat(), savedMsg);
    }


    @MessageMapping("/typing")
    public void typingMessage(@Payload MessageDTO messageDTO) {
        messagingTemplate.convertAndSend("/group/" + messageDTO.getChat(), messageDTO);
    }


    @MessageMapping("/notification")
    public void notificationMessage(@Payload Map<String, String> payload) {
        messagingTemplate.convertAndSendToUser(payload.get("recipient"), "/notification", payload);
    }

}

