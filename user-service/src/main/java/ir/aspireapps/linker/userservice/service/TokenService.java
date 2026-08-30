package ir.aspireapps.linker.userservice.service;

import ir.aspireapps.linker.common.utility.LoggingEvents;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.Base64;

@Slf4j
@Component
public class TokenService {
    public String generateSecureToken() {
        byte[] randomizedToke = new byte[64];
        SecureRandom secureRandom = new SecureRandom();
        secureRandom.nextBytes(randomizedToke);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(randomizedToke);
    }

    public String hashToken(String token) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(token.getBytes());
            return Base64.getUrlEncoder().withoutPadding().encodeToString(hash);
        } catch (NoSuchAlgorithmException e) {
            log.error("{} - Hash Algorithm Not Found", LoggingEvents.INTERNAL_SERVER_ERROR, e);
            throw new RuntimeException(e);
        }
    }
}
