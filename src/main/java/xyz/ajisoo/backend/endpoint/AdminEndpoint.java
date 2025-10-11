package xyz.ajisoo.backend.endpoint;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import xyz.ajisoo.backend.service.LightService;
import xyz.ajisoo.backend.service.SadBotService;
import xyz.ajisoo.backend.type.rest.LightStatus;
import xyz.ajisoo.backend.type.rest.Status;

@CrossOrigin(origins = "http://localhost:3000")
@RestController
@RequestMapping("/admin")
public class AdminEndpoint {

    @Autowired
    LightService lightService;

    @Autowired
    SadBotService sadBotService;

    @GetMapping("/")
    public ResponseEntity<Status> getStatus() {
        Status status = new Status();
        status.setLights(lightService.getAllLights());
        status.setSadBotOnline(sadBotService.isOnline());
        return ResponseEntity.ok(status);
    }

    @PostMapping("/light")
    public ResponseEntity<LightStatus> setLight(@RequestParam("name") String name, @RequestParam("on") boolean on) {
        return ResponseEntity.ok(lightService.setLight(name, on));
    }
}
