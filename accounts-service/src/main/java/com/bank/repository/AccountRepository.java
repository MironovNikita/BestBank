package com.bank.repository;

import com.bank.dto.account.AccountListDto;
import com.bank.dto.account.AccountOtherListDto;
import com.bank.entity.Account;
import org.springframework.data.r2dbc.repository.Modifying;
import org.springframework.data.r2dbc.repository.Query;
import org.springframework.data.r2dbc.repository.R2dbcRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.math.BigDecimal;

@Repository
public interface AccountRepository extends R2dbcRepository<Account, Long>, AccountRepositoryCustom {


    @Query("""
            SELECT * FROM accounts a
            WHERE a.owner_id = :id
            """)
    Flux<AccountListDto> getAllUserAccountsById(@Param("id") Long id);

    @Query("""
            UPDATE accounts a
            SET title = :title
            WHERE a.id = :id
            """)
    @Modifying
    Mono<Integer> editAccountTitleById(@Param("id") Long id, @Param("title") String title);

    @Query("""
            SELECT balance FROM accounts a
            WHERE a.id = :id
            """)
    Mono<BigDecimal> getAccountBalance(@Param("id") Long id);

    @Query("""
            UPDATE accounts a SET balance = :balance
            WHERE a.id = :id
            """)
    @Modifying
    Mono<Void> updateAccountBalance(@Param("id") Long id, @Param("balance") BigDecimal balance);

    @Query("""
            SELECT a.id, a.owner_id, a.currency, u.name, u.surname, u.phone FROM accounts a
            LEFT JOIN users u ON a.owner_id = u.id
            WHERE a.owner_id != :id
            ORDER BY owner_id
            """)
    Flux<AccountOtherListDto> getAllAccountsForMainPage(@Param("id") Long id);
}
