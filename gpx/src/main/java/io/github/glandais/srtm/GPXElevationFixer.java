package io.github.glandais.srtm;

import io.github.glandais.gpx.GPXPath;
import io.github.glandais.gpx.Point;
import io.github.glandais.gpx.filter.GPXFilter;
import io.github.glandais.gpx.storage.ValueKind;
import io.github.glandais.util.SmoothService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.inject.Singleton;
import java.util.ArrayList;
import java.util.List;

@RequiredArgsConstructor
@Service
@Singleton
@Slf4j
public class GPXElevationFixer {

    private final SRTMHelper srtmHelper;

    private final SmoothService smoothService;

    public void fixElevation(GPXPath path, boolean addIntermediatePoints) {
        fixElevation(path, addIntermediatePoints, 150);
    }

    public void fixElevation(GPXPath path, boolean addIntermediatePoints, double buffer) {
        log.info("Fixing elevation for {}", path.getName());

        setEleOnPath(path, addIntermediatePoints);
        smoothService.smoothEle(path, buffer);
        if (addIntermediatePoints) {
            GPXFilter.filterPointsDouglasPeucker(path);
        }

        log.info("Fixed elevation for {}", path.getName());
    }

    private void setEleOnPath(GPXPath path, boolean interpolate) {
        log.info("Setting elevations for {} ({})", path.getName(), path.getPoints().size());

        if (interpolate) {
            setEleOnPathInterpolate(path);
        } else {
            setEleOnPathPerPoint(path);
        }

        log.info("Set elevations for {} ({})", path.getName(), path.getPoints().size());
    }

    private void setEleOnPathInterpolate(GPXPath path) {
        List<Point> points = path.getPoints();
        List<Point> newPoints = new ArrayList<>();
        for (int j = 1; j < points.size(); j++) {
            Point p0 = points.get(j - 1);
            Point p1 = points.get(j);
            List<Point> subPoints = srtmHelper.getPointsBetween(p0, p1);
            for (int i = 1; i < subPoints.size(); i++) {
                if (j == 1 && i == 1) {
                    newPoints.add(subPoints.get(0));
                }
                newPoints.add(subPoints.get(i));
            }
        }
        path.setPoints(newPoints, ValueKind.computed);
    }

    private void setEleOnPathPerPoint(GPXPath path) {
        for (Point point : path.getPoints()) {
            point.setEle(srtmHelper.getElevationRad(point.getLon(), point.getLat()), ValueKind.srtm);
        }
    }

}
