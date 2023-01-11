package com.bouchov.quiz.entities;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.PagingAndSortingRepository;

import java.util.List;

public interface QuizResultRepository extends CrudRepository<QuizResult,Long>, PagingAndSortingRepository<QuizResult,Long> {
    @Query("select R from QuizResult R where R.quiz = :quiz and R.status in (:statuses)")
    Page<QuizResult> findAllByQuizAndStatus(Quiz quiz, List<QuizResultStatus> statuses, Pageable pageable);
}