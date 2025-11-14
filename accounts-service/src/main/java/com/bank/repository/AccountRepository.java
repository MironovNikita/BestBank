package com.bank.repository;

import com.bank.entity.Account;
import com.bank.entity.AccountMainPageDto;
import org.springframework.data.r2dbc.repository.Query;
import org.springframework.data.r2dbc.repository.R2dbcRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Repository
public interface AccountRepository extends R2dbcRepository<Account, Long> {

    Mono<Account> findByEmail(String email);

    @Query("""
            SELECT name, surname, phone FROM accounts
            """)
    Flux<AccountMainPageDto> getAllAccountsForMainPage();

    Mono<Account> findAccountById(Long id);
}
