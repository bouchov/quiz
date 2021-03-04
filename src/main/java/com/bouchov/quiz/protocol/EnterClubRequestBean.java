package com.bouchov.quiz.protocol;

import com.bouchov.quiz.entities.EnterClubRequest;
import com.bouchov.quiz.entities.EnterClubStatus;

/**
 * Alexandre Y. Bouchov
 * Date: 03.03.2021
 * Time: 16:43
 * Copyright 2014 ConnectiveGames LLC. All rights reserved.
 */
public class EnterClubRequestBean {
    private Long id;
    private UserBean user;
    private ClubBean club;
    private EnterClubStatus status;

    public EnterClubRequestBean() {
    }

    public EnterClubRequestBean(EnterClubRequest that) {
        this.id = that.getId();
        this.user = new UserBean(that.getUser());
        this.club = new ClubBean(that.getClub());
        this.status = that.getStatus();
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public UserBean getUser() {
        return user;
    }

    public void setUser(UserBean user) {
        this.user = user;
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
        return "[EnterClubRequestBean" +
                " id=" + id +
                ", user=" + user +
                ", club=" + club +
                ", status=" + status +
                ']';
    }
}
