package ir.aspireapps.linker.analysisservice.kafka;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import ir.aspireapps.linker.analysisservice.service.AnalysisService;
import ir.aspireapps.linker.analysisservice.service.OutboxService;
import ir.aspireapps.linker.common.error.ResourceNotFoundException;
import ir.aspireapps.linker.common.payload.KafkaErrorPayload;
import ir.aspireapps.linker.common.payload.LinkClickedPayload;
import ir.aspireapps.linker.common.payload.LinkDeletePayload;
import ir.aspireapps.linker.common.payload.LinkRegisteredPayload;
import ir.aspireapps.linker.common.utility.KafkaTopicsConstants;
import jakarta.annotation.Nonnull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.http.HttpStatus;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;

@Slf4j
@Component
@RequiredArgsConstructor
public class MessageListener {
    private final AnalysisService analysisService;
    private final ObjectMapper objectMapper;
    private final OutboxService outboxService;

    @KafkaListener(topics = KafkaTopicsConstants.LINK_VISIT_TOPIC, groupId = "linker")
    @Transactional
    public void visitListener(
            @Nonnull ConsumerRecord<String, String> record) {
        LinkClickedPayload payload = null;
        try {
            payload = objectMapper.readValue(record.value(), LinkClickedPayload.class);
            log.info("a) Received Kafka message at topic: [{}], with payload: [{}]", KafkaTopicsConstants.LINK_VISIT_TOPIC, payload);
            analysisService.clicked(payload);
        } catch (ResourceNotFoundException e) {
            try {
                outboxService.register(
                        0L,
                        KafkaTopicsConstants.ANALYTICS_ERROR_TOPIC,
                        objectMapper.writeValueAsString(
                                KafkaErrorPayload.builder()
                                        .timestamp(Instant.now())
                                        .status(HttpStatus.NOT_FOUND.value())
                                        .code(HttpStatus.NOT_FOUND.getReasonPhrase())
                                        .error("SHORTED_URL_NOT_FOUND")
                                        .message(e.getMessage())
                                        .sourceTopic(KafkaTopicsConstants.LINK_VISIT_TOPIC)
                                        .aggregateId(payload != null ? payload.shortedUrl() : "")
                                        .build()
                        )
                );
            } catch (JsonProcessingException ex) {
                log.error("Error parsing received Kafka Message at topic: [{}], with payload: [{}]",
                        KafkaTopicsConstants.LINK_VISIT_TOPIC, payload, ex);
                throw new RuntimeException("Error parsing received Kafka Message at topic: " + payload, ex);
            }
        } catch (JsonProcessingException e) {
            log.error("Error parsing received Kafka Message at topic: [{}], with payload: [{}]",
                    KafkaTopicsConstants.LINK_VISIT_TOPIC, payload, e);
            throw new RuntimeException("Error parsing received Kafka Message at topic: " + payload);
        }
        analysisService.clicked(payload);
    }

    @KafkaListener(topics = KafkaTopicsConstants.LINK_REGISTERED_TOPIC, groupId = "linker")
    @Transactional
    public void registeredListener(
            @Nonnull ConsumerRecord<String, String> record) {
        LinkRegisteredPayload payload = null;
        try {
            payload = objectMapper.readValue(record.value(), LinkRegisteredPayload.class);
            log.info("b) Received Kafka message at topic: [{}], with payload: [{}]", KafkaTopicsConstants.LINK_REGISTERED_TOPIC, payload);
            analysisService.register(payload);
        } catch (ResourceNotFoundException e) {
            try {
                outboxService.register(
                        0L,
                        KafkaTopicsConstants.ANALYTICS_ERROR_TOPIC,
                        objectMapper.writeValueAsString(
                                KafkaErrorPayload.builder()
                                        .timestamp(Instant.now())
                                        .status(HttpStatus.CONFLICT.value())
                                        .code(HttpStatus.CONFLICT.getReasonPhrase())
                                        .error("SHORTED_URL_ALREADY_REGISTERED")
                                        .message(e.getMessage())
                                        .sourceTopic(KafkaTopicsConstants.LINK_REGISTERED_TOPIC)
                                        .aggregateId(payload != null ? payload.shortedUrl() : "")
                                        .build()
                        )
                );
            } catch (JsonProcessingException ex) {
                log.error("Error parsing received Kafka Message at topic: [{}], with payload: [{}]",
                        KafkaTopicsConstants.LINK_REGISTERED_TOPIC, payload, ex);
                throw new RuntimeException("Error parsing received Kafka Message at topic: " + payload, ex);
            }
        } catch (JsonProcessingException e) {
            log.error("Error parsing received Kafka Message at topic: [{}], with payload: [{}]",
                    KafkaTopicsConstants.LINK_REGISTERED_TOPIC, payload, e);
            throw new RuntimeException("Error parsing link-registered-payload");
        }
        analysisService.register(payload);
    }

    @KafkaListener(topics = KafkaTopicsConstants.LINK_DELETED_TOPIC, groupId = "linker")
    @Transactional
    public void deletedListener(
            @Nonnull ConsumerRecord<String, String> record) {
        LinkDeletePayload payload = null;
        try {
            payload = objectMapper.readValue(record.value(), LinkDeletePayload.class);
            log.info("c) Received Kafka message at topic: [{}], with payload: [{}]", KafkaTopicsConstants.LINK_DELETED_TOPIC, payload);
            analysisService.delete(payload.shortUrl());
        } catch (ResourceNotFoundException e) {
            try {
                outboxService.register(
                        0L,
                        KafkaTopicsConstants.ANALYTICS_ERROR_TOPIC,
                        objectMapper.writeValueAsString(
                                KafkaErrorPayload.builder()
                                        .timestamp(Instant.now())
                                        .status(HttpStatus.NOT_FOUND.value())
                                        .code(HttpStatus.NOT_FOUND.getReasonPhrase())
                                        .error("SHORTED_URL_NOT_FOUND")
                                        .message(e.getMessage())
                                        .sourceTopic(KafkaTopicsConstants.LINK_DELETED_TOPIC)
                                        .aggregateId(payload != null ? payload.shortUrl() : "")
                                        .build()
                        )
                );
            } catch (JsonProcessingException ex) {
                log.error("Error parsing received Kafka Message at topic: [{}], with payload: [{}]",
                        KafkaTopicsConstants.LINK_DELETED_TOPIC, payload, ex);
                throw new RuntimeException("Error parsing received Kafka Message at topic: " + payload, ex);
            }
        } catch (JsonProcessingException e) {
            log.error("Error parsing received Kafka Message at topic: [{}], with payload: [{}]",
                    KafkaTopicsConstants.LINK_DELETED_TOPIC, payload, e);
            throw new RuntimeException("Error parsing link-deleted-payload", e);
        }
        analysisService.delete(payload.shortUrl());
    }
}
