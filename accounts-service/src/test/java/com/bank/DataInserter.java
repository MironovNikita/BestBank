package com.bank;

import com.bank.entity.Account;
import com.bank.entity.User;
import lombok.experimental.UtilityClass;
import org.springframework.r2dbc.core.DatabaseClient;
import reactor.core.publisher.Mono;

@UtilityClass
public class DataInserter {

    public static Mono<Void> insertIntoAccountsTable(DatabaseClient client, Account account) {
        return client.sql(
                        "INSERT INTO accounts(id, owner_id, title, currency, balance) VALUES(:id, :ownerId, :title, :currency, :balance)"
                )
                .bind("id", account.getId())
                .bind("ownerId", account.getOwnerId())
                .bind("title", account.getTitle())
                .bind("currency", account.getCurrency().name())
                .bind("balance", account.getBalance())
                .then();
    }

    public static Mono<Void> insertIntoUsersTable(DatabaseClient client, User user) {
        return client.sql(
                        "INSERT INTO users(id, email, password, name, surname, birthdate, phone)" +
                                "VALUES(:id, :email, :password, :name, :surname, :birthdate, :phone)"
                )
                .bind("id", user.getId())
                .bind("email", user.getEmail())
                .bind("password", user.getPassword())
                .bind("name", user.getName())
                .bind("surname", user.getSurname())
                .bind("birthdate", user.getBirthdate())
                .bind("phone", user.getPhone())
                .then();
    }
}
