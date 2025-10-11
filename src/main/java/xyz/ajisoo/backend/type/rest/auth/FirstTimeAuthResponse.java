package xyz.ajisoo.backend.type.rest.auth;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public class FirstTimeAuthResponse {
    private String refreshToken;
}
