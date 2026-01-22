package com.arsnyan.account.repository;

import com.arsnyan.account.model.User;

import java.util.Optional;

public interface UserRepository {
    Optional<User> findByEmail(String email);
    Optional<User> findByUsername(String username);
    Optional<User> findById(Long id);
    boolean existsByEmail(String email);
    boolean existsByUsername(String username);

    User insert(User user);
}
