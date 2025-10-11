package xyz.ajisoo.backend.util;

import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import java.util.Map;

@Component
public class LightInterface {

    private final RestTemplate restTemplate = new RestTemplate();
    
    public boolean getLightStatus(String hostname) {
        String url = String.format("http://%s/rpc/Switch.GetStatus?id=0", hostname);

        try {
            ResponseEntity<Map> response = restTemplate.getForEntity(url, Map.class);
            Map<String, Object> body = response.getBody();

            if (body == null || !body.containsKey("output")) {
                throw new IllegalStateException("Invalid response format: missing 'output' field");
            }

            Object outputValue = body.get("output");
            if (!(outputValue instanceof Boolean)) {
                throw new IllegalStateException("Invalid 'output' value type: expected boolean");
            }

            return (Boolean) outputValue;

        } catch (RestClientException e) {
            throw new RuntimeException("Network or connection error when contacting " + hostname, e);
        } catch (Exception e) {
            throw new RuntimeException("Failed to parse light status from " + hostname, e);
        }
    }

    public boolean setLight(String hostname, boolean on) {
        String url = String.format("http://%s/rpc/Switch.Set?id=0&on=%s", hostname, on);

        try {
            ResponseEntity<Map> response = restTemplate.getForEntity(url, Map.class);
            Map<String, Object> body = response.getBody();

            if (body == null || !body.containsKey("was_on")) {
                throw new IllegalStateException("Invalid response format: missing 'output' field");
            }

            return on;

        } catch (RestClientException e) {
            throw new RuntimeException("Network or connection error when contacting " + hostname, e);
        } catch (Exception e) {
            throw new RuntimeException("Failed to parse light status from " + hostname, e);
        }
    }
}
