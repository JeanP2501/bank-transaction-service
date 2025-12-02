package com.bank.transaction.repository;

import com.bank.transaction.model.entity.Transaction;
import com.bank.transaction.model.enums.TransactionType;
import org.springframework.data.mongodb.repository.ReactiveMongoRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;

/**
 * Reactive repository for Transaction entity
 */
@Repository
public interface TransactionRepository extends ReactiveMongoRepository<Transaction, String> {

    Flux<Transaction> findByAccountId(String accountId);
    Flux<Transaction> findByCreditId(String creditId);
    Flux<Transaction> findByCustomerId(String customerId);
    Flux<Transaction> findByTransactionType(TransactionType transactionType);

}
