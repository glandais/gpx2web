package io.github.glandais.srtm;

import io.github.glandais.gpx.GPXPath;
import io.github.glandais.gpx.GPXPerDistance;
import io.github.glandais.gpx.Point;
import io.github.glandais.gpx.filter.GPXFilter;
import io.github.glandais.gpx.storage.ValueKind;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import jakarta.inject.Singleton;

@RequiredArgsConstructor
@Service
@Singleton
@Slf4j
public class GPXElevationFixer {

    private final SRTMHelper srtmHelper;

    private final GPXPerDistance gpxPerDistance;

    public void fixElevation(GPXPath path, boolean addIntermediatePoints) {
        log.debug("Fixing elevation for {}", path.getName());

        if (addIntermediatePoints) {
            gpxPerDistance.computeOnePointPerDistance(path, 10);
        }
        setEleOnPath(path);
        if (addIntermediatePoints) {
            GPXFilter.filterPointsDouglasPeucker(path);
        }

        log.debug("Fixed elevation for {}", path.getName());
    }

    private void setEleOnPath(GPXPath path) {
        log.debug("Setting elevations for {} ({})", path.getName(), path.getPoints().size());

        for (Point point : path.getPoints()) {
            point.setEle(srtmHelper.getElevationRad(point.getLon(), point.getLat()), ValueKind.srtm);
        }

        log.debug("Set elevations for {} ({})", path.getName(), path.getPoints().size());
    }

}
