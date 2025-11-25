package com.bank.entity;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

@Data
@Table("cash")
public class CashOperation {

    @Id
    private Long id;
    @Column("account_id")
    private Long accountId;
    private OperationType operation;
    private Long amount;
}
