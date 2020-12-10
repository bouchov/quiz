package com.bouchov.quiz.entities;

import org.springframework.data.repository.CrudRepository;

import java.util.Optional;

public interface QuizParticipantRepository extends CrudRepository<QuizParticipant,Long> {
    Optional<QuizParticipant> getByQuizAndUser(Quiz quiz, User user);
}