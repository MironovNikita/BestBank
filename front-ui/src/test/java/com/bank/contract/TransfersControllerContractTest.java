package com.bank.contract;

import com.bank.dto.transfer.TransferOperationDto;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

public class TransfersControllerContractTest extends AbstractContractTest {

    @Test
    @DisplayName("Проверка осуществления перевода средств")
    void shouldOperateTransfer() {
        TransferOperationDto dto = new TransferOperationDto(3L, 2L, "test@test.ru", 1000L);

        webTestClient.post()
                .uri("/transfer")
                .header("Content-Type", "application/json")
                .bodyValue(dto)
                .exchange()
                .expectStatus().is3xxRedirection()
                .expectHeader().valueEquals("Location", "/main");
    }
}
