package ir.aspireapps.linker.linksservice.repository;

import ir.aspireapps.linker.linksservice.model.Link;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface LinkRepository extends JpaRepository<Link, Long> {
    Optional<Link> findByIdAndUserId(Long id, UUID userId);
}
