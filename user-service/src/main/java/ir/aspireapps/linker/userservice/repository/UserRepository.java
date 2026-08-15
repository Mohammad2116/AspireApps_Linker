package ir.aspireapps.linker.userservice.repository;

import ir.aspireapps.linker.userservice.dto.UserProfileResponse;
import ir.aspireapps.linker.userservice.model.User;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;
import java.util.UUID;

public interface UserRepository extends JpaRepository<User, UUID> {
    boolean existsByUsername(@NotEmpty @Size(min = 3, max = 100) String username);

    boolean existsByEmail(@NotEmpty @Size(min = 5, max = 254) @Email String email);

    Optional<User> findByUsername(@NotEmpty @Size(min = 3, max = 100) String username);

    @Query("""
                SELECT NEW ir.aspireapps.linker.userservice.dto.UserProfileResponse (
                        u.username,
                        u.email,
                        u.role,
                        u.status,
                        u.createdAt,
                        u.enabled,
                        u.emailVerified,
                        u.lockedUntil
                    )
                FROM User u
                WHERE u.username = :username
            """)
    Optional<UserProfileResponse> profile(@NotEmpty @Param("username") String username);

}
