package ir.aspireapps.linker.userservice.controller;

import ir.aspireapps.linker.userservice.dto.AuthResponse;
import ir.aspireapps.linker.userservice.dto.UserLoginRequest;
import ir.aspireapps.linker.userservice.dto.UserLogoutRequest;
import ir.aspireapps.linker.userservice.dto.UserRefreshRequest;
import ir.aspireapps.linker.userservice.form.UserLoginForm;
import ir.aspireapps.linker.userservice.form.UserRegisterForm;
import ir.aspireapps.linker.userservice.service.AuthService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/ir/aspireapps/linker/auth/web/v1/")
@RequiredArgsConstructor
public class AuthControllerWeb {
    private final AuthService authService;

    @GetMapping("register")
    public String register(
            Model model,
            HttpServletRequest servletRequest) {
        model.addAttribute("registerForm", new UserRegisterForm());
        return "register";
    }

    @GetMapping("login")
    public String login(
            Model model,
            HttpServletRequest servletRequest
    ) {
        model.addAttribute("loginForm", new UserLoginForm());
        return "login";
    }

    @PostMapping("login")
    public String loginProcess(
            @Valid @ModelAttribute("loginForm") UserLoginForm userLoginForm,
            BindingResult bindingResult,
            Model model,
            HttpServletRequest servletRequest
    ) {
        if (bindingResult.hasFieldErrors()) {
            model.addAttribute("generalError", true);
            return "login";
        }

        AuthResponse response = authService.login(
                UserLoginRequest.builder()
                        .username(userLoginForm.getUsername())
                        .password(userLoginForm.getPassword())
                        .build(),
                servletRequest.getHeader("User-Agent"),
                servletRequest.getRemoteAddr());

        return "home";
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
