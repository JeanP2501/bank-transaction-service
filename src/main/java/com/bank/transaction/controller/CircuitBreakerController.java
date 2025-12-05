package com.bank.transaction.controller;

import io.github.resilience4j.circuitbreaker.CircuitBreakerRegistry;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

import java.util.HashMap;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/api/circuit-breaker")
@RequiredArgsConstructor
public class CircuitBreakerController {

    private final CircuitBreakerRegistry circuitBreakerRegistry;

    @GetMapping("/status/{name}")
    public Mono<Map<String, Object>> getCircuitBreakerStatus(@PathVariable String name) {
        return Mono.fromCallable(() -> {
            var circuitBreaker = circuitBreakerRegistry.circuitBreaker(name);
            var metrics = circuitBreaker.getMetrics();

            Map<String, Object> status = new HashMap<>();
            status.put("name", name);
            status.put("state", circuitBreaker.getState().toString());
            status.put("failureRate", metrics.getFailureRate());
            status.put("numberOfBufferedCalls", metrics.getNumberOfBufferedCalls());
            status.put("numberOfFailedCalls", metrics.getNumberOfFailedCalls());
            status.put("numberOfSuccessfulCalls", metrics.getNumberOfSuccessfulCalls());

            return status;
        });
    }

    @GetMapping("/status")
    public Mono<Map<String, Object>> getAllCircuitBreakers() {
        return Mono.fromCallable(() -> {
            Map<String, Object> allStatus = new HashMap<>();

            circuitBreakerRegistry.getAllCircuitBreakers().forEach(cb -> {
                var metrics = cb.getMetrics();
                Map<String, Object> cbStatus = new HashMap<>();
                cbStatus.put("state", cb.getState().toString());
                cbStatus.put("failureRate", metrics.getFailureRate());
                cbStatus.put("numberOfBufferedCalls", metrics.getNumberOfBufferedCalls());

                allStatus.put(cb.getName(), cbStatus);
            });

            return allStatus;
        });
    }
}