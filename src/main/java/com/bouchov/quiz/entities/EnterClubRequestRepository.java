package com.bouchov.quiz.entities;

import org.springframework.data.repository.PagingAndSortingRepository;

import java.util.Optional;

/**
 * Alexandre Y. Bouchov
 * Date: 26.02.2021
 * Time: 13:52
 * Copyright 2014 ConnectiveGames LLC. All rights reserved.
 */
public interface EnterClubRequestRepository extends PagingAndSortingRepository<EnterClubRequest,Long> {
    Optional<EnterClubRequest> findByUserAndClub(User user, Club club);
}
