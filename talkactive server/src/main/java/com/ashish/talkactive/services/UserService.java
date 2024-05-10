package com.ashish.talkactive.services;



import com.ashish.talkactive.cloudinary.CloudinaryImageService;
import com.ashish.talkactive.dtos.NewUserDTO;
import com.ashish.talkactive.exceptions.UserNotFoundException;
import com.ashish.talkactive.models.Status;
import com.ashish.talkactive.models.User;
import com.ashish.talkactive.repositories.UserRepository;
import com.ashish.talkactive.security.JwtUtils;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import javax.xml.bind.DatatypeConverter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Service
public class UserService {

    @Autowired
    private JwtUtils jwtUtils;


    @Autowired
    private UserRepository userRepository;


    @Autowired
    private AuthenticationManager authenticationManager;


    @Autowired
    private PasswordEncoder encoder;


    @Autowired
    private MongoTemplate mongoTemplate;

    @Autowired
    private CloudinaryImageService cloudinaryImageService;


    public User registerUser(NewUserDTO newUserDTO){
        if (existsByEmail(newUserDTO.getEmail())) {
            throw new RuntimeException("Email is already taken!");
        }

        String base64 = newUserDTO.getProfileImage();
        String image = processProfileImage(base64, "Profile_Images");

        User user = new User(
                newUserDTO.getName(),
                newUserDTO.getEmail(),
                encoder.encode(newUserDTO.getPassword()),
                image
        );
        user.setStatus(Status.ONLINE);
        User storedUser = userRepository.save(user);

        return storedUser;
    }


    public String processProfileImage(String base64, String folder) {
        byte[] data = DatatypeConverter.parseBase64Binary(base64.split(",")[1]);
        Map<String, String> imageData = cloudinaryImageService.upload(data, "TalkActive/"+folder);
        return imageData.get("secure_url");
    }


    public User findById(String id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new UserNotFoundException("No User was found with the given ID: " + id));
        return user;
    }

    public User findByEmail(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new UserNotFoundException("No User was found with the given Email: " + email));
        return user;
    }

    public boolean existsByEmail(String email) {
        return userRepository.existsByEmail(email);
    }


    public String authenticateUser(String email, String password) {
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(email, password));
        SecurityContextHolder.getContext().setAuthentication(authentication);

        User user = userRepository.findByEmail(email).get();
        user.setStatus(Status.ONLINE);
        userRepository.save(user);

        String jwt = jwtUtils.generateJwtToken(authentication);
        return jwt;
    }


    public User getUserFromJwt(HttpServletRequest request) {
        String authorizationHeader = request.getHeader("Authorization");
        String token ="";
        if (authorizationHeader != null && authorizationHeader.startsWith("Bearer ")) {
            token = authorizationHeader.substring(7);
        } else {
            throw new RuntimeException("Jwt Token is invalid");
        }

        User user = findById(jwtUtils.getIdFromJwtToken(token));
        return user;
    }

    public void logoutUser(HttpServletRequest request) {
        User user = getUserFromJwt(request);
        user.setStatus(Status.OFFLINE);
        userRepository.save(user);
        SecurityContextHolder.getContext().setAuthentication(null);
    }

    public List<User> findAllFriends(HttpServletRequest request) {
        User user = getUserFromJwt(request);
        List<String> friends = user.getFriends();
        List<User> friendsList = new ArrayList<>();
        for (String s: friends){
            User oneFriend = findById(s);
            friendsList.add(oneFriend);
        }
        return friendsList;
    }

    public List<User> searchUsers(String searchString, HttpServletRequest request) {
        Query query = new Query();
        if (searchString != null) {
            Criteria keywordCriteria = Criteria.where("name").regex(searchString, "i");
            query.addCriteria(keywordCriteria);
        }
        List<User> searchedUsers = mongoTemplate.find(query, User.class);

        // we want to remove the user who is searching from the list if present in the list
        User userToRemove = getUserFromJwt(request);
        searchedUsers.removeIf(u -> u.getId().equals(userToRemove.getId()));

        return searchedUsers;
    }

    public void sendOrUnsendRequest(String selectedUserId, HttpServletRequest request){
        User selectedUser = findById(selectedUserId);
        User currentUser = getUserFromJwt(request);

        if (selectedUser.getFriendRequests().contains(currentUser.getId()) && currentUser.getSentFriendRequests().contains(selectedUserId)){
            selectedUser.getFriendRequests().remove(currentUser.getId());
            currentUser.getSentFriendRequests().remove(selectedUserId);
        } else {
            selectedUser.getFriendRequests().add(0, currentUser.getId());
            currentUser.getSentFriendRequests().add(0, selectedUserId);
        }

        userRepository.save(selectedUser);
        userRepository.save(currentUser);
    }


    public void acceptFriendRequest(HttpServletRequest request, String senderUserId){
        User sender = findById(senderUserId);
        User recipient = getUserFromJwt(request);

        sender.getFriends().add(recipient.getId());
        recipient.getFriends().add(senderUserId);

        recipient.getFriendRequests().remove(senderUserId);
        sender.getSentFriendRequests().remove(recipient.getId());

        userRepository.save(sender);
        userRepository.save(recipient);
    }

    public void cancelSentRequest(String recipientUserId, HttpServletRequest request){
        User recipientUser = findById(recipientUserId);
        User loggedInUser = getUserFromJwt(request);
        loggedInUser.getSentFriendRequests().remove(recipientUserId);
        recipientUser.getFriendRequests().remove(loggedInUser.getId());
        userRepository.save(recipientUser);
        userRepository.save(loggedInUser);
    }

    public List<User> retrieveFriendRequests(HttpServletRequest request){
        User user = getUserFromJwt(request);
        List<String> friendRequests = user.getFriendRequests();
        List<User> friendRequestList = new ArrayList<>();
        for (String s: friendRequests){
            User oneRequest = findById(s);
            friendRequestList.add(oneRequest);
        }
        return friendRequestList;
    }

    public List<User> retrieveSentFriendRequests(HttpServletRequest request){
        User user = getUserFromJwt(request);
        List<String> sentFriendRequests = user.getSentFriendRequests();
        List<User> sentFriendRequestList = new ArrayList<>();
        for (String s: sentFriendRequests){
            User oneSentRequest = findById(s);
            sentFriendRequestList.add(oneSentRequest);
        }
        return sentFriendRequestList;
    }
}
