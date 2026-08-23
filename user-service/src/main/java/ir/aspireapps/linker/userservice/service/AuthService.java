package ir.aspireapps.linker.userservice.service;

import io.jsonwebtoken.ExpiredJwtException;
import ir.aspireapps.linker.userservice.dto.AuthResponse;
import ir.aspireapps.linker.userservice.dto.UserLoginRequest;
import ir.aspireapps.linker.userservice.dto.UserRegisterRequest;
import ir.aspireapps.linker.userservice.model.RefreshToken;
import ir.aspireapps.linker.userservice.model.User;
import ir.aspireapps.linker.userservice.repository.UserRepository;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

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
        if (userRepository.existsByUsername(request.username()))
            throw new RuntimeException("Username is already in use");
        if (userRepository.existsByEmail(request.email()))
            throw new RuntimeException("Email is already in use");

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

        System.out.println("Request username " + request.username());
        User user = userRepository.findByUsername(request.username())
                .orElseThrow(() -> new RuntimeException("username not found"));

        if (passwordEncoder.matches(request.password(), user.getPassword())) {
            return AuthResponse.builder()
                    .accessToken(jwtService.generateAccessToken(user))
                    .accessTokenExpiresInSeconds(jwtService.accessTokenExpirationSeconds())
                    .refreshToken(refreshTokenService.generateRefreshToken(
                            user, deviceName, deviceIp))
                    .build();
        }
        System.out.println("Request Password " + request.password());
        System.out.println("user Password " + user.getPassword());
        throw new RuntimeException("password not matched");
    }

    @Transactional
    public AuthResponse refresh(@NotEmpty @Size(max = 512) String refreshToken,
                                @NotEmpty String deviceName,
                                @NotEmpty String deviceIp) throws ExpiredJwtException {
        Optional<RefreshToken> oldToken = refreshTokenService.verifyToken(refreshToken);
        if (oldToken.isEmpty()) throw new ExpiredJwtException(null, null, "Invalid Refresh Token");
        User user = oldToken.get().getUser();
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
        refreshTokenService.verifyToken(refreshToken)
                .orElseThrow(() -> new RuntimeException("Invalid Refresh Token"));
    }

    @Transactional
    public void logoutAll(@NotEmpty @Size(max = 512) String refreshToken) {
        RefreshToken oldToken = refreshTokenService.verifyToken(refreshToken)
                .orElseThrow(() -> new RuntimeException("Invalid Refresh Token"));
        User user = oldToken.getUser();
        refreshTokenService.revokeAll(user);
    }

    public long refreshTokenExpireSeconds() {
        return refreshTokenService.refreshTokenExpirationSeconds();
    }

    public long accessTokenExpireSeconds() {
        return jwtService.accessTokenExpirationSeconds();
    }
}
