package com.bank.entity;

import com.bank.dto.currency.Currency;
import lombok.AllArgsConstructor;
import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

import java.math.BigDecimal;

@Data
@Table(name = "accounts")
@AllArgsConstructor
public class Account {

    @Id
    private Long id;

    @Column("owner_id")
    private Long ownerId;

    private String title;

    private Currency currency;

    private BigDecimal balance;
}
