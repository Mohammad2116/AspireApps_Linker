package ir.aspireapps.linker.linksservice.service;

import ir.aspireapps.linker.linksservice.converter.LinkConverter;
import ir.aspireapps.linker.linksservice.dto.LinkRegisterRequest;
import ir.aspireapps.linker.linksservice.dto.LinkResponse;
import ir.aspireapps.linker.linksservice.dto.LinkUpdateStatusRequest;
import ir.aspireapps.linker.linksservice.model.Link;
import ir.aspireapps.linker.linksservice.model.LinkStatus;
import ir.aspireapps.linker.linksservice.repository.LinkRepository;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class LinkService {
    private final LinkRepository linksRepository;
    private final LinkConverter linkConverter;

    @Transactional
    public LinkResponse register(@Valid @NotNull LinkRegisterRequest request,
                                 @NotNull UUID userId) {
        Link newLink = Link.builder()
                .title(request.title())
                .originalUrl(request.url())
                .userId(userId)
                .expiresAt(request.expiresAt())
                .build();
        linkConverter.encode(newLink);
        newLink = linksRepository.save(newLink);
        return LinkResponse.builder()
                .id(newLink.getId())
                .title(newLink.getTitle())
                .originalUrl(newLink.getOriginalUrl())
                .shortUrl(newLink.getShortUrl())
                .userId(newLink.getUserId())
                .status(newLink.getStatus())
                .createdAt(newLink.getCreatedAt())
                .updatedAt(newLink.getUpdatedAt())
                .expiresAt(newLink.getExpiresAt())
                .build();

    }

    @Transactional
    public LinkResponse updateStatus(
            @Valid @NotNull LinkUpdateStatusRequest request,
            @NotNull UUID userId) {

        Link link = linksRepository.findByIdAndUserId(request.id(), userId)
                .orElseThrow(() -> new RuntimeException("Link not found"));
        if (link.getExpiresAt().isBefore(Instant.now()))
            link.setStatus(LinkStatus.EXPIRED);
        else
            link.setStatus(request.status());
        return LinkResponse.builder()
                .id(link.getId())
                .title(link.getTitle())
                .originalUrl(link.getOriginalUrl())
                .shortUrl(link.getShortUrl())
                .userId(link.getUserId())
                .status(link.getStatus())
                .createdAt(link.getCreatedAt())
                .updatedAt(link.getUpdatedAt())
                .expiresAt(link.getExpiresAt())
                .build();
    }

    @Transactional
    public LinkResponse details(
            @NotNull long id,
            @NotNull UUID userId) {
        Link link = linksRepository.findByIdAndUserId(id, userId)
                .orElseThrow(() -> new RuntimeException("Link not found"));
        if (link.getExpiresAt().isBefore(Instant.now()))
            link.setStatus(LinkStatus.EXPIRED);
        return LinkResponse.builder()
                .id(link.getId())
                .title(link.getTitle())
                .originalUrl(link.getOriginalUrl())
                .shortUrl(link.getShortUrl())
                .userId(link.getUserId())
                .status(link.getStatus())
                .createdAt(link.getCreatedAt())
                .updatedAt(link.getUpdatedAt())
                .expiresAt(link.getExpiresAt())
                .build();
    }

    public List<LinkResponse> userLinks(UUID userId) {
        return linksRepository.findUserLinks(userId);
    }

    @Transactional
    public void delete(@NotNull long linkId, @NotEmpty UUID userId) {
        Link link = linksRepository.findByIdAndUserId(linkId, userId)
                .orElseThrow(() -> new RuntimeException("Link not found"));
        linksRepository.delete(link);
    }

    @Transactional
    public void toggle(@NotNull long linkId, @NotEmpty UUID userId) {
        Link link = linksRepository.findByIdAndUserId(linkId, userId)
                .orElseThrow(() -> new RuntimeException("Link not found"));
        if (link.getExpiresAt().isBefore(Instant.now()))
            link.setStatus(LinkStatus.EXPIRED);
        else
            link.setStatus(toggleOf(link.getStatus()));
    }

    LinkStatus toggleOf(LinkStatus status) {
        if (status == LinkStatus.DISABLED)
            return LinkStatus.ACTIVE;
        return LinkStatus.DISABLED;
    }
}
