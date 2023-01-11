package com.bouchov.quiz.entities;

import jakarta.persistence.Entity;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;

/**
 * Alexandre Y. Bouchov
 * Date: 26.02.2021
 * Time: 13:41
 * Copyright 2014 ConnectiveGames LLC. All rights reserved.
 */
@Entity
@Table(uniqueConstraints = {
        @UniqueConstraint(columnNames = {"user_id", "club_id"})
})
public class EnterClubRequest extends BasicEntity {
    @ManyToOne(optional = false)
    private User user;
    @ManyToOne(optional = false)
    private Club club;
    private EnterClubStatus status;

    public EnterClubRequest() {
    }

    public EnterClubRequest(User user, Club club, EnterClubStatus status) {
        this.user = user;
        this.club = club;
        this.status = status;
    }

    public User getUser() {
        return user;
    }

    public Club getClub() {
        return club;
    }

    public EnterClubStatus getStatus() {
        return status;
    }

    public void setStatus(EnterClubStatus status) {
        this.status = status;
    }
}
