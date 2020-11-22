package io.github.glandais.guesser;

import io.github.glandais.gpx.GPXPath;
import io.github.glandais.virtual.Course;
import io.github.glandais.virtual.Cyclist;
import io.github.glandais.virtual.aero.cx.CxProvider;
import io.github.glandais.virtual.PowerProvider;
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
    private double score;

    public CourseWithScore(GPXPath gpxPath, Instant start, Cyclist cyclist, PowerProvider powerProvider, WindProvider windProvider, CxProvider cxProvider) {
        super(gpxPath, start, cyclist, powerProvider, windProvider, cxProvider);
    }
}
