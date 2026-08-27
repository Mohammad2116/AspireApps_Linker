package ir.aspireapps.linker.common.payload;

import lombok.Builder;

@Builder
public record LinkRegisteredPayload(
        String shortedUrl
) {
    public LinkRegisteredPayload(String shortedUrl) {
        this.shortedUrl = shortedUrl;
    }
}
