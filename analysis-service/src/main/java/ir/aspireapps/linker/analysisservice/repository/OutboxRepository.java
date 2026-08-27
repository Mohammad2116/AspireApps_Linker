package ir.aspireapps.linker.analysisservice.repository;

import ir.aspireapps.linker.analysisservice.model.OutboxMessage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.UUID;

public interface OutboxRepository extends JpaRepository<OutboxMessage, UUID> {
    @Query(
            value = """
                        SELECT * FROM outbox_messages
                        WHERE status = 'PENDING'
                        ORDER BY created_at ASC
                        LIMIT 10
                        FOR UPDATE SKIP LOCKED
                    """, nativeQuery = true
    )
    public List<OutboxMessage> findLast10PendingOutboxMessages();
}
