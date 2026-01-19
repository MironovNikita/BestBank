package com.bank.service;

import com.bank.common.exception.TransferException;
import com.bank.common.mapper.TransferOperationMapper;
import com.bank.dto.currency.Currency;
import com.bank.dto.transfer.TransferOperationDto;
import com.bank.entity.TransferOperation;
import com.bank.metrics.BlockMetrics;
import com.bank.repository.TransfersRepository;
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
public class TransfersServiceTest {

    @Mock
    private TransfersRepository transfersRepository;
    @Mock
    private TransferOperationMapper transferOperationMapper;
    @Mock
    private SecureBase64Converter converter;
    @Mock
    private AccountsServiceClient accountsServiceClient;
    @Mock
    private NotificationsService notificationsService;
    @Mock
    private ExchangeServiceClient exchangeServiceClient;
    @Mock
    private BlockerServiceClient blockerServiceClient;
    @Mock
    private BlockMetrics blockMetrics;

    @InjectMocks
    private TransfersServiceImpl transfersService;

    @Test
    @DisplayName("Проверка осуществления перевода")
    void shouldTransferCorrect() {
        TransferOperationDto dto =
                new TransferOperationDto(1L, Currency.RUB, 2L, Currency.EUR, "test@test.ru", BigDecimal.valueOf(1000), null);
        TransferOperation operation = new TransferOperation();
        operation.setAccountIdFrom(1L);
        operation.setAccountIdTo(2L);
        operation.setAmountFrom(dto.getAmountFrom());

        when(blockerServiceClient.checkOperation()).thenReturn(Mono.just(true));
        when(exchangeServiceClient.recountTransferAmount(dto)).thenReturn(Mono.just(BigDecimal.valueOf(10)));
        when(transferOperationMapper.toTransferOperation(dto)).thenReturn(operation);
        when(transfersRepository.save(operation)).thenReturn(Mono.just(operation));
        when(accountsServiceClient.transfer(dto)).thenReturn(Mono.empty());
        when(converter.decrypt(anyString())).thenReturn(dto.getEmail());
        when(notificationsService.sendTransferNotification(anyString(), anyString(), anyString())).thenReturn(Mono.empty());
        doNothing().when(blockMetrics).recordAllowedOperation(anyLong(), any(), anyLong(), any(), anyString());

        StepVerifier.create(transfersService.operateTransfer(dto))
                .verifyComplete();

        verify(blockerServiceClient).checkOperation();
        verify(exchangeServiceClient).recountTransferAmount(dto);
        verify(transferOperationMapper).toTransferOperation(dto);
        verify(accountsServiceClient).transfer(dto);
        verify(converter).decrypt(anyString());
        verify(notificationsService).sendTransferNotification(anyString(), anyString(), anyString());
        verify(transfersRepository).save(operation);
    }

    @Test
    @DisplayName("Проверка отказа в переводе, если недостаточно средств")
    void shouldNotTransferCorrectIfNotEnoughFunds() {
        TransferOperationDto dto =
                new TransferOperationDto(1L, Currency.RUB, 2L, Currency.EUR, "test@test.ru", BigDecimal.valueOf(-1000), null);
        TransferOperation operation = new TransferOperation();
        operation.setAccountIdFrom(1L);
        operation.setAccountIdTo(2L);
        operation.setAmountFrom(dto.getAmountFrom());

        when(blockerServiceClient.checkOperation()).thenReturn(Mono.just(true));
        when(exchangeServiceClient.recountTransferAmount(dto)).thenReturn(Mono.just(BigDecimal.valueOf(-10)));
        when(transferOperationMapper.toTransferOperation(dto)).thenReturn(operation);
        when(transfersRepository.save(operation)).thenReturn(Mono.just(operation));
        when(accountsServiceClient.transfer(dto)).thenReturn(Mono.error(new TransferException()));
        when(transfersRepository.delete(any(TransferOperation.class))).thenReturn(Mono.empty());

        StepVerifier.create(transfersService.operateTransfer(dto))
                .expectError(TransferException.class)
                .verify();

        verify(blockerServiceClient).checkOperation();
        verify(exchangeServiceClient).recountTransferAmount(dto);
        verify(transferOperationMapper).toTransferOperation(dto);
        verify(transfersRepository).save(any());
        verify(accountsServiceClient).transfer(dto);
        verify(notificationsService, never()).sendTransferNotification(anyString(), anyString(), anyString());
        verify(converter, never()).decrypt(anyString());
    }
}
