package com.umbral.domain.service;

import com.umbral.domain.dto.SearchResponse;
import com.umbral.domain.entity.User;
import com.umbral.domain.entity.UserRole;
import com.umbral.domain.repository.UserRepository;
import com.umbral.support.PostgresTestConfiguration;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.transaction.annotation.Transactional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest
@Import(PostgresTestConfiguration.class)
@Transactional
class SearchServiceTest {

    @Autowired
    private SearchService searchService;

    @Autowired
    private UserRepository userRepository;

    @Test
    void findsGamesAndUsersWithoutMatchingCaseExactly() {
        userRepository.save(new User(
                "PersonaExplorer",
                "persona-explorer@umbral.local",
                "not-a-real-password-hash",
                UserRole.MEMBER
        ));

        SearchResponse response = searchService.search("pErSoNa");

        assertEquals(1, response.games().size());
        assertEquals("Persona 5 Royal", response.games().getFirst().title());
        assertEquals(1, response.users().size());
        assertEquals("PersonaExplorer", response.users().getFirst().handle());
    }

    @Test
    void returnsNoResultsForQueriesShorterThanTwoCharacters() {
        SearchResponse response = searchService.search("p");

        assertTrue(response.games().isEmpty());
        assertTrue(response.users().isEmpty());
    }
}
