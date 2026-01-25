package com.arsnyan.account.service;

import com.arsnyan.account.dto.LoginRequestDto;
import com.arsnyan.account.dto.RegisterRequestDto;
import com.arsnyan.account.exception.EntityAlreadyExistsException;
import com.arsnyan.account.exception.InvalidCredentialsException;
import com.arsnyan.account.model.User;
import com.arsnyan.account.security.JwtService;
import com.arsnyan.account.security.RsaKeyProperties;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.Duration;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {
    @Mock
    private UserService userService;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private JwtService jwtService;

    @Mock
    private RsaKeyProperties rsaKeyProperties;

    @Mock
    private MailPreparationService mailPreparationService;

    @InjectMocks
    private AuthService authService;

    private static final String USERNAME = "username";
    private static final String EMAIL = "email";
    private static final String RAW_PASSWORD = "password";
    private static final String ENCODED_PASSWORD = "encodedPassword";
    private static final String JWT_TOKEN = "eyJhbGciOiJSUzI1NiIsInR5cCI6IkpXVCJ9.test.token";
    private static final Duration TOKEN_EXPIRATION = Duration.ofHours(1);

    @Nested
    class RegisterTests {
        private RegisterRequestDto registerRequest;
        private User savedUser;

        @BeforeEach
        void setUp() {
            registerRequest = new RegisterRequestDto(USERNAME, EMAIL, RAW_PASSWORD);
            savedUser = User.create(USERNAME, EMAIL, ENCODED_PASSWORD);
            savedUser.setUserId(0L);
        }

        @Test
        void withValidRequest_returnsAuthResponse() {
            given(userService.existsByUsername(USERNAME)).willReturn(false);
            given(userService.existsByEmail(EMAIL)).willReturn(false);
            given(passwordEncoder.encode(RAW_PASSWORD)).willReturn(ENCODED_PASSWORD);
            given(userService.create(any(User.class))).willReturn(savedUser);
            given(jwtService.generateToken(savedUser)).willReturn(JWT_TOKEN);
            given(rsaKeyProperties.expiration()).willReturn(TOKEN_EXPIRATION);

            var response = authService.register(registerRequest);

            verify(passwordEncoder).encode(RAW_PASSWORD);
            verify(jwtService).generateToken(savedUser);
            verify(mailPreparationService).sendMessage(registerRequest);

            assertThat(response).isNotNull();
            assertThat(response.token()).isEqualTo(JWT_TOKEN);
            assertThat(response.expiresIn()).isEqualTo(TOKEN_EXPIRATION.toSeconds());
            assertThat(response.user()).isNotNull();
            assertThat(response.user().username()).isEqualTo(USERNAME);
            assertThat(response.user().email()).isEqualTo(EMAIL);
        }

        @Test
        void withExistingUsername_throwsEntityAlreadyExistsException() {
            given(userService.existsByUsername(USERNAME)).willReturn(true);

            assertThatThrownBy(() -> authService.register(registerRequest))
                    .isInstanceOf(EntityAlreadyExistsException.class)
                    .hasMessageContaining("username")
                    .hasMessageContaining(USERNAME);

            verify(passwordEncoder, never()).encode(RAW_PASSWORD);
            verify(jwtService, never()).generateToken(any());
            verify(userService, never()).create(any(User.class));
        }

        @Test
        void withExistingEmail_throwsEntityAlreadyExistsException() {
            given(userService.existsByEmail(EMAIL)).willReturn(true);

            assertThatThrownBy(() -> authService.register(registerRequest))
                    .isInstanceOf(EntityAlreadyExistsException.class)
                    .hasMessageContaining("email")
                    .hasMessageContaining(EMAIL);

            verify(passwordEncoder, never()).encode(RAW_PASSWORD);
            verify(jwtService, never()).generateToken(any());
            verify(userService, never()).create(any(User.class));
        }
    }

    @Nested
    class AuthenticateTests {
        private LoginRequestDto loginRequest;
        private User existingUser;

        @BeforeEach
        void setUp() {
            loginRequest = new LoginRequestDto(USERNAME, RAW_PASSWORD);
            existingUser = User.create(USERNAME, EMAIL, ENCODED_PASSWORD);
        }

        @Test
        void withValidCredentials_returnsAuthResponse() {
            given(userService.findByUsernameOrEmail(loginRequest.username())).willReturn(Optional.of(existingUser));
            given(passwordEncoder.matches(RAW_PASSWORD, ENCODED_PASSWORD)).willReturn(true);
            given(jwtService.generateToken(existingUser)).willReturn(JWT_TOKEN);
            given(rsaKeyProperties.expiration()).willReturn(TOKEN_EXPIRATION);

            var response = authService.authenticate(loginRequest);

            assertThat(response).isNotNull();
            assertThat(response.token()).isEqualTo(JWT_TOKEN);
            assertThat(response.expiresIn()).isEqualTo(TOKEN_EXPIRATION.toSeconds());
            assertThat(response.user()).isNotNull();
            assertThat(response.user().username()).isEqualTo(loginRequest.username());

            verify(passwordEncoder).matches(RAW_PASSWORD, ENCODED_PASSWORD);
            verify(jwtService).generateToken(existingUser);
        }

        @Test
        void withNonExistentUser_throwsInvalidCredentialsException() {
            given(userService.findByUsernameOrEmail(loginRequest.username())).willReturn(Optional.empty());

            assertThatThrownBy(() -> authService.authenticate(loginRequest))
                    .isInstanceOf(InvalidCredentialsException.class)
                    .hasMessageContaining("Invalid username or password");

            verify(passwordEncoder, never()).matches(any(), any());
            verify(jwtService, never()).generateToken(any());
        }

        @Test
        void withWrongPassword_throwsInvalidCredentialsException() {
            given(userService.findByUsernameOrEmail(loginRequest.username())).willReturn(Optional.of(User.create(
                    USERNAME,
                    EMAIL,
                    "some other encoded password"
            )));

            assertThatThrownBy(() -> authService.authenticate(loginRequest))
                    .isInstanceOf(InvalidCredentialsException.class)
                    .hasMessageContaining("Invalid username or password");

            verify(passwordEncoder, atLeastOnce()).matches(any(), any());
            verify(jwtService, never()).generateToken(any());
        }
    }
}