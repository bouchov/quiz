package com.bouchov.quiz.protocol;

import com.bouchov.quiz.entities.QuizAnswerStatus;

public class ResponseBean {
    private QuestionBean question;
    private QuizAnswerStatus answer;
    private QuizResultBean result;
    private String message;

    public ResponseBean(QuizAnswerStatus answer) {
        this.answer = answer;
    }

    public ResponseBean(QuestionBean question) {
        this.question = question;
    }

    public ResponseBean(QuizResultBean result) {
        this.result = result;
    }

    public ResponseBean(String message) {
        this.message = message;
    }

    public QuestionBean getQuestion() {
        return question;
    }

    public void setQuestion(QuestionBean question) {
        this.question = question;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public QuizAnswerStatus getAnswer() {
        return answer;
    }

    public void setAnswer(QuizAnswerStatus answer) {
        this.answer = answer;
    }

    public QuizResultBean getResult() {
        return result;
    }

    public void setResult(QuizResultBean result) {
        this.result = result;
    }

    @Override
    public String toString() {
        return "ResponseBean{" +
                "question=" + question +
                ", answer=" + answer +
                ", result=" + result +
                ", message='" + message + '\'' +
                '}';
    }
}
