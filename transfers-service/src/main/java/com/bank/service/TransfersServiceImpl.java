package com.bank.service;

import com.bank.common.exception.TransferException;
import com.bank.common.mapper.TransferOperationMapper;
import com.bank.dto.transfer.TransferOperationDto;
import com.bank.entity.TransferOperation;
import com.bank.repository.TransfersRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

@Slf4j
@Service
@RequiredArgsConstructor
public class TransfersServiceImpl {

    private final TransfersRepository transfersRepository;
    private final WebClient accountsWebClient;
    private final TransferOperationMapper transferOperationMapper;

    @Transactional
    public Mono<Void> operateTransfer(TransferOperationDto transferOperationDto) {
        TransferOperation transferOperation = transferOperationMapper.toTransferOperation(transferOperationDto);

        return accountsWebClient
                .post()
                .uri("/accounts/transfer")
                .bodyValue(transferOperationDto)
                .retrieve()
                .toBodilessEntity()
                .doOnSuccess(v -> log.info("Перевод c ID {} на ID {} выполнен успешно.", transferOperationDto.getAccountIdFrom(), transferOperationDto.getAccountIdTo()))
                .then(transfersRepository.save(transferOperation))
                .then()
                .onErrorResume(ex -> {
                    log.error("Перевод со счёта ID: {} на счёт ID: {} завершился с ошибкой: {}",transferOperationDto.getAccountIdFrom(), transferOperationDto.getAccountIdTo(), ex.getMessage());
                    return Mono.error(new TransferException());
                });
    }
}
