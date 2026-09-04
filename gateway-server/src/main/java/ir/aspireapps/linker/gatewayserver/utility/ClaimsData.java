package ir.aspireapps.linker.gatewayserver.utility;

import lombok.Builder;

import java.util.List;

@Builder
public record ClaimsData(
        String username,
        String userId,
        String status,
        List<String> rolesNames
) {
}
