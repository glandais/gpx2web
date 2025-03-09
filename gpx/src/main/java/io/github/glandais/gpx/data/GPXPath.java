package io.github.glandais.gpx.data;

import io.github.glandais.gpx.data.values.Unit;
import io.github.glandais.gpx.data.values.ValueKind;
import io.github.glandais.gpx.util.Vector;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.List;

@Getter
@Slf4j
@NoArgsConstructor
public class GPXPath {

    private GPXPathType type;

    @Setter
    private String name;

    // m
    private double minElevation;
    // m
    private double maxElevation;
    // m
    private double totalElevation;
    // m
    private double totalElevationNegative;

    // rad
    private double minlon;
    private double maxlon;
    private double minlat;
    private double maxlat;

    private List<Point> points = new ArrayList<>();

    // m
    private double[] dists;
    // m
    private double dist;
    // m
    private double[] eles;
    // epoch millis
    private long[] time;

    public GPXPath(String name, GPXPathType type) {
        super();
        this.name = name;
        this.type = type;
    }

    public void setPoints(List<Point> points, ValueKind kind) {
        this.points = new ArrayList<>(points);
        computeArrays(kind);
    }

    public void addPoint(Point p) {
        points.add(p);
    }

    public void computeArrays(ValueKind kind) {
        Point previousPoint = null;
        dist = 0;
        dists = new double[points.size()];
        eles = new double[points.size()];
        time = new long[points.size()];

        minElevation = Double.MAX_VALUE;
        maxElevation = -Double.MAX_VALUE;
        totalElevation = 0;
        totalElevationNegative = 0;
        minlon = Double.MAX_VALUE;
        maxlon = -Double.MAX_VALUE;
        minlat = Double.MAX_VALUE;
        maxlat = -Double.MAX_VALUE;

        for (int i = 0; i < points.size(); i++) {
            Point p = points.get(i);
            eles[i] = p.getEle();
            time[i] = p.getEpochMilli();

            double lon = p.getLon();
            double lat = p.getLat();
            minlon = Math.min(minlon, lon);
            maxlon = Math.max(maxlon, lon);
            minlat = Math.min(minlat, lat);
            maxlat = Math.max(maxlat, lat);
            minElevation = Math.min(minElevation, eles[i]);
            maxElevation = Math.max(maxElevation, eles[i]);

            if (previousPoint != null) {
                double d = previousPoint.distanceTo(p);
                dist += d;
                double dele = eles[i] - previousPoint.getEle();
                if (dele > 0) {
                    totalElevation += dele;
                } else {
                    totalElevationNegative += dele;
                }
            }
            dists[i] = dist;
            p.setDist(dists[i], kind);
            p.computeElapsedTime(points.get(0).getInstant(), kind);

            previousPoint = p;
        }

        for (int i = 0; i < points.size(); i++) {
            Point p = points.get(i);

            int maxi = i + 1;
            while (maxi < dists.length && (dists[maxi] - dists[i] == 0)) {
                maxi++;
            }
            maxi = Math.min(dists.length - 1, maxi);

            double dist = dists[maxi] - dists[i];
            if (dist > 0) {
                double dele = eles[maxi] - eles[i];
                double grade = dele / dist;
                p.setGrade(grade, kind);

                double dt = time[maxi] - time[i];
                p.putDebug("dist_computed", dist, Unit.METERS);
                p.setSpeed(1000.0 * dist / dt, kind);

                Point pmin = points.get(i);
                Point pmax = points.get(maxi);
                Vector v_from = pmin.project();
                Vector v_to = pmax.project();
                double dy2 = v_to.y() - v_from.y();
                double dx2 = v_to.x() - v_from.x();
                double bearing = Math.atan2(-dy2, dx2);

                p.setBearing(bearing, kind);
            } else {
                p.setGrade(0.0, kind);
                p.setBearing(0.0, kind);
            }
        }

        log.debug("{} {} {} {} {} {}", minlon, maxlon, minlat, maxlat, minElevation, maxElevation);
    }

    public int size() {
        return points.size();
    }

    public double getMinlatDeg() {
        return Math.toDegrees(minlat);
    }

    public double getMinlonDeg() {
        return Math.toDegrees(minlon);
    }

    public double getMaxlatDeg() {
        return Math.toDegrees(maxlat);
    }

    public double getMaxlonDeg() {
        return Math.toDegrees(maxlon);
    }

}
