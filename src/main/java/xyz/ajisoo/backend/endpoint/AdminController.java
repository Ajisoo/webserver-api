package xyz.ajisoo.backend.endpoint;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import xyz.ajisoo.backend.service.LightService;
import xyz.ajisoo.backend.service.SadBotService;
import xyz.ajisoo.backend.type.rest.admin.LightStatus;
import xyz.ajisoo.backend.type.rest.admin.SetLightRequest;
import xyz.ajisoo.backend.type.rest.admin.StatusResponse;

@RestController
@RequestMapping("/admin")
public class AdminController {

    @Autowired
    LightService lightService;

    @Autowired
    SadBotService sadBotService;

    @GetMapping
    public ResponseEntity<StatusResponse> getStatus() {
        StatusResponse status = new StatusResponse();
        status.setLights(lightService.getAllLights());
        status.setSadBotOnline(sadBotService.isOnline());
        return ResponseEntity.ok(status);
    }

    @PostMapping("/light")
    public ResponseEntity<LightStatus> setLight(@RequestBody SetLightRequest setLightRequest) {
        return ResponseEntity.ok(lightService.setLight(setLightRequest.getName(), setLightRequest.isSetOn()));
    }
}
