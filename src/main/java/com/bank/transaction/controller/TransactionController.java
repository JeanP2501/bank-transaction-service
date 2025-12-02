package com.bank.transaction.controller;

import com.bank.transaction.model.dto.*;
import com.bank.transaction.service.TransactionService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

/**
 * REST Controller for Transaction operations
 * Provides endpoints for deposits, withdrawals, payments, and charges
 */
@Slf4j
@RestController
@RequestMapping("/api/transactions")
@RequiredArgsConstructor
public class TransactionController {

    private final TransactionService transactionService;

    /**
     * Process a deposit to an account
     * POST /api/transactions/deposit
     * @param request the deposit request
     * @return Mono of TransactionResponse with 201 status
     */
    @PostMapping("/deposit")
    public Mono<ResponseEntity<TransactionResponse>> deposit(@Valid @RequestBody DepositRequest request) {
        log.info("POST /api/transactions/deposit - Deposit of {} to account {}",
                request.getAmount(), request.getAccountId());
        return transactionService.processDeposit(request)
                .map(response -> ResponseEntity.status(HttpStatus.CREATED).body(response));
    }

    /**
     * Process a withdrawal from an account
     * POST /api/transactions/withdrawal
     * @param request the withdrawal request
     * @return Mono of TransactionResponse with 201 status
     */
    @PostMapping("/withdrawal")
    public Mono<ResponseEntity<TransactionResponse>> withdrawal(@Valid @RequestBody WithdrawalRequest request) {
        log.info("POST /api/transactions/withdrawal - Withdrawal of {} from account {}",
                request.getAmount(), request.getAccountId());
        return transactionService.processWithdrawal(request)
                .map(response -> ResponseEntity.status(HttpStatus.CREATED).body(response));
    }

    /**
     * Process a payment to a credit
     * POST /api/transactions/payment
     * @param request the payment request
     * @return Mono of TransactionResponse with 201 status
     */
    @PostMapping("/payment")
    public Mono<ResponseEntity<TransactionResponse>> payment(@Valid @RequestBody PaymentRequest request) {
        log.info("POST /api/transactions/payment - Payment of {} to credit {}",
                request.getAmount(), request.getCreditId());
        return transactionService.processPayment(request)
                .map(response -> ResponseEntity.status(HttpStatus.CREATED).body(response));
    }

    /**
     * Process a charge to a credit card
     * POST /api/transactions/charge
     * @param request the charge request
     * @return Mono of TransactionResponse with 201 status
     */
    @PostMapping("/charge")
    public Mono<ResponseEntity<TransactionResponse>> charge(@Valid @RequestBody ChargeRequest request) {
        log.info("POST /api/transactions/charge - Charge of {} to credit {}",
                request.getAmount(), request.getCreditId());
        return transactionService.processCharge(request)
                .map(response -> ResponseEntity.status(HttpStatus.CREATED).body(response));
    }

    /**
     * Get all transactions
     * GET /api/transactions
     * @return Flux of TransactionResponse with 200 status
     */
    @GetMapping
    public Mono<ResponseEntity<Flux<TransactionResponse>>> findAll() {
        log.info("GET /api/transactions - Fetching all transactions");
        return Mono.just(ResponseEntity.ok(transactionService.findAll()));
    }

    /**
     * Get transaction by ID
     * GET /api/transactions/{id}
     * @param id the transaction id
     * @return Mono of TransactionResponse with 200 status
     */
    @GetMapping("/{id}")
    public Mono<ResponseEntity<TransactionResponse>> findById(@PathVariable String id) {
        log.info("GET /api/transactions/{} - Fetching transaction by id", id);
        return transactionService.findById(id)
                .map(ResponseEntity::ok);
    }

    /**
     * Get all transactions by account ID
     * GET /api/transactions/account/{accountId}
     * @param accountId the account id
     * @return Flux of TransactionResponse with 200 status
     */
    @GetMapping("/account/{accountId}")
    public Mono<ResponseEntity<Flux<TransactionResponse>>> findByAccountId(@PathVariable String accountId) {
        log.info("GET /api/transactions/account/{} - Fetching transactions for account", accountId);
        return Mono.just(ResponseEntity.ok(transactionService.findByAccountId(accountId)));
    }

    /**
     * Get all transactions by credit ID
     * GET /api/transactions/credit/{creditId}
     * @param creditId the credit id
     * @return Flux of TransactionResponse with 200 status
     */
    @GetMapping("/credit/{creditId}")
    public Mono<ResponseEntity<Flux<TransactionResponse>>> findByCreditId(@PathVariable String creditId) {
        log.info("GET /api/transactions/credit/{} - Fetching transactions for credit", creditId);
        return Mono.just(ResponseEntity.ok(transactionService.findByCreditId(creditId)));
    }

    /**
     * Get all transactions by customer ID
     * GET /api/transactions/customer/{customerId}
     * @param customerId the customer id
     * @return Flux of TransactionResponse with 200 status
     */
    @GetMapping("/customer/{customerId}")
    public Mono<ResponseEntity<Flux<TransactionResponse>>> findByCustomerId(@PathVariable String customerId) {
        log.info("GET /api/transactions/customer/{} - Fetching transactions for customer", customerId);
        return Mono.just(ResponseEntity.ok(transactionService.findByCustomerId(customerId)));
    }
}
