package ir.aspireapps.linker.common.dto;

import ir.aspireapps.linker.common.model.HitState;
import lombok.Builder;

@Builder
public record AnalysisResponse(
        String shortedUrl,
        HitState linkHitState
) {
}
