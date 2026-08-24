package ir.aspireapps.linker.linksservice.dto;

import lombok.Builder;

@Builder
public record RedirectResponse(
        String originalUrl
) {
    public RedirectResponse(String originalUrl) {
        this.originalUrl = originalUrl;
    }
}
