package ir.aspireapps.linker.linksservice.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import ir.aspireapps.linker.common.payload.LinkClickedPayload;
import ir.aspireapps.linker.common.payload.LinkDeletePayload;
import ir.aspireapps.linker.common.payload.LinkRegisteredPayload;
import ir.aspireapps.linker.linksservice.dto.RedirectResponse;
import ir.aspireapps.linker.linksservice.model.Link;
import ir.aspireapps.linker.linksservice.model.OutboxMessage;
import ir.aspireapps.linker.linksservice.repository.OutboxRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class OutboxService {
    private final OutboxRepository outboxRepository;
    private final ObjectMapper objectMapper;

    public List<OutboxMessage> findLast10PendingMessages() {
        return outboxRepository.findLast10PendingOutboxMessages();
    }

    @Transactional
    public void register(Link link) {
        LinkRegisteredPayload payload = LinkRegisteredPayload.builder()
                .shortedUrl(link.getShortUrl())
                .build();
        String payloadString;
        try {
            payloadString = objectMapper.writeValueAsString(payload);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
        OutboxMessage outboxMessage = new OutboxMessage();
        outboxMessage.setAggregateId(link.getId());
        outboxMessage.setTopic("link-registered-topic");
        outboxMessage.setPayload(payloadString);
        outboxRepository.save(outboxMessage);
    }

    @Transactional
    public void visit(String shortUrl, RedirectResponse redirectResponse) {
        OutboxMessage outboxMessage = new OutboxMessage();
        outboxMessage.setAggregateId(redirectResponse.id());
        outboxMessage.setTopic("link-visit-topic");
        try {
            outboxMessage.setPayload(
                    objectMapper.writeValueAsString(
                            LinkClickedPayload.builder()
                                    .shortedUrl(shortUrl)
                                    .currentHitState(redirectResponse.hitState())
                                    .build()
                    )
            );
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Could not serialize link clicked payload" + e);
        }
        outboxRepository.save(outboxMessage);
    }

    public void delete(Link link) {
        OutboxMessage outboxMessage = new OutboxMessage();
        outboxMessage.setAggregateId(link.getId());
        outboxMessage.setTopic("link-deleted-topic");
        try {
            outboxMessage.setPayload(
                    objectMapper.writeValueAsString(
                            LinkDeletePayload.builder()
                                    .shortUrl(link.getShortUrl())
                                    .build()
                    ));
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
        outboxRepository.save(outboxMessage);
    }
}
