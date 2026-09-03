package ir.aspireapps.linker.userservice.controller;

import ir.aspireapps.linker.common.utility.LoggingEvents;
import ir.aspireapps.linker.userservice.dto.*;
import ir.aspireapps.linker.userservice.error.InvalidJwtToken;
import ir.aspireapps.linker.userservice.service.AuthService;
import ir.aspireapps.linker.userservice.utility.InputNormalizer;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequestMapping("/ir/aspireapps/linker/auth/api/v1/")
@RequiredArgsConstructor
public class AuthController {
    private final AuthService authService;

    @PostMapping("register")
    public ResponseEntity<AuthResponse> register(
            @NotNull @Valid @RequestBody UserRegisterRequest request,
            HttpServletRequest servletRequest) {
        request = InputNormalizer.normalize(request);
        AuthResponse result = authService.register(
                request,
                servletRequest.getHeader("User-Agent"),
                servletRequest.getRemoteAddr()
        );
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(result);
    }

    @PostMapping("login")
    public ResponseEntity<AuthResponse> login(
            @NotNull @Valid @RequestBody UserLoginRequest request,
            HttpServletRequest servletRequest) {
        request = InputNormalizer.normalize(request);
        AuthResponse result = authService.login(
                request,
                servletRequest.getHeader("User-Agent"),
                servletRequest.getRemoteAddr()
        );
        return ResponseEntity
                .status(HttpStatus.OK)
                .body(result);
    }

    @PostMapping("refresh")
    public ResponseEntity<AuthResponse> refresh(
            @NotNull @Valid @RequestBody UserRefreshRequest request,
            HttpServletRequest servletRequest) {
        AuthResponse result;
        try {
            result = authService.refresh(
                    request.refreshToken(),
                    servletRequest.getHeader("User-Agent"),
                    servletRequest.getRemoteAddr());
        } catch (InvalidJwtToken invalidJwtToken) {
            log.debug("{} - refreshing failed}", LoggingEvents.AUTH_LOGIN_FAILED);
            throw invalidJwtToken;
        }
        log.info("{} - User auth refreshed successfully", LoggingEvents.AUTH_LOGIN_SUCCESS);
        return ResponseEntity
                .status(HttpStatus.ACCEPTED)
                .body(result);
    }

    @PostMapping("logout")
    @PreAuthorize("hasAnyRole('ADMIN', 'USER')")
    public ResponseEntity<Void> logout(
            @NotNull @Valid UserLogoutRequest request) {
        try {
            authService.logout(request.refreshToken());
        } catch (InvalidJwtToken invalidJwtToken) {
            log.debug("{} - User auth logout failed", LoggingEvents.AUTH_LOGIN_FAILED);
            throw invalidJwtToken;
        }
        log.info("{} - User logged out successfully", LoggingEvents.AUTH_LOGOUT_SUCCESS);
        return ResponseEntity
                .status(HttpStatus.ACCEPTED)
                .body(null);
    }

    @PostMapping("logout/all")
    @PreAuthorize("hasAnyRole('ADMIN', 'USER')")
    public ResponseEntity<Void> logoutAll(
            @NotNull @Valid @RequestBody UserLogoutRequest request) {
        authService.logoutAll(request.refreshToken());
        log.info("{} - User logged out all sessions successfully", LoggingEvents.AUTH_LOGOUT_ALL_SUCCESS);
        return ResponseEntity
                .status(HttpStatus.ACCEPTED)
                .body(null);
    }
}
