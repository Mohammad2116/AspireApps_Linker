package ir.aspireapps.linker.userservice.controller;

import ir.aspireapps.linker.userservice.dto.AuthResponse;
import ir.aspireapps.linker.userservice.dto.UserLoginRequest;
import ir.aspireapps.linker.userservice.dto.UserLogoutRequest;
import ir.aspireapps.linker.userservice.dto.UserRefreshRequest;
import ir.aspireapps.linker.userservice.service.AuthService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/ir/aspireapps/linker/auth/web/v1/")
@RequiredArgsConstructor
public class AuthControllerWeb {
    final AuthService authService;

//    @GetMapping("register")
//    public String register(
//            @NotNull @Valid UserRegistrationRequest request,
//            HttpServletRequest servletRequest) {
//        return "register";
//    }

    @GetMapping("login")
    public String login(
            @NotNull @Valid UserLoginRequest request,
            HttpServletRequest servletRequest) {
        return "login";
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
