package com.bouchov.quiz.protocol;

import com.bouchov.quiz.entities.EnterClubStatus;

import java.util.Arrays;

/**
 * Alexandre Y. Bouchov
 * Date: 03.03.2021
 * Time: 16:50
 * Copyright 2014 ConnectiveGames LLC. All rights reserved.
 */
public class EnterClubRequestFilterBean extends FilterBean {
    private EnterClubStatus[] status;

    public EnterClubRequestFilterBean() {
    }

    public EnterClubStatus[] getStatus() {
        return status;
    }

    public void setStatus(EnterClubStatus[] status) {
        this.status = status;
    }

    @Override
    public String toString() {
        return "[EnterClubRequestFilterBean super=" + super.toString() +
                ", status=" + Arrays.toString(status) +
                ']';
    }
}
