package xyz.ajisoo.backend.service;

import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class SadBotService {

    public boolean isOnline() {
        return ProcessHandle.allProcesses()
                .map(ProcessHandle::info)
                .map(ProcessHandle.Info::commandLine)
                .filter(Optional::isPresent)
                .map(Optional::get)
                .anyMatch(cmd -> cmd.contains("python3 bot.py"));
    }
}
