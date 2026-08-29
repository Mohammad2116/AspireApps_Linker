package ir.aspireapps.linker.linksservice.service;

import ir.aspireapps.linker.common.model.HitState;
import ir.aspireapps.linker.linksservice.dto.RedisLinkCache;
import jakarta.validation.constraints.NotBlank;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;

@Service
@RequiredArgsConstructor
public class RedisLinkCacheService {
    private final RedisTemplate<String, RedisLinkCache> redisTemplate;

    public void setWithTtl(String key, RedisLinkCache value) {
        int ttlSeconds = switch (value.getHitState()) {
            case LOW -> 5;
            case NORMAL -> 30;
            case HIGH -> 120;
            case VERY_HIGH -> 300;
        };
        redisTemplate.opsForValue().set(key, value, Duration.ofSeconds(ttlSeconds));
    }

    public RedisLinkCache get(String key) {
        return redisTemplate.opsForValue().get(key);
    }

    public void evict(String key) {
        redisTemplate.delete(key);
    }

//    public boolean exists(String key) {
//        return redisTemplate.hasKey(key);
//    }

    public void refreshTtl(@NotBlank String shortUrl, HitState hitState) {
        int ttlSeconds = switch (hitState) {
            case LOW -> 5;
            case NORMAL -> 30;
            case HIGH -> 120;
            case VERY_HIGH -> 300;
        };
        redisTemplate.expire(shortUrl, Duration.ofSeconds(ttlSeconds));
    }
}
