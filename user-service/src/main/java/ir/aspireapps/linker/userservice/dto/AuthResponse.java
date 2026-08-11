package ir.aspireapps.linker.userservice.dto;

import lombok.Builder;

@Builder
public record AuthResponse(
        String accessToken,
        long accessTokenExpiresInSeconds,
        String refreshToken
) {
}
