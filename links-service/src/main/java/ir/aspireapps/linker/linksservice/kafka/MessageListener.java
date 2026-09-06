package ir.aspireapps.linker.linksservice.kafka;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import ir.aspireapps.linker.common.payload.LinkClickedPayload;
import ir.aspireapps.linker.common.utility.KafkaTopicsConstants;
import ir.aspireapps.linker.linksservice.service.LinkService;
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
    private final LinkService linkService;
    private final ObjectMapper objectMapper;

    @KafkaListener(topics = KafkaTopicsConstants.POPULARITY_RESPONSE_TOPIC, groupId = "linker")
    @Transactional
    public void visitListener(
            @Nonnull ConsumerRecord<String, String> record) {
        LinkClickedPayload payload = null;
        try {
            payload = objectMapper.readValue(record.value(), LinkClickedPayload.class);
            log.info("Received Kafka message at topic: [{}], with payload: [{}]", KafkaTopicsConstants.POPULARITY_RESPONSE_TOPIC, payload);
        } catch (JsonProcessingException e) {
            log.error("Error parsing received Kafka Message at topic: [{}], with payload: [{}]",
                    KafkaTopicsConstants.LINK_VISIT_TOPIC, payload, e);
            throw new RuntimeException("Error parsing link-visit-payload");
        }
        linkService.updateHitState(payload);
    }
}
