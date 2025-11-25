package com.bank.repository;

import com.bank.entity.TransferOperation;
import org.springframework.data.r2dbc.repository.R2dbcRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface TransfersRepository extends R2dbcRepository<TransferOperation, Long> {
}
