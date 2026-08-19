package ir.aspireapps.linker.common.dto;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;
import lombok.Builder;

@Builder
public record UserRefreshRequest(
        String returnUrl,
        @NotEmpty @Size(max = 512) String refreshToken
) {
}
