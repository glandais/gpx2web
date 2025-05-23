package io.github.glandais.gpx.srtm;

import io.github.glandais.gpx.data.GPXPath;
import io.github.glandais.gpx.data.Point;
import io.github.glandais.gpx.util.SmoothService;
import jakarta.inject.Singleton;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@RequiredArgsConstructor
@Service
@Singleton
@Slf4j
public class GPXElevationFixer {

    private final GpxElevationProvider gpxElevationProvider;

    private final SmoothService smoothService;

    public void fixElevation(GPXPath path) {
        log.debug("Fixing elevation for {}", path.getName());

        setEleOnPath(path);
        smoothService.smoothEle(path, 150);

        log.debug("Fixed elevation for {}", path.getName());
    }

    private void setEleOnPath(GPXPath path) {
        log.debug(
                "Setting elevations for {} ({})",
                path.getName(),
                path.getPoints().size());

        for (Point point : path.getPoints()) {
            point.setEle(gpxElevationProvider.getElevationRad(point.getLon(), point.getLat()));
        }

        log.debug("Set elevations for {} ({})", path.getName(), path.getPoints().size());
    }
}
