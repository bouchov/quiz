package com.bouchov.quiz.protocol;

import com.bouchov.quiz.entities.QuestionSelectionStrategy;
import com.bouchov.quiz.entities.Quiz;
import com.bouchov.quiz.entities.QuizStatus;
import com.bouchov.quiz.entities.QuizType;

import java.util.Date;
import java.util.List;

public class QuizBean {
    private Long id;
    private String author;
    private String name;
    private QuizType type;
    private int minPlayers;
    private int maxPlayers;
    private QuestionSelectionStrategy selectionStrategy;
    private int questionsNumber;
    private Date startDate;
    private Date startedDate;
    private QuizStatus status;
    private QuizResultBean result;
    private List<QuestionBean> questions;

    public QuizBean() {
    }

    public QuizBean(Quiz quiz) {
        this.id = quiz.getId();
        this.author = quiz.getAuthor().getNickname();
        this.name = quiz.getName();
        this.type = quiz.getType();
        this.minPlayers = quiz.getMinPlayers();
        this.maxPlayers = quiz.getMaxPlayers();
        this.selectionStrategy = quiz.getSelectionStrategy();
        this.questionsNumber = quiz.getQuestionsNumber();
        this.startDate = quiz.getStartDate();
        this.startedDate = quiz.getStartedDate();
        this.status = quiz.getStatus();
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getAuthor() {
        return author;
    }

    public void setAuthor(String author) {
        this.author = author;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public QuizType getType() {
        return type;
    }

    public void setType(QuizType type) {
        this.type = type;
    }

    public int getMinPlayers() {
        return minPlayers;
    }

    public void setMinPlayers(int minPlayers) {
        this.minPlayers = minPlayers;
    }

    public int getMaxPlayers() {
        return maxPlayers;
    }

    public void setMaxPlayers(int maxPlayers) {
        this.maxPlayers = maxPlayers;
    }

    public QuestionSelectionStrategy getSelectionStrategy() {
        return selectionStrategy;
    }

    public void setSelectionStrategy(QuestionSelectionStrategy selectionStrategy) {
        this.selectionStrategy = selectionStrategy;
    }

    public int getQuestionsNumber() {
        return questionsNumber;
    }

    public void setQuestionsNumber(int questionsNumber) {
        this.questionsNumber = questionsNumber;
    }

    public Date getStartDate() {
        return startDate;
    }

    public void setStartDate(Date startDate) {
        this.startDate = startDate;
    }

    public Date getStartedDate() {
        return startedDate;
    }

    public void setStartedDate(Date startedDate) {
        this.startedDate = startedDate;
    }

    public QuizStatus getStatus() {
        return status;
    }

    public void setStatus(QuizStatus status) {
        this.status = status;
    }

    public List<QuestionBean> getQuestions() {
        return questions;
    }

    public void setQuestions(List<QuestionBean> questions) {
        this.questions = questions;
    }

    public QuizResultBean getResult() {
        return result;
    }

    public void setResult(QuizResultBean result) {
        this.result = result;
    }

    @Override
    public String toString() {
        return "QuizBean{" +
                "id=" + id +
                ", author='" + author + '\'' +
                ", name='" + name + '\'' +
                ", type=" + type +
                ", minPlayers=" + minPlayers +
                ", maxPlayers=" + maxPlayers +
                ", selectionStrategy=" + selectionStrategy +
                ", questionsNumber=" + questionsNumber +
                ", startDate=" + startDate +
                ", startedDate=" + startedDate +
                ", status=" + status +
                ", result=" + result +
                ", questions=" + questions +
                '}';
    }
}
