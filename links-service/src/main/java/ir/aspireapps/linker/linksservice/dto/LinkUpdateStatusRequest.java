package ir.aspireapps.linker.linksservice.dto;

import ir.aspireapps.linker.common.model.LinkStatus;
import jakarta.validation.constraints.NotNull;

public record LinkUpdateStatusRequest(
        @NotNull
        Long id,
        @NotNull
        LinkStatus status
) {
}
