package com.bouchov.quiz.entities;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;

public interface QuizRepository extends CrudRepository<Quiz,Long> {
    @Query("select Q from Quiz Q where Q.name like %:name% and Q.status in :status")
    Iterable<Quiz> findAllByNameAndStatus(@Param("name") String name, @Param("status") QuizStatus... status);

    @Query("select Q from Quiz Q where Q.status in :status")
    Iterable<Quiz> findAllByStatus(@Param("status") QuizStatus... status);
}
