package ir.aspireapps.linker.linksservice.dto;

import ir.aspireapps.linker.common.model.HitState;
import ir.aspireapps.linker.linksservice.model.LinkStatus;
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
        HitState hitState,
        Instant createdAt,
        Instant updatedAt,
        Instant expiresAt
) {
    public LinkResponse(long id, String title, String originalUrl, String shortUrl, UUID userId, LinkStatus status, HitState hitState, Instant createdAt, Instant updatedAt, Instant expiresAt) {
        this.id = id;
        this.title = title;
        this.originalUrl = originalUrl;
        this.shortUrl = shortUrl;
        this.userId = userId;
        this.status = status;
        this.hitState = hitState;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
        this.expiresAt = expiresAt;
    }
}
