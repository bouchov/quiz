package com.bouchov.quiz.entities;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.ManyToMany;
import javax.persistence.ManyToOne;
import java.util.Set;

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

    private boolean autoInclusion;

    @ManyToMany(mappedBy = "clubs")
    private Set<User> participants;

    public Club() {
    }

    public Club(String name, String uid, User owner, boolean autoInclusion) {
        this.name = name;
        this.uid = uid;
        this.owner = owner;
        this.autoInclusion = autoInclusion;
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

    public boolean isAutoInclusion() {
        return autoInclusion;
    }

    public void setAutoInclusion(boolean autoInclusion) {
        this.autoInclusion = autoInclusion;
    }

    public Set<User> getParticipants() {
        return participants;
    }

    public void setParticipants(Set<User> participants) {
        this.participants = participants;
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
