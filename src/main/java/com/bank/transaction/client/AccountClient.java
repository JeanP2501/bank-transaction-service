package com.bank.transaction.client;

import com.bank.transaction.exception.ServiceUnavailableException;
import com.bank.transaction.model.dto.AccBalanceUpdRequest;
import com.bank.transaction.model.dto.AccountResponse;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.retry.annotation.Retry;
import io.github.resilience4j.timelimiter.annotation.TimeLimiter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import reactor.core.publisher.Mono;

import javax.security.auth.login.AccountNotFoundException;
import java.math.BigDecimal;
import java.time.Duration;
import java.util.Map;

/**
 * Client for communicating with Account Service
 */
@Slf4j
@Component
public class AccountClient {

    private final WebClient webClient;

    public AccountClient(WebClient.Builder webClientBuilder,
                         @Value("${account.service.url}") String accountServiceUrl) {
        this.webClient = webClientBuilder
                .baseUrl(accountServiceUrl)
                .build();
    }

    /**
     * Get account by ID from Account Service
     * @param accountId the account id
     * @return Mono of AccountResponse
     */
    @CircuitBreaker(name = "accountService", fallbackMethod = "getAccountFallback")
    @Retry(name = "accountService")
    @TimeLimiter(name = "accountService")
    public Mono<AccountResponse> getAccount(String accountId) {
        log.debug("Calling Account Service to get account with id: {}", accountId);

        return webClient.get()
                .uri("/api/accounts/{id}", accountId)
                .retrieve()
                .onStatus(status -> status.value() == 404,
                        response -> Mono.error(new AccountNotFoundException(accountId)))
                .bodyToMono(AccountResponse.class)
                .timeout(Duration.ofSeconds(2))
                .doOnSuccess(account -> log.debug("Account found: {}", account.getId()))
                .doOnError(WebClientResponseException.class, ex -> {
                    log.error("Error calling Account Service: {} - {}", ex.getStatusCode(), ex.getMessage());
                });
    }

    /**
     * Fallback method when circuit is open or service fails
     */
    private Mono<AccountResponse> getAccountFallback(String accountId, Exception ex) {
        log.warn("Circuit breaker activated for getAccount. AccountId: {}. Reason: {}",
                accountId, ex.getClass().getSimpleName());

        // Si es AccountNotFoundException, propagarla (no es fallo del servicio)
        if (ex instanceof AccountNotFoundException) {
            return Mono.error(ex);
        }

        // Para otros errores, retornar error de servicio no disponible
        return Mono.error(new ServiceUnavailableException(
                "Account service is currently unavailable. Please try again later."));
    }

    @Retry(name = "accountService")
    @TimeLimiter(name = "accountService")
    public Mono<AccountResponse> updateBalance(String accountId, AccBalanceUpdRequest req) {
        log.debug("Calling Account Service to update balance with id: {}", accountId);

        return webClient.put()
                .uri("/api/accounts/balance/{id}", accountId)
                .bodyValue(req)
                .retrieve()
                .onStatus(status -> status.value() == 404,
                        response -> Mono.error(new AccountNotFoundException(accountId)))
                .bodyToMono(AccountResponse.class)
                .timeout(Duration.ofSeconds(2))
                .doOnSuccess(account -> log.debug("Balance updated for account: {}", account.getId()))
                .doOnError(WebClientResponseException.class, ex -> {
                    log.error("Error calling Account Service: {} - {}", ex.getStatusCode(), ex.getMessage());
                });
    }

}
