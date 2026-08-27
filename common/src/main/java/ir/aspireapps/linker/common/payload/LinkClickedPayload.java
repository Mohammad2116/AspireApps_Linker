package ir.aspireapps.linker.common.payload;

import ir.aspireapps.linker.common.model.HitState;
import lombok.Builder;

@Builder
public record LinkClickedPayload(
        String shortedUrl,
        HitState currentHitState
) {
    public LinkClickedPayload(String shortedUrl, HitState currentHitState) {
        this.shortedUrl = shortedUrl;
        this.currentHitState = currentHitState;
    }
}

