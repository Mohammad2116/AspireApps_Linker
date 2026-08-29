package ir.aspireapps.linker.linksservice.service;

import ir.aspireapps.linker.common.model.LinkStatus;
import ir.aspireapps.linker.linksservice.dto.RedirectResponse;
import ir.aspireapps.linker.linksservice.dto.RedisLinkCache;
import ir.aspireapps.linker.linksservice.model.Link;
import ir.aspireapps.linker.linksservice.repository.LinkRepository;
import jakarta.validation.constraints.NotBlank;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.Instant;

@Slf4j
@Service
@RequiredArgsConstructor
public class RedirectService {
    private final LinkRepository linkRepository;
    private final OutboxService outboxService;
    private final RedisLinkCacheService redisLinkCacheService;
    private final LinkService linkService;

    public RedirectResponse visitApi(@NotBlank String shortUrl) {
        RedirectResponse redirectResponse;
        Instant expirationDate;
        boolean cached = true;
        RedisLinkCache linkCache = redisLinkCacheService.get(shortUrl);
        if (linkCache != null) {
            redirectResponse = new RedirectResponse(linkCache.getId(), linkCache.getOriginalUrl(), linkCache.getHitState());
            expirationDate = linkCache.getExpiresAt();
        } else {
            Link link = linkRepository.findByShortUrlAndStatus(shortUrl, LinkStatus.ACTIVE)
                    .orElseThrow(() -> new RuntimeException("shortUrl not found"));
            redirectResponse = new RedirectResponse(link.getId(), link.getOriginalUrl(), link.getHitState());
            expirationDate = link.getExpiresAt();
            cached = false;
        }
        if (expirationDate.isBefore(Instant.now()))
            linkService.checkExpiration(redirectResponse.id());

        outboxService.visit(shortUrl, redirectResponse);
        if (!cached)
            redisLinkCacheService.setWithTtl(
                    shortUrl,
                    new RedisLinkCache(redirectResponse.id(),
                            shortUrl,
                            redirectResponse.originalUrl(),
                            redirectResponse.hitState(),
                            expirationDate));
        else
            redisLinkCacheService.refreshTtl(shortUrl, redirectResponse.hitState());
        return redirectResponse;
    }
}
