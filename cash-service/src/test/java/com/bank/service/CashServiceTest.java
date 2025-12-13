package com.bank.service;

import com.bank.common.exception.NotEnoughFundsException;
import com.bank.common.mapper.CashOperationMapper;
import com.bank.dto.cash.CashOperationDto;
import com.bank.entity.CashOperation;
import com.bank.entity.OperationType;
import com.bank.exception.BlockerException;
import com.bank.repository.CashRepository;
import com.bank.security.SecureBase64Converter;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.math.BigDecimal;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class CashServiceTest {

    @Mock
    private CashRepository cashRepository;
    @Mock
    private CashOperationMapper cashOperationMapper;
    @Mock
    private SecureBase64Converter converter;
    @Mock
    private AccountsServiceClient accountsServiceClient;
    @Mock
    private NotificationsServiceClient notificationsServiceClient;
    @Mock
    private BlockerServiceClient blockerServiceClient;

    @InjectMocks
    private CashServiceImpl cashService;

    @Test
    @DisplayName("Проверка операции с наличными")
    void shouldOperateCashCorrect() {
        Long accountId = 1L;
        CashOperationDto dto = new CashOperationDto(accountId, 3L, "GET", "test@test.ru", BigDecimal.valueOf(1000));
        CashOperation operation = new CashOperation();
        operation.setAccountId(accountId);
        operation.setOperation(OperationType.valueOf(dto.getOperation()));
        operation.setAmount(dto.getAmount());

        when(blockerServiceClient.checkOperation()).thenReturn(Mono.just(true));
        when(cashOperationMapper.toCashOperation(dto)).thenReturn(operation);
        when(accountsServiceClient.getCurrentAccountBalance(accountId)).thenReturn(Mono.just(BigDecimal.valueOf(10000L)));
        when(accountsServiceClient.updateRemoteBalance(BigDecimal.valueOf(9000), accountId)).thenReturn(Mono.empty());
        when(cashRepository.save(operation)).thenReturn(Mono.just(operation));
        when(converter.decrypt(anyString())).thenReturn(dto.getEmail());
        when(notificationsServiceClient.sendNotification(anyString(), anyString(), anyString())).thenReturn(Mono.empty());

        StepVerifier.create(cashService.operateCash(dto))
                .verifyComplete();

        verify(blockerServiceClient).checkOperation();
        verify(cashOperationMapper).toCashOperation(dto);
        verify(accountsServiceClient).getCurrentAccountBalance(accountId);
        verify(accountsServiceClient).updateRemoteBalance(BigDecimal.valueOf(9000), accountId);
        verify(cashRepository).save(operation);
        verify(converter).decrypt(anyString());
        verify(notificationsServiceClient).sendNotification(anyString(), anyString(), anyString());
    }

    @Test
    @DisplayName("Проверка операции с наличными - недостаточно средств")
    void shouldNotOperateCashIfNotEnoughFunds() {
        Long accountId = 1L;
        CashOperationDto dto = new CashOperationDto(accountId, 3L, "GET", "test@test.ru", BigDecimal.valueOf(1000));
        CashOperation operation = new CashOperation();
        operation.setAccountId(accountId);
        operation.setOperation(OperationType.valueOf(dto.getOperation()));
        operation.setAmount(dto.getAmount());

        when(blockerServiceClient.checkOperation()).thenReturn(Mono.just(true));
        when(cashOperationMapper.toCashOperation(dto)).thenReturn(operation);
        when(accountsServiceClient.getCurrentAccountBalance(accountId)).thenReturn(Mono.just(BigDecimal.ZERO));

        StepVerifier.create(cashService.operateCash(dto))
                .expectError(NotEnoughFundsException.class)
                .verify();

        verify(blockerServiceClient).checkOperation();
        verify(cashOperationMapper).toCashOperation(dto);
        verify(accountsServiceClient).getCurrentAccountBalance(accountId);
        verify(accountsServiceClient, never()).updateRemoteBalance(BigDecimal.valueOf(9000L), accountId);
        verify(cashRepository, never()).save(operation);
        verify(converter, never()).decrypt(anyString());
        verify(notificationsServiceClient, never()).sendNotification(anyString(), anyString(), anyString());
    }

    @Test
    @DisplayName("Проверка определения подозрительной операции")
    void shouldThrowBlockerException() {
        Long accountId = 1L;
        CashOperationDto dto = new CashOperationDto(accountId, 3L, "GET", "test@test.ru", BigDecimal.valueOf(1000));
        CashOperation operation = new CashOperation();
        operation.setAccountId(accountId);
        operation.setOperation(OperationType.valueOf(dto.getOperation()));
        operation.setAmount(dto.getAmount());

        when(blockerServiceClient.checkOperation()).thenReturn(Mono.just(false));

        StepVerifier.create(cashService.operateCash(dto))
                .expectError(BlockerException.class)
                .verify();

        verify(blockerServiceClient).checkOperation();
        verify(cashOperationMapper).toCashOperation(dto);
        verify(accountsServiceClient, never()).getCurrentAccountBalance(accountId);
        verify(accountsServiceClient, never()).updateRemoteBalance(BigDecimal.valueOf(9000L), accountId);
        verify(cashRepository, never()).save(operation);
        verify(converter, never()).decrypt(anyString());
        verify(notificationsServiceClient, never()).sendNotification(anyString(), anyString(), anyString());

    }
}
