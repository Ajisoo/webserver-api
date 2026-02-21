package xyz.ajisoo.backend.endpoint;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;

@RestController
@RequestMapping("/stream")
public class StreamController {

    private static final String STREAM_DIR = "/var/www/stream/";

    @GetMapping("/{filename:.+}")
    public ResponseEntity<Resource> getStreamFile(
            @PathVariable String filename,
            HttpServletRequest request) throws IOException {

        Path filePath = Paths.get(STREAM_DIR, filename);
        Resource resource = new UrlResource(filePath.toUri());

        if (!resource.exists()) {
            return ResponseEntity.notFound().build();
        }

        String contentType = filename.endsWith(".m3u8")
                ? "application/vnd.apple.mpegurl"
                : "video/MP2T";

        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(contentType))
                .body(resource);
    }
}