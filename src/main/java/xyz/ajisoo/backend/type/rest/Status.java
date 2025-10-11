package xyz.ajisoo.backend.type.rest;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class Status {

    List<LightStatus> lights;
    boolean sadBotOnline;
}
