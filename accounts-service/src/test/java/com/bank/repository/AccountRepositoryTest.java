package com.bank.repository;

import com.bank.dto.transfer.TransferOperationDto;
import com.bank.entity.Account;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import reactor.test.StepVerifier;

import static com.bank.DataCreator.createAccount;
import static com.bank.DataCreator.createTransferOperationDto;
import static com.bank.DataInserter.insertIntoAccountsTable;
import static org.assertj.core.api.Assertions.assertThat;

public class AccountRepositoryTest extends AbstractRepositoryTest {

    @Autowired
    private AccountRepository accountRepository;

    @Test
    @DisplayName("Проверка поиска аккаунта по email")
    void shouldFindAccountByEmail() {
        Account account = createAccount(1L);
        insertIntoAccountsTable(databaseClient, account).block();

        StepVerifier.create(accountRepository.getAccountByEmail(account.getEmail()))
                .assertNext(result -> {
                    assertThat(account.getId().equals(result.getId()));
                    assertThat(account.getEmail().equals(result.getEmail()));
                    assertThat(account.getName().equals(result.getName()));
                })
                .verifyComplete();
    }

    @Test
    @DisplayName("Проверка поиска аккаунтов для главной страницы")
    void shouldFindAllAccountsForMainPage() {
        Account account1 = createAccount(1L);
        Account account2 = createAccount(2L);
        account2.setEmail("smth@test.ru");
        account2.setPhone("89542223311");

        insertIntoAccountsTable(databaseClient, account1).block();
        insertIntoAccountsTable(databaseClient, account2).block();

        StepVerifier.create(accountRepository.getAllAccountsForMainPage(account1.getId()))
                .assertNext(result -> {
                    assertThat(account2.getId().toString().equals(result.getId()));
                    assertThat(account2.getPhone().equals(result.getPhone()));
                    assertThat(account2.getName().equals(result.getName()));
                })
                .verifyComplete();
    }

    @Test
    @DisplayName("Проверка поиска аккаунта по ID")
    void shouldFindAccountByID() {
        Account account = createAccount(1L);
        insertIntoAccountsTable(databaseClient, account).block();

        StepVerifier.create(accountRepository.findAccountById(account.getId()))
                .assertNext(result -> {
                    assertThat(account.getId().equals(result.getId()));
                    assertThat(account.getEmail().equals(result.getEmail()));
                    assertThat(account.getName().equals(result.getName()));
                })
                .verifyComplete();
    }

    @Test
    @DisplayName("Проверка получения баланса аккаунта")
    void shouldGetAccountBalance() {
        Account account = createAccount(1L);
        insertIntoAccountsTable(databaseClient, account).block();

        StepVerifier.create(accountRepository.getAccountBalance(account.getId()))
                .assertNext(result -> assertThat(account.getBalance().equals(result)))
                .verifyComplete();
    }

    @Test
    @DisplayName("Проверка обновления баланса аккаунта")
    void shouldUpdateAccountBalance() {
        Account account = createAccount(1L);
        insertIntoAccountsTable(databaseClient, account).block();

        StepVerifier.create(accountRepository.updateAccountBalance(account.getId(), 1000L))
                .verifyComplete();

        Long updatedBalance = accountRepository.getAccountBalance(account.getId()).block();

        assertThat(updatedBalance).isEqualTo(1000L);
    }

    @Test
    @DisplayName("Проверка осуществления перевода")
    void shouldTransfer() {
        Account account1 = createAccount(1L);
        account1.setBalance(1000L);
        Account account2 = createAccount(2L);
        account2.setEmail("smth@test.ru");
        account2.setPhone("89542223311");
        TransferOperationDto dto = createTransferOperationDto(account1.getId(), account2.getId());

        insertIntoAccountsTable(databaseClient, account1).block();
        insertIntoAccountsTable(databaseClient, account2).block();

        StepVerifier.create(accountRepository.transfer(dto))
                .verifyComplete();

        Long account1Balance = accountRepository.getAccountBalance(account1.getId()).block();
        Long account2Balance = accountRepository.getAccountBalance(account2.getId()).block();

        assertThat(account1Balance).isEqualTo(0L);
        assertThat(account2Balance).isEqualTo(1000L);
    }
}
