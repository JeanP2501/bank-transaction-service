package com.bank.transaction.client;

import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.retry.annotation.Retry;
import io.github.resilience4j.timelimiter.annotation.TimeLimiter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import reactor.core.publisher.Mono;

import java.math.BigDecimal;
import java.time.Duration;
import java.util.Map;

/**
 * Client for communicating with Account Service - Commission endpoints.
 */
@Slf4j
@Component
public class CommissionClient {

    private final WebClient webClient;

    public CommissionClient(WebClient.Builder webClientBuilder,
                            @Value("${account.service.url}") String accountServiceUrl) {
        this.webClient = webClientBuilder
                .baseUrl(accountServiceUrl)
                .build();
    }

    /**
     * Calculate and apply commission for a transaction.
     * This will increment the transaction counter and return the commission.
     *
     * @param accountId the account id
     * @return Mono with commission amount
     */
    @CircuitBreaker(name = "accountService", fallbackMethod = "calculateCommissionFallback")
    @Retry(name = "accountService")
    @TimeLimiter(name = "accountService")
    public Mono<BigDecimal> calculateCommission(String accountId) {
        log.debug("Calculating commission for account: {}", accountId);

        return webClient.post()
                .uri("/api/accounts/{id}/calculate-commission", accountId)
                .retrieve()
                .bodyToMono(Map.class)
                .timeout(Duration.ofSeconds(2))
                .map(response -> {
                    Object commissionObj = response.get("commission");
                    if (commissionObj instanceof Number) {
                        return new BigDecimal(commissionObj.toString());
                    }
                    return BigDecimal.ZERO;
                })
                .doOnSuccess(commission ->
                        log.debug("Commission calculated for account {}: {}", accountId, commission))
                .doOnError(ex -> {
                    log.error("Error calculating commission for account {}: {}",
                            accountId, ex.getMessage());
                });
    }

    /**
     * Fallback for calculateCommission
     */
    private Mono<BigDecimal> calculateCommissionFallback(String accountId, Exception ex) {
        log.warn("Circuit breaker activated for calculateCommission. AccountId: {}. Returning zero commission",
                accountId);
        return Mono.just(BigDecimal.ZERO);
    }

    /**
     * Get next transaction commission without applying it.
     *
     * @param accountId the account id
     * @return Mono with commission amount
     */
    @CircuitBreaker(name = "accountService", fallbackMethod = "getNextCommissionFallback")
    @Retry(name = "accountService")
    @TimeLimiter(name = "accountService")
    public Mono<BigDecimal> getNextCommission(String accountId) {
        log.debug("Getting next commission for account: {}", accountId);

        return webClient.get()
                .uri("/api/accounts/{id}/commission", accountId)
                .retrieve()
                .bodyToMono(Map.class)
                .timeout(Duration.ofSeconds(2))
                .map(response -> {
                    Object commissionObj = response.get("nextTransactionCommission");
                    if (commissionObj instanceof Number) {
                        return new BigDecimal(commissionObj.toString());
                    }
                    return BigDecimal.ZERO;
                })
                .doOnError(ex -> {
                    log.error("Error getting next commission for account {}: {}",
                            accountId, ex.getMessage());
                });
    }

    /**
     * Fallback for getNextCommission
     */
    private Mono<BigDecimal> getNextCommissionFallback(String accountId, Exception ex) {
        log.warn("Circuit breaker activated for getNextCommission. AccountId: {}. Returning zero",
                accountId);
        return Mono.just(BigDecimal.ZERO);
    }

}