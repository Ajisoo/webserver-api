package xyz.ajisoo.backend.endpoint;

import io.jsonwebtoken.JwtException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

@RestController
@RequestMapping("/auth")
public class AuthController {
    public static final Logger LOGGER = LoggerFactory.getLogger(AuthController.class);

    @Value("${cookie.verification}")
    boolean verify;

    @Autowired
    private JwtService jwtService;

    @Value("${otp.path}")
    private String OTP_FILE_PATH;

    @PostMapping
    public ResponseEntity<FirstTimeAuthResponse> authenticate(@RequestBody FirstTimeAuthRequest firstTimeAuthRequest) {
        try {
            List<String> roles = validateOtpAndGetRoles(firstTimeAuthRequest.getOtp());

            // If validation is successful, generate the token
            String subject = "user-authenticated-via-otp-" + Instant.now().toEpochMilli();
            String refreshToken = jwtService.generateRefreshToken(subject, roles);

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
            List<String> roles = jwtService.extractRoles(requestRefreshToken);

            // Generate a new access token
            String newAccessToken = jwtService.generateAccessToken(subject, roles);

            // Generate a new refresh token to slide the session window
            String newRefreshToken = jwtService.generateRefreshToken(subject, roles);

            return ResponseEntity.ok(new TokenRefreshResponse(newAccessToken, newRefreshToken));

        } catch (JwtException e) {
            // This catches invalid signature, malformed token, expired token, etc.
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid Refresh Token", e);
        }
    }

    private List<String> validateOtpAndGetRoles(String submittedOtp) throws IOException, NoSuchAlgorithmException {
        if (!verify) {
            return Collections.emptyList();
        }
        if (!Files.exists(Path.of(OTP_FILE_PATH))) {
            throw new SecurityException("Invalid OTP.");
        }

        String storedData = new String(Files.readAllBytes(Path.of(OTP_FILE_PATH)));
        String[] parts = storedData.trim().split(":");
        if (parts.length != 3) {
            throw new SecurityException("Malformed OTP data.");
        }

        String storedHash = parts[0];
        long expirationTs = Long.parseLong(parts[1]);
        String rolesString = parts[2];

        // 1. Check if expired
        if (Instant.now().getEpochSecond() > expirationTs) {
            throw new SecurityException("OTP has expired.");
        }

        // 2. Check if the hash matches
        String submittedHash = hashSha256(submittedOtp);
        if (!MessageDigest.isEqual(storedHash.getBytes(), submittedHash.getBytes())) {
            throw new SecurityException("Invalid OTP.");
        }

        return Arrays.asList(rolesString.split(","));
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
