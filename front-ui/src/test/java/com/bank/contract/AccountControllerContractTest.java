package com.bank.contract;

import com.bank.dto.account.AccountCreateDto;
import com.bank.dto.account.AccountDeleteDto;
import com.bank.dto.account.AccountEditDto;
import com.bank.dto.currency.Currency;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

public class AccountControllerContractTest extends AbstractContractTest {

    @Test
    @DisplayName("Проверка создания счёта")
    void shouldCreateAccount() {
        AccountCreateDto dto = new AccountCreateDto();
        dto.setTitle("Test");
        dto.setCurrency(Currency.USD);
        dto.setEmail("test@test.ru");

        webTestClient
                .post()
                .uri("/accounts/create")
                .bodyValue(dto)
                .exchange()
                .expectStatus()
                .is3xxRedirection()
                .expectHeader().valueEquals("Location", "/main");
    }

    @Test
    @DisplayName("Проверка изменения счёта")
    void shouldEditAccount() {
        AccountEditDto dto = new AccountEditDto();
        dto.setId(2L);
        dto.setNewTitle("New Title");
        dto.setCurrency(Currency.RUB);
        dto.setEmail("test@test.ru");

        webTestClient
                .post()
                .uri("/accounts/edit")
                .bodyValue(dto)
                .exchange()
                .expectStatus()
                .is3xxRedirection()
                .expectHeader().valueEquals("Location", "/main");
    }

    @Test
    @DisplayName("Проверка удаления счёта")
    void shouldDeleteAccount() {
        AccountDeleteDto dto = new AccountDeleteDto();
        dto.setId(2L);
        dto.setCurrency(Currency.RUB);
        dto.setEmail("test@test.ru");

        webTestClient
                .post()
                .uri("/accounts/delete")
                .bodyValue(dto)
                .exchange()
                .expectStatus()
                .is3xxRedirection()
                .expectHeader().valueEquals("Location", "/main");
    }
}
