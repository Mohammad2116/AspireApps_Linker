package ir.aspireapps.linker.userservice.feign;

import ir.aspireapps.linker.common.dto.LinkRegisterRequest;
import ir.aspireapps.linker.common.dto.LinkResponse;
import ir.aspireapps.linker.userservice.config.FeignConfiguration;
import jakarta.validation.constraints.NotNull;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@FeignClient(
        name = "links-service",
        configuration = FeignConfiguration.class
)
public interface LinksServiceClient {
    @GetMapping("/ir/aspireapps/linker/links/api/v1/user/links")
    List<LinkResponse> userLinks();

    @PostMapping("/ir/aspireapps/linker/links/api/v1/register")
    LinkResponse registerLink(@NotNull LinkRegisterRequest request);

    @DeleteMapping("/ir/aspireapps/linker/links/api/v1/delete/{linkId}")
    void deleteLink(@NotNull @PathVariable long linkId);

    @PutMapping("/ir/aspireapps/linker/links/api/v1/toggle/{linkId}")
    void toggleLink(@NotNull @PathVariable long linkId);
}
