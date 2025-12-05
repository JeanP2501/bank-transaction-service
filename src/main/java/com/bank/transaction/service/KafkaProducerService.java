package com.bank.transaction.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;
import reactor.kafka.sender.KafkaSender;
import reactor.kafka.sender.SenderRecord;

import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class KafkaProducerService {

    private final KafkaSender<String, String> kafkaSender;
    private final ObjectMapper objectMapper;

    @Value("${kafka.topics.transaction-events}")
    private String accountEventsTopic;

    public <T> Mono<Void> sendEvent(String key, T event) {
        return Mono.fromCallable(() -> objectMapper.writeValueAsString(event))
                .flatMap(jsonEvent -> {
                    ProducerRecord<String, String> record =
                            new ProducerRecord<>(accountEventsTopic, key, jsonEvent);

                    return kafkaSender.send(Mono.just(SenderRecord.create(record, UUID.randomUUID())))
                            .doOnNext(result -> log.info("Event sent - Topic: {}, Key: {}",
                                    accountEventsTopic, key))
                            .doOnError(error -> log.error("Error sending event: {}",
                                    error.getMessage()))
                            .then();
                })
                .onErrorResume(JsonProcessingException.class, e -> {
                    log.error("Error serializing event: {}", e.getMessage());
                    return Mono.error(e);
                });
    }
}