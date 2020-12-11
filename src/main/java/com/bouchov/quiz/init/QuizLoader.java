package com.bouchov.quiz.init;

import com.bouchov.quiz.entities.*;
import com.bouchov.quiz.protocol.QuizBean;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.io.InputStream;
import java.util.Objects;

public class QuizLoader {
    private final String fileName;

    public QuizLoader(String fileName) {
        this.fileName = fileName;
    }

    public QuizLoader() {
        this("quiz.json");
    }

    public void load(QuizRepository repository, UserRepository userRepo, User defaultAuthor)
            throws IOException {
        try(InputStream stream = QuizLoader.class.getResourceAsStream(fileName)) {
            Objects.requireNonNull(stream, "file not found");
            ObjectMapper mapper = new ObjectMapper();
            QuizBean[] beans = mapper.readValue(stream, QuizBean[].class);
            for (QuizBean quiz : beans) {
                User author = defaultAuthor;
                if (quiz.getAuthor() != null) {
                    author = userRepo.findByLogin(quiz.getAuthor()).orElse(defaultAuthor);
                }
                QuizStatus status = quiz.getStatus() == null ?
                        QuizStatus.DRAFT : quiz.getStatus();

                repository.save(new Quiz(author,
                        quiz.getName(),
                        quiz.getType(),
                        quiz.getMinPlayers(),
                        quiz.getMaxPlayers(),
                        quiz.getStartDate(),
                        quiz.getStartedDate(),
                        status));
            }
        }
    }
}
