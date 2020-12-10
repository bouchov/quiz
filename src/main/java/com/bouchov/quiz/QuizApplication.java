package com.bouchov.quiz;

import com.bouchov.quiz.entities.*;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

import javax.transaction.Transactional;
import java.util.ArrayList;
import java.util.Arrays;

@SpringBootApplication
public class QuizApplication {

    @Bean
    @Transactional
    CommandLineRunner init(CategoryRepository categoryRepository,
                            UserRepository userRepository,
                            QuizRepository quizRepository,
                            QuestionRepository questionRepository) {
        return (evt) -> {
            User user = userRepository.save(new User("admin", "admin", "Admin", UserRole.ADMIN));
            Category category = categoryRepository.save(new Category("General"));
            Quiz quiz = quizRepository.save(new Quiz(user, "Test quiz #1", QuizType.SIMPLE, QuizStatus.ACTIVE));
            quiz.setQuestions(new ArrayList<>());
            quiz.getQuestions().add(questionRepository.save(new Question(category, quiz,
                    "question #1",
                    0,
                    1,
                    Arrays.asList(
                            "right answer",
                            "wrong answer"
                    )
            )));
            quiz.getQuestions().add(questionRepository.save(new Question(category, quiz,
                    "question #2",
                    0,
                    1,
                    Arrays.asList(
                            "right answer",
                            "wrong answer"
                    )
            )));
            quizRepository.save(quiz);
        };
    }

    public static void main(String[] args) {
        SpringApplication.run(QuizApplication.class, args);
    }

}
