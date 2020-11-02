package io.github.glandais.virtual.wind;

import io.github.glandais.gpx.Point;
import lombok.AllArgsConstructor;

import java.time.Duration;
import java.time.ZonedDateTime;

@AllArgsConstructor
public class WindProviderConstant implements WindProvider {

    private final Wind wind;

    @Override
    public Wind getWind(Point location, ZonedDateTime now, Duration ellapsed) {
        return wind;
    }

}
