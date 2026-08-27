package ir.aspireapps.linker.common.payload;

import lombok.Builder;

@Builder
public record LinkDeletePayload(
        String shortUrl
) {
}
