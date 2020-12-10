package com.bouchov.quiz.entities;

import org.springframework.data.repository.CrudRepository;

import java.util.Optional;

public interface UserRepository
        extends CrudRepository<User, Long> {
    Optional<User> findByLogin(String login);
}
