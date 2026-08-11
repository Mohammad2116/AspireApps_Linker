package ir.aspireapps.linker.userservice.controller;

import ir.aspireapps.linker.userservice.dto.AuthResponse;
import ir.aspireapps.linker.userservice.dto.UserLoginRequest;
import ir.aspireapps.linker.userservice.dto.UserRefreshRequest;
import ir.aspireapps.linker.userservice.dto.UserRegistrationRequest;
import ir.aspireapps.linker.userservice.service.AuthService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/ir/aspireapps/linker/api/v1/auth/")
@RequiredArgsConstructor
public class controller {
    private final AuthService authService;

    @PostMapping("register")
    public ResponseEntity<AuthResponse> register(
            @NotNull @Valid UserRegistrationRequest request,
            HttpServletRequest servletRequest) {
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
                request,
                servletRequest.getHeader("User-Agent"),
                servletRequest.getRemoteAddr());

        return ResponseEntity
                .status(HttpStatus.ACCEPTED)
                .body(result);
    }
}
