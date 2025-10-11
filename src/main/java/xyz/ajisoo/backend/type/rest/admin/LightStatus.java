package xyz.ajisoo.backend.type.rest.admin;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class LightStatus {

    String name;
    boolean on;
    String error;
}
