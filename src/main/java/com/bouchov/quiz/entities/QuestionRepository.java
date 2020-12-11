package com.bouchov.quiz.entities;

import org.springframework.data.repository.CrudRepository;

import java.util.List;
import java.util.Optional;

public interface QuestionRepository extends CrudRepository<Question,Long> {
    Optional<Question> findQuestionById(Long id);

    Iterable<Question> findAllByCategory(Category category);
}
