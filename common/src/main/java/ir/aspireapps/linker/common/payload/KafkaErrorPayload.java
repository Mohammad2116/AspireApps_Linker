package ir.aspireapps.linker.common.payload;

import java.time.Instant;

public record KafkaErrorPayload(
        Instant timestamp,
        int status,
        String code,
        String error,
        String message,
        String aggregateId,
        String sourceTopic
) {
}
