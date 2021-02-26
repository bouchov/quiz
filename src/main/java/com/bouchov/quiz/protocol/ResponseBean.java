package com.bouchov.quiz.protocol;

public class ResponseBean {
    private QuestionBean question;
    private AnswerBean answer;
    private QuizResultBean result;
    private QuizBean quiz;
    private String message;

    public ResponseBean(AnswerBean answer) {
        this.answer = answer;
    }

    public ResponseBean(QuestionBean question) {
        this.question = question;
    }

    public ResponseBean(QuizResultBean result) {
        this.result = result;
    }

    public ResponseBean(QuizBean quiz) {
        this.quiz = quiz;
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

    public AnswerBean getAnswer() {
        return answer;
    }

    public void setAnswer(AnswerBean answer) {
        this.answer = answer;
    }

    public QuizResultBean getResult() {
        return result;
    }

    public void setResult(QuizResultBean result) {
        this.result = result;
    }

    public QuizBean getQuiz() {
        return quiz;
    }

    public void setQuiz(QuizBean quiz) {
        this.quiz = quiz;
    }

    @Override
    public String toString() {
        return "ResponseBean{" +
                "question=" + question +
                ", answer=" + answer +
                ", result=" + result +
                ", quiz=" + quiz +
                ", message='" + message + '\'' +
                '}';
    }
}
