package com.bouchov.quiz.protocol;

import com.bouchov.quiz.entities.Club;

/**
 * Alexandre Y. Bouchov
 * Date: 18.02.2021
 * Time: 10:10
 * Copyright 2014 ConnectiveGames LLC. All rights reserved.
 */
public class ClubBean {
    private Long id;
    private String uid;
    private String name;
    private Boolean autoInclusion;
    private Boolean owner;

    public ClubBean() {
    }

    public ClubBean(Club that) {
        this(that, null);
    }

    public ClubBean(Club that, Boolean owner) {
        this.id = that.getId();
        this.uid = that.getUid();
        this.name = that.getName();
        this.autoInclusion = owner != null && owner && that.isAutoInclusion();
        this.owner = owner;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getUid() {
        return uid;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Boolean isAutoInclusion() {
        return autoInclusion;
    }

    public void setAutoInclusion(Boolean autoInclusion) {
        this.autoInclusion = autoInclusion;
    }

    public Boolean getOwner() {
        return owner;
    }

    public void setOwner(Boolean owner) {
        this.owner = owner;
    }

    @Override
    public String toString() {
        return "[ClubBean" +
                " id=" + id +
                ", uid='" + uid + '\'' +
                ", name='" + name + '\'' +
                ", owner=" + owner +
                ", autoInclusion=" + autoInclusion +
                ']';
    }
}
