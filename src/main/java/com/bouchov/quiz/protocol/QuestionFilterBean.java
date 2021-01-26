package com.bouchov.quiz.protocol;

/**
 * Alexandre Y. Bouchov
 * Date: 26.01.2021
 * Time: 9:25
 * Copyright 2014 ConnectiveGames LLC. All rights reserved.
 */
public class QuestionFilterBean {
    private Long categoryId;
    private Long quizId;
    private Integer page;
    private Integer size;

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
        return "[QuestionFilterBean " +
                "categoryId=" + categoryId +
                ", quizId=" + quizId +
                ", page=" + page +
                ", size=" + size +
                ']';
    }
}
