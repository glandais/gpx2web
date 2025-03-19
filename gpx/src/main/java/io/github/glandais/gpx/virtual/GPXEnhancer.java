package io.github.glandais.gpx.virtual;

import io.github.glandais.gpx.data.GPX;
import io.github.glandais.gpx.data.GPXPath;
import io.github.glandais.gpx.data.Point;
import io.github.glandais.gpx.filter.GPXFilter;
import io.github.glandais.gpx.filter.GPXPerDistance;
import io.github.glandais.gpx.filter.GPXPerSecond;
import io.github.glandais.gpx.srtm.GPXElevationFixer;
import io.github.glandais.gpx.virtual.maxspeed.MaxSpeedComputer;
import io.github.glandais.gpx.virtual.power.aero.aero.AeroProviderConstant;
import io.github.glandais.gpx.virtual.power.aero.wind.WindProviderNone;
import io.github.glandais.gpx.virtual.power.cyclist.PowerProviderConstant;
import jakarta.inject.Singleton;
import java.time.Instant;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@RequiredArgsConstructor
@Service
@Singleton
public class GPXEnhancer {

    private final GPXElevationFixer gpxElevationFixer;

    private final MaxSpeedComputer maxSpeedComputer;

    private final VirtualizeService virtualizeService;

    private final GPXPerSecond gpxPerSecond;

    private final GPXPerDistance gpxPerDistance;

    private final StartTimeProvider startTimeProvider;

    public void virtualize(GPX gpx, boolean filter) {
        for (int i = 0; i < gpx.paths().size(); i++) {
            virtualize(gpx.paths().get(i), filter, i + 1);
        }
    }

    public void virtualize(GPXPath gpxPath, boolean filter) {
        virtualize(gpxPath, filter, 1);
    }

    public void virtualize(GPXPath gpxPath, boolean filter, int ddays) {
        Cyclist cyclist = Cyclist.getDefault();
        Bike bike = Bike.getDefault();

        Point point = gpxPath.getPoints().get(0);
        Instant instant = startTimeProvider.getStart(point, ddays);

        Course course = new Course(
                gpxPath,
                instant,
                cyclist,
                bike,
                new PowerProviderConstant(),
                new WindProviderNone(),
                new AeroProviderConstant());
        virtualize(course, filter);
    }

    public void virtualize(Course course, boolean filter) {
        GPXPath gpxPath = course.getGpxPath();
        gpxPerDistance.computeOnePointPerDistance(gpxPath, 10.0);
        gpxElevationFixer.fixElevation(gpxPath);
        maxSpeedComputer.computeMaxSpeeds(course);
        virtualizeService.virtualizeTrack(course);
        gpxPerSecond.computeOnePointPerSecond(gpxPath);
        if (filter) {
            GPXFilter.filterPointsDouglasPeucker(gpxPath);
        }
    }
}
