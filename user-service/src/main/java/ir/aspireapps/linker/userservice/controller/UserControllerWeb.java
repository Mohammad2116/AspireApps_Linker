package ir.aspireapps.linker.userservice.controller;

import ir.aspireapps.linker.userservice.dto.UserProfileResponse;
import ir.aspireapps.linker.userservice.service.UserService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.constraints.NotEmpty;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/ir/aspireapps/linker/user/web/v1/")
@RequiredArgsConstructor
@PreAuthorize("hasAnyRole('ADMIN', 'USER')")
public class UserControllerWeb {
    private static final Logger log = LoggerFactory.getLogger(UserControllerWeb.class);
    private final UserService userService;

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
        return "profile";
    }
}
