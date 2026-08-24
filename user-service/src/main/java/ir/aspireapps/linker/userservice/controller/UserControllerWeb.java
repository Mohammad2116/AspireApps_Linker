package ir.aspireapps.linker.userservice.controller;

import ir.aspireapps.linker.common.dto.LinkRegisterRequest;
import ir.aspireapps.linker.common.dto.LinkResponse;
import ir.aspireapps.linker.userservice.dto.UserProfileResponse;
import ir.aspireapps.linker.userservice.error.ResourceNotFoundException;
import ir.aspireapps.linker.userservice.feign.LinksServiceClient;
import ir.aspireapps.linker.userservice.form.AddLinkForm;
import ir.aspireapps.linker.userservice.model.SubscriptionStatus;
import ir.aspireapps.linker.userservice.service.UserService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.time.ZoneId;

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
            HttpServletRequest servletRequest,
            HttpServletResponse servletResponse) {
        log.info("X-USERNAME: {}", username);
        log.info("X-USER-ROLES: {}", roles);

        UserProfileResponse user;
        try {
            user = userService.profile(username);
        } catch (ResourceNotFoundException e) {
            AuthControllerWeb.removeTokenCookies(servletResponse);
            return "redirect:/ir/aspireapps/linker/home";
        }

        model.addAttribute("profile", user);
        model.addAttribute("links", linksServiceClient.userLinks());
        model.addAttribute("AUTHENTICATED", true);
        return "profile";
    }

    @GetMapping("addLink")
    public String addLink(
            @NotEmpty @RequestHeader("X-USER-STATUS") String status,
            Model model,
            HttpServletRequest servletRequest) {
        if (!status.equals(SubscriptionStatus.PREMIUM.name()))
            model.addAttribute("freeAccount", "freeAccount");
        model.addAttribute("addLinkForm", new AddLinkForm());
        model.addAttribute("AUTHENTICATED", true);
        return "addLink";
    }

    @PostMapping("addLinkProcess")
    public String addLinkProcess(
            @NotEmpty @RequestHeader("X-USERNAME") String username,
            @NotEmpty @RequestHeader("X-USER-ROLES") String roles,
            @NotEmpty @RequestHeader("X-USER-STATUS") String status,
            @Valid @ModelAttribute AddLinkForm addLinkForm,
            Model model,
            HttpServletRequest servletRequest) {
        if (!status.equals(SubscriptionStatus.PREMIUM.name()))
            model.addAttribute("freeAccount", "freeAccount");

        LinkRegisterRequest request = LinkRegisterRequest.builder()
                .title(addLinkForm.getTitle())
                .url(addLinkForm.getOriginalUrl())
                .isActivated(addLinkForm.isStatus())
                .expiresAt(addLinkForm.getExpiresAt()
                        .atStartOfDay(ZoneId.systemDefault()).toInstant())
                .build();

        LinkResponse response = linksServiceClient.registerLink(request);

        UserProfileResponse user = userService.profile(username);

        model.addAttribute("profile", user);
        model.addAttribute("links", linksServiceClient.userLinks());
        model.addAttribute("AUTHENTICATED", true);
        return "profile";
    }

    @GetMapping("delete/{linkId}")
    public String deleteLinkProcess(@Valid @PathVariable long linkId,
                                    @NotEmpty @RequestHeader("X-USERNAME") String username,
                                    Model model,
                                    HttpServletRequest servletRequest) {
        linksServiceClient.deleteLink(linkId);

        UserProfileResponse user = userService.profile(username);

        model.addAttribute("profile", user);
        model.addAttribute("links", linksServiceClient.userLinks());
        model.addAttribute("AUTHENTICATED", true);
        return "profile";
    }

    @GetMapping("toggle/{linkId}")
    public String toggleLinkProcess(@Valid @PathVariable long linkId,
                                    @NotEmpty @RequestHeader("X-USERNAME") String username,
                                    Model model,
                                    HttpServletRequest servletRequest) {
        linksServiceClient.toggleLink(linkId);

        UserProfileResponse user = userService.profile(username);

        model.addAttribute("profile", user);
        model.addAttribute("links", linksServiceClient.userLinks());
        model.addAttribute("AUTHENTICATED", true);
        return "profile";
    }
}
