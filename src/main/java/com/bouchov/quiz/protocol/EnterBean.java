package com.bouchov.quiz.protocol;

public class EnterBean {
    private Long participantId;

    public Long getParticipantId() {
        return participantId;
    }

    public void setParticipantId(Long participantId) {
        this.participantId = participantId;
    }

    @Override
    public String toString() {
        return "EnterBean{" +
                "participantId=" + participantId +
                '}';
    }
}
