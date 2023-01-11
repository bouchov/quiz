package com.bouchov.quiz.entities;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

/**
 * Alexandre Y. Bouchov
 * Date: 05.02.2021
 * Time: 15:17
 * Copyright 2014 ConnectiveGames LLC. All rights reserved.
 */
public interface ClubRepository extends CrudRepository<Club,Long>, PagingAndSortingRepository<Club,Long> {
    Optional<Club> findByName(String name);

    Optional<Club> findByUid(String uid);

    Page<Club> findAllByParticipants(
            @Param("user") User user,
            Pageable pageable);

    @Query("select C from Club C left outer join C.participants P where P = :user and upper(C.name) like %:name%")
    Page<Club> findAllByParticipantsAndNameUpper(
            @Param("user") User user,
            @Param("name") String name,
            Pageable pageable);
}
