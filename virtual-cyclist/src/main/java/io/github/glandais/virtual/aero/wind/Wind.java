package io.github.glandais.virtual.aero.wind;

import lombok.Data;

@Data
public class Wind {

    // m.s-2
    private final double windSpeed;

    // rad (0 = N, Pi/2 = E)
    private final double windDirection;

}
