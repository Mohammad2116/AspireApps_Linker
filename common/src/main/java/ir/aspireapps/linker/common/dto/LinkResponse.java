package ir.aspireapps.linker.common.dto;

import ir.aspireapps.linker.common.model.LinkStatus;
import lombok.Builder;

import java.time.Instant;
import java.util.UUID;

@Builder
public record LinkResponse(
        long id,
        String title,
        String originalUrl,
        String shortUrl,
        UUID userId,
        LinkStatus status,
        Instant createdAt,
        Instant updatedAt,
        Instant expiresAt
) {
    public LinkResponse(long id, String title, String originalUrl, String shortUrl, UUID userId, LinkStatus status, Instant createdAt, Instant updatedAt, Instant expiresAt) {
        this.id = id;
        this.title = title;
        this.originalUrl = originalUrl;
        this.shortUrl = shortUrl;
        this.userId = userId;
        this.status = status;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
        this.expiresAt = expiresAt;
    }
}
