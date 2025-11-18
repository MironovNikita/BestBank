package com.bank.repository;

import com.bank.entity.CashOperation;
import org.springframework.data.r2dbc.repository.R2dbcRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CashRepository extends R2dbcRepository<CashOperation, Long> {
}
