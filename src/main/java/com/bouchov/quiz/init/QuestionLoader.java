package com.bouchov.quiz.init;

import com.bouchov.quiz.entities.*;
import com.bouchov.quiz.protocol.QuestionBean;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.io.InputStream;
import java.util.Objects;

public class QuestionLoader {
    private final String fileName;

    public QuestionLoader(String fileName) {
        this.fileName = fileName;
    }

    public QuestionLoader() {
        this("questions.json");
    }

    public void load(QuestionRepository repository, CategoryRepository categoryRepo, Club club)
            throws IOException {
        try(InputStream stream = QuestionLoader.class.getResourceAsStream(fileName)) {
            Objects.requireNonNull(stream, "file not found");
            ObjectMapper mapper = new ObjectMapper();
            QuestionBean[] beans = mapper.readValue(stream, QuestionBean[].class);
            for (QuestionBean question : beans) {
                Category category = categoryRepo.findByName(question.getCategory()).orElseThrow();
                repository.save(new Question(
                        club,
                        category,
                        question.getText(),
                        question.getAnswer(),
                        question.getValue(),
                        new ObjectMapper().writeValueAsString(question.getOptions())
                ));
            }
        }
    }
}
