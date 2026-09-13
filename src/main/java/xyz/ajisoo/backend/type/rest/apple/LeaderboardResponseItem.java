package xyz.ajisoo.backend.type.rest.apple;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import xyz.ajisoo.backend.type.entity.AppleLeaderboardEntry;

import java.time.Instant;
import java.util.Date;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class LeaderboardResponseItem {
    public LeaderboardResponseItem(AppleLeaderboardEntry ale) {
        this.name = ale.getName();
        this.completedAt = ale.getCompletedAt();
    }
    private String name;
    private Instant completedAt;
}
