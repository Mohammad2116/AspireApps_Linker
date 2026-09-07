package ir.aspireapps.linker.analysisservice.repository;

import ir.aspireapps.linker.analysisservice.model.AnalyzeData;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface AnalysisRepository extends JpaRepository<AnalyzeData, Long> {
    boolean existsByShortedUrl(@NotEmpty @Size(min = 4, max = 10) String shortedUrl);

    Optional<AnalyzeData> findByShortedUrl(String shortedUrl);

    void deleteByShortedUrl(String shortedUrl);
}
