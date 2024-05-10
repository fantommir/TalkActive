package com.ashish.talkactive.repositories;

import com.ashish.talkactive.models.Status;
import com.ashish.talkactive.models.User;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepository extends MongoRepository<User, String> {

    List<User> findAllByStatus(Status status);

    boolean existsByEmail(String email);

    Optional<User> findByEmail(String email);
}
