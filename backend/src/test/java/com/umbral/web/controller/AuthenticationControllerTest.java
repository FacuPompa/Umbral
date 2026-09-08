package com.umbral.web.controller;

import com.umbral.support.PostgresTestConfiguration;
import jakarta.servlet.http.HttpSession;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.transaction.annotation.Transactional;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@Import(PostgresTestConfiguration.class)
@Transactional
class AuthenticationControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void exposesACsrfTokenForBrowserRequests() throws Exception {
        mockMvc.perform(get("/api/auth/csrf"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").isNotEmpty())
                .andExpect(jsonPath("$.headerName").value("X-XSRF-TOKEN"));
    }

    @Test
    void registersNewUsersAsMembers() throws Exception {
        mockMvc.perform(post("/api/auth/register")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "handle": "facu",
                                  "email": "facu@example.com",
                                  "password": "una-clave-segura"
                                }
                                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.handle").value("facu"))
                .andExpect(jsonPath("$.email").value("facu@example.com"))
                .andExpect(jsonPath("$.role").value("MEMBER"));
    }

    @Test
    void registersMembersAndAuthenticatesThemWithASession() throws Exception {
        mockMvc.perform(post("/api/auth/register")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "handle": "marina",
                                  "email": "marina@example.com",
                                  "password": "una-clave-segura"
                                }
                                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.role").value("MEMBER"));

        MvcResult loginResult = mockMvc.perform(post("/api/auth/login")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "handle": "marina",
                                  "password": "una-clave-segura"
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.handle").value("marina"))
                .andReturn();

        HttpSession session = loginResult.getRequest().getSession(false);

        mockMvc.perform(get("/api/auth/me").session((org.springframework.mock.web.MockHttpSession) session))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.handle").value("marina"))
                .andExpect(jsonPath("$.role").value("MEMBER"));
    }

    @Test
    void rejectsDuplicatedEmailAndIncorrectCredentials() throws Exception {
        mockMvc.perform(post("/api/auth/register")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "handle": "sofia",
                                  "email": "sofia@example.com",
                                  "password": "una-clave-segura"
                                }
                                """))
                .andExpect(status().isCreated());

        mockMvc.perform(post("/api/auth/register")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "handle": "otra-sofia",
                                  "email": "sofia@example.com",
                                  "password": "una-clave-segura"
                                }
                                """))
                .andExpect(status().isConflict());

        mockMvc.perform(post("/api/auth/login")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "handle": "sofia",
                                  "password": "clave-incorrecta"
                                }
                                """))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void rejectsPrivateEndpointsWithoutAnAuthenticatedSession() throws Exception {
        mockMvc.perform(get("/api/me/game-progress"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void acceptsPasswordsWithEightCharactersAndRejectsShorterOnes() throws Exception {
        mockMvc.perform(post("/api/auth/register")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "handle": "ocho",
                                  "email": "ocho@example.com",
                                  "password": "clave123"
                                }
                                """))
                .andExpect(status().isCreated());

        mockMvc.perform(post("/api/auth/register")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "handle": "siete",
                                  "email": "siete@example.com",
                                  "password": "clave12"
                                }
                                """))
                .andExpect(status().isBadRequest());
    }
}
