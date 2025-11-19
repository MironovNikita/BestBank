package com.bank.entity;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

@Data
@Table("transfers")
public class TransferOperation {

    @Id
    private Long id;
    @Column("account_id_from")
    private Long accountIdFrom;
    @Column("account_id_to")
    private Long accountIdTo;
    private Long amount;
}
