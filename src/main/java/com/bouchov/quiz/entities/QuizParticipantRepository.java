package com.bouchov.quiz.entities;

import org.springframework.data.repository.CrudRepository;

import java.util.Optional;

public interface QuizParticipantRepository extends CrudRepository<QuizParticipant,Long> {
    Optional<QuizParticipant> getByQuizResultAndUser(QuizResult quizResult, User user);
}