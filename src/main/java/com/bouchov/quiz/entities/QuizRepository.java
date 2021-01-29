package com.bouchov.quiz.entities;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;

public interface QuizRepository extends CrudRepository<Quiz,Long> {
    @Query("select Q from Quiz Q where Q.name like %:name% and Q.status in :status")
    Page<Quiz> findAllByNameAndStatus(@Param("name") String name, Pageable pageable, @Param("status") QuizStatus... status);

    @Query("select Q from Quiz Q where Q.status in :status")
    Page<Quiz> findAllByStatus(Pageable pageable, @Param("status") QuizStatus... status);
}
