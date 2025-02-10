package io.github.glandais.virtual;

import io.github.glandais.gpx.GPXPerDistance;
import io.github.glandais.gpx.GPXPerSecond;
import io.github.glandais.gpx.data.GPXPath;
import io.github.glandais.gpx.filter.GPXFilter;
import io.github.glandais.srtm.GPXElevationFixer;
import io.github.glandais.virtual.aero.cx.CxProviderConstant;
import io.github.glandais.virtual.aero.wind.WindProviderNone;
import io.github.glandais.virtual.cyclist.PowerProviderConstant;
import jakarta.inject.Singleton;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.Instant;

@RequiredArgsConstructor
@Service
@Singleton
public class GPXPathEnhancer {

    private final GPXElevationFixer gpxElevationFixer;

    private final MaxSpeedComputer maxSpeedComputer;

    private final PowerComputer powerComputer;

    private final GPXPerSecond gpxPerSecond;

    private final GPXPerDistance gpxPerDistance;


    public void virtualize(GPXPath gpxPath, boolean filter) {
        Cyclist cyclist = new Cyclist();
        Course course = new Course(gpxPath, Instant.now(), cyclist, new PowerProviderConstant(), new WindProviderNone(), new CxProviderConstant());
        virtualize(course, filter);
    }

    public void virtualize(Course course, boolean filter) {
        GPXPath gpxPath = course.getGpxPath();
        gpxPerDistance.computeOnePointPerDistance(gpxPath, 10.0);
        gpxElevationFixer.fixElevation(gpxPath);
        maxSpeedComputer.computeMaxSpeeds(course);
        powerComputer.computeTrack(course);
        gpxPerSecond.computeOnePointPerSecond(gpxPath);
        if (filter) {
            GPXFilter.filterPointsDouglasPeucker(gpxPath);
        }
    }
}
