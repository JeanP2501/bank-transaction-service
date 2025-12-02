package com.bank.transaction.client;

import com.bank.transaction.model.dto.CreditResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import reactor.core.publisher.Mono;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

/**
 * Client for communicating with Credit Service
 */
@Slf4j
@Component
public class CreditClient {

    private final WebClient webClient;

    public CreditClient(@Value("${credit.service.url}") String creditServiceUrl) {
        this.webClient = WebClient.builder()
                .baseUrl(creditServiceUrl)
                .build();
    }

    /**
     * Get credit by ID from Credit Service
     * @param creditId the credit id
     * @return Mono of CreditResponse
     */
    public Mono<CreditResponse> getCredit(String creditId) {
        log.debug("Calling Credit Service to get credit with id: {}", creditId);

        return webClient.get()
                .uri("/api/credits/{id}", creditId)
                .retrieve()
                .bodyToMono(CreditResponse.class)
                .doOnSuccess(credit -> log.debug("Credit found: {}", credit.getId()))
                .doOnError(WebClientResponseException.class, ex -> {
                    log.error("Error calling Credit Service: {} - {}", ex.getStatusCode(), ex.getMessage());
                })
                .onErrorResume(WebClientResponseException.NotFound.class, ex -> {
                    log.warn("Credit not found with id: {}", creditId);
                    return Mono.empty();
                });
    }

    /**
     * Make payment to credit
     * @param creditId the credit id
     * @param amount payment amount
     * @param description payment description
     * @return Mono of CreditResponse
     */
    public Mono<CreditResponse> makePayment(String creditId, BigDecimal amount, String description) {
        log.debug("Making payment of {} to credit: {}", amount, creditId);

        Map<String, Object> paymentData = new HashMap<>();
        paymentData.put("amount", amount);
        paymentData.put("description", description);

        return webClient.post()
                .uri("/api/credits/{id}/payment", creditId)
                .bodyValue(paymentData)
                .retrieve()
                .bodyToMono(CreditResponse.class)
                .doOnSuccess(credit -> log.debug("Payment successful. New balance: {}", credit.getBalance()));
    }

    /**
     * Make charge to credit card
     * @param creditId the credit id
     * @param amount charge amount
     * @param description charge description
     * @return Mono of CreditResponse
     */
    public Mono<CreditResponse> makeCharge(String creditId, BigDecimal amount, String description) {
        log.debug("Making charge of {} to credit: {}", amount, creditId);

        Map<String, Object> chargeData = new HashMap<>();
        chargeData.put("amount", amount);
        chargeData.put("description", description);

        return webClient.post()
                .uri("/api/credits/{id}/charge", creditId)
                .bodyValue(chargeData)
                .retrieve()
                .bodyToMono(CreditResponse.class)
                .doOnSuccess(credit -> log.debug("Charge successful. New balance: {}", credit.getBalance()));
    }
}
