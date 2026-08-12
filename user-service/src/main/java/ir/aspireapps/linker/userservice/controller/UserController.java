package ir.aspireapps.linker.userservice.controller;

import ir.aspireapps.linker.userservice.dto.UserProfileResponse;
import ir.aspireapps.linker.userservice.service.UserService;
import jakarta.validation.constraints.NotEmpty;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/ir/aspireapps/linker/api/v1/user/")
@RequiredArgsConstructor
@PreAuthorize("hasAnyRole('ADMIN', 'USER')")
public class UserController {
    private final UserService userService;

    @GetMapping("profile")
    public ResponseEntity<UserProfileResponse> profile(
            @NotEmpty @RequestHeader("X-USERNAME") String username) {
        return ResponseEntity
                .status(HttpStatus.OK)
                .body(
                        userService.profile(username)
                );
    }
}
