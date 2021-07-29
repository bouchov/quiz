package com.bouchov.quiz.protocol;

/**
 * Alexandre Y. Bouchov
 * Date: 29.07.2021
 * Time: 15:20
 * Copyright 2014 ConnectiveGames LLC. All rights reserved.
 */
public class UpdateResultBean {
    private int created;
    private int modified;
    private int total;

    public UpdateResultBean() {
    }

    public UpdateResultBean(int created, int modified, int total) {
        this.created = created;
        this.modified = modified;
        this.total = total;
    }

    public int getCreated() {
        return created;
    }

    public void setCreated(int created) {
        this.created = created;
    }

    public int getModified() {
        return modified;
    }

    public void setModified(int modified) {
        this.modified = modified;
    }

    public int getTotal() {
        return total;
    }

    public void setTotal(int total) {
        this.total = total;
    }

    @Override
    public String toString() {
        return "[UpdateResultBean" +
                " created=" + created +
                ", modified=" + modified +
                ", total=" + total +
                ']';
    }
}
