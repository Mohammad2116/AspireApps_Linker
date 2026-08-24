package ir.aspireapps.linker.linksservice.repository;

import ir.aspireapps.linker.linksservice.dto.LinkResponse;
import ir.aspireapps.linker.linksservice.dto.RedirectResponse;
import ir.aspireapps.linker.linksservice.model.Link;
import ir.aspireapps.linker.linksservice.model.LinkStatus;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface LinkRepository extends JpaRepository<Link, Long> {
    Optional<Link> findByIdAndUserId(@NotNull Long id, @NotNull UUID userId);

    @Query(
            """
                    SELECT NEW ir.aspireapps.linker.linksservice.dto.LinkResponse (
                            l.id,
                            l.title,
                            l.originalUrl,
                            l.shortUrl,
                            l.userId,
                            l.status,
                            l.createdAt,
                            l.updatedAt,
                            l.expiresAt
                        )
                    FROM Link l
                    WHERE l.userId = :userId
                    """
    )
    List<LinkResponse> findUserLinks(@NotEmpty @Param("userId") UUID userId);

    @Query(
            """
                        SELECT NEW ir.aspireapps.linker.linksservice.dto.RedirectResponse (
                                l.originalUrl
                                )
                        FROM Link l
                        WHERE l.shortUrl = :shorted AND l.status = :status
                    """
    )
    Optional<RedirectResponse> findByShortedAndStatus(@Param("shorted") String shorted, @Param("status") LinkStatus status);
}
