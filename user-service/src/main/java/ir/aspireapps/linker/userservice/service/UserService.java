package ir.aspireapps.linker.userservice.service;

import ir.aspireapps.linker.userservice.dto.UserProfileResponse;
import ir.aspireapps.linker.userservice.repository.UserRepository;
import jakarta.validation.constraints.NotEmpty;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class UserService {
    private final UserRepository userRepository;


    public UserProfileResponse profile(@NotEmpty String username) {
        return userRepository.profile(username)
                .orElseThrow(() -> new RuntimeException("User not found"));
    }
}
