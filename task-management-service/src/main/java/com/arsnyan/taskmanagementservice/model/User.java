package com.arsnyan.taskmanagementservice.model;

import jakarta.persistence.*;
import lombok.*;

import java.util.Set;

@Entity
@Table(name = "users")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class User {
    @Id
    @Column(name = "user_id")
    private Long userId;

    @Column(name = "username", unique = true, nullable = false)
    private String username;

    @Column(name = "email", unique = true)
    private String email;

    @OneToMany(mappedBy = "owner", fetch = FetchType.LAZY)
    private Set<Task> tasks;

    public static User create(Long userId, String username, String email) {
        return User.builder()
                .userId(userId)
                .username(username)
                .email(email)
                .build();
    }
}
