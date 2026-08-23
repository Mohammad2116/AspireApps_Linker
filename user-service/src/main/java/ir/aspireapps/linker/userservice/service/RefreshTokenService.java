package ir.aspireapps.linker.userservice.service;

import ir.aspireapps.linker.userservice.error.InvalidJwtToken;
import ir.aspireapps.linker.userservice.model.RefreshToken;
import ir.aspireapps.linker.userservice.model.User;
import ir.aspireapps.linker.userservice.repository.RefreshTokenRepository;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;

@Slf4j
@Service
@Transactional(readOnly = true)
public class RefreshTokenService {

    private final long refreshTokenExpirationMs;
    private final TokenService tokenService;
    private final RefreshTokenRepository refreshTokenRepository;

    public RefreshTokenService(
            @Value("${security.jwt.refresh-token-expiration-ms}") long refreshTokenExpirationMs,
            TokenService tokenService,
            RefreshTokenRepository refreshTokenRepository) {
        this.refreshTokenRepository = refreshTokenRepository;
        this.refreshTokenExpirationMs = refreshTokenExpirationMs;
        this.tokenService = tokenService;
    }

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
    public RefreshToken verifyToken(@NotEmpty @Size(max = 512) String token) {
        String hashedToken = tokenService.hashToken(token);
        RefreshToken refreshToken = refreshTokenRepository.findByTokenHash(hashedToken)
                .orElse(null);

        if (refreshToken == null) {
            log.error("Invalid Refresh Token");
            throw new InvalidJwtToken("Invalid refresh Token");
        }

        if (refreshToken.getExpiresAt().isBefore(Instant.now())) {
            log.error("Expired Refresh Token");
            throw new InvalidJwtToken("Expired refresh token used");
        }

        if (refreshToken.isRevoked()) {
            log.error("Revoked Refresh Token");
            throw new InvalidJwtToken("Revoked refresh token used");
        }

        refreshToken.revoke();
        return refreshToken;
    }

    @Transactional
    public void revokeAll(User user) {
        List<RefreshToken> tokens = refreshTokenRepository.findAllByUserAndRevokedFalse(user);
        tokens.forEach(RefreshToken::revoke);
    }

    public long refreshTokenExpirationSeconds() {
        return refreshTokenExpirationMs / 1000;
    }
}
