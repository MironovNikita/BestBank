package com.bank.repository;

import com.bank.entity.User;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import reactor.test.StepVerifier;

import static com.bank.DataCreator.createUser;
import static com.bank.DataInserter.insertIntoUsersTable;
import static org.assertj.core.api.Assertions.assertThat;

public class UserRepositoryTest extends AbstractRepositoryTest {

    @Autowired
    private UserRepository userRepository;

    @Test
    @DisplayName("Проверка получения пользователя по email")
    void shouldFindUserByEmail() {
        User user = createUser(1L);
        insertIntoUsersTable(databaseClient, user).block();

        StepVerifier.create(userRepository.getUserByEmail(user.getEmail()))
                .assertNext(result -> {
                    assertThat(user.getId()).isEqualTo(result.getId());
                    assertThat(user.getEmail()).isEqualTo(result.getEmail());
                    assertThat(user.getName()).isEqualTo(result.getName());
                })
                .verifyComplete();
    }
}
