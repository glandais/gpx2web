package io.github.glandais.guesser;

import io.github.glandais.gpx.data.GPXPath;
import io.github.glandais.virtual.Bike;
import io.github.glandais.virtual.Course;
import io.github.glandais.virtual.Cyclist;
import io.github.glandais.virtual.PowerProvider;
import io.github.glandais.virtual.aero.aero.AeroProvider;
import io.github.glandais.virtual.aero.wind.WindProvider;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.time.Instant;

@ToString(callSuper = true)
@EqualsAndHashCode(callSuper = true)
public class CourseWithScore extends Course {

    @Getter
    @Setter
    protected double score;

    public CourseWithScore(GPXPath gpxPath, Instant start, Cyclist cyclist, Bike bike, PowerProvider powerProvider, WindProvider windProvider, AeroProvider aeroProvider) {
        super(gpxPath, start, cyclist, bike, powerProvider, windProvider, aeroProvider);
    }
}
