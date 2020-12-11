package com.bouchov.quiz;

import com.bouchov.quiz.entities.*;
import com.bouchov.quiz.init.CategoryLoader;
import com.bouchov.quiz.init.QuestionLoader;
import com.bouchov.quiz.init.QuizLoader;
import com.bouchov.quiz.init.UserLoader;
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
            UserLoader userLoader = new UserLoader();
            userLoader.load(userRepository);
            CategoryLoader categoryLoader = new CategoryLoader();
            categoryLoader.load(categoryRepository);
            QuizLoader quizLoader = new QuizLoader();
            quizLoader.load(quizRepository, userRepository, user);
            QuestionLoader questionLoader = new QuestionLoader();
            questionLoader.load(questionRepository, categoryRepository);
        };
    }

    public static void main(String[] args) {
        SpringApplication.run(QuizApplication.class, args);
    }

}
