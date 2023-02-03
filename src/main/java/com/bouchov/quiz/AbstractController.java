package com.bouchov.quiz;

import com.bouchov.quiz.entities.*;
import com.bouchov.quiz.protocol.FilterBean;
import jakarta.servlet.http.HttpSession;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;

import java.security.Principal;
import java.util.Optional;

public class AbstractController {

    protected Optional<User> getUser(Principal principal, UserRepository repository) {
        return repository.findByLogin(principal.getName());
    }

    protected Optional<Club> getClub(HttpSession session, ClubRepository repository) {
        Long clubId = (Long) session.getAttribute(SessionAttributes.CLUB_ID);
        if (clubId == null) {
            return Optional.empty();
        }
        return repository.findById(clubId);
    }

    protected static PageRequest toPageable(FilterBean filter, Sort sort) {
        int pageNumber = filter.getPage() == null ? 0 : filter.getPage();
        int pageSize = filter.getSize() == null ? 10 : filter.getSize();
        return PageRequest.of(pageNumber, pageSize, sort);
    }
}
