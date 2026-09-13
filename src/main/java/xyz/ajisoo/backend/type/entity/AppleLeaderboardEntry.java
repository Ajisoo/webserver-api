package xyz.ajisoo.backend.type.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "apple_leaderboard", indexes = {
        @Index(name = "idx_apple_leaderboard_completed_at", columnList = "completed_at")
})
public class AppleLeaderboardEntry {
    @Id
    @GeneratedValue
    private Long id;
    private String sessionId;
    private String name;
    @Column(name = "completed_at")
    private Instant completedAt;
}
