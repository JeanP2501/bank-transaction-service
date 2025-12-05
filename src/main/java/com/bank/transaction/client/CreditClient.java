package com.bank.transaction.client;

import com.bank.transaction.exception.CreditNotFoundException;
import com.bank.transaction.exception.ServiceUnavailableException;
import com.bank.transaction.model.dto.CreditResponse;
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
import java.util.HashMap;
import java.util.Map;

/**
 * Client for communicating with Credit Service
 */
@Slf4j
@Component
public class CreditClient {

    private final WebClient webClient;

    public CreditClient(WebClient.Builder webClientBuilder,
                        @Value("${credit.service.url}") String creditServiceUrl) {
        this.webClient = webClientBuilder
                .baseUrl(creditServiceUrl)
                .build();
    }

    /**
     * Get credit by ID from Credit Service
     * @param creditId the credit id
     * @return Mono of CreditResponse
     */
    @CircuitBreaker(name = "creditService", fallbackMethod = "getCreditFallback")
    @Retry(name = "creditService")
    @TimeLimiter(name = "creditService")
    public Mono<CreditResponse> getCredit(String creditId) {
        log.debug("Calling Credit Service to get credit with id: {}", creditId);

        return webClient.get()
                .uri("/api/credits/{id}", creditId)
                .retrieve()
                .onStatus(status -> status.value() == 404,
                        response -> Mono.error(new CreditNotFoundException(creditId)))
                .bodyToMono(CreditResponse.class)
                .timeout(Duration.ofSeconds(2))
                .doOnSuccess(credit -> log.debug("Credit found: {}", credit.getId()))
                .doOnError(ex -> {
                    log.error("Error calling Credit Service for credit {}: {}",
                            creditId, ex.getMessage());
                });
    }

    /**
     * Fallback for getCredit
     */
    private Mono<CreditResponse> getCreditFallback(String creditId, Exception ex) {
        log.warn("Circuit breaker activated for getCredit. CreditId: {}. Reason: {}",
                creditId, ex.getClass().getSimpleName());

        // Si es CreditNotFoundException, propagarla
        if (ex instanceof CreditNotFoundException) {
            return Mono.error(ex);
        }

        return Mono.error(new ServiceUnavailableException(
                "Credit service is currently unavailable. Please try again later."));
    }

    /**
     * Make payment to credit
     * @param creditId the credit id
     * @param amount payment amount
     * @param description payment description
     * @return Mono of CreditResponse
     */
    @CircuitBreaker(name = "creditService", fallbackMethod = "makePaymentFallback")
    @Retry(name = "creditService")
    @TimeLimiter(name = "creditService")
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
                .timeout(Duration.ofSeconds(5)) // Más tiempo para operaciones de escritura
                .doOnSuccess(credit ->
                        log.debug("Payment successful. New balance: {}", credit.getBalance()))
                .doOnError(ex -> {
                    log.error("Error making payment to credit {}: {}", creditId, ex.getMessage());
                });
    }

    /**
     * Fallback for makePayment
     */
    private Mono<CreditResponse> makePaymentFallback(String creditId, BigDecimal amount,
                                                     String description, Exception ex) {
        log.error("Circuit breaker activated for makePayment. CreditId: {}, Amount: {}. Reason: {}",
                creditId, amount, ex.getClass().getSimpleName());

        return Mono.error(new ServiceUnavailableException(
                "Credit payment service is currently unavailable. Payment not processed."));
    }

    /**
     * Make charge to credit card
     * @param creditId the credit id
     * @param amount charge amount
     * @param description charge description
     * @return Mono of CreditResponse
     */
    @CircuitBreaker(name = "creditService", fallbackMethod = "makeChargeFallback")
    @Retry(name = "creditService")
    @TimeLimiter(name = "creditService")
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
                .timeout(Duration.ofSeconds(2))
                .doOnSuccess(credit ->
                        log.debug("Charge successful. New balance: {}", credit.getBalance()))
                .doOnError(ex -> {
                    log.error("Error making charge to credit {}: {}", creditId, ex.getMessage());
                });
    }

    /**
     * Fallback for makeCharge
     */
    private Mono<CreditResponse> makeChargeFallback(String creditId, BigDecimal amount,
                                                    String description, Exception ex) {
        log.error("Circuit breaker activated for makeCharge. CreditId: {}, Amount: {}. Reason: {}",
                creditId, amount, ex.getClass().getSimpleName());

        return Mono.error(new ServiceUnavailableException(
                "Credit charge service is currently unavailable. Charge not processed."));
    }
}
