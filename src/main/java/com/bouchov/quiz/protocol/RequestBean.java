package com.bouchov.quiz.protocol;

public class RequestBean {
    private EnterBean enter;
    private Integer answer;
    private Boolean next;

    public EnterBean getEnter() {
        return enter;
    }

    public void setEnter(EnterBean enter) {
        this.enter = enter;
    }

    public Integer getAnswer() {
        return answer;
    }

    public void setAnswer(Integer answer) {
        this.answer = answer;
    }

    public Boolean getNext() {
        return next;
    }

    public void setNext(Boolean next) {
        this.next = next;
    }

    @Override
    public String toString() {
        return "RequestBean{" +
                "enter=" + enter +
                ", answer=" + answer +
                ", next=" + next +
                '}';
    }
}
