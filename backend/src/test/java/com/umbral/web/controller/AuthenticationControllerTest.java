package com.umbral.web.controller;

import com.umbral.support.PostgresTestConfiguration;
import com.jayway.jsonpath.JsonPath;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpSession;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.request.RequestPostProcessor;
import org.springframework.transaction.annotation.Transactional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@Import(PostgresTestConfiguration.class)
@Transactional
// Other controller tests use csrf(), which replaces the shared filter's token repository.
@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_CLASS)
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
                        .with(browserCsrf())
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
                        .with(browserCsrf())
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
                        .with(browserCsrf())
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
                        .with(browserCsrf())
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
                        .with(browserCsrf())
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
                        .with(browserCsrf())
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
                        .with(browserCsrf())
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
                        .with(browserCsrf())
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

    @Test
    void renewsAnExistingSessionIdentifierOnSuccessfulLogin() throws Exception {
        registerSessionTestUser();
        MockHttpSession session = new MockHttpSession();
        String previousId = session.getId();

        mockMvc.perform(post("/api/auth/login")
                        .session(session).with(browserCsrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(loginBody("una-clave-segura")))
                .andExpect(status().isOk());

        assertNotEquals(previousId, session.getId());
        mockMvc.perform(get("/api/auth/me").session(session))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.handle").value("session-test"));
    }

    @Test
    void failedLoginDoesNotAuthenticateOrRenewAnAnonymousSession() throws Exception {
        registerSessionTestUser();
        MockHttpSession session = new MockHttpSession();
        String previousId = session.getId();

        mockMvc.perform(post("/api/auth/login")
                        .session(session).with(browserCsrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(loginBody("clave-incorrecta")))
                .andExpect(status().isUnauthorized());

        assertEquals(previousId, session.getId());
        mockMvc.perform(get("/api/auth/me").session(session))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void rejectsLoginWithoutCsrfOrWithAnInvalidToken() throws Exception {
        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(loginBody("una-clave-segura")))
                .andExpect(status().isForbidden());

        MvcResult csrfResult = mockMvc.perform(get("/api/auth/csrf"))
                .andExpect(status().isOk()).andReturn();
        Cookie cookie = csrfResult.getResponse().getCookie("XSRF-TOKEN");
        assertNotNull(cookie);
        mockMvc.perform(post("/api/auth/login")
                        .cookie(cookie).header("X-XSRF-TOKEN", "invalid-token")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(loginBody("una-clave-segura")))
                .andExpect(status().isForbidden());
    }

    @Test
    void rotatesRealCsrfTokensAndInvalidatesTheSessionOnLogout() throws Exception {
        registerSessionTestUser();
        MvcResult initialCsrf = mockMvc.perform(get("/api/auth/csrf"))
                .andExpect(status().isOk()).andReturn();
        Cookie initialCookie = initialCsrf.getResponse().getCookie("XSRF-TOKEN");
        assertNotNull(initialCookie);
        String initialToken = JsonPath.read(initialCsrf.getResponse().getContentAsString(), "$.token");

        MvcResult login = mockMvc.perform(post("/api/auth/login")
                        .cookie(initialCookie).header("X-XSRF-TOKEN", initialToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(loginBody("una-clave-segura")))
                .andExpect(status().isOk()).andReturn();
        MockHttpSession session = (MockHttpSession) login.getRequest().getSession(false);
        assertNotNull(session);
        Cookie clearedOnLogin = login.getResponse().getCookie("XSRF-TOKEN");
        assertNotNull(clearedOnLogin);
        assertEquals(0, clearedOnLogin.getMaxAge());

        // Simulate the browser discarding the cookie cleared by login.
        mockMvc.perform(post("/api/auth/logout").session(session)
                        .header("X-XSRF-TOKEN", initialToken))
                .andExpect(status().isForbidden());
        mockMvc.perform(get("/api/auth/me").session(session))
                .andExpect(status().isOk());

        MvcResult renewedCsrf = mockMvc.perform(get("/api/auth/csrf").session(session))
                .andExpect(status().isOk()).andReturn();
        Cookie renewedCookie = renewedCsrf.getResponse().getCookie("XSRF-TOKEN");
        assertNotNull(renewedCookie);
        assertNotEquals(initialCookie.getValue(), renewedCookie.getValue());
        String renewedToken = JsonPath.read(renewedCsrf.getResponse().getContentAsString(), "$.token");

        MvcResult logout = mockMvc.perform(post("/api/auth/logout").session(session)
                        .cookie(renewedCookie).header("X-XSRF-TOKEN", renewedToken))
                .andExpect(status().isNoContent()).andReturn();
        assertTrue(session.isInvalid());
        Cookie clearedOnLogout = logout.getResponse().getCookie("XSRF-TOKEN");
        assertNotNull(clearedOnLogout);
        assertEquals(0, clearedOnLogout.getMaxAge());
        mockMvc.perform(get("/api/auth/me"))
                .andExpect(status().isUnauthorized());
    }

    private void registerSessionTestUser() throws Exception {
        mockMvc.perform(post("/api/auth/register").with(browserCsrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"handle":"session-test","email":"session-test@example.com",
                                 "password":"una-clave-segura"}
                                """))
                .andExpect(status().isCreated());
    }

    private String loginBody(String password) {
        return """
                {"handle":"session-test","password":"%s"}
                """.formatted(password);
    }

    private RequestPostProcessor browserCsrf() throws Exception {
        MvcResult result = mockMvc.perform(get("/api/auth/csrf"))
                .andExpect(status().isOk()).andReturn();
        Cookie cookie = result.getResponse().getCookie("XSRF-TOKEN");
        assertNotNull(cookie);
        String token = JsonPath.read(result.getResponse().getContentAsString(), "$.token");
        String headerName = JsonPath.read(result.getResponse().getContentAsString(), "$.headerName");

        return request -> {
            request.setCookies(cookie);
            request.addHeader(headerName, token);
            return request;
        };
    }
}
