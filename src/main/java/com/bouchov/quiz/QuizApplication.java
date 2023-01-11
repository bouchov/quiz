package com.bouchov.quiz;

import com.bouchov.quiz.entities.*;
import com.bouchov.quiz.init.CategoryLoader;
import com.bouchov.quiz.init.QuestionLoader;
import com.bouchov.quiz.init.QuizLoader;
import com.bouchov.quiz.init.UserLoader;
import jakarta.transaction.Transactional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Bean;
import org.springframework.scheduling.annotation.EnableScheduling;

import java.util.Set;

@SpringBootApplication
@EnableScheduling
@EnableCaching
public class QuizApplication {
    private final Logger logger = LoggerFactory.getLogger(QuizApplication.class);

    @Bean
    @Transactional
    CommandLineRunner init(CategoryRepository categoryRepository,
                            UserRepository userRepository,
                            ClubRepository clubRepository,
                            EnterClubRequestRepository enterClubRepository,
                            QuizRepository quizRepository,
                            QuestionRepository questionRepository) {
        return (evt) -> {
            logger.info("initialize server");
            User admin = userRepository.findByLogin("admin").orElse(null);
            if (admin != null) {
                logger.info("server already initialized");
                return;
            }
            admin = userRepository.save(new User("admin", "admin", "Admin", UserRole.ADMIN));
            Club club = clubRepository.save(new Club(
                    "Club of " + admin.getNickname(),
                    IdGenerator.generate(),
                    admin,
                    true));
            admin.setClubs(Set.of(club));
            userRepository.save(admin);

            UserLoader userLoader = new UserLoader();
            userLoader.load(userRepository, enterClubRepository, club);
            CategoryLoader categoryLoader = new CategoryLoader();
            categoryLoader.load(categoryRepository);
            QuestionLoader questionLoader = new QuestionLoader();
            questionLoader.load(questionRepository, categoryRepository, club);
            QuizLoader quizLoader = new QuizLoader();
            quizLoader.load(quizRepository, questionRepository, userRepository, admin, club);
            logger.info("service successfully initialized");
        };
    }

    public static void main(String[] args) {
        SpringApplication.run(QuizApplication.class, args);
    }

}
