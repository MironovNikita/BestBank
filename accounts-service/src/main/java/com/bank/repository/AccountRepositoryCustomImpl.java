package com.bank.repository;

import com.bank.common.exception.TransferException;
import com.bank.dto.transfer.TransferOperationDto;
import lombok.RequiredArgsConstructor;
import org.springframework.r2dbc.core.DatabaseClient;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Mono;

@Repository
@RequiredArgsConstructor
public class AccountRepositoryCustomImpl implements AccountRepositoryCustom {

    private final DatabaseClient databaseClient;

    @Override
    public Mono<Void> transfer(TransferOperationDto transferOperationDto) {
        String sql = """
                UPDATE accounts
                SET balance =
                            CASE
                                WHEN id = :fromId THEN balance - :amount
                                WHEN id = :toId THEN balance + :amount
                            END
                WHERE id IN (:fromId, :toId) AND (SELECT balance FROM accounts WHERE id = :fromId) >= :amount
                """;

        return databaseClient.sql(sql)
                .bind("fromId", transferOperationDto.getAccountIdFrom())
                .bind("toId", transferOperationDto.getAccountIdTo())
                .bind("amount", transferOperationDto.getAmount())
                .fetch()
                .rowsUpdated()
                .flatMap(updated -> {
                    if (updated != 2) return Mono.error(new TransferException());
                    return Mono.empty();
                });
    }
}
