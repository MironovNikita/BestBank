package com.bank.contoller;

import com.bank.dto.login.LoginRequest;
import com.bank.dto.login.LoginResponse;
import com.bank.dto.user.RegisterUserRequest;
import com.bank.dto.user.UserPasswordChangeDto;
import com.bank.dto.user.UserUpdateDto;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import reactor.core.publisher.Mono;

import static com.bank.DataCreator.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

public class UserControllerTest extends AbstractControllerTest {

    @Test
    @DisplayName("Проверка регистрации нового аккаунта")
    void shouldRegisterNewAccount() {
        RegisterUserRequest rq = createRegisterRq();

        when(userService.register(rq)).thenReturn(Mono.empty());

        webTestClient.post()
                .uri("/users/register")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(rq)
                .exchange()
                .expectStatus().isOk();

        verify(userService).register(rq);
    }

    @Test
    @DisplayName("Проверка регистрации нового аккаунта с некорректным номером")
    void shouldNotRegisterNewAccountIfIncorrectFields() {
        RegisterUserRequest rq = createRegisterRq();
        rq.setPhone("23");

        webTestClient.post()
                .uri("/users/register")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(rq)
                .exchange()
                .expectStatus().isBadRequest();

        verify(userService, never()).register(any());
    }

    @Test
    @DisplayName("Проверка изменения пароля")
    void shouldEditPassword() {
        Long accountId = 1L;
        UserPasswordChangeDto dto = createAccountPasswordChangeDto();

        when(userService.editPassword(accountId, dto)).thenReturn(Mono.empty());

        webTestClient.post()
                .uri("/users/{id}/editPassword", accountId)
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(dto)
                .exchange()
                .expectStatus().isOk();

        verify(userService).editPassword(accountId, dto);
    }

    @Test
    @DisplayName("Проверка изменения данных аккаунта")
    void shouldEditAccountData() {
        Long accountId = 1L;
        UserUpdateDto dto = createAccountUpdateDto();

        when(userService.editUser(accountId, dto)).thenReturn(Mono.empty());

        webTestClient.post()
                .uri("/users/{id}/editUser", accountId)
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(dto)
                .exchange()
                .expectStatus().isOk();

        verify(userService).editUser(accountId, dto);
    }

    @Test
    @DisplayName("Проверка успешного логина")
    void shouldLogin() {
        LoginRequest rq = createLoginRequest();
        LoginResponse rs = new LoginResponse().setId(1L).setEmail("test@test.ru").setName("test");

        when(userService.login(rq)).thenReturn(Mono.just(rs));

        webTestClient.post()
                .uri("/users/login")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(rq)
                .exchange()
                .expectStatus().isOk()
                .expectBody(LoginResponse.class)
                .isEqualTo(rs);

        verify(userService).login(rq);
    }

    @Test
    @DisplayName("Проверка удаления пользователя")
    void shouldDeleteUser() {
        Long id = 1L;
        String email = "test@test.ru";

        when(userService.deleteUser(id, email)).thenReturn(Mono.empty());

        webTestClient.post()
                .uri("/users/delete/{id}", id)
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(email)
                .exchange()
                .expectStatus().isOk();

        verify(userService).deleteUser(id, email);
    }
}
