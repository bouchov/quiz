package com.bouchov.quiz.protocol;

import java.util.List;

/**
 * Alexandre Y. Bouchov
 * Date: 26.01.2021
 * Time: 14:45
 * Copyright 2014 ConnectiveGames LLC. All rights reserved.
 */
public class ChangedCollectionBean {
    private List<Long> added;
    private List<Long> removed;

    public ChangedCollectionBean() {
    }

    public List<Long> getAdded() {
        return added;
    }

    public void setAdded(List<Long> added) {
        this.added = added;
    }

    public List<Long> getRemoved() {
        return removed;
    }

    public void setRemoved(List<Long> removed) {
        this.removed = removed;
    }

    @Override
    public String toString() {
        return "[ChangedCollectionBean " +
                "added=" + added +
                ", removed=" + removed +
                ']';
    }
}
