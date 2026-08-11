package ir.aspireapps.linker.userservice.service;

import ir.aspireapps.linker.userservice.dto.AuthResponse;
import ir.aspireapps.linker.userservice.dto.UserLoginRequest;
import ir.aspireapps.linker.userservice.dto.UserRefreshRequest;
import ir.aspireapps.linker.userservice.dto.UserRegistrationRequest;
import ir.aspireapps.linker.userservice.model.RefreshToken;
import ir.aspireapps.linker.userservice.model.User;
import ir.aspireapps.linker.userservice.repository.UserRepository;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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
            @NotNull @Valid UserRegistrationRequest request,
            @NotEmpty String deviceName,
            @NotEmpty String deviceIp
    ) {
        if (userRepository.existsByUsername(request.username()))
            throw new RuntimeException("Username is already in use");
        if (userRepository.existsByEmail(request.email()))
            throw new RuntimeException("Email is already in use");

        User newUser = User.builder()
                .username(request.username())
                .password(request.password())
                .email(request.email())
                .build();
        User savedUser = userRepository.save(newUser);

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

        User user = userRepository.findByUsernameOrEmail(request.username(), request.email())
                .orElseThrow(() -> new RuntimeException("Wrong username or password"));

        if (passwordEncoder.matches(request.password(), user.getPassword())) {
            return AuthResponse.builder()
                    .accessToken(jwtService.generateAccessToken(user))
                    .accessTokenExpiresInSeconds(jwtService.accessTokenExpirationSeconds())
                    .refreshToken(refreshTokenService.generateRefreshToken(
                            user, deviceName, deviceIp))
                    .build();
        }
        throw new RuntimeException("Wrong username or password");
    }

    @Transactional
    public AuthResponse refresh(@NotNull @Valid UserRefreshRequest request,
                                @NotEmpty String deviceName,
                                @NotEmpty String deviceIp) {
        RefreshToken oldToken = refreshTokenService.verifyToken(request.refreshToken())
                .orElseThrow(() -> new RuntimeException("Invalid Refresh Token"));
        User user = oldToken.getUser();
        return AuthResponse.builder()
                .accessToken(jwtService.generateAccessToken(user))
                .accessTokenExpiresInSeconds(jwtService.accessTokenExpirationSeconds())
                .refreshToken(refreshTokenService.generateRefreshToken(
                        user, deviceName, deviceIp
                ))
                .build();
    }
}
