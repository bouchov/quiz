package com.bouchov.quiz.protocol;

import java.util.List;

/**
 * Alexandre Y. Bouchov
 * Date: 29.07.2021
 * Time: 12:10
 * Copyright 2014 ConnectiveGames LLC. All rights reserved.
 */
public class SessionBean {
    private UserBean user;
    private List<QuizBean> games;

    public SessionBean() {
    }

    public SessionBean(UserBean user, List<QuizBean> games) {
        this.user = user;
        this.games = games;
    }

    public UserBean getUser() {
        return user;
    }

    public void setUser(UserBean user) {
        this.user = user;
    }

    public List<QuizBean> getGames() {
        return games;
    }

    public void setGames(List<QuizBean> games) {
        this.games = games;
    }

    @Override
    public String toString() {
        return "[SessionBean" +
                " user=" + user +
                ", games=" + games +
                ']';
    }
}
