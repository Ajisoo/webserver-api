package xyz.ajisoo.backend.endpoint;

import io.jsonwebtoken.JwtException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import xyz.ajisoo.backend.service.JwtService;
import xyz.ajisoo.backend.type.rest.auth.FirstTimeAuthRequest;
import xyz.ajisoo.backend.type.rest.auth.FirstTimeAuthResponse;
import xyz.ajisoo.backend.type.rest.auth.TokenRefreshRequest;
import xyz.ajisoo.backend.type.rest.auth.TokenRefreshResponse;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.Instant;

@CrossOrigin(origins = "http://localhost:3000")
@RestController
@RequestMapping("/auth")
public class AuthController {

    @Autowired
    private JwtService jwtService;

    @Value("${otp.path}")
    private String OTP_FILE_PATH;

    @PostMapping("/")
    public ResponseEntity<FirstTimeAuthResponse> authenticate(@RequestBody FirstTimeAuthRequest firstTimeAuthRequest) {
        try {
            validateOtp(firstTimeAuthRequest.getOtp());

            // If validation is successful, generate the token
            String subject = "user-authenticated-via-otp-" + Instant.now().toEpochMilli();
            String refreshToken = jwtService.generateRefreshToken(subject);

            // Invalidate the OTP by deleting the file
            Files.deleteIfExists(Path.of(OTP_FILE_PATH));

            return ResponseEntity.ok(new FirstTimeAuthResponse(refreshToken));

        } catch (IOException | NoSuchAlgorithmException e) {
            // Handle server-side errors (e.g., file not found, hashing algorithm issue)
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Authentication service error.", e);
        } catch (SecurityException e) {
            // Handle specific authentication failures
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, e.getMessage());
        }
    }

    @PostMapping("/refresh")
    public ResponseEntity<TokenRefreshResponse> refresh(@RequestBody TokenRefreshRequest request) {
        String requestRefreshToken = request.getRefreshToken();

        try {
            // Validate the refresh token and extract the subject
            String subject = jwtService.extractSubject(requestRefreshToken);

            // Generate a new access token
            String newAccessToken = jwtService.generateAccessToken(subject);

            // Generate a new refresh token to slide the session window
            String newRefreshToken = jwtService.generateRefreshToken(subject);

            return ResponseEntity.ok(new TokenRefreshResponse(newAccessToken, newRefreshToken));

        } catch (JwtException e) {
            // This catches invalid signature, malformed token, expired token, etc.
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Invalid Refresh Token", e);
        }
    }

    private void validateOtp(String submittedOtp) throws IOException, NoSuchAlgorithmException {
        if (!Files.exists(Path.of(OTP_FILE_PATH))) {
            throw new SecurityException("Invalid OTP.");
        }

        String storedData = new String(Files.readAllBytes(Path.of(OTP_FILE_PATH)));
        String[] parts = storedData.trim().split(":");
        if (parts.length != 2) {
            throw new SecurityException("Malformed OTP data.");
        }

        String storedHash = parts[0];
        long expirationTs = Long.parseLong(parts[1]);

        // 1. Check if expired
        if (Instant.now().getEpochSecond() > expirationTs) {
            throw new SecurityException("OTP has expired.");
        }

        // 2. Check if the hash matches
        String submittedHash = hashSha256(submittedOtp);
        if (!MessageDigest.isEqual(storedHash.getBytes(), submittedHash.getBytes())) {
            throw new SecurityException("Invalid OTP.");
        }
    }

    private String hashSha256(String input) throws NoSuchAlgorithmException {
        MessageDigest digest = MessageDigest.getInstance("SHA-256");
        byte[] hash = digest.digest(input.getBytes(StandardCharsets.UTF_8));
        StringBuilder hexString = new StringBuilder();
        for (byte b : hash) {
            String hex = Integer.toHexString(0xff & b);
            if (hex.length() == 1) hexString.append('0');
            hexString.append(hex);
        }
        return hexString.toString();
    }
}
