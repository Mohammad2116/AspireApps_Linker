package ir.aspireapps.linker.userservice.model;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.Instant;

@Entity
@Table(name = "refresh_tokens")
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class RefreshToken {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;
    @Column(nullable = false, length = 512, unique = true)
    private String tokenHash;

    @Column(name = "device_ip", columnDefinition = "inet")
    @JdbcTypeCode(SqlTypes.INET)
    private String deviceIp;

    @Column(length = 254)
    private String deviceName;

    @Column(nullable = false)
    private Instant expiresAt;
    @Column(nullable = false)
    @Builder.Default
    private boolean revoked = false;
    @Column(nullable = true)
    private Instant revokedAt;

    @ManyToOne(
            fetch = FetchType.LAZY
    )
    @JoinColumn(name = "user_id")
    private User user;

    public void revoke() {
        this.setRevokedAt(Instant.now());
        this.setRevoked(true);
    }
}
