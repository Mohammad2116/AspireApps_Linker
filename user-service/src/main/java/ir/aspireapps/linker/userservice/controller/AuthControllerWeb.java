package ir.aspireapps.linker.userservice.controller;

import ir.aspireapps.linker.common.utility.LoggingEvents;
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
import org.springframework.beans.factory.annotation.Value;
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
    @Value("${app.security.cookies-security")
    private static String cookiesSecure;

    private final AuthService authService;

    private static String extractRefreshToken(HttpServletRequest servletRequest) {
        Cookie[] cookies = servletRequest.getCookies();
        if (cookies != null) {
            Cookie cookie = Arrays.stream(servletRequest.getCookies())
                    .filter(c -> c.getName().equals("REFRESH_TOKEN"))
                    .findFirst().orElse(null);
            if (cookie != null)
                return cookie.getValue();
        }
        return null;
    }

    private static void addAuthenticationToModel(Model model, boolean state) {
        model.addAttribute("AUTHENTICATED", state);
    }

    protected static void removeTokenCookies(HttpServletResponse servletResponse) {
        Cookie accessCookie = new Cookie("ACCESS_TOKEN", null);
        accessCookie.setHttpOnly(true);
        accessCookie.setSecure(Boolean.parseBoolean(cookiesSecure));
        accessCookie.setPath("/");
        accessCookie.setMaxAge(0);

        Cookie refreshCookie = new Cookie("REFRESH_TOKEN", null);
        refreshCookie.setHttpOnly(true);
        refreshCookie.setSecure(Boolean.parseBoolean(cookiesSecure));
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
        log.debug("Check request cookies for valid refresh token");
        if (refreshToken != null) {
            if (authService.isRefreshTokenValid(refreshToken)) {
                log.debug("Refresh token is valid, so view already logged in items on page");
                addAuthenticationToModel(model, true);
            } else {
                log.debug("Refresh token is not valid(revoked, expired or invalid cookies)");
                log.info("Removing all auth cookies");
                removeTokenCookies(servletResponse);
            }
        } else
            log.debug("There is no refresh cookie available");
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
            } else {
                removeTokenCookies(servletResponse);
            }
        }
        if (bindingResult.hasFieldErrors()) {
            model.addAttribute("generalError", true);
            log.warn("{} - Binding error occurred at registration page, return to register page with error message", LoggingEvents.USER_REGISTRATION_FAILED);
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
            log.error("{} - Duplicate user registration attempt, return to register page", LoggingEvents.USER_REGISTRATION_FAILED, e);
            model.addAttribute("duplicateResourceException", true);
            return "register";
        }

        generateTokenCookies(servletResponse, authResponse);
        log.info("{} - User registration complete, redirecting to profile page", LoggingEvents.USER_REGISTERED);
        return "redirect:/linker/user/web/v1/profile";
    }

    @GetMapping("login")
    public String login(
            @RequestParam(required = false) String returnUrl,
            Model model,
            HttpServletRequest servletRequest,
            HttpServletResponse servletResponse) {

        String refreshToken = extractRefreshToken(servletRequest);
        if (refreshToken != null) {
            log.info("Check validity of refresh token");
            if (authService.isRefreshTokenValid(refreshToken)) {
                log.info("{} - Refresh token is valid redirecting to profile page is consider as new target",
                        LoggingEvents.AUTH_LOGIN_SUCCESS);
                return "redirect:/linker/user/web/v1/profile";
            } else {
                log.warn("Refresh token is expired or used or invalid, remove it and continue to logging in page");
                removeTokenCookies(servletResponse);
            }
        } else
            log.warn("There is no refresh cookie available in header, so redirect to login page");

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
            log.warn("{} - Binding error occurred at login page, return to login page with error message", LoggingEvents.AUTH_LOGIN_FAILED);
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
            log.info("{} - Authentication using login form was successful", LoggingEvents.AUTH_LOGIN_SUCCESS);
        } catch (ResourceNotFoundException e) {
            model.addAttribute("generalError", true);
            log.error("{} - Something went wrong while try to create AuthResponse using login form data" +
                    ", return to login page", LoggingEvents.AUTH_LOGIN_FAILED);
            return "login";
        }
        generateTokenCookies(servletResponse, authResponse);

        String returnUrl = userLoginForm.getReturnUrl();
        if (returnUrl == null || returnUrl.isBlank()) {
            log.info("{} - login using login form was successful, no redirect request so goto profile", LoggingEvents.AUTH_LOGIN_SUCCESS);
            return "redirect:/linker/user/web/v1/profile";
        }
        if (!returnUrl.startsWith("/"))
            returnUrl = "/" + returnUrl;
        log.info("{} - login using login form was successful, redirecting requests to {}", LoggingEvents.AUTH_LOGIN_SUCCESS, returnUrl);
        return "redirect:" + userLoginForm.getReturnUrl();
    }

    @PostMapping("refresh")
    public ResponseEntity<AuthResponse> refresh(
            @NotNull @Valid @RequestBody UserRefreshRequest request,
            HttpServletRequest servletRequest,
            HttpServletResponse servletResponse) {
        AuthResponse authResponse;
        log.info("Refreshing request received, try to validate it");
        try {
            authResponse = authService.refresh(
                    request.refreshToken(),
                    servletRequest.getHeader("User-Agent"),
                    servletRequest.getRemoteAddr());
        } catch (InvalidJwtToken e) {
            log.warn("{} - Refreshing using token failed, return FORBIDDEN status", LoggingEvents.REFRESHING_FAILED);
            return ResponseEntity
                    .status(HttpStatus.FORBIDDEN)
                    .body(null);
        }
        generateTokenCookies(servletResponse, authResponse);
        log.info("{} - Refreshing using token succeed, return a new authResponse as result", LoggingEvents.REFRESHING_SUCCEED);
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
            log.info("{} - logging out was successful, redirecting to home page", LoggingEvents.AUTH_LOGOUT_SUCCESS);
        } catch (InvalidJwtToken e) {
            log.error("{} - Invalid refresh token used for logging out, redirect to home page", LoggingEvents.AUTH_LOGOUT_FAILED);
            removeTokenCookies(servletResponse);
        }
        return "redirect:/linker/home";
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
                .secure(Boolean.parseBoolean(cookiesSecure))
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
