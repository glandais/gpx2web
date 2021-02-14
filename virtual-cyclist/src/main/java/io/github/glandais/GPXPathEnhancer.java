package io.github.glandais;

import io.github.glandais.gpx.GPXPath;
import io.github.glandais.gpx.GPXPerSecond;
import io.github.glandais.srtm.GPXElevationFixer;
import io.github.glandais.virtual.Course;
import io.github.glandais.virtual.Cyclist;
import io.github.glandais.virtual.MaxSpeedComputer;
import io.github.glandais.virtual.PowerComputer;
import io.github.glandais.virtual.aero.cx.CxProviderConstant;
import io.github.glandais.virtual.aero.wind.WindProviderNone;
import io.github.glandais.virtual.cyclist.PowerProviderConstant;
import org.springframework.stereotype.Service;

import java.time.Instant;

@Service
public class GPXPathEnhancer {

    private final GPXElevationFixer gpxElevationFixer;

    private final MaxSpeedComputer maxSpeedComputer;

    private final PowerComputer powerComputer;

    private final GPXPerSecond gpxPerSecond;

    public GPXPathEnhancer(final GPXElevationFixer gpxElevationFixer,
                           final MaxSpeedComputer maxSpeedComputer,
                           final PowerComputer powerComputer,
                           final GPXPerSecond gpxPerSecond) {

        this.gpxElevationFixer = gpxElevationFixer;
        this.maxSpeedComputer = maxSpeedComputer;
        this.powerComputer = powerComputer;
        this.gpxPerSecond = gpxPerSecond;
    }

    public void virtualize(GPXPath gpxPath) {
        gpxElevationFixer.fixElevation(gpxPath);
        Cyclist cyclist = new Cyclist();
        Course course = new Course(gpxPath, Instant.now(), cyclist, new PowerProviderConstant(), new WindProviderNone(), new CxProviderConstant());
        maxSpeedComputer.computeMaxSpeeds(course);
        powerComputer.computeTrack(course);
        gpxPerSecond.computeOnePointPerSecond(gpxPath);
    }
}
