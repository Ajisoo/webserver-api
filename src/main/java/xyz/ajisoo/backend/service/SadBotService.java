package xyz.ajisoo.backend.service;

import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.Optional;

@Service
public class SadBotService {

    public boolean isOnline() {
        try {
            // Use --quiet to suppress console output and rely only on the exit code.
            ProcessBuilder processBuilder = new ProcessBuilder("systemctl", "is-active", "--quiet", "sadbot.service");

            // Start the process
            Process process = processBuilder.start();

            // Wait for the process to complete and get its exit code
            int exitCode = process.waitFor();

            // An exit code of 0 means the service is active.
            return exitCode == 0;

        } catch (IOException | InterruptedException e) {
            // Log the exception for debugging purposes.
            // This can happen if 'systemctl' isn't found or the process is interrupted.
            System.err.println("Error checking sadbot.service status: " + e.getMessage());

            // If we can't check the status, it's safest to assume it's offline.
            return false;
        }
    }
}
