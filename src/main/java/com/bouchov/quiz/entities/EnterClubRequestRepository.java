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
 * Date: 26.02.2021
 * Time: 13:52
 * Copyright 2014 ConnectiveGames LLC. All rights reserved.
 */
public interface EnterClubRequestRepository extends CrudRepository<EnterClubRequest,Long>,
        PagingAndSortingRepository<EnterClubRequest,Long> {
    Optional<EnterClubRequest> findByUserAndClub(User user, Club club);

    @Query("select R from EnterClubRequest R where R.club = :club and R.status in (:status)")
    Page<EnterClubRequest> findAllByClubAndStatus(@Param("club") Club club, EnterClubStatus[] status, Pageable pageable);
}
