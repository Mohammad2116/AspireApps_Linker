package ir.aspireapps.linker.linksservice.dto;

import ir.aspireapps.linker.common.model.HitState;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class RedisLinkCache {
    private Long id;
    private String shortUrl;
    private String originalUrl;
    private HitState hitState;
    private Instant expiresAt;
}
