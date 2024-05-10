package com.ashish.talkactive.repositories;

import com.ashish.talkactive.models.Chat;
import com.ashish.talkactive.models.User;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Set;

@Repository
public interface ChatRepository extends MongoRepository<Chat, String> {

    List<Chat> findByUsersIn(Set<User> users);

}
