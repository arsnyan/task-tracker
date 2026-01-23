package com.arsnyan.account.service;

import com.arsnyan.account.dto.AuthResponse;
import com.arsnyan.account.dto.LoginRequest;
import com.arsnyan.account.dto.RegisterRequest;
import com.arsnyan.account.dto.UserResponse;
import com.arsnyan.account.exception.EntityAlreadyExistsException;
import com.arsnyan.account.exception.InvalidCredentialsException;
import com.arsnyan.account.model.User;
import com.arsnyan.account.security.JwtService;
import com.arsnyan.account.security.RsaKeyProperties;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuthService {
    private final UserService userService;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final RsaKeyProperties rsaKeyProperties;

    @Transactional
    public AuthResponse register(RegisterRequest request) {
        log.debug("Registering user with email: {}", request.email());

        if (userService.existsByUsername(request.username())) {
            throw new EntityAlreadyExistsException("User already exists with username: " + request.username());
        }

        if (userService.existsByEmail(request.email())) {
            throw new EntityAlreadyExistsException("User already exists with email: " + request.email());
        }

        var encodedPassword = passwordEncoder.encode(request.password());
        var user = User.create(request.username(), request.password(), encodedPassword);
        var savedUser = userService.create(user);

        log.info("User registered successfully: {}", savedUser.getEmail());

        return createAuthResponse(savedUser);
    }

    @Transactional(readOnly = true)
    public AuthResponse authenticate(LoginRequest request) {
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

    private AuthResponse createAuthResponse(User user) {
        var token = jwtService.generateToken(user);
        var userResponse = UserResponse.from(user);
        return AuthResponse.of(token, rsaKeyProperties.expiration(), userResponse);
    }
}
