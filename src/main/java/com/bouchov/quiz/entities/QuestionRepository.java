package com.bouchov.quiz.entities;

import org.springframework.data.repository.CrudRepository;

import java.util.List;
import java.util.Optional;

public interface QuestionRepository extends CrudRepository<Question,Long> {
    Iterable<Question> findAllByCategory(Category category);
}
