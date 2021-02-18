package com.bouchov.quiz.entities;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.ManyToOne;

/**
 * Alexandre Y. Bouchov
 * Date: 05.02.2021
 * Time: 14:51
 * Copyright 2014 ConnectiveGames LLC. All rights reserved.
 */
@Entity
public class Club extends BasicEntity {
    @Column(unique = true, nullable = false)
    private String name;

    @Column(unique = true, nullable = false)
    private String uid;

    @ManyToOne(optional = false)
    private User owner;

    public Club() {
    }

    public Club(String name, String uid, User owner) {
        this.name = name;
        this.uid = uid;
        this.owner = owner;
    }

    public String getName() {
        return name;
    }

    public String getUid() {
        return uid;
    }

    public User getOwner() {
        return owner;
    }

    @Override
    public String toString() {
        return "[Club" +
                " name='" + name + '\'' +
                ", uid='" + uid + '\'' +
                ", owner=" + owner +
                ']';
    }
}
