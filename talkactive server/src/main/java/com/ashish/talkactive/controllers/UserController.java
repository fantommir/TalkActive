package com.ashish.talkactive.controllers;


import com.ashish.talkactive.dtos.NewUserDTO;
import com.ashish.talkactive.dtos.UserResponseDTO;
import com.ashish.talkactive.models.User;
import com.ashish.talkactive.services.UserService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@CrossOrigin
@RequestMapping("/api")
public class UserController {

    @Autowired
    private UserService userService;


    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody Map<String, String> payload) {
        String jwtToken = userService.authenticateUser(payload.get("email"), payload.get("password"));
        User storedUser = userService.findByEmail(payload.get("email"));

        UserResponseDTO response = new UserResponseDTO();
        response.setUser(storedUser);
        response.setToken(jwtToken);

        return new  ResponseEntity<>(response, HttpStatus.OK);
    }


    @PostMapping("/register")
    public ResponseEntity<?> register(@Valid @RequestBody NewUserDTO newUserDTO) {
        String rawPassword = newUserDTO.getPassword();
        User registeredUser = userService.registerUser(newUserDTO);
        String jwtToken = userService.authenticateUser(newUserDTO.getEmail(), rawPassword);

        UserResponseDTO response = new UserResponseDTO();
        response.setUser(registeredUser);
        response.setToken(jwtToken);

        return new  ResponseEntity<>(response, HttpStatus.OK);
    }


    @GetMapping("/me")
    public ResponseEntity<?> getLoggedInUserDetails(HttpServletRequest request) {
        User user = userService.getUserFromJwt(request);

        UserResponseDTO response = new UserResponseDTO();
        response.setUser(user);

        return new  ResponseEntity<>(response, HttpStatus.OK);
    }

    @GetMapping("/logout")
    public ResponseEntity<String> logoutUser(HttpServletRequest request) {
        userService.logoutUser(request);
        return new  ResponseEntity<>("Logged out successfully!", HttpStatus.OK);
    }

    @GetMapping("/friend/{id}")
    public ResponseEntity<?> getFriendDetails(@PathVariable("id") String id) {
        User friend = userService.findById(id);
        return new  ResponseEntity<>(friend, HttpStatus.OK);
    }


    @GetMapping("/friends")
    public ResponseEntity<List<User>> getAllFriends(HttpServletRequest request) {
        return ResponseEntity.ok(userService.findAllFriends(request));
    }

    @GetMapping("/users")
    public ResponseEntity<List<User>> searchUsers(@RequestParam(value = "search", required = false) String searchString, HttpServletRequest request) {
        List<User> searchedUsers = userService.searchUsers(searchString, request);
        return new ResponseEntity<>(searchedUsers, HttpStatus.OK);
    }

    @PostMapping("/friend-request/send")
    public ResponseEntity<?> sendOrUnsendRequest(@RequestBody Map<String, String> payload, HttpServletRequest request) {
        userService.sendOrUnsendRequest(payload.get("selectedUserId"), request);
        return new ResponseEntity<>(HttpStatus.OK);
    }

    @PostMapping("/friend-request/accept")
    public ResponseEntity<?> acceptFriendRequest(@RequestBody Map<String, String> payload, HttpServletRequest request) {
        userService.acceptFriendRequest(request, payload.get("senderUserId"));
        return new ResponseEntity<>(HttpStatus.OK);
    }

    @GetMapping("/friend-requests")
    public ResponseEntity<List<User>> retrieveFriendRequests(HttpServletRequest request) {
        List<User> allFriendRequests = userService.retrieveFriendRequests(request);
        return new ResponseEntity<>(allFriendRequests, HttpStatus.OK);
    }

    @GetMapping("/friend-requests/sent")
    public ResponseEntity<List<User>> retrieveSentFriendRequests(HttpServletRequest request){
        List<User> allSentFriendRequests = userService.retrieveSentFriendRequests(request);
        return new ResponseEntity<>(allSentFriendRequests, HttpStatus.OK);
    }

    @PostMapping("/friend-request/cancel-sent")
    public ResponseEntity<?> cancelSentRequest(@RequestBody Map<String, String> payload, HttpServletRequest request){
        userService.cancelSentRequest(payload.get("recipientUserId"), request);
        return new ResponseEntity<>("Friend Request Cancelled!", HttpStatus.OK);
    }
}
