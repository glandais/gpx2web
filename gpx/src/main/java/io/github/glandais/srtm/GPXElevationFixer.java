package io.github.glandais.srtm;

import io.github.glandais.gpx.GPXFilter;
import io.github.glandais.gpx.GPXPath;
import io.github.glandais.gpx.Point;
import io.github.glandais.util.SmootherService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
@Slf4j
public class GPXElevationFixer {

    private SRTMHelper srtmHelper;

    public GPXElevationFixer(SRTMHelper srtmHelper) {
        super();
        this.srtmHelper = srtmHelper;
    }

    public void fixElevation(GPXPath path) {
        fixElevation(path, true);
    }

    public void fixElevation(GPXPath path, boolean interpolate) {
        log.info("Fixing elevation for {}", path.getName());

        if (interpolate) {
            GPXFilter.filterPointsDouglasPeucker(path);
        }
        setZOnPath(path, interpolate);
        smoothZ(path, 300);
        if (interpolate) {
            GPXFilter.filterPointsDouglasPeucker(path);
        }

        log.info("Fixed elevation for {}", path.getName());
    }

    private void setZOnPath(GPXPath path, boolean interpolate) {
        log.info("Setting elevations for {} ({})", path.getName(), path.getPoints().size());

        if (interpolate) {
            setZOnPathInterpolate(path);
        } else {
            setZOnPathPerPoint(path);
        }

        log.info("Set elevations for {} ({})", path.getName(), path.getPoints().size());
    }

    private void setZOnPathInterpolate(GPXPath path) {
        List<Point> points = path.getPoints();
        List<Point> newPoints = new ArrayList<>();
        for (int j = 1; j < points.size() - 1; j++) {
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
        path.setPoints(newPoints);
    }

    private void setZOnPathPerPoint(GPXPath path) {
        for (Point point : path.getPoints()) {
            point.setZ(srtmHelper.getElevationRad(point.getLon(), point.getLat()));
        }
    }

    public void smoothZ(GPXPath path, double buffer) {
        log.info("Smoothing zs");
        List<Point> points = path.getPoints();
        double[] zs = path.getZs();
        double[] dists = path.getDists();
        double[] newZs = new double[zs.length];
        for (int j = 0; j < newZs.length; j++) {
            newZs[j] = SmootherService.computeNewValue(j, buffer, zs, dists);
            Point p = points.get(j);
            p.setZ(newZs[j]);
        }
        path.computeArrays();
        log.info("Smoothed zs");
    }

}
