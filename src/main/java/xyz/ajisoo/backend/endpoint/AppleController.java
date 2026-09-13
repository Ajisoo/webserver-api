package xyz.ajisoo.backend.endpoint;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import xyz.ajisoo.backend.service.AppleService;
import xyz.ajisoo.backend.type.entity.AppleSession;
import xyz.ajisoo.backend.type.rest.apple.CompleteRequest;
import xyz.ajisoo.backend.type.rest.apple.CompleteResponse;
import xyz.ajisoo.backend.type.rest.apple.LeaderboardResponseItem;
import xyz.ajisoo.backend.type.rest.apple.StatusResponse;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/apple")
public class AppleController {

    @Autowired
    AppleService appleService;

    @PostMapping("/visit/{paintingId}")
    public ResponseEntity<StatusResponse> visit(
            @PathVariable Long paintingId,
            HttpServletRequest request,
            HttpServletResponse response
    ) {
        AppleSession session = appleService.visit(
                paintingId,
                request,
                response
        );

        return ResponseEntity.ok(new StatusResponse(session));
    }

    @GetMapping("/status")
    public ResponseEntity<StatusResponse> getStatus(
            HttpServletRequest request
    ) {
        AppleSession session = appleService.getStatus(request);

        if (session == null) {
            return ResponseEntity.ok(new StatusResponse());
        }

        return ResponseEntity.ok(new StatusResponse(session));
    }

    @PostMapping("/complete")
    public ResponseEntity<CompleteResponse> complete(
            @RequestBody CompleteRequest completeRequest,
            HttpServletRequest request
    ) {
        String name = completeRequest.getName();

        try {
            return ResponseEntity.ok(
                    new CompleteResponse(
                            appleService.complete(name, request)
                    )
            );
        } catch (IllegalStateException e) {
            return ResponseEntity.ok(
                    new CompleteResponse(e.getMessage())
            );
        }
    }

    @GetMapping("/leaderboard")
    public ResponseEntity<List<LeaderboardResponseItem>> getLeaderboard() {
        return ResponseEntity.ok(
                appleService.getLeaderboard()
                        .stream()
                        .map(LeaderboardResponseItem::new)
                        .collect(Collectors.toList())
        );
    }
}