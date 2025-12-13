package com.bank.entity;

import com.bank.dto.currency.Currency;
import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

import java.math.BigDecimal;

@Data
@Table("transfers")
public class TransferOperation {

    @Id
    private Long id;
    @Column("account_id_from")
    private Long accountIdFrom;
    @Column("currency_from")
    private Currency currencyFrom;
    @Column("account_id_to")
    private Long accountIdTo;
    @Column("currency_to")
    private Currency currencyTo;
    @Column("amount_from")
    private BigDecimal amountFrom;
    @Column("amount_to")
    private BigDecimal amountTo;
}
