package ir.aspireapps.linker.userservice.controller;

import ir.aspireapps.linker.userservice.dto.*;
import ir.aspireapps.linker.userservice.service.AuthService;
import ir.aspireapps.linker.userservice.utility.InputNormalizer;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/ir/aspireapps/linker/auth/api/v1/")
@RequiredArgsConstructor
public class AuthController {
    private final AuthService authService;

    @PostMapping("register")
    public ResponseEntity<AuthResponse> register(
            @NotNull @Valid UserRegisterRequest request,
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
            @NotNull @Valid UserLoginRequest request,
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
            @NotNull @Valid UserRefreshRequest request,
            HttpServletRequest servletRequest) {
        AuthResponse result = authService.refresh(
                request.refreshToken(),
                servletRequest.getHeader("User-Agent"),
                servletRequest.getRemoteAddr());

        return ResponseEntity
                .status(HttpStatus.ACCEPTED)
                .body(result);
    }

    @PostMapping("logout")
    @PreAuthorize("hasAnyRole('ADMIN', 'USER')")
    public ResponseEntity<Void> logout(
            @NotNull @Valid UserLogoutRequest request) {
        authService.logout(request.refreshToken());
        return ResponseEntity
                .status(HttpStatus.ACCEPTED)
                .body(null);
    }

    @PostMapping("logout/all")
    @PreAuthorize("hasAnyRole('ADMIN', 'USER')")
    public ResponseEntity<Void> logoutAll(
            @NotNull @Valid UserLogoutRequest request) {
        authService.logoutAll(request.refreshToken());
        return ResponseEntity
                .status(HttpStatus.ACCEPTED)
                .body(null);
    }
}
