package com.bank.repository;

import com.bank.dto.AccountMainPageDto;
import com.bank.entity.Account;
import org.springframework.data.r2dbc.repository.Query;
import org.springframework.data.r2dbc.repository.R2dbcRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Repository
public interface AccountRepository extends R2dbcRepository<Account, Long> {

    @Query("""
            SELECT * from accounts a
            WHERE a.email = :email
            """)
    Mono<Account> getAccountByEmail(@Param("email") String email);

    @Query("""
            SELECT id, name, surname, phone FROM accounts
            WHERE id != :id
            """)
    Flux<AccountMainPageDto> getAllAccountsForMainPage(@Param("id") Long id);

    Mono<Account> findAccountById(Long id);
}
