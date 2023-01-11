package com.bouchov.quiz.entities;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.data.repository.query.Param;

import java.util.Collection;
import java.util.List;

public interface QuestionRepository extends CrudRepository<Question,Long>, PagingAndSortingRepository<Question,Long> {
    Page<Question> findAllByCategoryAndClub(Category category, Club club, Pageable pageable);

    Page<Question> findAllByClub(Club club, Pageable pageable);

    @Query("select Q from Question Q where Q.club = :club and Q.id not in :ids")
    List<Question> findAllByClubBut(@Param("club") Club club, @Param("ids") Collection<Long> ids, Pageable pageable);
}
