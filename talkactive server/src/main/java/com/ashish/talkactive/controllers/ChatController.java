package com.ashish.talkactive.controllers;


import com.ashish.talkactive.dtos.ChatRequest;
import com.ashish.talkactive.models.Chat;
import com.ashish.talkactive.models.User;
import com.ashish.talkactive.repositories.ChatRepository;
import com.ashish.talkactive.repositories.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;
import java.util.Set;

@RestController
@CrossOrigin
@RequestMapping("/api")
public class ChatController {


    @Autowired
    private ChatRepository chatRepository;

    @Autowired
    private UserRepository userRepository;


    @PostMapping("/fetch-chat")
    public ResponseEntity<Chat> fetchOrCreateChat(@RequestBody ChatRequest chatRequest) {
        String userOneId = chatRequest.getUser1();
        String userTwoId = chatRequest.getUser2();
        Optional<User> userOneOptional = userRepository.findById(userOneId);
        Optional<User> userTwoOptional = userRepository.findById(userTwoId);

        if (userOneOptional.isPresent() && userTwoOptional.isPresent()) {
            User userOne = userOneOptional.get();
            User userTwo = userTwoOptional.get();

            Set<User> users = Set.of(userOne, userTwo);

            List<Chat> chats = chatRepository.findByUsersIn(users);

            for (Chat c : chats) {
                if ((c.getUsers().get(0).getId().equals(userOneId) && c.getUsers().get(1).getId().equals(userTwoId)) || (c.getUsers().get(0).getId().equals(userTwoId) && c.getUsers().get(1).getId().equals(userOneId))) {
                    Chat resultChat = c;
                    return new ResponseEntity<>(resultChat, HttpStatus.OK);
                }
            }
            Chat newChat = new Chat(List.of(userOne, userTwo));
            Chat  storedChat = chatRepository.save(newChat);
            return new ResponseEntity<>(storedChat, HttpStatus.OK);
        } else {
            throw new RuntimeException("One of the user does not exist");
        }
    }

}
