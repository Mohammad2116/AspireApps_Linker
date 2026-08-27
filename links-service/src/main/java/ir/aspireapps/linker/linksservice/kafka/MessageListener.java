package ir.aspireapps.linker.linksservice.kafka;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import ir.aspireapps.linker.common.payload.LinkClickedPayload;
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

    @KafkaListener(topics = "popularity-response-topic", groupId = "linker")
    @Transactional
    public void visitListener(
            @Nonnull ConsumerRecord<String, String> record) {
        LinkClickedPayload payload;
        try {
            payload = objectMapper.readValue(record.value(), LinkClickedPayload.class);
        } catch (JsonProcessingException e) {
            log.error("Error parsing link-visit-payload", e);
            throw new RuntimeException("Error parsing link-visit-payload");
        }
        linkService.updateHitState(payload);
    }
}
