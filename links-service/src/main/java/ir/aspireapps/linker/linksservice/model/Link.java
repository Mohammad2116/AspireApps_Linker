package ir.aspireapps.linker.linksservice.model;

import ir.aspireapps.linker.common.model.HitState;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.annotations.UpdateTimestamp;
import org.hibernate.type.SqlTypes;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "links")
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
@ToString
public class Link {
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "links_seq")
    @SequenceGenerator(name = "links_seq", sequenceName = "links_seq", allocationSize = 1)
    private long id;

    @Column(nullable = false, length = 254)
    private String title;
    @Column(nullable = false, length = 1024)
    private String originalUrl;

    @Column(nullable = true, length = 10, unique = true)
    private String shortUrl;
    @Column(nullable = false)
    private UUID userId;

    @Enumerated(EnumType.STRING)
    @JdbcTypeCode(SqlTypes.NAMED_ENUM)
    @Column(nullable = false, columnDefinition = "link_status")
    @Builder.Default
    private LinkStatus status = LinkStatus.ACTIVE;

    @Enumerated(EnumType.STRING)
    @JdbcTypeCode(SqlTypes.NAMED_ENUM)
    @Column(nullable = false, columnDefinition = "hit_state")
    @Builder.Default
    private HitState hitState = HitState.NORMAL;

    @Builder.Default
    @CreationTimestamp
    private Instant createdAt = Instant.now();
    @Builder.Default
    @UpdateTimestamp
    private Instant updatedAt = Instant.now();
    @Column(nullable = false)
    private Instant expiresAt;
}
