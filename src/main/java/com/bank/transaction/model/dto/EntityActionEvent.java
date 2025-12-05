package com.bank.transaction.model.dto;

import lombok.*;
import lombok.experimental.SuperBuilder;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class EntityActionEvent<T> extends BaseEvent {
    private String entityType;
    private T payload;

    public static <T> EntityActionEvent<T> of(String aggregateId, T payload) {
        EntityActionEvent<T> event = new EntityActionEvent<>();
        event.setEventId(UUID.randomUUID().toString());
        event.setEventType("ENTITY_CREATED");
        event.setTimestamp(LocalDateTime.now());
        event.setPayload(payload);
        return event;
    }

}