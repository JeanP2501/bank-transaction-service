package com.bank.transaction.client;

import com.bank.transaction.model.dto.AccountResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import reactor.core.publisher.Mono;

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
    public Mono<AccountResponse> getAccount(String accountId) {
        log.debug("Calling Account Service to get account with id: {}", accountId);

        return webClient.get()
                .uri("/api/accounts/{id}", accountId)
                .retrieve()
                .bodyToMono(AccountResponse.class)
                .doOnSuccess(account -> log.debug("Account found: {}", account.getId()))
                .doOnError(WebClientResponseException.class, ex -> {
                    log.error("Error calling Account Service: {} - {}", ex.getStatusCode(), ex.getMessage());
                })
                .onErrorResume(WebClientResponseException.NotFound.class, ex -> {
                    log.warn("Account not found with id: {}", accountId);
                    return Mono.empty();
                });
    }
}
