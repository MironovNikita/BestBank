package com.bank.contract;

import com.bank.dto.account.AccountPasswordChangeDto;
import com.bank.dto.account.AccountUpdateDto;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

public class AccountControllerContractTest extends AbstractContractTest {

    @Test
    @DisplayName("Изменение данных аккаунта")
    void shouldEditAccount() {
        AccountUpdateDto updateDto = new AccountUpdateDto();
        updateDto.setEmail("test@test.ru");

        webTestClient
                .post()
                .uri("/editAccount")
                .header("Content-Type", "application/json")
                .bodyValue(updateDto)
                .exchange()
                .expectStatus().is3xxRedirection()
                .expectHeader().valueEquals("Location", "/main");
    }

    @Test
    @DisplayName("Изменение аккаунта")
    void shouldEditPassword() {
        AccountPasswordChangeDto dto = new AccountPasswordChangeDto("Password1111", "Password1111");

        webTestClient
                .post()
                .uri("/editAccount")
                .header("Content-Type", "application/json")
                .bodyValue(dto)
                .exchange()
                .expectStatus().is3xxRedirection()
                .expectHeader().valueEquals("Location", "/main");
    }
}
