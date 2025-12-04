package com.bank.transaction.client;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import reactor.core.publisher.Mono;

import java.math.BigDecimal;
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
    public Mono<BigDecimal> calculateCommission(String accountId) {
        log.debug("Calculating commission for account: {}", accountId);

        return webClient.post()
                .uri("/api/accounts/{id}/calculate-commission", accountId)
                .retrieve()
                .bodyToMono(Map.class)
                .map(response -> {
                    Object commissionObj = response.get("commission");
                    if (commissionObj instanceof Number) {
                        return new BigDecimal(commissionObj.toString());
                    }
                    return BigDecimal.ZERO;
                })
                .doOnSuccess(commission ->
                        log.debug("Commission calculated for account {}: {}", accountId, commission))
                .onErrorResume(WebClientResponseException.class, ex -> {
                    log.error("Error calculating commission: {} - {}",
                            ex.getStatusCode(), ex.getMessage());
                    return Mono.just(BigDecimal.ZERO); // No comisión en caso de error
                });
    }

    /**
     * Get next transaction commission without applying it.
     *
     * @param accountId the account id
     * @return Mono with commission amount
     */
    public Mono<BigDecimal> getNextCommission(String accountId) {
        log.debug("Getting next commission for account: {}", accountId);

        return webClient.get()
                .uri("/api/accounts/{id}/commission", accountId)
                .retrieve()
                .bodyToMono(Map.class)
                .map(response -> {
                    Object commissionObj = response.get("nextTransactionCommission");
                    if (commissionObj instanceof Number) {
                        return new BigDecimal(commissionObj.toString());
                    }
                    return BigDecimal.ZERO;
                })
                .onErrorResume(ex -> Mono.just(BigDecimal.ZERO));
    }
}