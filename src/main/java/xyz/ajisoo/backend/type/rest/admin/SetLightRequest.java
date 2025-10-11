package xyz.ajisoo.backend.type.rest.admin;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class SetLightRequest {
    private String name;
    private boolean setOn;
}
