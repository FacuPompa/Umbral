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
    void registersMembersAndAuthenticatesThemWithABearerToken() throws Exception {
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
                .andExpect(jsonPath("$.user.handle").value("marina"))
                .andReturn();

        org.junit.jupiter.api.Assertions.assertNull(loginResult.getRequest().getSession(false));
        String access = JsonPath.read(loginResult.getResponse().getContentAsString(), "$.accessToken");
        mockMvc.perform(get("/api/auth/me").header("Authorization", "Bearer " + access))
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
    void neverAuthenticatesAnHttpSessionEvenOnSuccessfulLogin() throws Exception {
        registerSessionTestUser();
        MockHttpSession session = new MockHttpSession();
        mockMvc.perform(post("/api/auth/login").session(session).with(browserCsrf())
                        .contentType(MediaType.APPLICATION_JSON).content(loginBody("una-clave-segura")))
                .andExpect(status().isOk()).andExpect(jsonPath("$.accessToken").isNotEmpty());
        org.junit.jupiter.api.Assertions.assertNull(session.getAttribute(
                org.springframework.security.web.context.HttpSessionSecurityContextRepository.SPRING_SECURITY_CONTEXT_KEY));
        mockMvc.perform(get("/api/auth/me").session(session)).andExpect(status().isUnauthorized());
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
    void rotatesRealCsrfOnLoginAndLogoutAndRevokesTheBearerGrant() throws Exception {
        registerSessionTestUser();
        MvcResult login = mockMvc.perform(post("/api/auth/login").with(browserCsrf())
                        .contentType(MediaType.APPLICATION_JSON).content(loginBody("una-clave-segura")))
                .andExpect(status().isOk()).andReturn();
        String access = JsonPath.read(login.getResponse().getContentAsString(), "$.accessToken");
        Cookie refresh = login.getResponse().getCookie(AuthCookies.REFRESH);
        assertNotNull(refresh);
        assertTrue(refresh.isHttpOnly());
        assertEquals(0, login.getResponse().getCookie("XSRF-TOKEN").getMaxAge());
        org.junit.jupiter.api.Assertions.assertNull(login.getRequest().getSession(false));
        mockMvc.perform(post("/api/auth/logout").cookie(refresh)).andExpect(status().isForbidden());
        MvcResult logout = mockMvc.perform(post("/api/auth/logout").with(browserCsrf()).cookie(refresh))
                .andExpect(status().isNoContent()).andReturn();
        assertEquals(0, logout.getResponse().getCookie(AuthCookies.REFRESH).getMaxAge());
        mockMvc.perform(get("/api/auth/me").header("Authorization", "Bearer " + access))
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
            java.util.List<Cookie> cookies = new java.util.ArrayList<>();
            if (request.getCookies() != null) cookies.addAll(java.util.List.of(request.getCookies()));
            cookies.add(cookie);
            request.setCookies(cookies.toArray(Cookie[]::new));
            request.addHeader(headerName, token);
            return request;
        };
    }
}
