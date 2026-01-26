package com.arsnyan.taskmanagementservice.service;

import com.arsnyan.taskmanagementservice.exception.NoSuchEntityException;
import com.arsnyan.taskmanagementservice.model.User;
import com.arsnyan.taskmanagementservice.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UserService {
    private final UserRepository userRepository;

    public User getUser(String username) {
        return userRepository.getUserByUsername(username)
                .orElseThrow(() -> new NoSuchEntityException("Username %s doesn't exist"));
    }
}
