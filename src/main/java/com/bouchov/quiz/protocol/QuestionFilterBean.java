package com.bouchov.quiz.protocol;

/**
 * Alexandre Y. Bouchov
 * Date: 26.01.2021
 * Time: 9:25
 * Copyright 2014 ConnectiveGames LLC. All rights reserved.
 */
public class QuestionFilterBean extends FilterBean {
    private Long categoryId;
    private Long quizId;

    public QuestionFilterBean() {
    }

    public Long getCategoryId() {
        return categoryId;
    }

    public void setCategoryId(Long categoryId) {
        this.categoryId = categoryId;
    }

    public Long getQuizId() {
        return quizId;
    }

    public void setQuizId(Long quizId) {
        this.quizId = quizId;
    }

    @Override
    public String toString() {
        return "[QuestionFilterBean" +
                " super=" + super.toString() +
                ", categoryId=" + categoryId +
                ", quizId=" + quizId +
                ']';
    }
}
