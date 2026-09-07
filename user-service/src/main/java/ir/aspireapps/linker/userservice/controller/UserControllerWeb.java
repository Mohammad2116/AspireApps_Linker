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

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;

@Slf4j
@Controller
@RequestMapping("/ir/aspireapps/linker/user/web/v1/")
@RequiredArgsConstructor
@PreAuthorize("hasAnyRole('ADMIN', 'USER')")
public class UserControllerWeb {
    private final UserService userService;
    private final LinksServiceClient linksServiceClient;
    private final AuthControllerWeb authControllerWeb;

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
            authControllerWeb.removeTokenCookies(servletResponse);
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
            @NotEmpty @RequestHeader(HeaderConstants.X_USER_STATE) String status,
            Model model,
            HttpServletRequest servletRequest) {
        if (!status.equals(SubscriptionStatus.PREMIUM.name()))
            model.addAttribute("freeAccount", "freeAccount");
        model.addAttribute("addLinkForm", new AddLinkForm());
        model.addAttribute("AUTHENTICATED", true);
        model.addAttribute("minExpiresAt", LocalDateTime.now().plusMinutes(1)
                .format(DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss")));
        return "addLink";
    }

    @PostMapping("addLinkProcess")
    public String addLinkProcess(
            @NotEmpty @RequestHeader(HeaderConstants.X_USERNAME) String username,
            @NotEmpty @RequestHeader(HeaderConstants.X_USER_ROLES) String roles,
            @NotEmpty @RequestHeader(HeaderConstants.X_USER_STATE) String status,
            @Valid @ModelAttribute AddLinkForm addLinkForm,
            Model model,
            HttpServletRequest servletRequest) {
        if (!status.equals(SubscriptionStatus.PREMIUM.name())) {
            model.addAttribute("freeAccount", "freeAccount");
            trimFreeAccountDate(addLinkForm);
        }

        LinkRegisterRequest request = LinkRegisterRequest.builder()
                .title(addLinkForm.getTitle())
                .url(addLinkForm.getOriginalUrl())
                .isActivated(addLinkForm.isStatus())
                .expiresAt(addLinkForm.getExpiresAt().atZone(ZoneId.systemDefault()).toInstant())
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

    private void trimFreeAccountDate(@Valid AddLinkForm addLinkForm) {
        long minSeconds = 5 * 60;
        long maxSeconds = 7 * 24 * 60 * 60;

        LocalDateTime now = LocalDateTime.now();
        long currentDiffSec =
                Duration.between(now, addLinkForm.getExpiresAt()).toSeconds();

        if (currentDiffSec < minSeconds) {
            addLinkForm.setExpiresAt(now.plusSeconds(minSeconds));
        }

        if (currentDiffSec > maxSeconds) {
            addLinkForm.setExpiresAt(now.plusSeconds(maxSeconds));
        }
    }

    @GetMapping("delete/{linkId}")
    public String deleteLinkProcess(@Valid @PathVariable long linkId,
                                    @NotEmpty @RequestHeader(HeaderConstants.X_USERNAME) String username,
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
                                    @NotEmpty @RequestHeader(HeaderConstants.X_USERNAME) String username,
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
