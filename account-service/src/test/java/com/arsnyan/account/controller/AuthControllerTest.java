package com.arsnyan.account.controller;

import com.arsnyan.account.config.SecurityConfiguration;
import com.arsnyan.account.dto.AuthResponseDto;
import com.arsnyan.account.dto.LoginRequestDto;
import com.arsnyan.account.dto.RegisterRequestDto;
import com.arsnyan.account.dto.UserResponseDto;
import com.arsnyan.account.model.User;
import com.arsnyan.account.model.UserRole;
import com.arsnyan.account.security.RsaKeyProperties;
import com.arsnyan.account.service.AuthService;
import com.arsnyan.account.service.UserService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.security.KeyPairGenerator;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import java.time.Duration;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(AuthController.class)
@Import({SecurityConfiguration.class, AuthControllerTest.TestSecurityBeans.class})
class AuthControllerTest {
    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private AuthService authService;

    @MockitoBean
    private UserService userService;

    @TestConfiguration
    static class TestSecurityBeans {
        @Bean
        RsaKeyProperties rsaKeyProperties() throws Exception {
            var keyGen = KeyPairGenerator.getInstance("RSA");
            keyGen.initialize(2048);
            var keyPair = keyGen.generateKeyPair();

            return new RsaKeyProperties(
                    (RSAPrivateKey) keyPair.getPrivate(),
                    (RSAPublicKey) keyPair.getPublic(),
                    Duration.ofHours(1),
                    "account-service"
            );
        }
    }

    private static final String USERNAME = "johndoe";
    private static final String EMAIL = "john@example.com";
    private static final String PASSWORD = "password123";

    private RegisterRequestDto validRegisterRequest() {
        return new RegisterRequestDto(USERNAME, EMAIL, PASSWORD);
    }

    private LoginRequestDto validLoginRequest() {
        return new LoginRequestDto(USERNAME, PASSWORD);
    }

    private AuthResponseDto authResponse() {
        var userResponse = new UserResponseDto(1L, USERNAME, EMAIL, UserRole.USER);
        return AuthResponseDto.of("jwt.token.here", 3600000L, userResponse);
    }

    private User testUser() {
        var user = User.create(USERNAME, EMAIL, "encodedPassword");
        user.setUserId(1L);
        return user;
    }

    @BeforeEach
    void resetMocks() {
        Mockito.clearInvocations(authService, userService);
    }

    @Nested
    @DisplayName("POST /user")
    class RegisterTests {
        @Test
        void withValidRequest_returns201Created() throws Exception {
            given(authService.register(any(RegisterRequestDto.class))).willReturn(authResponse());

            mockMvc.perform(post("/user")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validRegisterRequest()))
                        .with(csrf()))
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.token").value("jwt.token.here"))
                    .andExpect(jsonPath("$.tokenType").value("Bearer"))
                    .andExpect(jsonPath("$.expiresIn").value(3600))
                    .andExpect(jsonPath("$.user.userId").value(1))
                    .andExpect(jsonPath("$.user.username").value(USERNAME))
                    .andExpect(jsonPath("$.user.email").value(EMAIL))
                    .andExpect(jsonPath("$.user.role").value("USER"));

            verify(authService).register(any(RegisterRequestDto.class));
        }

        @Nested
        @DisplayName("Validation failure tests")
        class FailValidationTests {
            @Test
            void withBlankUsername_returns400() throws Exception {
                var requestBody = new RegisterRequestDto("", EMAIL, PASSWORD);

                mockMvc.perform(post("/user")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(requestBody))
                                .with(csrf()))
                        .andExpect(status().isBadRequest());

                verify(authService, never()).register(any(RegisterRequestDto.class));
            }

            @Test
            void withShortUsername_returns400() throws Exception {
                var requestBody = new RegisterRequestDto("12", EMAIL, PASSWORD);

                mockMvc.perform(post("/user")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(requestBody))
                                .with(csrf()))
                        .andExpect(status().isBadRequest());

                verify(authService, never()).register(any(RegisterRequestDto.class));
            }

            @Test
            void withNullEmail_returns400() throws Exception {
                var requestBody = new RegisterRequestDto(USERNAME, null, PASSWORD);

                mockMvc.perform(post("/user")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(requestBody))
                                .with(csrf()))
                        .andExpect(status().isBadRequest());

                verify(authService, never()).register(any(RegisterRequestDto.class));
            }

            @Test
            void withInvalidEmail_returns400() throws Exception {
                var requestBody = new RegisterRequestDto(USERNAME, "invalid@", PASSWORD);

                mockMvc.perform(post("/user")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(requestBody))
                                .with(csrf()))
                        .andExpect(status().isBadRequest());

                verify(authService, never()).register(any(RegisterRequestDto.class));
            }

            @Test
            void withShortPassword_returns400() throws Exception {
                var requestBody = new RegisterRequestDto(USERNAME, EMAIL, "passw");

                mockMvc.perform(post("/user")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(requestBody))
                                .with(csrf()))
                        .andExpect(status().isBadRequest());

                verify(authService, never()).register(any(RegisterRequestDto.class));
            }
        }
    }

    @Nested
    @DisplayName("POST /login")
    class LoginTests {
        @Test
        void withValidRequest_returns200Created() throws Exception {
            given(authService.authenticate(any(LoginRequestDto.class))).willReturn(authResponse());

            mockMvc.perform(post("/login")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(validLoginRequest()))
                            .with(csrf()))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.token").value("jwt.token.here"))
                    .andExpect(jsonPath("$.tokenType").value("Bearer"))
                    .andExpect(jsonPath("$.expiresIn").value(3600))
                    .andExpect(jsonPath("$.user.userId").value(1))
                    .andExpect(jsonPath("$.user.username").value(USERNAME))
                    .andExpect(jsonPath("$.user.email").value(EMAIL))
                    .andExpect(jsonPath("$.user.role").value("USER"));

            verify(authService).authenticate(any(LoginRequestDto.class));
        }

        @Nested
        @DisplayName("Validation failure tests")
        class FailValidationTests {
            @Test
            void withBlankUsername_returns400() throws Exception {
                var requestBody = new LoginRequestDto("", PASSWORD);

                mockMvc.perform(post("/login")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(requestBody))
                                .with(csrf()))
                        .andExpect(status().isBadRequest());

                verify(authService, never()).authenticate(any(LoginRequestDto.class));
            }

            @Test
            void withBlankPassword_returns400() throws Exception {
                var requestBody = new LoginRequestDto(USERNAME, "");

                mockMvc.perform(post("/login")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(requestBody))
                                .with(csrf()))
                        .andExpect(status().isBadRequest());

                verify(authService, never()).authenticate(any(LoginRequestDto.class));
            }
        }
    }

    @Nested
    @DisplayName("GET /user")
    class GetCurrentUserTests {
        @Test
        void withValidJwt_returns200Ok() throws Exception {
            given(userService.findByUsernameOrEmail(EMAIL)).willReturn(Optional.of(testUser()));

            mockMvc.perform(get("/user")
                        .with(jwt().jwt(builder -> builder.subject(EMAIL))))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.userId").value(1))
                    .andExpect(jsonPath("$.username").value(USERNAME))
                    .andExpect(jsonPath("$.email").value(EMAIL))
                    .andExpect(jsonPath("$.role").value("USER"));

            verify(userService).findByUsernameOrEmail(EMAIL);
        }

        @Test
        void withValidJwt_butNotFound_returns404NotFound() throws Exception {
            given(userService.findByUsernameOrEmail(EMAIL)).willReturn(Optional.empty());

            mockMvc.perform(get("/user")
                            .with(jwt().jwt(builder -> builder.subject(EMAIL))))
                    .andExpect(status().isNotFound());

            verify(userService).findByUsernameOrEmail(EMAIL);
        }

        @Test
        void withoutAuthentication_returns401Unauthorized() throws Exception {
            mockMvc.perform(get("/user"))
                    .andExpect(status().isUnauthorized());

            verify(userService, never()).findByUsernameOrEmail(EMAIL);
        }
    }
}