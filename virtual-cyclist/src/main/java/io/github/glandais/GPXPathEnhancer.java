package io.github.glandais;

import io.github.glandais.gpx.GPXPath;
import io.github.glandais.gpx.GPXPerSecond;
import io.github.glandais.srtm.GPXElevationFixer;
import io.github.glandais.virtual.*;
import io.github.glandais.virtual.cx.CxProviderConstant;
import io.github.glandais.virtual.power.PowerProviderConstant;
import io.github.glandais.virtual.wind.WindProviderNone;
import org.springframework.stereotype.Service;

import java.time.ZonedDateTime;

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
        Course course = new Course(gpxPath, ZonedDateTime.now(), cyclist, new PowerProviderConstant(), new WindProviderNone(), new CxProviderConstant());
        maxSpeedComputer.computeMaxSpeeds(course);
        powerComputer.computeTrack(course);
        gpxPerSecond.computeOnePointPerSecond(gpxPath);
    }
}
