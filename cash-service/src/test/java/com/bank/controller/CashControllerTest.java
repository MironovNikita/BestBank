package com.bank.controller;

import com.bank.config.MockSecurityConfig;
import com.bank.dto.cash.CashOperationDto;
import com.bank.service.CashService;
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
@WebFluxTest(controllers = CashController.class)
@Import({MockSecurityConfig.class})
public class CashControllerTest {

    @Autowired
    private WebTestClient webClient;

    @MockitoBean
    private CashService cashService;

    @Test
    @DisplayName("Проверка метода операций с наличными")
    void shouldOperateCashCorrect() {
        CashOperationDto dto = new CashOperationDto(1L, "GET", "test@test.ru", 1000L);

        when(cashService.operateCash(dto)).thenReturn(Mono.empty());

        webClient.post()
                .uri("/cash")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(dto)
                .exchange()
                .expectStatus().isOk();

        verify(cashService).operateCash(dto);
    }

    @Test
    @DisplayName("Проверка метода операций с наличными, если передана отрицательная сумма")
    void shouldNotOperateCashIfAmountIsNegative() {
        CashOperationDto dto = new CashOperationDto(1L, "PUT", "test@test.ru", -1000L);

        webClient.post()
                .uri("/cash")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(dto)
                .exchange()
                .expectStatus().isBadRequest();

        verify(cashService, never()).operateCash(dto);
    }
}
