package ir.aspireapps.linker.linksservice.controller;

import ir.aspireapps.linker.linksservice.dto.LinkRegisterRequest;
import ir.aspireapps.linker.linksservice.dto.LinkResponse;
import ir.aspireapps.linker.linksservice.dto.LinkUpdateStatusRequest;
import ir.aspireapps.linker.linksservice.service.LinkService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.ws.rs.HeaderParam;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/ir/aspireapps/linker/links/api/v1/**")
@RequiredArgsConstructor
public class LinkController {
    private final LinkService linkService;

    @PostMapping("register")
    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    public ResponseEntity<LinkResponse> register(
            @Valid @NotNull @RequestBody LinkRegisterRequest request,
            @NotEmpty @HeaderParam("X-USER-ID") String userId) {
        return ResponseEntity
                .status(HttpStatus.ACCEPTED)
                .body(
                        linkService.register(request, UUID.fromString(userId))
                );
    }

    @PutMapping("update/status")
    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    public ResponseEntity<LinkResponse> updateStatus(
            @Valid @NotNull @RequestBody LinkUpdateStatusRequest request,
            @NotEmpty @HeaderParam("X-USER-ID") String userId) {
        return ResponseEntity
                .status(HttpStatus.ACCEPTED)
                .body(
                        linkService.updateStatus(request, UUID.fromString(userId))
                );
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    public ResponseEntity<LinkResponse> details(
            @NotNull @RequestBody Long id,
            @NotEmpty @HeaderParam("X-USER-ID") String userId) {
        return ResponseEntity
                .status(HttpStatus.ACCEPTED)
                .body(
                        linkService.details(id, UUID.fromString(userId))
                );
    }

    @GetMapping("/user/links")
    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    public ResponseEntity<List<LinkResponse>> userLinks(
            @NotEmpty @RequestHeader("X-USER-ID") String userId) {
        return ResponseEntity
                .status(HttpStatus.OK)
                .body(
                        linkService.userLinks(UUID.fromString(userId))
                );
    }
}
