package com.bouchov.quiz.services;

import com.bouchov.quiz.entities.*;
import com.bouchov.quiz.protocol.QuestionBean;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.Set;
import java.util.stream.Collectors;

import static java.util.Collections.shuffle;

public abstract class AbstractQuizManager implements QuizManager {
    protected final QuizServiceImpl service;
    protected final Random rnd = new Random();

    protected AbstractQuizManager(QuizServiceImpl service) {
        this.service = service;
    }

    protected Question nextQuestion(QuizParticipant participant) {
        Set<Long> used = participant.getAnswers().stream()
                .map((qa) -> qa.getQuestion().getId())
                .collect(Collectors.toSet());
        Quiz quiz = participant.getQuizResult().getQuiz();
        Question selectedQuestion = null;
        if (quiz.getSelectionStrategy() == QuestionSelectionStrategy.QUIZ) {
            List<Question> questions = new ArrayList<>(quiz.getQuestions());
            shuffle(questions);
            for (Question question : questions) {
                if (!used.contains(question.getId())) {
                    selectedQuestion = question;
                    break;
                }
            }
        } else if (quiz.getSelectionStrategy() == QuestionSelectionStrategy.SOME) {
            if (quiz.getQuestionsNumber() > used.size()) {
                selectedQuestion = anyNotUsedQuestion(used);
            }
        } else if (quiz.getSelectionStrategy() == QuestionSelectionStrategy.ALL) {
            selectedQuestion = anyNotUsedQuestion(used);
        } else {
            throw new UnsupportedOperationException("nextQuestion");
        }
        if (selectedQuestion != null
                && quiz.getSelectionStrategy() != QuestionSelectionStrategy.QUIZ) {
            quiz.getQuestions().add(selectedQuestion);
        }
        return selectedQuestion;
    }

    private Question anyNotUsedQuestion(Set<Long> used) {
        List<Question> questions = service.listQuestions(used, 10);
        if (questions.isEmpty()) {
            return null;
        } else if (questions.size() > 1) {
            return questions.get(rnd.nextInt(questions.size()));
        } else {
            return questions.get(0);
        }
    }

    protected QuestionBean toQuestion(Question question) {
        return toQuestion(question, null, null);
    }

    protected QuestionBean toQuestion(Question question, Integer number, Integer total) {
        ArrayList<Option> options = new ArrayList<>(question.getOptions());
        shuffle(options);
        return new QuestionBean(question, options, number, total);
    }

    protected void checkAnswerAndSaveResult(QuizParticipant participant, int answer, QuizAnswer quizAnswer) {
        quizAnswer.setAnswer(answer);
        if (quizAnswer.getQuestion().getAnswer() == answer) {
            quizAnswer.setStatus(QuizAnswerStatus.SUCCESS);
            quizAnswer.setValue(quizAnswer.getQuestion().getValue());
            participant.setRightAnswers(participant.getRightAnswers() + 1);
        } else {
            quizAnswer.setStatus(QuizAnswerStatus.FAILED);
            quizAnswer.setValue(0);
            participant.setWrongAnswers(participant.getWrongAnswers() + 1);
        }
        participant.setValue(participant.getValue() + quizAnswer.getValue());
    }
}
