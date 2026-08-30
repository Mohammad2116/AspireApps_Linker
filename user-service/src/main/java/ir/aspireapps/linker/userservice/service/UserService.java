package ir.aspireapps.linker.userservice.service;

import ir.aspireapps.linker.userservice.dto.UserProfileResponse;
import ir.aspireapps.linker.userservice.error.ResourceNotFoundException;
import ir.aspireapps.linker.userservice.repository.UserRepository;
import jakarta.validation.constraints.NotEmpty;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class UserService {
    private final UserRepository userRepository;

    public UserProfileResponse profile(@NotEmpty String username) {
        return userRepository.profile(username)
                .orElseThrow(() -> {
                    log.warn("Username [{}] not found", username);
                    return new ResourceNotFoundException("User not found");
                });
    }
}
