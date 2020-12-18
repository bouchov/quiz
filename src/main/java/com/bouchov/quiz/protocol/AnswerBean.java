package com.bouchov.quiz.protocol;

import com.bouchov.quiz.entities.QuizAnswer;
import com.bouchov.quiz.entities.QuizAnswerStatus;

public class AnswerBean {
    private QuizAnswerStatus status;
    private int answer;
    private int value;
    private int rightAnswer;
    private QuestionBean question;

    public AnswerBean() {
    }

    public AnswerBean(QuizAnswer answer, QuestionBean question) {
        this.status = answer.getStatus();
        this.answer = answer.getAnswer();
        this.value = answer.getValue();
        this.rightAnswer = answer.getQuestion().getAnswer();
        this.question = question;
    }

    public QuizAnswerStatus getStatus() {
        return status;
    }

    public void setStatus(QuizAnswerStatus status) {
        this.status = status;
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

    public int getRightAnswer() {
        return rightAnswer;
    }

    public void setRightAnswer(int rightAnswer) {
        this.rightAnswer = rightAnswer;
    }

    public QuestionBean getQuestion() {
        return question;
    }

    public void setQuestion(QuestionBean question) {
        this.question = question;
    }

    @Override
    public String toString() {
        return "AnswerBean{" +
                "status=" + status +
                ", answer=" + answer +
                ", value=" + value +
                ", rightAnswer=" + rightAnswer +
                ", question=" + question +
                '}';
    }
}
