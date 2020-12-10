package com.bouchov.quiz.entities;

import javax.persistence.*;

@Entity
public class QuizAnswer {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;
    @ManyToOne(fetch = FetchType.LAZY)
    private Quiz quiz;
    @ManyToOne(fetch = FetchType.LAZY)
    private Question question;
    @ManyToOne(fetch = FetchType.LAZY)
    private User answerer;
    private int answer;
    private int value;
    private QuizAnswerStatus status;

    public QuizAnswer() {
    }

    public QuizAnswer(Quiz quiz, Question question, User answerer, int answer, int value, QuizAnswerStatus status) {
        this.quiz = quiz;
        this.question = question;
        this.answerer = answerer;
        this.answer = answer;
        this.value = value;
        this.status = status;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Quiz getQuiz() {
        return quiz;
    }

    public void setQuiz(Quiz quiz) {
        this.quiz = quiz;
    }

    public Question getQuestion() {
        return question;
    }

    public void setQuestion(Question question) {
        this.question = question;
    }

    public User getAnswerer() {
        return answerer;
    }

    public void setAnswerer(User answerer) {
        this.answerer = answerer;
    }

    public int getAnswer() {
        return answer;
    }

    public void setAnswer(int answer) {
        this.answer = answer;
    }

    public int getValue() {
        return value;
    }

    public void setValue(int value) {
        this.value = value;
    }

    public QuizAnswerStatus getStatus() {
        return status;
    }

    public void setStatus(QuizAnswerStatus status) {
        this.status = status;
    }
}
