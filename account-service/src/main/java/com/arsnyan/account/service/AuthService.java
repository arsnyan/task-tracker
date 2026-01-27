package com.arsnyan.account.service;

import com.arsnyan.account.config.KafkaConfiguration;
import com.arsnyan.account.dto.AuthResponseDto;
import com.arsnyan.account.dto.LoginRequestDto;
import com.arsnyan.account.dto.RegisterRequestDto;
import com.arsnyan.account.dto.UserResponseDto;
import com.arsnyan.account.exception.EntityAlreadyExistsException;
import com.arsnyan.account.exception.InvalidCredentialsException;
import com.arsnyan.account.model.User;
import com.arsnyan.account.model.message.UserEvent;
import com.arsnyan.account.security.JwtService;
import com.arsnyan.account.security.RsaKeyProperties;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuthService {
    private static final String USER_CREATED_EVENT_NAME = "USER_CREATED";

    private final UserService userService;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final RsaKeyProperties rsaKeyProperties;
    private final MailPreparationService mailPreparationService;
    private final KafkaTemplate<String, UserEvent> kafkaTemplate;

    @Transactional
    public AuthResponseDto register(RegisterRequestDto request) {
        log.debug("Registering user with email: {}", request.email());

        if (userService.existsByUsername(request.username())) {
            throw new EntityAlreadyExistsException("User already exists with username: " + request.username());
        }

        if (userService.existsByEmail(request.email())) {
            throw new EntityAlreadyExistsException("User already exists with email: " + request.email());
        }

        var encodedPassword = passwordEncoder.encode(request.password());
        var user = User.create(request.username(), request.email(), encodedPassword);
        var savedUser = userService.create(user);

        log.info("User registered successfully: {}", savedUser.getEmail());

        mailPreparationService.sendMessage(request);
        sendUserCreatedEvent(savedUser);

        return createAuthResponse(savedUser);
    }

    @Transactional(readOnly = true)
    public AuthResponseDto authenticate(LoginRequestDto request) {
        log.debug("Authenticating user: {}", request.username());

        var user = userService.findByUsernameOrEmail(request.username())
                .orElseThrow(InvalidCredentialsException::new);

        if (!passwordEncoder.matches(request.password(), user.getPassword())) {
            log.warn("Invalid password attempt for user: {}", request.username());
            throw new InvalidCredentialsException();
        }

        log.info("User authenticated successfully: {}", user.getEmail());

        return createAuthResponse(user);
    }

    private AuthResponseDto createAuthResponse(User user) {
        var token = jwtService.generateToken(user);
        var userResponse = UserResponseDto.from(user);
        return AuthResponseDto.of(token, rsaKeyProperties.expiration().toMillis(), userResponse);
    }

    private void sendUserCreatedEvent(User user) {
        var event = UserEvent.ofCreatedType(user.getUserId(), user.getUsername());
        kafkaTemplate.send(KafkaConfiguration.USER_EVENTS_TOPIC_NAME, USER_CREATED_EVENT_NAME, event);
    }
}
