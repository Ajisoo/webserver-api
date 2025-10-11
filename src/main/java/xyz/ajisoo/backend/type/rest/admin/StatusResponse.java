package xyz.ajisoo.backend.type.rest.admin;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class StatusResponse {

    List<LightStatus> lights;
    boolean sadBotOnline;
}
