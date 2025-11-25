package com.bank.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Table;

import java.time.LocalDate;

@Data
@Table(name = "accounts")
@AllArgsConstructor
public class Account {

    @Id
    private Long id;

    private String email;

    private String password;

    private String name;

    private String surname;

    private LocalDate birthdate;

    private String phone;

    private Long balance;
}
