package com.arsnyan.taskmanagementservice.service;

import com.arsnyan.taskmanagementservice.model.User;
import com.arsnyan.taskmanagementservice.model.message.UserEvent;
import com.arsnyan.taskmanagementservice.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserEventService {
    private final UserRepository userRepository;

    @Transactional
    public void process(UserEvent userEvent) {
        switch (userEvent.type()) {
            case CREATED -> handleCreated(userEvent);
            case DELETED -> handleDeleted(userEvent);
        }
    }

    private void handleCreated(UserEvent userEvent) {
        log.info("Adding user {} with ID={}", userEvent.username(), userEvent.userId());
        try {
            userRepository.save(User.create(userEvent.userId(), userEvent.username()));
        } catch (DataIntegrityViolationException e) {
            log.info("User with ID={} already exists, aborting", userEvent.userId());
        }
    }

    private void handleDeleted(UserEvent userEvent) {
        log.info("Deleting user with ID={}", userEvent.userId());
        userRepository.deleteById(userEvent.userId());
    }
}
