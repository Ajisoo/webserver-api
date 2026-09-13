package xyz.ajisoo.backend.service;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import xyz.ajisoo.backend.repository.AppleLeaderboardRepository;
import xyz.ajisoo.backend.repository.AppleSessionRepository;
import xyz.ajisoo.backend.type.entity.AppleLeaderboardEntry;
import xyz.ajisoo.backend.type.entity.AppleSession;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Service
public class AppleService {

    private static final Logger LOGGER = LoggerFactory.getLogger(AppleService.class);

    private static final String SESSION_COOKIE = "huntSession";
    private static final int SESSION_DURATION_SECONDS = 60 * 60 * 12;

    @Value("${apple.leaderboard.max}")
    private int max;

    @Autowired
    private AppleLeaderboardRepository appleLeaderboardRepository;

    @Autowired
    private AppleSessionRepository appleSessionRepository;

    public List<AppleLeaderboardEntry> getLeaderboard() {
        LOGGER.info("getLeaderboard() called.");

        Pageable pageable = PageRequest.of(0, max);
        return appleLeaderboardRepository.findAllByOrderByCompletedAtAsc(pageable);
    }

    public Instant complete(
            String name,
            HttpServletRequest request
    ) {
        LOGGER.info("complete() called with name={}.", name);

        String sessionId = getSessionId(request);

        AppleSession session = appleSessionRepository.findById(sessionId)
                .orElseThrow(() -> new IllegalStateException("Hunt session not found"));

        if (!session.isFound1() || !session.isFound2() || !session.isFound3()) {
            throw new IllegalStateException("Hunt is not complete");
        }

        if (session.isCompleted()) {
            throw new IllegalStateException("Hunt already completed");
        }

        AppleLeaderboardEntry entry = new AppleLeaderboardEntry();
        entry.setName(name);
        entry.setCompletedAt(Instant.now());
        entry.setSessionId(sessionId);

        appleLeaderboardRepository.save(entry);

        session.setCompleted(true);
        session.setCompletedAt(entry.getCompletedAt());
        appleSessionRepository.save(session);

        return entry.getCompletedAt();
    }

    public AppleSession visit(
            long paintingId,
            HttpServletRequest request,
            HttpServletResponse response
    ) {
        LOGGER.info("visit() called with paintingId={}", paintingId);

        if (paintingId < 1 || paintingId > 3) {
            throw new IllegalArgumentException("Invalid painting ID");
        }

        String sessionId = getSessionId(request);

        // Create a new session if this browser doesn't have one
        if (sessionId == null) {
            sessionId = UUID.randomUUID().toString();

            Cookie cookie = new Cookie(SESSION_COOKIE, sessionId);
            cookie.setMaxAge(SESSION_DURATION_SECONDS);
            cookie.setPath("/");
            cookie.setHttpOnly(true);
            cookie.setSecure(true);

            response.addCookie(cookie);
        }

        String finalSessionId = sessionId;

        AppleSession session = appleSessionRepository.findById(sessionId)
                .orElseGet(() -> new AppleSession(finalSessionId));

        switch ((int) paintingId) {
            case 1 -> session.setFound1(true);
            case 2 -> session.setFound2(true);
            case 3 -> session.setFound3(true);
        }

        return appleSessionRepository.save(session);
    }

    public AppleSession getStatus(HttpServletRequest request) {
        LOGGER.info("getStatus() called.");

        String sessionId = getSessionId(request);

        if (sessionId == null) {
            return null;
        }

        return appleSessionRepository.findById(sessionId).orElse(null);
    }

    private String getSessionId(HttpServletRequest request) {
        if (request.getCookies() == null) {
            return null;
        }

        for (Cookie cookie : request.getCookies()) {
            if (SESSION_COOKIE.equals(cookie.getName())) {
                return cookie.getValue();
            }
        }

        return null;
    }
}