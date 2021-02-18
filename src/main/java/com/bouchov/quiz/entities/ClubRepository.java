package com.bouchov.quiz.entities;

import org.springframework.data.repository.CrudRepository;

import java.util.List;
import java.util.Optional;

/**
 * Alexandre Y. Bouchov
 * Date: 05.02.2021
 * Time: 15:17
 * Copyright 2014 ConnectiveGames LLC. All rights reserved.
 */
public interface ClubRepository extends CrudRepository<Club,Long> {
    Optional<Club> findByName(String name);

    Optional<Club> findByUid(String uid);

    List<Club> findAllByOwner(User owner);
}
