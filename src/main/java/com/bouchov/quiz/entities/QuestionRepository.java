package com.bouchov.quiz.entities;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.data.repository.query.Param;

import java.util.Collection;
import java.util.List;

public interface QuestionRepository extends PagingAndSortingRepository<Question,Long> {
    Page<Question> findAllByCategory(Category category, Pageable pageable);

    @Query("select Q from Question Q where Q.id not in :ids")
    List<Question> findAllBut(@Param("ids") Collection<Long> ids, Pageable pageable);
}
