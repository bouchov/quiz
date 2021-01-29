package com.bouchov.quiz.protocol;

/**
 * Alexandre Y. Bouchov
 * Date: 29.01.2021
 * Time: 11:37
 * Copyright 2014 ConnectiveGames LLC. All rights reserved.
 */
public class QuizFilterBean extends FilterBean {
    private String name;

    public QuizFilterBean() {
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    @Override
    public String toString() {
        return "[QuizFilterBean" +
                " super=" + super.toString() +
                ", name='" + name + '\'' +
                ']';
    }
}
