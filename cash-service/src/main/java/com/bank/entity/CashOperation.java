package com.bank.entity;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

import java.math.BigDecimal;

@Data
@Table("cash")
public class CashOperation {

    @Id
    private Long id;
    @Column("account_id")
    private Long accountId;
    @Column("user_id")
    private Long userId;
    private OperationType operation;
    private BigDecimal amount;
}
