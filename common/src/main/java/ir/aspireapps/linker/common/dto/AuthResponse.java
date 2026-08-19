package ir.aspireapps.linker.common.dto;

import lombok.Builder;

@Builder
public record AuthResponse(
        String accessToken,
        long accessTokenExpiresInSeconds,
        String refreshToken
) {
}
