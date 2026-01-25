package com.arsnyan.account.service;

import com.arsnyan.account.TestcontainersConfiguration;
import com.arsnyan.account.exception.EntityAlreadyExistsException;
import com.arsnyan.account.model.User;
import com.arsnyan.account.model.UserRole;
import com.arsnyan.account.repository.UserRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.jdbc.Sql;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

@SpringBootTest
@Import(TestcontainersConfiguration.class)
@Sql(statements = "TRUNCATE TABLE users RESTART IDENTITY CASCADE", executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD)
class UserServiceTest {
    @Autowired
    private UserService userService;

    @Autowired
    private UserRepository userRepository;

    @Nested
    @DisplayName("findByUsernameOrEmail")
    class FindByUsernameOrEmailTests {
        @Test
        void returnsUser_whenSearchingByUsername() {
            var user = User.create("johndoe", "john@example.com", "password");
            userRepository.saveAndFlush(user);

            var result = userService.findByUsernameOrEmail("johndoe");

            assertThat(result).isPresent();
            assertThat(result.get().getUsername()).isEqualTo("johndoe");
            assertThat(result.get().getEmail()).isEqualTo("john@example.com");
        }

        @Test
        void returnsUser_whenSearchingByEmail() {
            var user = User.create("johndoe", "john@example.com", "password");
            userRepository.saveAndFlush(user);

            var result = userService.findByUsernameOrEmail("john@example.com");

            assertThat(result).isPresent();
            assertThat(result.get().getUsername()).isEqualTo("johndoe");
            assertThat(result.get().getEmail()).isEqualTo("john@example.com");
        }

        @Test
        void returnsEmpty_whenUserDoesNotExist() {
            var result = userService.findByUsernameOrEmail("nonexistent");

            assertThat(result).isEmpty();
        }

        @Test
        void returnsEmpty_whenSearchingWithPartialMatch() {
            var user = User.create("johndoe", "john@example.com", "password");
            userRepository.saveAndFlush(user);

            var result = userService.findByUsernameOrEmail("john");

            assertThat(result).isEmpty();
        }
    }

    @Nested
    @DisplayName("existsByUsername")
    class ExistsByUsernameTests {
        @Test
        void returnsTrue_whenUsernameExists() {
            var user = User.create("johndoe", "john@example.com", "password");
            userRepository.saveAndFlush(user);

            var exists = userService.existsByUsername("johndoe");

            assertThat(exists).isTrue();
        }

        @Test
        void returnsFalse_whenUsernameDoesNotExist() {
            var exists = userService.existsByUsername("nonexistent");

            assertThat(exists).isFalse();
        }

        @Test
        void returnsFalse_whenSearchingByEmailInsteadOfUsername() {
            var user = User.create("johndoe", "john@example.com", "password");
            userRepository.saveAndFlush(user);

            var exists = userService.existsByUsername("john@example.com");

            assertThat(exists).isFalse();
        }
    }

    @Nested
    @DisplayName("existsByEmail")
    class ExistsByEmailTests {
        @Test
        void returnsTrue_whenEmailExists() {
            var user = User.create("johndoe", "john@example.com", "password");
            userRepository.saveAndFlush(user);

            var exists = userService.existsByEmail("john@example.com");

            assertThat(exists).isTrue();
        }

        @Test
        void returnsFalse_whenEmailDoesNotExist() {
            var exists = userService.existsByEmail("nonexistent@example.com");

            assertThat(exists).isFalse();
        }

        @Test
        void returnsFalse_whenSearchingByUsernameInsteadOfEmail() {
            var user = User.create("johndoe", "john@example.com", "password");
            userRepository.saveAndFlush(user);

            var exists = userService.existsByEmail("johndoe");

            assertThat(exists).isFalse();
        }
    }

    @Nested
    @DisplayName("create")
    class CreateTests {
        @Test
        void succeeds_whenUserDoesNotExist() {
            var user = User.create("username", "email@example.com", "password");

            var createdUser = userService.create(user);

            assertThat(userRepository.findAll()).hasSize(1);
            assertThat(createdUser).isNotNull();
            assertThat(createdUser.getUsername()).isEqualTo("username");
            assertThat(createdUser.getEmail()).isEqualTo("email@example.com");
            assertThat(createdUser.getRole()).isEqualTo(UserRole.USER);
        }

        @Test
        void fails_whenUsernameAlreadyExists() {
            var existingUser = User.create("username", "first@example.com", "password");
            userRepository.saveAndFlush(existingUser);

            var duplicateUser = User.create("username", "second@example.com", "password");

            assertThrows(EntityAlreadyExistsException.class, () -> userService.create(duplicateUser));
            assertThat(userRepository.findAll()).hasSize(1);
        }

        @Test
        void fails_whenEmailAlreadyExists() {
            var existingUser = User.create("first", "email@example.com", "password");
            userRepository.saveAndFlush(existingUser);

            var duplicateUser = User.create("second", "email@example.com", "password");

            assertThrows(EntityAlreadyExistsException.class, () -> userService.create(duplicateUser));
            assertThat(userRepository.findAll()).hasSize(1);
        }
    }
}