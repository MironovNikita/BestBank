package com.bank.service;

import com.bank.common.exception.NotEnoughFundsException;
import com.bank.common.mapper.CashOperationMapper;
import com.bank.dto.cash.CashOperationDto;
import com.bank.entity.CashOperation;
import com.bank.entity.OperationType;
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
    private NotificationServiceClient notificationServiceClient;

    @InjectMocks
    private CashServiceImpl cashService;

    @Test
    @DisplayName("Проверка операции с наличными")
    void shouldOperateCashCorrect() {
        Long accountId = 1L;
        CashOperationDto dto = new CashOperationDto(accountId, "GET", "test@test.ru", 1000L);
        CashOperation operation = new CashOperation();
        operation.setAccountId(accountId);
        operation.setOperation(OperationType.valueOf(dto.getOperation()));
        operation.setAmount(dto.getAmount());

        when(cashOperationMapper.toCashOperation(dto)).thenReturn(operation);
        when(accountsServiceClient.getCurrentBalance(accountId)).thenReturn(Mono.just(10000L));
        when(accountsServiceClient.updateRemoteBalance(9000L, accountId)).thenReturn(Mono.empty());
        when(cashRepository.save(operation)).thenReturn(Mono.just(operation));
        when(converter.decrypt(anyString())).thenReturn(dto.getEmail());
        when(notificationServiceClient.sendNotification(anyString(), anyString(), anyString())).thenReturn(Mono.empty());

        StepVerifier.create(cashService.operateCash(dto))
                .verifyComplete();

        verify(cashOperationMapper).toCashOperation(dto);
        verify(accountsServiceClient).getCurrentBalance(accountId);
        verify(accountsServiceClient).updateRemoteBalance(9000L, accountId);
        verify(cashRepository).save(operation);
        verify(converter).decrypt(anyString());
        verify(notificationServiceClient).sendNotification(anyString(), anyString(), anyString());
    }

    @Test
    @DisplayName("Проверка операции с наличными - недостаточно средств")
    void shouldNotOperateCashIfNotEnoughFunds() {
        Long accountId = 1L;
        CashOperationDto dto = new CashOperationDto(accountId, "GET", "test@test.ru", 1000L);
        CashOperation operation = new CashOperation();
        operation.setAccountId(accountId);
        operation.setOperation(OperationType.valueOf(dto.getOperation()));
        operation.setAmount(dto.getAmount());

        when(cashOperationMapper.toCashOperation(dto)).thenReturn(operation);
        when(accountsServiceClient.getCurrentBalance(accountId)).thenReturn(Mono.just(0L));

        StepVerifier.create(cashService.operateCash(dto))
                .expectError(NotEnoughFundsException.class)
                .verify();

        verify(cashOperationMapper).toCashOperation(dto);
        verify(accountsServiceClient).getCurrentBalance(accountId);
        verify(accountsServiceClient, never()).updateRemoteBalance(9000L, accountId);
        verify(cashRepository, never()).save(operation);
        verify(converter, never()).decrypt(anyString());
        verify(notificationServiceClient, never()).sendNotification(anyString(), anyString(), anyString());
    }
}
