package ir.aspireapps.linker.analysisservice.kafka;

import ir.aspireapps.linker.analysisservice.model.OutboxMessage;
import ir.aspireapps.linker.analysisservice.repository.OutboxRepository;
import ir.aspireapps.linker.common.model.EventStatus;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.Message;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@Component
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class MessageScheduler {
    private final OutboxRepository outboxRepository;
    private final KafkaTemplate<String, String> kafkaTemplate;

    @Scheduled(fixedRate = 1000)
    @Transactional
    public void schedule() {
        List<OutboxMessage> outboxMessages = outboxRepository.findLast10PendingOutboxMessages();
        for (OutboxMessage outboxMessage : outboxMessages) {
            Message<String> message = MessageBuilder
                    .withPayload(outboxMessage.getPayload())
                    .setHeader(KafkaHeaders.TOPIC, outboxMessage.getTopic())
                    .setHeader(KafkaHeaders.KEY, outboxMessage.getAggregateId().toString())
                    .setHeader("schemaVersion", "1")
                    .build();
            try {
                kafkaTemplate.send(message).get();
            } catch (Exception e) {
                log.info("Error while sending kafka message");
                throw new RuntimeException(e);
            }
            outboxMessage.setStatus(EventStatus.PROCEED);
        }
    }
}
