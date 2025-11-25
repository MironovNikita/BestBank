package com.bank.service;

import com.bank.common.exception.TransferException;
import com.bank.common.mapper.TransferOperationMapper;
import com.bank.dto.transfer.TransferOperationDto;
import com.bank.entity.TransferOperation;
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
    private NotificationsServiceClient notificationsServiceClient;

    @InjectMocks
    private TransfersServiceImpl transfersService;

    @Test
    @DisplayName("Проверка осуществления перевода")
    void shouldTransferCorrect() {
        TransferOperationDto dto = new TransferOperationDto(1L, 2L, "test@test.ru", 1000L);
        TransferOperation operation = new TransferOperation();
        operation.setAccountIdFrom(1L);
        operation.setAccountIdTo(2L);
        operation.setAmount(dto.getAmount());

        when(transferOperationMapper.toTransferOperation(dto)).thenReturn(operation);
        when(accountsServiceClient.transfer(dto)).thenReturn(Mono.empty());
        when(notificationsServiceClient.sendTransferNotification(anyString(), anyString(), anyString())).thenReturn(Mono.empty());
        when(converter.decrypt(anyString())).thenReturn(dto.getEmail());
        when(transfersRepository.save(operation)).thenReturn(Mono.empty());

        StepVerifier.create(transfersService.operateTransfer(dto))
                .verifyComplete();

        verify(transferOperationMapper).toTransferOperation(dto);
        verify(accountsServiceClient).transfer(dto);
        verify(notificationsServiceClient).sendTransferNotification(anyString(), anyString(), anyString());
        verify(converter).decrypt(anyString());
        verify(transfersRepository).save(operation);
    }

    @Test
    @DisplayName("Проверка осуществления перевода")
    void shouldNotTransferCorrectIfNotEnoughFunds() {
        TransferOperationDto dto = new TransferOperationDto(1L, 2L, "test@test.ru", -1000L);
        TransferOperation operation = new TransferOperation();
        operation.setAccountIdFrom(1L);
        operation.setAccountIdTo(2L);
        operation.setAmount(dto.getAmount());

        when(transferOperationMapper.toTransferOperation(dto)).thenReturn(operation);
        when(accountsServiceClient.transfer(dto)).thenReturn(Mono.error(new TransferException()));

        StepVerifier.create(transfersService.operateTransfer(dto))
                .expectError(TransferException.class)
                .verify();

        verify(transferOperationMapper).toTransferOperation(dto);
        verify(accountsServiceClient).transfer(dto);
        verify(notificationsServiceClient, never()).sendTransferNotification(anyString(), anyString(), anyString());
        verify(transfersRepository, never()).save(any());
        verify(converter, never()).decrypt(anyString());
    }
}
