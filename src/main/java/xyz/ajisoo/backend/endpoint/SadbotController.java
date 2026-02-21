package xyz.ajisoo.backend.endpoint;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import xyz.ajisoo.backend.service.SadBotService;
import xyz.ajisoo.backend.type.rest.sadbot.SadbotStatusResponse;

@RestController
@RequestMapping("/sadbot")
public class SadbotController {

    @Autowired
    SadBotService sadBotService;

    @GetMapping
    public ResponseEntity<SadbotStatusResponse> getStatus() {
        SadbotStatusResponse status = new SadbotStatusResponse();
        status.setOnline(sadBotService.isOnline());
        return ResponseEntity.ok(status);
    }

    @PostMapping("/restart")
    public ResponseEntity<Boolean> restartSadbot() {
        return ResponseEntity.ok(sadBotService.restart());
    }
}
