package io.github.glandais.virtual.aero.wind;

import io.github.glandais.gpx.Point;
import lombok.AllArgsConstructor;

@AllArgsConstructor
public class WindProviderConstant implements WindProvider {

    private final Wind wind;

    @Override
    public Wind getWind(Point location, double ellapsed) {
        return wind;
    }

}
