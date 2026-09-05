package ir.aspireapps.linker.userservice.controller;

import ir.aspireapps.linker.common.dto.LinkRegisterRequest;
import ir.aspireapps.linker.common.dto.LinkResponse;
import ir.aspireapps.linker.common.utility.HeaderConstants;
import ir.aspireapps.linker.common.utility.LoggingEvents;
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
            @NotEmpty @RequestHeader(HeaderConstants.X_USERNAME) String username,
            @NotEmpty @RequestHeader(HeaderConstants.X_USER_ROLES) String roles,
            Model model,
            HttpServletResponse servletResponse) {

        UserProfileResponse user;
        try {
            user = userService.profile(username);
        } catch (ResourceNotFoundException e) {
            AuthControllerWeb.removeTokenCookies(servletResponse);
            log.error("User with username[{}] didn't exists in database, remove all Auth cookies and tokens then redirect to to home page ", username);
            return "redirect:/linker/home";
        }

        log.info("{} - Calling links-service from FeignServer to collect user's links", LoggingEvents.EXTERNAL_SERVICE_CALL);

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

        log.info("{} - Calling links-service from FeignServer to register new link", LoggingEvents.EXTERNAL_SERVICE_CALL);

        LinkResponse response = linksServiceClient.registerLink(request);
        if (response == null)
            log.warn("{} - Registering new link at links-service failed", LoggingEvents.EXTERNAL_SERVICE_ERROR);
        else {
            log.info("{} - Registering new link at links-server succeeds", LoggingEvents.LINK_CREATED);
        }

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
        log.info("{} - Calling links-service from FeignServer to delete new link", LoggingEvents.EXTERNAL_SERVICE_CALL);
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
        log.info("{} - Calling links-service from FeignServer to toggle new link", LoggingEvents.EXTERNAL_SERVICE_CALL);
        linksServiceClient.toggleLink(linkId);

        UserProfileResponse user = userService.profile(username);

        model.addAttribute("profile", user);
        model.addAttribute("links", linksServiceClient.userLinks());
        model.addAttribute("AUTHENTICATED", true);
        return "profile";
    }
}
