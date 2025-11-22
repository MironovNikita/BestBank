package com.bank.contoller;

import com.bank.config.MockSecurityConfig;
import com.bank.controller.AccountController;
import com.bank.dto.account.AccountMainPageDto;
import com.bank.dto.account.AccountPasswordChangeDto;
import com.bank.dto.account.AccountUpdateDto;
import com.bank.dto.account.RegisterAccountRequest;
import com.bank.dto.cash.BalanceDto;
import com.bank.dto.cash.UpdateBalanceRq;
import com.bank.dto.login.LoginRequest;
import com.bank.dto.login.LoginResponse;
import com.bank.dto.transfer.TransferOperationDto;
import com.bank.service.AccountService;
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
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import static com.bank.DataCreator.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ActiveProfiles("test")
@AutoConfigureWebTestClient
@WebFluxTest(controllers = AccountController.class)
@Import({MockSecurityConfig.class})
public class AccountControllerTest {

    @Autowired
    private WebTestClient webTestClient;

    @MockitoBean
    private AccountService accountService;

    @Test
    @DisplayName("Проверка регистрации нового аккаунта")
    void shouldRegisterNewAccount() {
        RegisterAccountRequest rq = createRegisterRq();

        when(accountService.register(rq)).thenReturn(Mono.empty());

        webTestClient.post()
                .uri("/accounts/register")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(rq)
                .exchange()
                .expectStatus().isOk();

        verify(accountService).register(rq);
    }

    @Test
    @DisplayName("Проверка регистрации нового аккаунта с некорректным номером")
    void shouldNotRegisterNewAccountIfIncorrectFields() {
        RegisterAccountRequest rq = createRegisterRq();
        rq.setPhone("23");

        webTestClient.post()
                .uri("/accounts/register")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(rq)
                .exchange()
                .expectStatus().isBadRequest();

        verify(accountService, never()).register(any());
    }

    @Test
    @DisplayName("Проверка получения списка аккаунтов, доступных для перевода")
    void shouldGetAllAvailableAccounts() {
        Long accountId = 1L;
        AccountMainPageDto first = createAccountMainPageDto("2");
        AccountMainPageDto second = createAccountMainPageDto("3");

        when(accountService.getAllAccounts(accountId)).thenReturn(Flux.just(first, second));

        webTestClient.get()
                .uri("/accounts/{id}", accountId)
                .exchange()
                .expectStatus().isOk()
                .expectBodyList(AccountMainPageDto.class)
                .hasSize(2)
                .contains(first, second);

        verify(accountService).getAllAccounts(accountId);
    }

    @Test
    @DisplayName("Проверка изменения пароля")
    void shouldEditPassword() {
        Long accountId = 1L;
        AccountPasswordChangeDto dto = createAccountPasswordChangeDto();

        when(accountService.editPassword(accountId, dto)).thenReturn(Mono.empty());

        webTestClient.post()
                .uri("/accounts/{id}/editPassword", accountId)
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(dto)
                .exchange()
                .expectStatus().isOk();

        verify(accountService).editPassword(accountId, dto);
    }

    @Test
    @DisplayName("Проверка изменения данных аккаунта")
    void shouldEditAccountData() {
        Long accountId = 1L;
        AccountUpdateDto dto = createAccountUpdateDto();

        when(accountService.editAccount(accountId, dto)).thenReturn(Mono.empty());

        webTestClient.post()
                .uri("/accounts/{id}/editAccount", accountId)
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(dto)
                .exchange()
                .expectStatus().isOk();

        verify(accountService).editAccount(accountId, dto);
    }

    @Test
    @DisplayName("Проверка успешного логина")
    void shouldLogin() {
        LoginRequest rq = createLoginRequest();
        LoginResponse rs = new LoginResponse().setId(1L).setEmail("test@test.ru").setName("test");

        when(accountService.login(rq)).thenReturn(Mono.just(rs));

        webTestClient.post()
                .uri("/accounts/login")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(rq)
                .exchange()
                .expectStatus().isOk()
                .expectBody(LoginResponse.class)
                .isEqualTo(rs);

        verify(accountService).login(rq);
    }

    @Test
    @DisplayName("Проверка получения баланса по конкретному аккаунту")
    void shouldGetBalanceByAccountId() {
        Long accountId = 1L;
        BalanceDto balanceDto = new BalanceDto(accountId, 1000L);

        when(accountService.getBalance(accountId)).thenReturn(Mono.just(balanceDto));

        webTestClient.get()
                .uri("/accounts/{id}/balance", accountId)
                .exchange()
                .expectStatus().isOk()
                .expectBody(BalanceDto.class)
                .isEqualTo(balanceDto);

        verify(accountService).getBalance(accountId);
    }

    @Test
    @DisplayName("Проверка обновления баланса по ID аккаунта")
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
}
