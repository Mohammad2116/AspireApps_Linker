package ir.aspireapps.linker.userservice.feign;

import ir.aspireapps.linker.userservice.dto.LinksResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.List;

@FeignClient(name = "links-service")
public interface LinksServiceClient {
    @GetMapping("/api/aspireapps/linker/links/api/v1/user/links")
    List<LinksResponse> temp();
}
