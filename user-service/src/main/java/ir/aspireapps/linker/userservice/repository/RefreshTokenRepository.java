package ir.aspireapps.linker.userservice.repository;

import ir.aspireapps.linker.userservice.model.RefreshToken;
import ir.aspireapps.linker.userservice.model.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface RefreshTokenRepository extends JpaRepository<RefreshToken, Long> {
    Optional<RefreshToken> findByTokenHash(String hashedToken);

    List<RefreshToken> findAllByUserAndNotRevoked(User user);

}
