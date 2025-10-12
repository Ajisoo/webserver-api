package xyz.ajisoo.backend.util;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

@Component
public class LightMappingProvider {

    @Value("${lights.file}")
    public String fileLocation;

    private Map<String, String> lightMapping;

    public Map<String, String> getLightMapping() {
        if (lightMapping == null) {
            if (StringUtils.isEmpty(fileLocation)) {
                lightMapping = new HashMap<>();
                return lightMapping;
            }
            lightMapping = new HashMap<>();
            try (BufferedReader br = new BufferedReader(new FileReader(fileLocation))) {
                String line;
                while ((line = br.readLine()) != null) {
                    line = line.trim();
                    if (line.isEmpty() || line.startsWith("#")) continue; // skip blank or comment lines

                    String[] parts = line.split(",");
                    if (parts.length >= 2) {
                        String name = parts[0].trim();
                        String host = parts[1].trim();
                        lightMapping.put(name, host);
                    }
                }
            } catch (IOException e) {
                throw new RuntimeException("Failed to load light mappings from: " + fileLocation, e);
            }
        }
        return lightMapping;
    }
}
