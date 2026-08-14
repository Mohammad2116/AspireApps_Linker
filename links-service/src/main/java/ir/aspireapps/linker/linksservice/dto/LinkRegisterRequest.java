package ir.aspireapps.linker.linksservice.dto;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.Instant;

public record LinkRegisterRequest(
        @NotEmpty @Size(min = 1, max = 254)
        String title,

        @NotEmpty @Size(min = 3, max = 1024)
        String url,

        @NotNull
        boolean isActivated,

        @NotNull
        Instant expiresAt
) {
}
