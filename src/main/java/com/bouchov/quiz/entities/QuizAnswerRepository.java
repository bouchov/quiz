package com.bouchov.quiz.entities;

import org.springframework.data.repository.CrudRepository;

import java.util.Optional;

public interface QuizAnswerRepository extends CrudRepository<QuizAnswer,Long> {

    Optional<QuizAnswer> findByQuizAndAnswererAndStatus(Quiz quiz, User answerer, QuizAnswerStatus status);
}
