package xyz.ajisoo.backend.type.entity;

import jakarta.annotation.Nullable;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;
import java.time.temporal.ChronoUnit;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "apple_session")
public class AppleSession {

    public AppleSession(String id) {
        this.id = id;
        createdAt = Instant.now();
        expiresAt = createdAt.plus(12, ChronoUnit.HOURS);
    }

    @Id
    private String id;
    private Instant createdAt;
    private Instant expiresAt;

    private boolean found1;
    private boolean found2;
    private boolean found3;

    private boolean completed;
    private @Nullable Instant completedAt;
}
