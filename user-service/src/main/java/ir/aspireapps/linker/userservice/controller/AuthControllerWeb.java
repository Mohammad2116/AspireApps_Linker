package ir.aspireapps.linker.userservice.controller;

import ir.aspireapps.linker.userservice.dto.*;
import ir.aspireapps.linker.userservice.error.DuplicateResourceException;
import ir.aspireapps.linker.userservice.error.InvalidJwtToken;
import ir.aspireapps.linker.userservice.error.ResourceNotFoundException;
import ir.aspireapps.linker.userservice.form.UserLoginForm;
import ir.aspireapps.linker.userservice.form.UserRegisterForm;
import ir.aspireapps.linker.userservice.service.AuthService;
import ir.aspireapps.linker.userservice.utility.InputNormalizer;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import java.time.Duration;
import java.util.Arrays;

@Slf4j
@Controller
@RequestMapping("/ir/aspireapps/linker/auth/web/v1/")
@RequiredArgsConstructor
public class AuthControllerWeb {
    private final AuthService authService;

    private static String extractRefreshToken(HttpServletRequest servletRequest) {
        Cookie[] cookies = servletRequest.getCookies();
        String refreshToken = null;
        Cookie refCookie = null;
        if (cookies != null)
            refCookie = Arrays.stream(cookies)
                    .filter(cookie -> "REFRESH_TOKEN".equals(cookie.getName()))
                    .findFirst()
                    .orElse(null);
        if (refCookie != null)
            refreshToken = refCookie.getValue();
        return refreshToken;
    }

    private static void addAuthenticationToModel(Model model, boolean state) {
        model.addAttribute("AUTHENTICATED", state);
    }

    protected static void removeTokenCookies(HttpServletResponse servletResponse) {
        Cookie accessCookie = new Cookie("ACCESS_TOKEN", null);
        accessCookie.setHttpOnly(true);
        accessCookie.setSecure(true);
        accessCookie.setPath("/");
        accessCookie.setMaxAge(0);

        Cookie refreshCookie = new Cookie("REFRESH_TOKEN", null);
        refreshCookie.setHttpOnly(true);
        refreshCookie.setSecure(true);
        refreshCookie.setPath("/");
        refreshCookie.setMaxAge(0);

        servletResponse.addCookie(accessCookie);
        servletResponse.addCookie(refreshCookie);
    }

    @GetMapping("register")
    public String register(
            Model model,
            HttpServletRequest servletRequest,
            HttpServletResponse servletResponse) {
        model.addAttribute("registerForm", new UserRegisterForm());
        String refreshToken = extractRefreshToken(servletRequest);
        addAuthenticationToModel(model, false);
        log.info("Here in refresh refresh token is: {}", refreshToken);
        if (refreshToken != null) {
            if (authService.isRefreshTokenValid(refreshToken)) {
                log.info("token is valued to set model to view authenticated items");
                addAuthenticationToModel(model, true);
            } else {
                removeTokenCookies(servletResponse);
            }
        }
        return "register";
    }

    @PostMapping("register")
    public String registerProcess(
            @Valid @ModelAttribute("registerForm") UserRegisterForm userRegisterForm,
            BindingResult bindingResult,
            Model model,
            HttpServletRequest servletRequest,
            HttpServletResponse servletResponse) {
        String refreshToken = extractRefreshToken(servletRequest);
        if (refreshToken != null) {
            if (authService.isRefreshTokenValid(refreshToken)) {
                addAuthenticationToModel(model, true);
            }
        }
        if (bindingResult.hasFieldErrors()) {
            model.addAttribute("generalError", true);
            return "register";
        }
        userRegisterForm = InputNormalizer.normalize(userRegisterForm);
        AuthResponse authResponse;
        try {
            authResponse = authService.register(
                    UserRegisterRequest.builder()
                            .username(userRegisterForm.getUsername())
                            .email(userRegisterForm.getEmail())
                            .password(userRegisterForm.getPassword())
                            .passwordConfirm(userRegisterForm.getPasswordConfirm())
                            .build(),
                    servletRequest.getHeader("User-Agent"),
                    servletRequest.getRemoteUser());
        } catch (DuplicateResourceException e) {
            model.addAttribute("duplicateResourceException", true);
            return "register";
        }

        generateTokenCookies(servletResponse, authResponse);
        return "redirect:/ir/aspireapps/linker/user/web/v1/profile";
    }

    @GetMapping("login")
    public String login(
            @RequestParam(required = false) String returnUrl,
            Model model,
            HttpServletRequest servletRequest,
            HttpServletResponse servletResponse) {
        log.info("Starting to log in");
        String refreshToken = extractRefreshToken(servletRequest);
        if (refreshToken != null) {
            log.info("There is a refresh token in cookies, test it's validity");
            if (authService.isRefreshTokenValid(refreshToken)) {
                log.info("Refresh token is valid one to redirecting to profile page is consider as new target");
                return "redirect:/ir/aspireapps/linker/user/web/v1/profile";
            } else {
                log.info("Refresh token is expired or used or invalid, remove them all and continue to logging in");
                removeTokenCookies(servletResponse);
            }
        }

        model.addAttribute("loginForm", new UserLoginForm());
        model.addAttribute("returnUrl", returnUrl);
        return "login";
    }

    @PostMapping("login")
    public String loginProcess(
            @Valid @ModelAttribute("loginForm") UserLoginForm userLoginForm,
            BindingResult bindingResult,
            Model model,
            HttpServletRequest servletRequest,
            HttpServletResponse servletResponse
    ) {
        log.info("returnUrl = [{}]", userLoginForm.getReturnUrl());
        if (bindingResult.hasFieldErrors()) {
            model.addAttribute("generalError", true);
            return "login";
        }

        userLoginForm = InputNormalizer.normalize(userLoginForm);
        AuthResponse authResponse;
        try {
            authResponse = authService.login(
                    UserLoginRequest.builder()
                            .username(userLoginForm.getUsername())
                            .password(userLoginForm.getPassword())
                            .build(),
                    servletRequest.getHeader("User-Agent"),
                    servletRequest.getRemoteAddr());
        } catch (ResourceNotFoundException e) {
            model.addAttribute("generalError", true);
            return "login";
        }
        generateTokenCookies(servletResponse, authResponse);

        String returnUrl = userLoginForm.getReturnUrl();
        if (returnUrl == null || returnUrl.isBlank())
            return "redirect:/ir/aspireapps/linker/user/web/v1/profile";
        if (!returnUrl.startsWith("/"))
            returnUrl = "/" + returnUrl;
        log.info("Redirecting to: {}", returnUrl);
        return "redirect:" + userLoginForm.getReturnUrl();
    }

    @PostMapping("refresh")
    public ResponseEntity<AuthResponse> refresh(
            @NotNull @Valid @RequestBody UserRefreshRequest request,
            HttpServletRequest servletRequest,
            HttpServletResponse servletResponse) {
        AuthResponse authResponse;
        try {
            authResponse = authService.refresh(
                    request.refreshToken(),
                    servletRequest.getHeader("User-Agent"),
                    servletRequest.getRemoteAddr());
        } catch (InvalidJwtToken e) {
            return ResponseEntity
                    .status(HttpStatus.FORBIDDEN)
                    .body(null);
        }
        log.info("New AuthResponse create as below:");
        log.info(authResponse.toString());
        generateTokenCookies(servletResponse, authResponse);

        return ResponseEntity
                .status(HttpStatus.OK)
                .body(authResponse);
    }

    @GetMapping("logout")
    @PreAuthorize("hasAnyRole('ADMIN', 'USER')")
    public String logout(
            HttpServletRequest servletRequest,
            HttpServletResponse servletResponse) {
        String refreshToken = extractRefreshToken(servletRequest);
        try {
            authService.logout(refreshToken);
            removeTokenCookies(servletResponse);
        } catch (InvalidJwtToken e) {
            removeTokenCookies(servletResponse);
        }
        return "redirect:/ir/aspireapps/linker/home";
    }

    @PostMapping("logout/all")
    @PreAuthorize("hasAnyRole('ADMIN', 'USER')")
    public ResponseEntity<Void> logoutAll(
            @NotNull @Valid @RequestBody UserLogoutRequest request) {
        authService.logoutAll(request.refreshToken());
        return ResponseEntity
                .status(HttpStatus.ACCEPTED)
                .body(null);
    }

    private void generateTokenCookies(HttpServletResponse servletResponse, AuthResponse authResponse) {
        addTokenCookie(servletResponse,
                "ACCESS_TOKEN",
                authResponse.accessToken(),
                Duration.ofSeconds(authService.accessTokenExpireSeconds()));
        addTokenCookie(servletResponse,
                "REFRESH_TOKEN",
                authResponse.refreshToken(),
                Duration.ofSeconds(authService.refreshTokenExpireSeconds()));
    }

    private void addTokenCookie(HttpServletResponse servletResponse, String tokenName, String tokenValue, Duration duration) {
        ResponseCookie cookie = ResponseCookie.from(tokenName, tokenValue)
                .httpOnly(true)
                .secure(true)
                .sameSite("Lax")
                .path("/")
                .maxAge(duration)
                .build();
        servletResponse.addHeader(
                HttpHeaders.SET_COOKIE,
                cookie.toString()
        );
    }
}
