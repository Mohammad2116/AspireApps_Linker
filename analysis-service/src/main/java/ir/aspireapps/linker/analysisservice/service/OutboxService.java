package ir.aspireapps.linker.analysisservice.service;

import ir.aspireapps.linker.analysisservice.model.OutboxMessage;
import ir.aspireapps.linker.analysisservice.repository.OutboxRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class OutboxService {
    private final OutboxRepository outboxRepository;

    public List<OutboxMessage> findLast10PendingMessages() {
        return outboxRepository.findLast10PendingOutboxMessages();
    }

    @Transactional
    public void register(long aggregateId, String topic, String payload) {
        OutboxMessage outboxMessage = new OutboxMessage();
        outboxMessage.setAggregateId(aggregateId);
        outboxMessage.setTopic(topic);
        outboxMessage.setPayload(payload);
        outboxRepository.save(outboxMessage);
    }
}
