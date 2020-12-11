package com.bouchov.quiz.services;

import com.bouchov.quiz.entities.Question;
import com.bouchov.quiz.entities.QuizAnswer;
import com.bouchov.quiz.entities.QuizParticipant;
import com.bouchov.quiz.protocol.OptionBean;
import com.bouchov.quiz.protocol.QuestionBean;

import java.util.ArrayList;
import java.util.Set;
import java.util.stream.Collectors;

import static java.util.Collections.shuffle;

public abstract class AbstractQuizManager implements QuizManager {
    protected final QuizServiceImpl service;

    protected AbstractQuizManager(QuizServiceImpl service) {
        this.service = service;
    }

    protected Question nextQuestion(QuizParticipant participant) {
        Set<Long> used = participant.getAnswers().stream()
                .map((qa) -> qa.getQuestion().getId())
                .collect(Collectors.toSet());
        Question selectedQuestion = null;
        for (Question question : participant.getQuiz().getQuestions()) {
            if (!used.contains(question.getId())) {
                selectedQuestion = question;
                break;
            }
        }
        return selectedQuestion;
    }

    protected QuestionBean toQuestion(Question question) {
        ArrayList<OptionBean> options = new ArrayList<>();
        for (int i = 0; i < question.getOptions().size(); i++) {
            options.add(new OptionBean(i, question.getOptions().get(i)));
        }
        shuffle(options);
        return new QuestionBean(question.getCategory().getName(), question.getText(), null, options);
    }
}
