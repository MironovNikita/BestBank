package com.bank.contract;

import com.bank.dto.cash.CashOperationDto;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

public class CashControllerContractTest extends AbstractContractTest {

    @Test
    @DisplayName("Проверка осуществления операции с наличными")
    void shouldOperateCash() {
        CashOperationDto dto = new CashOperationDto(3L, 1L, "PUT", "test@test.ru", BigDecimal.valueOf(1000));

        webTestClient.post()
                .uri("/cash")
                .header("Content-Type", "application/json")
                .bodyValue(dto)
                .exchange()
                .expectStatus().is3xxRedirection()
                .expectHeader().valueEquals("Location", "/main");
    }
}
