package com.bouchov.quiz.protocol;

import com.bouchov.quiz.entities.EnterClubStatus;

/**
 * Alexandre Y. Bouchov
 * Date: 26.02.2021
 * Time: 13:36
 * Copyright 2014 ConnectiveGames LLC. All rights reserved.
 */
public class ClubRequestBean {
    private ClubBean club;
    private EnterClubStatus status;

    public ClubRequestBean() {
    }

    public ClubRequestBean(ClubBean club, EnterClubStatus status) {
        this.club = club;
        this.status = status;
    }

    public ClubBean getClub() {
        return club;
    }

    public void setClub(ClubBean club) {
        this.club = club;
    }

    public EnterClubStatus getStatus() {
        return status;
    }

    public void setStatus(EnterClubStatus status) {
        this.status = status;
    }

    @Override
    public String toString() {
        return "[ClubRequestBean" +
                " club=" + club +
                ", status=" + status +
                ']';
    }
}
