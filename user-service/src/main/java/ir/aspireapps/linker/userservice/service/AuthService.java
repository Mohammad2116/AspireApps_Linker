package ir.aspireapps.linker.userservice.service;

import ir.aspireapps.linker.userservice.dto.AuthResponse;
import ir.aspireapps.linker.userservice.dto.UserLoginRequest;
import ir.aspireapps.linker.userservice.dto.UserRegisterRequest;
import ir.aspireapps.linker.userservice.error.DuplicateResourceException;
import ir.aspireapps.linker.userservice.error.InvalidJwtToken;
import ir.aspireapps.linker.userservice.error.ResourceNotFoundException;
import ir.aspireapps.linker.userservice.model.RefreshToken;
import ir.aspireapps.linker.userservice.model.User;
import ir.aspireapps.linker.userservice.repository.UserRepository;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class AuthService {
    private final UserRepository userRepository;
    private final JwtService jwtService;
    private final RefreshTokenService refreshTokenService;
    private final PasswordEncoder passwordEncoder;

    @Transactional
    public AuthResponse register(
            @NotNull @Valid UserRegisterRequest request,
            @NotEmpty String deviceName,
            @NotEmpty String deviceIp
    ) {
        if (userRepository.existsByUsername(request.username())) {
            log.warn("Username [{}] is already taken", request.username());
            throw new DuplicateResourceException("Username is already in use");
        }
        if (userRepository.existsByEmail(request.email())) {
            log.warn("Email [{}] is already taken", request.email());
            throw new DuplicateResourceException("Email is already in use");
        }

        User newUser = User.builder()
                .username(request.username())
                .password(
                        passwordEncoder.encode(
                                request.password()
                        )
                )
                .email(request.email())
                .build();
        User savedUser = userRepository.save(newUser);
        log.info("New user: username[{}], email[{}], status[{}], ... ==> inserted into users table", newUser.getUsername(), savedUser.getEmail(), savedUser.getStatus());
        log.debug("New user tokens will generate based on : device name[{}], device ip[{}]", deviceName, deviceIp);
        return AuthResponse.builder()
                .accessToken(jwtService.generateAccessToken(savedUser))
                .accessTokenExpiresInSeconds(jwtService.accessTokenExpirationSeconds())
                .refreshToken(refreshTokenService.generateRefreshToken(
                        savedUser, deviceName, deviceIp))
                .build();
    }


    @Transactional
    public AuthResponse login(
            @NotNull @Valid UserLoginRequest request,
            @NotEmpty String deviceName,
            @NotEmpty String deviceIp) {
        User user = userRepository.findByUsername(request.username())
                .orElseThrow(() -> {
                    log.warn("Username [{}] not found in database", request.username());
                    return new ResourceNotFoundException("username or password not found");
                });
        log.debug("Login info: username[{}], password[{}...]", request.username(),
                user.getPassword().subSequence(
                        user.getPassword().length() - 5,
                        user.getPassword().length() - 1));
        log.debug("user login tokens will generate based on : device name[{}], device ip[{}]", deviceName, deviceIp);
        if (passwordEncoder.matches(request.password(), user.getPassword())) {
            return AuthResponse.builder()
                    .accessToken(jwtService.generateAccessToken(user))
                    .accessTokenExpiresInSeconds(jwtService.accessTokenExpirationSeconds())
                    .refreshToken(refreshTokenService.generateRefreshToken(
                            user, deviceName, deviceIp))
                    .build();
        }
        log.warn("Wrong password {}}] for username [{}] entered", request.password(), request.username());
        throw new ResourceNotFoundException("username or password not found");
    }

    @Transactional
    public AuthResponse refresh(@NotEmpty @Size(max = 512) String refreshToken,
                                @NotEmpty String deviceName,
                                @NotEmpty String deviceIp) throws InvalidJwtToken {
        RefreshToken oldToken = refreshTokenService.verifyToken(refreshToken);
        User user = oldToken.getUser();
        log.debug("Old token injected user is [{}]", user.getUsername());
        log.debug("user refresh tokens will generate based on : device name[{}], device ip[{}]", deviceName, deviceIp);
        return AuthResponse.builder()
                .accessToken(jwtService.generateAccessToken(user))
                .accessTokenExpiresInSeconds(jwtService.accessTokenExpirationSeconds())
                .refreshToken(refreshTokenService.generateRefreshToken(
                        user, deviceName, deviceIp
                ))
                .build();
    }

    @Transactional
    public void logout(@NotEmpty @Size(max = 512) String refreshToken) {
        refreshTokenService.verifyToken(refreshToken);
    }

    @Transactional
    public void logoutAll(@NotEmpty @Size(max = 512) String refreshToken) {
        RefreshToken oldToken = refreshTokenService.verifyToken(refreshToken);
        User user = oldToken.getUser();
        refreshTokenService.revokeAll(user);
    }

    public long refreshTokenExpireSeconds() {
        return refreshTokenService.refreshTokenExpirationSeconds();
    }

    public long accessTokenExpireSeconds() {
        return jwtService.accessTokenExpirationSeconds();
    }

    public boolean isRefreshTokenValid(String refreshToken) {
        return refreshTokenService.isValid(refreshToken);
    }
}
