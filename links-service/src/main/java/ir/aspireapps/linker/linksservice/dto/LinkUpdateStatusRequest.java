package ir.aspireapps.linker.linksservice.dto;

import ir.aspireapps.linker.linksservice.model.LinkStatus;

public record LinkUpdateStatusRequest(
        Long id,
        LinkStatus status
) {
}
