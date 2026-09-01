package com.umbral.web.controller;

import com.umbral.domain.entity.Checkpoint;
import com.umbral.domain.entity.Game;
import com.umbral.domain.entity.JournalEntry;
import com.umbral.domain.entity.JournalEntryType;
import com.umbral.domain.entity.JournalReply;
import com.umbral.domain.entity.User;
import com.umbral.domain.entity.UserGameProgress;
import com.umbral.domain.repository.CheckpointRepository;
import com.umbral.domain.repository.GameRepository;
import com.umbral.domain.repository.JournalEntryRepository;
import com.umbral.domain.repository.JournalReplyRepository;
import com.umbral.domain.repository.UserGameProgressRepository;
import com.umbral.domain.repository.UserRepository;
import com.umbral.support.PostgresTestConfiguration;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@Import(PostgresTestConfiguration.class)
@Transactional
class JournalReplyControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private GameRepository gameRepository;

    @Autowired
    private CheckpointRepository checkpointRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private UserGameProgressRepository userGameProgressRepository;

    @Autowired
    private JournalEntryRepository journalEntryRepository;

    @Autowired
    private JournalReplyRepository journalReplyRepository;

    @Test
    void returnsRepliesWhenCurrentUserCanReadTheirEntry() throws Exception {
        Game persona5Royal = findPersona5Royal();
        Checkpoint madarame = checkpointAt(persona5Royal, 2);
        saveProgressForDemoUser(persona5Royal, madarame);
        JournalEntry entry = createEntryAt(madarame);
        User author = userRepository.findById(1L).orElseThrow();

        journalReplyRepository.save(new JournalReply(entry, author, "Primera respuesta."));
        journalReplyRepository.save(new JournalReply(entry, author, "Segunda respuesta."));

        mockMvc.perform(get("/api/journal-entries/{entryId}/replies", entry.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].content").value("Primera respuesta."))
                .andExpect(jsonPath("$[1].content").value("Segunda respuesta."));
    }

    @Test
    void createsReplyWhenCurrentUserCanReadTheirEntry() throws Exception {
        Game persona5Royal = findPersona5Royal();
        Checkpoint madarame = checkpointAt(persona5Royal, 2);
        saveProgressForDemoUser(persona5Royal, madarame);
        JournalEntry entry = createEntryAt(madarame);

        mockMvc.perform(post("/api/me/journal-entries/{entryId}/replies", entry.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "content": "Una respuesta creada desde el controller."
                                }
                                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.authorHandle").value("umbral-demo"))
                .andExpect(jsonPath("$.content")
                        .value("Una respuesta creada desde el controller."));
    }

    @Test
    void hidesRepliesWhenCurrentUserDidNotReachTheEntryCheckpoint() throws Exception {
        Game persona5Royal = findPersona5Royal();
        Checkpoint kamoshida = checkpointAt(persona5Royal, 1);
        Checkpoint madarame = checkpointAt(persona5Royal, 2);
        saveProgressForDemoUser(persona5Royal, kamoshida);
        JournalEntry entry = createEntryAt(madarame);

        mockMvc.perform(get("/api/journal-entries/{entryId}/replies", entry.getId()))
                .andExpect(status().isNotFound());
    }

    @Test
    void rejectsBlankReplyContent() throws Exception {
        Game persona5Royal = findPersona5Royal();
        Checkpoint madarame = checkpointAt(persona5Royal, 2);
        saveProgressForDemoUser(persona5Royal, madarame);
        JournalEntry entry = createEntryAt(madarame);

        mockMvc.perform(post("/api/me/journal-entries/{entryId}/replies", entry.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "content": " "
                                }
                                """))
                .andExpect(status().isBadRequest());
    }

    private Game findPersona5Royal() {
        return gameRepository.findAll().stream()
                .filter(game -> game.getTitle().equals("Persona 5 Royal"))
                .findFirst()
                .orElseThrow();
    }

    private Checkpoint checkpointAt(Game game, int index) {
        return checkpointRepository.findByGameIdOrderByPositionAsc(game.getId()).get(index);
    }

    private void saveProgressForDemoUser(Game game, Checkpoint checkpoint) {
        User demoUser = userRepository.findById(1L).orElseThrow();
        userGameProgressRepository.save(new UserGameProgress(demoUser, game, checkpoint));
    }

    private JournalEntry createEntryAt(Checkpoint checkpoint) {
        User author = userRepository.findById(2L).orElseThrow();
        return journalEntryRepository.save(new JournalEntry(
                author,
                checkpoint,
                JournalEntryType.QUESTION,
                "Entrada usada para probar las respuestas."
        ));
    }
}
