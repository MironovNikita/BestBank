package com.bank.contract;

import com.bank.dto.currency.Currency;
import com.bank.dto.transfer.TransferOperationDto;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

public class TransfersControllerContractTest extends AbstractContractTest {

    @Test
    @DisplayName("Проверка осуществления перевода средств на свой счёт")
    void shouldOperateTransferSelf() {
        TransferOperationDto dto =
                new TransferOperationDto(2L, Currency.RUB, 3L, Currency.EUR, "test@test.ru", BigDecimal.valueOf(1000), null);

        webTestClient.post()
                .uri("/transfer/self")
                .header("Content-Type", "application/json")
                .bodyValue(dto)
                .exchange()
                .expectStatus().is3xxRedirection()
                .expectHeader().valueEquals("Location", "/main");
    }

    @Test
    @DisplayName("Проверка осуществления перевода средств на чужой счёт")
    void shouldOperateTransferOther() {
        TransferOperationDto dto =
                new TransferOperationDto(2L, Currency.RUB, 3L, Currency.EUR, "test@test.ru", BigDecimal.valueOf(1000), null);

        webTestClient.post()
                .uri("/transfer/other")
                .header("Content-Type", "application/json")
                .bodyValue(dto)
                .exchange()
                .expectStatus().is3xxRedirection()
                .expectHeader().valueEquals("Location", "/main");
    }
}
