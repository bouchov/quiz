package com.bouchov.quiz.init;

import com.bouchov.quiz.entities.*;
import com.bouchov.quiz.protocol.QuizBean;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class QuizLoader {
    private final String fileName;

    public QuizLoader(String fileName) {
        this.fileName = fileName;
    }

    public QuizLoader() {
        this("quiz.json");
    }

    public void load(QuizRepository repository,
            QuestionRepository questionRepo,
            UserRepository userRepo,
            User defaultAuthor,
            Club club)
            throws IOException {
        try(InputStream stream = QuizLoader.class.getResourceAsStream(fileName)) {
            Objects.requireNonNull(stream, "file not found");
            ObjectMapper mapper = new ObjectMapper();
            QuizBean[] beans = mapper.readValue(stream, QuizBean[].class);
            List<Question> allQuestions = new ArrayList<>();
            questionRepo.findAll().forEach(allQuestions::add);

            for (QuizBean quiz : beans) {
                User author = defaultAuthor;
                if (quiz.getAuthor() != null) {
                    author = userRepo.findByLogin(quiz.getAuthor()).orElse(defaultAuthor);
                }
                QuizStatus status = quiz.getStatus() == null ?
                        QuizStatus.ACTIVE : quiz.getStatus();
                int questionsNumber = quiz.getQuestionsNumber();
                if (quiz.getSelectionStrategy() == QuestionSelectionStrategy.QUIZ) {
                    questionsNumber = allQuestions.size();
                }
                Quiz entity = new Quiz(author,
                        club,
                        quiz.getName(),
                        quiz.getType(),
                        quiz.getMinPlayers(),
                        quiz.getMaxPlayers(),
                        quiz.getSelectionStrategy(),
                        questionsNumber,
                        quiz.getStartDate(),
                        quiz.getStartedDate(),
                        status);
                if (quiz.getSelectionStrategy() == QuestionSelectionStrategy.QUIZ) {
                    entity.setQuestions(allQuestions);
                }

                repository.save(entity);
            }
        }
    }
}
