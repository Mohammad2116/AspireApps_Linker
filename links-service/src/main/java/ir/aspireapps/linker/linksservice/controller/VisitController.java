package ir.aspireapps.linker.linksservice.controller;

import ir.aspireapps.linker.linksservice.dto.RedirectResponse;
import ir.aspireapps.linker.linksservice.service.RedirectService;
import jakarta.validation.constraints.NotBlank;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class VisitController {
    private final RedirectService redirectService;

    @GetMapping("/ir/aspireapps/linker/links/api/v1/visit/{shorted}")
    public ResponseEntity<RedirectResponse> visit(
            @NotBlank @PathVariable String shorted) {
        RedirectResponse result = redirectService.visitApi(shorted);

        return ResponseEntity
                .status(result != null ? HttpStatus.FOUND : HttpStatus.NOT_FOUND)
                .body(result);
    }
}
