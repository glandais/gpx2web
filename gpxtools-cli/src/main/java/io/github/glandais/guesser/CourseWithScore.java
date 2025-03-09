package io.github.glandais.guesser;

import io.github.glandais.gpx.data.GPXPath;
import io.github.glandais.gpx.virtual.Bike;
import io.github.glandais.gpx.virtual.Course;
import io.github.glandais.gpx.virtual.Cyclist;
import io.github.glandais.gpx.virtual.power.aero.aero.AeroProvider;
import io.github.glandais.gpx.virtual.power.aero.wind.WindProvider;
import io.github.glandais.gpx.virtual.power.cyclist.CyclistPowerProvider;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.time.Instant;

@ToString(callSuper = true)
@EqualsAndHashCode(callSuper = true)
@Getter
@Setter
public class CourseWithScore extends Course {

    protected double score;

    public CourseWithScore(GPXPath gpxPath, Instant start, Cyclist cyclist, Bike bike, CyclistPowerProvider powerProvider, WindProvider windProvider, AeroProvider aeroProvider) {
        super(gpxPath, start, cyclist, bike, powerProvider, windProvider, aeroProvider);
    }
}
