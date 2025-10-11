package xyz.ajisoo.backend.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import xyz.ajisoo.backend.type.rest.LightStatus;
import xyz.ajisoo.backend.util.LightInterface;
import xyz.ajisoo.backend.util.LightMappingProvider;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Service
@PreAuthorize("isAuthenticated()")
public class LightService {

    @Autowired
    LightMappingProvider lightMappingProvider;

    @Autowired
    LightInterface lightInterface;

    public List<LightStatus> getAllLights() {
        List<LightStatus> lightStatuses = new ArrayList<>();
        Map<String, String> lightInfo = lightMappingProvider.getLightMapping();
        for (Map.Entry<String, String> light : lightInfo.entrySet()) {
            try {
                boolean result = lightInterface.getLightStatus(light.getValue());
                lightStatuses.add(new LightStatus(light.getKey(), result, null));
            } catch (Exception e) {
                lightStatuses.add(new LightStatus(light.getKey(), false, e.getMessage()));
            }
        }
        return lightStatuses;
    }

    public LightStatus setLight(String name, boolean on) {
        try {
            boolean result = lightInterface.setLight(lightMappingProvider.getLightMapping().get(name), on);
            return new LightStatus(name, result, null);
        } catch (Exception e) {
            return new LightStatus(name, false, e.getMessage());
        }
    }
}
