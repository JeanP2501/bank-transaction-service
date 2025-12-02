package com.bank.transaction.service;

import com.bank.transaction.client.AccountClient;
import com.bank.transaction.client.CreditClient;
import com.bank.transaction.exception.InsufficientFundsException;
import com.bank.transaction.exception.TransactionException;
import com.bank.transaction.mapper.TransactionMapper;
import com.bank.transaction.model.dto.*;
import com.bank.transaction.model.entity.Transaction;
import com.bank.transaction.model.enums.TransactionStatus;
import com.bank.transaction.model.enums.TransactionType;
import com.bank.transaction.repository.TransactionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

/**
 * Service layer for Transaction operations
 * Implements business logic for deposits, withdrawals, payments, and charges
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class TransactionService {

    private final TransactionRepository transactionRepository;
    private final TransactionMapper transactionMapper;
    private final AccountClient accountClient;
    private final CreditClient creditClient;

    /**
     * Process a deposit to an account
     * @param request the deposit request
     * @return Mono of TransactionResponse
     */
    public Mono<TransactionResponse> processDeposit(DepositRequest request) {
        log.debug("Processing deposit of {} to account: {}", request.getAmount(), request.getAccountId());

        return accountClient.getAccount(request.getAccountId())
                .switchIfEmpty(Mono.error(new TransactionException("Account not found: " + request.getAccountId())))
                .flatMap(account -> {
                    String customerId = account.getCustomerId();
                    BigDecimal currentBalance = account.getBalance();
                    BigDecimal newBalance = currentBalance.add(request.getAmount());

                    Transaction transaction = Transaction.builder()
                            .transactionType(TransactionType.DEPOSIT)
                            .amount(request.getAmount())
                            .accountId(request.getAccountId())
                            .customerId(customerId)
                            .status(TransactionStatus.COMPLETED)
                            .description(request.getDescription())
                            .balanceAfter(newBalance)
                            .build();

                    return transactionRepository.save(transaction);
                })
                .doOnSuccess(t -> log.info("Deposit completed: {}", t.getId()))
                .map(transactionMapper::toResponse)
                .onErrorResume(e -> {
                    log.error("Deposit failed: {}", e.getMessage());
                    return saveFailedTransaction(TransactionType.DEPOSIT, request.getAmount(),
                            request.getAccountId(), null, null, e.getMessage())
                            .map(transactionMapper::toResponse);
                });
    }

    /**
     * Process a withdrawal from an account
     * @param request the withdrawal request
     * @return Mono of TransactionResponse
     */
    public Mono<TransactionResponse> processWithdrawal(WithdrawalRequest request) {
        log.debug("Processing withdrawal of {} from account: {}", request.getAmount(), request.getAccountId());

        return accountClient.getAccount(request.getAccountId())
                .switchIfEmpty(Mono.error(new TransactionException("Account not found: " + request.getAccountId())))
                .flatMap(account -> {
                    String customerId = account.getCustomerId();
                    BigDecimal currentBalance = account.getBalance();

                    if (currentBalance.compareTo(request.getAmount()) < 0) {
                        return Mono.error(new InsufficientFundsException(request.getAmount(), currentBalance));
                    }

                    BigDecimal newBalance = currentBalance.subtract(request.getAmount());

                    Transaction transaction = Transaction.builder()
                            .transactionType(TransactionType.WITHDRAWAL)
                            .amount(request.getAmount())
                            .accountId(request.getAccountId())
                            .customerId(customerId)
                            .status(TransactionStatus.COMPLETED)
                            .description(request.getDescription())
                            .balanceAfter(newBalance)
                            .build();

                    return transactionRepository.save(transaction);
                })
                .doOnSuccess(t -> log.info("Withdrawal completed: {}", t.getId()))
                .map(transactionMapper::toResponse)
                .onErrorResume(e -> {
                    log.error("Withdrawal failed: {}", e.getMessage());
                    return saveFailedTransaction(TransactionType.WITHDRAWAL, request.getAmount(),
                            request.getAccountId(), null, null, e.getMessage())
                            .map(transactionMapper::toResponse);
                });
    }

    /**
     * Process a payment to a credit
     * @param request the payment request
     * @return Mono of TransactionResponse
     */
    public Mono<TransactionResponse> processPayment(PaymentRequest request) {
        log.debug("Processing payment of {} to credit: {}", request.getAmount(), request.getCreditId());

        return creditClient.getCredit(request.getCreditId())
                .switchIfEmpty(Mono.error(new TransactionException("Credit not found: " + request.getCreditId())))
                .flatMap(credit -> {
                    String customerId = credit.getCustomerId();

                    return creditClient.makePayment(request.getCreditId(), request.getAmount(), request.getDescription())
                            .flatMap(updatedCredit -> {
                                BigDecimal newBalance = updatedCredit.getBalance();

                                Transaction transaction = Transaction.builder()
                                        .transactionType(TransactionType.PAYMENT)
                                        .amount(request.getAmount())
                                        .creditId(request.getCreditId())
                                        .customerId(customerId)
                                        .status(TransactionStatus.COMPLETED)
                                        .description(request.getDescription())
                                        .balanceAfter(newBalance)
                                        .build();

                                return transactionRepository.save(transaction);
                            });
                })
                .doOnSuccess(t -> log.info("Payment completed: {}", t.getId()))
                .map(transactionMapper::toResponse)
                .onErrorResume(e -> {
                    log.error("Payment failed: {}", e.getMessage());
                    return saveFailedTransaction(TransactionType.PAYMENT, request.getAmount(),
                            null, request.getCreditId(), null, e.getMessage())
                            .map(transactionMapper::toResponse);
                });
    }

    /**
     * Process a charge to a credit card
     * @param request the charge request
     * @return Mono of TransactionResponse
     */
    public Mono<TransactionResponse> processCharge(ChargeRequest request) {
        log.debug("Processing charge of {} to credit: {}", request.getAmount(), request.getCreditId());

        return creditClient.getCredit(request.getCreditId())
                .switchIfEmpty(Mono.error(new TransactionException("Credit not found: " + request.getCreditId())))
                .flatMap(credit -> {
                    String customerId = credit.getCustomerId();

                    return creditClient.makeCharge(request.getCreditId(), request.getAmount(), request.getDescription())
                            .flatMap(updatedCredit -> {
                                BigDecimal newBalance = updatedCredit.getBalance();

                                Transaction transaction = Transaction.builder()
                                        .transactionType(TransactionType.CHARGE)
                                        .amount(request.getAmount())
                                        .creditId(request.getCreditId())
                                        .customerId(customerId)
                                        .status(TransactionStatus.COMPLETED)
                                        .description(request.getDescription())
                                        .balanceAfter(newBalance)
                                        .build();

                                return transactionRepository.save(transaction);
                            });
                })
                .doOnSuccess(t -> log.info("Charge completed: {}", t.getId()))
                .map(transactionMapper::toResponse)
                .onErrorResume(e -> {
                    log.error("Charge failed: {}", e.getMessage());
                    return saveFailedTransaction(TransactionType.CHARGE, request.getAmount(),
                            null, request.getCreditId(), null, e.getMessage())
                            .map(transactionMapper::toResponse);
                });
    }

    /**
     * Find all transactions
     * @return Flux of TransactionResponse
     */
    public Flux<TransactionResponse> findAll() {
        log.debug("Finding all transactions");
        return transactionRepository.findAll()
                .map(transactionMapper::toResponse);
    }

    /**
     * Find transaction by ID
     * @param id the transaction id
     * @return Mono of TransactionResponse
     */
    public Mono<TransactionResponse> findById(String id) {
        log.debug("Finding transaction by id: {}", id);
        return transactionRepository.findById(id)
                .switchIfEmpty(Mono.error(new TransactionException("Transaction not found: " + id)))
                .map(transactionMapper::toResponse);
    }

    /**
     * Find all transactions by account ID
     * @param accountId the account id
     * @return Flux of TransactionResponse
     */
    public Flux<TransactionResponse> findByAccountId(String accountId) {
        log.debug("Finding transactions for account: {}", accountId);
        return transactionRepository.findByAccountId(accountId)
                .map(transactionMapper::toResponse);
    }

    /**
     * Find all transactions by credit ID
     * @param creditId the credit id
     * @return Flux of TransactionResponse
     */
    public Flux<TransactionResponse> findByCreditId(String creditId) {
        log.debug("Finding transactions for credit: {}", creditId);
        return transactionRepository.findByCreditId(creditId)
                .map(transactionMapper::toResponse);
    }

    /**
     * Find all transactions by customer ID
     * @param customerId the customer id
     * @return Flux of TransactionResponse
     */
    public Flux<TransactionResponse> findByCustomerId(String customerId) {
        log.debug("Finding transactions for customer: {}", customerId);
        return transactionRepository.findByCustomerId(customerId)
                .map(transactionMapper::toResponse);
    }

    /**
     * Save a failed transaction
     */
    private Mono<Transaction> saveFailedTransaction(TransactionType type, BigDecimal amount,
                                                    String accountId, String creditId, String customerId, String errorMessage) {

        Transaction transaction = Transaction.builder()
                .transactionType(type)
                .amount(amount)
                .accountId(accountId)
                .creditId(creditId)
                .customerId(customerId != null ? customerId : "unknown")
                .status(TransactionStatus.FAILED)
                .errorMessage(errorMessage)
                .build();

        return transactionRepository.save(transaction);
    }
}
