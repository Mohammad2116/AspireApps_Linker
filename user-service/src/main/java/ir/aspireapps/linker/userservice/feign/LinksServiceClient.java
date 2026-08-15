package ir.aspireapps.linker.userservice.feign;

import ir.aspireapps.linker.common.dto.LinkResponse;
import ir.aspireapps.linker.userservice.config.FeignConfiguration;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.List;

@FeignClient(
        name = "links-service",
        configuration = FeignConfiguration.class
)
public interface LinksServiceClient {
    @GetMapping("/ir/aspireapps/linker/links/api/v1/user/links")
    List<LinkResponse> userLinks();
}
