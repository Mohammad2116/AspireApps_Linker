package ir.aspireapps.linker.userservice.dto;

import ir.aspireapps.linker.userservice.model.SubscriptionStatus;
import ir.aspireapps.linker.userservice.model.UserRole;

import java.time.Instant;

public record UserProfileResponse(
        String username,
        String email,
        UserRole role,
        SubscriptionStatus status,
        Instant createdAt,
        boolean enabled,
        boolean emailVerified,
        Instant lockeUntil
) {
}
