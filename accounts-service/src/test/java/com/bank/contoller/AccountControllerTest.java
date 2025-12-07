package com.bank.contoller;

import com.bank.dto.account.*;
import com.bank.dto.cash.BalanceDto;
import com.bank.dto.cash.UpdateBalanceRq;
import com.bank.dto.transfer.TransferOperationDto;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.math.BigDecimal;

import static com.bank.DataCreator.*;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class AccountControllerTest extends AbstractControllerTest {

    @Test
    @DisplayName("Проверка получения пользовательских валют")
    void shouldGetUsersCurrencies() {
        Long userId = 1L;
        AccountListDto dto = createAccountListDto(1L, userId);

        when(accountService.getUserAccounts(userId)).thenReturn(Flux.just(dto));

        webTestClient.get()
                .uri("/accounts/currencies/{id}", userId)
                .exchange()
                .expectStatus().isOk()
                .expectBodyList(AccountListDto.class)
                .hasSize(1)
                .contains(dto);

        verify(accountService).getUserAccounts(userId);
    }

    @Test
    @DisplayName("Проверка создания счёта")
    void shouldCreateUserAccount() {
        Long userId = 1L;
        AccountCreateDto dto = createAccountCreateDto();

        when(accountService.createAccount(dto, userId)).thenReturn(Mono.empty());

        webTestClient.post()
                .uri("/accounts/create/{id}", userId)
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(dto)
                .exchange()
                .expectStatus().isOk();

        verify(accountService).createAccount(dto, userId);
    }

    @Test
    @DisplayName("Проверка удаления счёта")
    void shouldDeleteUserAccount() {
        Long accountId = 1L;
        AccountDeleteDto dto = createAccountDeleteDto(accountId);

        when(accountService.deleteAccount(dto)).thenReturn(Mono.empty());

        webTestClient.post()
                .uri("/accounts/delete")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(dto)
                .exchange()
                .expectStatus().isOk();

        verify(accountService).deleteAccount(dto);
    }

    @Test
    @DisplayName("Проверка изменения счёта")
    void shouldEditUserAccount() {
        Long accountId = 1L;
        AccountEditDto dto = createAccountEditDto(accountId);

        when(accountService.editAccount(dto)).thenReturn(Mono.empty());

        webTestClient.post()
                .uri("/accounts/edit")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(dto)
                .exchange()
                .expectStatus().isOk();

        verify(accountService).editAccount(dto);
    }

    @Test
    @DisplayName("Проверка получения баланса по конкретному счёту")
    void shouldGetBalanceByAccountId() {
        Long accountId = 1L;
        BalanceDto balanceDto = new BalanceDto(accountId, BigDecimal.valueOf(1000L));

        when(accountService.getAccountBalance(accountId)).thenReturn(Mono.just(balanceDto));

        webTestClient.get()
                .uri("/accounts/{id}/balance", accountId)
                .exchange()
                .expectStatus().isOk()
                .expectBody(BalanceDto.class)
                .isEqualTo(balanceDto);

        verify(accountService).getAccountBalance(accountId);
    }

    @Test
    @DisplayName("Проверка обновления баланса по ID счёта")
    void shouldUpdateBalanceByAccountId() {
        Long accountId = 1L;
        UpdateBalanceRq rq = createUpdateBalanceRq();

        when(accountService.updateBalance(accountId, rq)).thenReturn(Mono.empty());

        webTestClient.post()
                .uri("/accounts/{id}/balance", accountId)
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(rq)
                .exchange()
                .expectStatus().isOk();

        verify(accountService).updateBalance(accountId, rq);
    }

    @Test
    @DisplayName("Проверка метода перевода средств")
    void shouldMakeTransfer() {
        TransferOperationDto dto = createTransferOperationDto(1L, 2L);

        when(accountService.transfer(dto)).thenReturn(Mono.empty());

        webTestClient.post()
                .uri("/accounts/transfer")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(dto)
                .exchange()
                .expectStatus().isOk();

        verify(accountService).transfer(dto);
    }

    @Test
    @DisplayName("Проверка получения списка аккаунтов, доступных для перевода")
    void shouldGetAllAvailableAccounts() {
        Long accountId = 1L;
        AccountOtherListDto first = createAccountOtherListDto(2L, 3L);
        AccountOtherListDto second = createAccountOtherListDto(3L, 3L);

        when(accountService.getAllOtherAccounts(accountId)).thenReturn(Flux.just(first, second));

        webTestClient.get()
                .uri("/accounts/{id}", accountId)
                .exchange()
                .expectStatus().isOk()
                .expectBodyList(AccountOtherListDto.class)
                .hasSize(2)
                .contains(first, second);

        verify(accountService).getAllOtherAccounts(accountId);
    }
}
