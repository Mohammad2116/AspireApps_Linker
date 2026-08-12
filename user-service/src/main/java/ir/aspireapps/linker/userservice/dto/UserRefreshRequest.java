package ir.aspireapps.linker.userservice.dto;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;

public record UserRefreshRequest(
        @NotEmpty @Size(max = 512) String refreshToken
) {
}
