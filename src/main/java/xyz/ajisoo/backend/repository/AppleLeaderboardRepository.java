package xyz.ajisoo.backend.repository;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import xyz.ajisoo.backend.type.entity.AppleLeaderboardEntry;

import java.util.List;

public interface AppleLeaderboardRepository extends JpaRepository<AppleLeaderboardEntry, Long> {
    List<AppleLeaderboardEntry> findAllByOrderByCompletedAtAsc(Pageable pageable);
}
