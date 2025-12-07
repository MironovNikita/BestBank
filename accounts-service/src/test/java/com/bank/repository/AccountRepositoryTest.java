package com.bank.repository;

import com.bank.dto.currency.Currency;
import com.bank.dto.transfer.TransferOperationDto;
import com.bank.entity.Account;
import com.bank.entity.User;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import reactor.test.StepVerifier;

import java.math.BigDecimal;

import static com.bank.DataCreator.*;
import static com.bank.DataInserter.insertIntoAccountsTable;
import static com.bank.DataInserter.insertIntoUsersTable;
import static org.assertj.core.api.Assertions.assertThat;

public class AccountRepositoryTest extends AbstractRepositoryTest {

    @Autowired
    private AccountRepository accountRepository;

    @Test
    @DisplayName("Проверка поиска пользовательских счетов по его ID")
    void shouldFindAllUserAccountByUserId() {
        Long userId = 1L;
        User user = createUser(userId);
        Account account = createAccount(1L, userId, Currency.RUB);
        insertIntoUsersTable(databaseClient, user).block();
        insertIntoAccountsTable(databaseClient, account).block();

        StepVerifier.create(accountRepository.getAllUserAccountsById(account.getId()))
                .assertNext(result -> {
                    assertThat(account.getId().equals(result.getId()));
                    assertThat(account.getCurrency().equals(result.getCurrency()));
                    assertThat(account.getTitle().equals(result.getTitle()));
                })
                .verifyComplete();
    }

    @Test
    @DisplayName("Проверка получения баланса по ID счёта")
    void shouldFindAccountBalanceById() {
        Long userId = 1L;
        User user = createUser(userId);
        Account account = createAccount(1L, userId, Currency.RUB);
        insertIntoUsersTable(databaseClient, user).block();
        insertIntoAccountsTable(databaseClient, account).block();

        StepVerifier.create(accountRepository.getAccountBalance(account.getId()))
                .assertNext(result -> assertThat(result.longValue() == 1000L).isTrue())
                .verifyComplete();
    }

    @Test
    @DisplayName("Проверка изменения названия счёта по ID")
    void shouldChangeAccountTitleById() {
        Long userId = 1L;
        User user = createUser(userId);
        Account account = createAccount(1L, userId, Currency.RUB);
        insertIntoUsersTable(databaseClient, user).block();
        insertIntoAccountsTable(databaseClient, account).block();

        StepVerifier.create(accountRepository.editAccountTitleById(account.getId(), "New Title"))
                .expectNext(1)
                .verifyComplete();
    }

    @Test
    @DisplayName("Проверка получения баланса счёта")
    void shouldGetAccountBalanceById() {
        Long userId = 1L;
        User user = createUser(userId);
        Account account = createAccount(1L, userId, Currency.RUB);
        insertIntoUsersTable(databaseClient, user).block();
        insertIntoAccountsTable(databaseClient, account).block();
    }

    @Test
    @DisplayName("Проверка обновления баланса аккаунта")
    void shouldUpdateAccountBalance() {
        Long userId = 1L;
        User user = createUser(userId);
        Account account = createAccount(1L, userId, Currency.RUB);
        insertIntoUsersTable(databaseClient, user).block();
        insertIntoAccountsTable(databaseClient, account).block();

        StepVerifier.create(accountRepository.updateAccountBalance(account.getId(), BigDecimal.valueOf(2000)))
                .verifyComplete();

        BigDecimal updatedBalance = accountRepository.getAccountBalance(account.getId()).block();

        assertThat(updatedBalance).isEqualByComparingTo(BigDecimal.valueOf(2000));
    }

    @Test
    @DisplayName("Проверка поиска аккаунтов для главной страницы")
    void shouldFindAllAccountsForMainPage() {
        User user1 = createUser(1L);
        User user2 = createUser(2L);
        user2.setEmail("smth@test.ru");
        user2.setPhone("89542223311");
        Account account1 = createAccount(1L, user2.getId(), Currency.RUB);
        Account account2 = createAccount(2L, user2.getId(), Currency.EUR);
        insertIntoUsersTable(databaseClient, user1).block();
        insertIntoUsersTable(databaseClient, user2).block();
        insertIntoAccountsTable(databaseClient, account1).block();
        insertIntoAccountsTable(databaseClient, account2).block();

        StepVerifier.create(accountRepository.getAllAccountsForMainPage(account1.getId()))
                .assertNext(result -> {
                    assertThat(account1.getId().equals(result.getId()));
                    assertThat(account1.getCurrency() == result.getCurrency());
                })
                .assertNext(result -> {
                    assertThat(account2.getId().equals(result.getId()));
                    assertThat(account2.getCurrency() == result.getCurrency());
                })
                .verifyComplete();
    }

    @Test
    @DisplayName("Проверка осуществления перевода")
    void shouldTransfer() {
        User user1 = createUser(1L);
        User user2 = createUser(2L);
        user2.setEmail("smth@test.ru");
        user2.setPhone("89542223311");
        Account account1 = createAccount(1L, user2.getId(), Currency.RUB);
        Account account2 = createAccount(2L, user2.getId(), Currency.EUR);
        insertIntoUsersTable(databaseClient, user1).block();
        insertIntoUsersTable(databaseClient, user2).block();
        insertIntoAccountsTable(databaseClient, account1).block();
        insertIntoAccountsTable(databaseClient, account2).block();
        TransferOperationDto dto = createTransferOperationDto(account1.getId(), account2.getId());
        dto.setAmountTo(BigDecimal.valueOf(1000));

        StepVerifier.create(accountRepository.transfer(dto))
                .verifyComplete();

        BigDecimal account1Balance = accountRepository.getAccountBalance(account1.getId()).block();
        BigDecimal account2Balance = accountRepository.getAccountBalance(account2.getId()).block();

        assertThat(account1Balance).isEqualByComparingTo(BigDecimal.valueOf(0.00));
        assertThat(account2Balance).isEqualByComparingTo(BigDecimal.valueOf(2000.00));
    }


}
