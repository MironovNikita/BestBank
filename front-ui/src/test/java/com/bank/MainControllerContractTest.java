package com.bank;

import com.bank.dto.account.RegisterAccountRequest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class MainControllerContractTest extends AbstractContractTest {

    @Test
    @DisplayName("Проверка успешной регистрации аккаунта")
    void shouldRegisterSuccessfully() {
        RegisterAccountRequest rq = new RegisterAccountRequest();
        rq.setEmail("test@test.ru");
        rq.setPassword("Password1111");
        rq.setName("Test");
        rq.setSurname("Test");
        rq.setPhone("89996665522");
        rq.setBirthdate(LocalDate.of(1990, 1, 1));

        webTestClient.post()
                .uri("/register")
                .header("Content-Type", "application/json")
                .bodyValue(rq)
                .exchange()
                .expectStatus().is2xxSuccessful();
    }

    @Test
    @DisplayName("Проверка регистрации нового аккаунта с некорректными данными")
    void shouldNotRegisterNewAccountIfIncorrectFields() {
        RegisterAccountRequest rq = new RegisterAccountRequest();
        rq.setEmail("test");
        rq.setPassword("Pa");
        rq.setName("Test");
        rq.setSurname("Test");
        rq.setPhone("99");
        rq.setBirthdate(LocalDate.of(1990, 1, 1));

        webTestClient.post()
                .uri("/register")
                .header("Content-Type", "application/json")
                .bodyValue(rq)
                .exchange()
                .expectStatus().is2xxSuccessful()
                .expectBody(String.class)
                .consumeWith(response -> {
                    String body = response.getResponseBody();
                    assertNotNull(body);
                    assertTrue(body.contains("email"));
                    assertTrue(body.contains("phone"));
                    assertTrue(body.contains("password"));
                });
    }

    @Test
    @DisplayName("Отображение main-страницы")
    void shouldGetMainPage() {

        webTestClient
                .get()
                .uri("/main")
                .exchange()
                .expectStatus().isOk();
    }
}
