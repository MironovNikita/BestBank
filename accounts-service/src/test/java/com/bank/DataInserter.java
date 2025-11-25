package com.bank;

import com.bank.entity.Account;
import lombok.experimental.UtilityClass;
import org.springframework.r2dbc.core.DatabaseClient;
import reactor.core.publisher.Mono;

@UtilityClass
public class DataInserter {

    public static Mono<Void> insertIntoAccountsTable(DatabaseClient client, Account account) {
        return client.sql(
                        "INSERT INTO accounts(id, email, password, name, surname, birthdate, phone, balance)" +
                                "VALUES(:id, :email, :password, :name, :surname, :birthdate, :phone, :balance)"
                )
                .bind("id", account.getId())
                .bind("email", account.getEmail())
                .bind("password", account.getPassword())
                .bind("name", account.getName())
                .bind("surname", account.getSurname())
                .bind("birthdate", account.getBirthdate())
                .bind("phone", account.getPhone())
                .bind("balance", account.getBalance())
                .then();
    }
}
