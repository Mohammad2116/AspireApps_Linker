package ir.aspireapps.linker.common.payload;

import lombok.Builder;

import java.time.Instant;

@Builder
public record KafkaErrorPayload(
        Instant timestamp,
        int status,
        String code,
        String error,
        String message,
        String aggregateId,
        String sourceTopic
) {
    public KafkaErrorPayload(Instant timestamp, int status, String code, String error, String message, String aggregateId, String sourceTopic) {
        this.timestamp = timestamp;
        this.status = status;
        this.code = code;
        this.error = error;
        this.message = message;
        this.aggregateId = aggregateId;
        this.sourceTopic = sourceTopic;
    }
}
