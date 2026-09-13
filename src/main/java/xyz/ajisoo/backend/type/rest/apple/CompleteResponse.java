package xyz.ajisoo.backend.type.rest.apple;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.lang.Nullable;

import java.time.Instant;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class CompleteResponse {

    public CompleteResponse(Instant completedAt) {
        this.success = true;
        this.completedAt = completedAt;
        this.reason = null;
    }

    public CompleteResponse(String reason) {
        this.success = false;
        this.completedAt = null;
        this.reason = reason;
    }

    private boolean success;
    private @Nullable Instant completedAt;
    private @Nullable String reason;
}