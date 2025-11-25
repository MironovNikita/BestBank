package com.bank.controller;

import com.bank.config.MockSecurityConfig;
import com.bank.dto.transfer.TransferOperationDto;
import com.bank.service.TransfersService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient;
import org.springframework.boot.test.autoconfigure.web.reactive.WebFluxTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.reactive.server.WebTestClient;
import reactor.core.publisher.Mono;

import static org.mockito.Mockito.*;

@ActiveProfiles("test")
@AutoConfigureWebTestClient
@WebFluxTest(controllers = TransfersController.class)
@Import({MockSecurityConfig.class})
public class TransfersControllerTest {

    @Autowired
    private WebTestClient webClient;

    @MockitoBean
    private TransfersService transfersService;

    @Test
    @DisplayName("Проверка метода перевода")
    void shouldTransferCorrect() {
        TransferOperationDto dto = new TransferOperationDto(1L, 2L, "test@test.ru", 1000L);

        when(transfersService.operateTransfer(dto)).thenReturn(Mono.empty());

        webClient.post()
                .uri("/transfer")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(dto)
                .exchange()
                .expectStatus().isOk();

        verify(transfersService).operateTransfer(dto);
    }

    @Test
    @DisplayName("Проверка метода перевода, если передана отрицательная сумма")
    void shouldNotOperateCashIfAmountIsNegative() {
        TransferOperationDto dto = new TransferOperationDto(1L, 2L, "test@test.ru", -1000L);

        webClient.post()
                .uri("/transfer")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(dto)
                .exchange()
                .expectStatus().isBadRequest();

        verify(transfersService, never()).operateTransfer(dto);
    }
}
