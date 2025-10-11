package xyz.ajisoo.backend.type.rest.auth;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class FirstTimeAuthRequest {
    private String otp;
}
