package com.arsnyan.account.service;

import com.arsnyan.account.exception.EntityAlreadyExistsException;
import com.arsnyan.account.model.User;
import com.arsnyan.account.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserService {
    private final UserRepository userRepository;

    @Transactional(readOnly = true)
    public Optional<User> findByUsernameOrEmail(String identifier) {
        return userRepository.findByUsernameOrEmail(identifier);
    }

    @Transactional(readOnly = true)
    public boolean existsByUsername(String username) {
        return userRepository.existsByUsername(username);
    }

    @Transactional(readOnly = true)
    public boolean existsByEmail(String email) {
        return userRepository.existsByEmail(email);
    }

    @Transactional
    public User create(User user) {
        try {
            return userRepository.save(user);
        } catch (DataIntegrityViolationException e) {
            log.error("User {} already exists", user.getUsername(), e);
            throw new EntityAlreadyExistsException("User already exists");
        }
    }
}
