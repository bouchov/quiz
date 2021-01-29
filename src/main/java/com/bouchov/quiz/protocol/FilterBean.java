package com.bouchov.quiz.protocol;

/**
 * Alexandre Y. Bouchov
 * Date: 29.01.2021
 * Time: 11:34
 * Copyright 2014 ConnectiveGames LLC. All rights reserved.
 */
public class FilterBean {
    private Integer page;
    private Integer size;

    public FilterBean() {
    }

    public Integer getPage() {
        return page;
    }

    public void setPage(Integer page) {
        this.page = page;
    }

    public Integer getSize() {
        return size;
    }

    public void setSize(Integer size) {
        this.size = size;
    }

    @Override
    public String toString() {
        return "[FilterBean " +
                "page=" + page +
                ", size=" + size +
                ']';
    }
}
