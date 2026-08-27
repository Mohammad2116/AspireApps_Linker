package ir.aspireapps.linker.common.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;
import java.util.UUID;

@MappedSuperclass
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public abstract class OutboxMessage {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;
    @Column(nullable = false)
    private Long aggregateId;
    @Column(nullable = false)
    private String topic;
    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private EventStatus status;
    @Column(nullable = false)
    private String payload;
    @Column(nullable = false, updatable = false)
    private Instant createAt;
    @Column(nullable = false)
    private Instant updateAt;
}