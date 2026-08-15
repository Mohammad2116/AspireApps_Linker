package ir.aspireapps.linker.userservice.controller;

import ir.aspireapps.linker.userservice.dto.UserProfileResponse;
import ir.aspireapps.linker.userservice.feign.LinksServiceClient;
import ir.aspireapps.linker.userservice.service.UserService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.constraints.NotEmpty;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;

@Slf4j
@Controller
@RequestMapping("/ir/aspireapps/linker/user/web/v1/")
@RequiredArgsConstructor
@PreAuthorize("hasAnyRole('ADMIN', 'USER')")
public class UserControllerWeb {
    private final UserService userService;
    private final LinksServiceClient linksServiceClient;

    @GetMapping("profile")
    public String profile(
            @NotEmpty @RequestHeader("X-USERNAME") String username,
            @NotEmpty @RequestHeader("X-USER-ROLES") String roles,
            Model model,
            HttpServletRequest servletRequest) {
        log.info("X-USERNAME: {}", username);
        log.info("X-USER-ROLES: {}", roles);

        UserProfileResponse user = userService.profile(username);

        model.addAttribute("profile", user);
        model.addAttribute("links", linksServiceClient.userLinks());
        return "profile";
    }
}
