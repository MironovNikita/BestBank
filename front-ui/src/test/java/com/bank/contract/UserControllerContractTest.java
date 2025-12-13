package com.bank.contract;

import com.bank.dto.user.UserPasswordChangeDto;
import com.bank.dto.user.UserUpdateDto;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

public class UserControllerContractTest extends AbstractContractTest {

    @Test
    @DisplayName("Изменение данных аккаунта")
    void shouldEditUser() {
        UserUpdateDto updateDto = new UserUpdateDto();
        updateDto.setEmail("test@test.ru");

        webTestClient
                .post()
                .uri("/editUser")
                .header("Content-Type", "application/json")
                .bodyValue(updateDto)
                .exchange()
                .expectStatus().is3xxRedirection()
                .expectHeader().valueEquals("Location", "/main");
    }

    @Test
    @DisplayName("Изменение пароля аккаунта")
    void shouldEditPassword() {
        UserPasswordChangeDto dto = new UserPasswordChangeDto("Password1111", "Password1111");

        webTestClient
                .post()
                .uri("/editPassword")
                .header("Content-Type", "application/json")
                .bodyValue(dto)
                .exchange()
                .expectStatus().is3xxRedirection()
                .expectHeader().valueEquals("Location", "/main");
    }

    @Test
    @DisplayName("Проверка удаления пользователя")
    void shouldDeleteUser() {
        webTestClient
                .post()
                .uri("/deleteUser")
                .header("Content-Type", "application/json")
                .exchange()
                .expectStatus().is3xxRedirection()
                .expectHeader().valueEquals("Location", "/main");
    }
}
