package ir.aspireapps.linker.analysisservice.kafka;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import ir.aspireapps.linker.analysisservice.service.AnalysisService;
import ir.aspireapps.linker.common.payload.LinkClickedPayload;
import ir.aspireapps.linker.common.payload.LinkDeletePayload;
import ir.aspireapps.linker.common.payload.LinkRegisteredPayload;
import ir.aspireapps.linker.common.utility.KafkaTopicsConstants;
import jakarta.annotation.Nonnull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Component
@RequiredArgsConstructor
public class MessageListener {
    private final AnalysisService analysisService;
    private final ObjectMapper objectMapper;

    @KafkaListener(topics = KafkaTopicsConstants.LINK_VISIT_TOPIC, groupId = "linker")
    @Transactional
    public void visitListener(
            @Nonnull ConsumerRecord<String, String> record) {
        LinkClickedPayload payload = null;
        try {
            payload = objectMapper.readValue(record.value(), LinkClickedPayload.class);
            log.info("Received Kafka message at topic: [{}], with payload: [{}]", KafkaTopicsConstants.LINK_VISIT_TOPIC, payload);
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
            log.info("Received Kafka message at topic: [{}], with payload: [{}]", KafkaTopicsConstants.LINK_REGISTERED_TOPIC, payload);
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
            log.info("Received Kafka message at topic: [{}], with payload: [{}]", KafkaTopicsConstants.LINK_DELETED_TOPIC, payload);
        } catch (Exception e) {
            log.error("Error parsing received Kafka Message at topic: [{}], with payload: [{}]",
                    KafkaTopicsConstants.LINK_DELETED_TOPIC, payload, e);
            throw new RuntimeException("Error parsing link-deleted-payload", e);
        }
        analysisService.delete(payload.shortUrl());
    }
}
