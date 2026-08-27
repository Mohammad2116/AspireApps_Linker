package ir.aspireapps.linker.analysisservice.model;

import ir.aspireapps.linker.common.model.HitState;
import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;

@Entity
@Table(name = "analysis")
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class AnalyzeData {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;
    @Column(nullable = false, unique = true)
    private String shortedUrl;
    @Column(nullable = false)
    @Builder.Default
    private long hitCount = 0;
    @Column(nullable = false)
    @Builder.Default
    private long allTimeHitCount = 0;
    @Column(nullable = false)
    @Builder.Default
    private Instant counterResetAt = Instant.now();
    @Column(nullable = false)
    @Builder.Default
    private HitState linkHitState = HitState.NORMAL;

    public void incClickCount() {
        this.allTimeHitCount++;
        this.hitCount++;
    }
}
