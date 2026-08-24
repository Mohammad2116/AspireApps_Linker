package ir.aspireapps.linker.linksservice.service;

import ir.aspireapps.linker.linksservice.dto.RedirectResponse;
import ir.aspireapps.linker.linksservice.model.LinkStatus;
import ir.aspireapps.linker.linksservice.repository.LinkRepository;
import jakarta.validation.constraints.NotBlank;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class RedirectService {
    private final LinkRepository linkRepository;

    public RedirectResponse visitApi(@NotBlank String shorted) {
        return linkRepository.findByShortedAndStatus(shorted, LinkStatus.ACTIVE)
                .orElseThrow(() -> new RuntimeException("Shorted url not found"));
    }
}
