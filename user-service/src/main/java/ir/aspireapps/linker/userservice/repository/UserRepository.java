package ir.aspireapps.linker.userservice.repository;

import ir.aspireapps.linker.userservice.model.User;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface UserRepository extends JpaRepository<User, UUID> {
    boolean existsByUsername(@NotEmpty @Size(min = 3, max = 100) String username);

    boolean existsByEmail(@NotEmpty @Size(min = 5, max = 254) @Email String email);

    Optional<User> findByUsernameOrEmail(@NotEmpty @Size(min = 3, max = 100) String username,
                                         @NotEmpty @Size(min = 5, max = 254) @Email String email);
}
