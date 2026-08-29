package ir.aspireapps.linker.linksservice.dto;

import ir.aspireapps.linker.common.model.HitState;
import lombok.Builder;

@Builder
public record RedirectResponse(
        Long id,
        String originalUrl,
        HitState hitState
) {

    public RedirectResponse(Long id, String originalUrl, HitState hitState) {
        this.id = id;
        this.originalUrl = originalUrl;
        this.hitState = hitState;
    }
}
