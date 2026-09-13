package xyz.ajisoo.backend.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import xyz.ajisoo.backend.type.entity.AppleSession;

public interface AppleSessionRepository extends JpaRepository<AppleSession, String> {

}
