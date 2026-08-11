package ir.aspireapps.linker.userservice.service;

import ir.aspireapps.linker.userservice.model.RefreshToken;
import ir.aspireapps.linker.userservice.model.User;
import ir.aspireapps.linker.userservice.repository.RefreshTokenRepository;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

@Slf4j
@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class RefreshTokenService {
    private final TokenService tokenService;
    private final RefreshTokenRepository refreshTokenRepository;
    @Value("${security.jwt.refresh-token-expiration-ms}")
    private final long refreshTokenExpirationMs;

    @Transactional
    public String generateRefreshToken(User user, String deviceName, String deviceIp) {
        String rawToken = tokenService.generateSecureToken();
        String hashedToken = tokenService.hashToken(rawToken);

        RefreshToken refreshToken = RefreshToken.builder()
                .user(user)
                .tokenHash(hashedToken)
                .deviceName(deviceName)
                .deviceIp(deviceIp)
                .expiresAt(Instant.now().plusMillis(refreshTokenExpirationMs))
                .build();
        refreshTokenRepository.save(refreshToken);

        return rawToken;
    }

    @Transactional
    public Optional<RefreshToken> verifyToken(@NotEmpty @Size(max = 512) String token) {
        String hashedToken = tokenService.hashToken(token);
        RefreshToken refreshToken = refreshTokenRepository.findByTokenHash(hashedToken)
                .orElse(null);

        if (refreshToken == null) {
            log.error("Invalid Refresh Token");
            return Optional.empty();
        }

        if (refreshToken.getExpiresAt().isBefore(Instant.now())) {
            log.error("Expired Refresh Token");
            return Optional.empty();
        }

        if (refreshToken.isRevoked()) {
            log.error("Revoked Refresh Token");
            return Optional.empty();
        }

        refreshToken.revoke();
        return Optional.of(refreshToken);
    }

    @Transactional
    public void revokeAll(User user) {
        List<RefreshToken> tokens = refreshTokenRepository.findAllByUserAndNotRevoked(user);
        tokens.forEach(RefreshToken::revoke);
    }
}
