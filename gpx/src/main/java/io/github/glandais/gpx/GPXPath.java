package io.github.glandais.gpx;

import io.github.glandais.gpx.storage.Unit;
import io.github.glandais.gpx.storage.ValueKind;
import io.github.glandais.util.Vector;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.List;

@Data
@Slf4j
@NoArgsConstructor
public class GPXPath {

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
    private String name;

    // m
    private double[] dists;
    // m
    private double dist;
    // m
    private double[] eles;
    // epoch millis
    private long[] time;

    public GPXPath(String name) {
        super();
        this.name = name;
    }

    public void setPoints(List<Point> points, ValueKind kind) {
        this.points = points;
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
            p.put(PointField.ellapsed, (time[i] - time[0]) / 1000.0, Unit.SECONDS, kind);

            previousPoint = p;
        }

        for (int i = 0; i < points.size(); i++) {
            Point p = points.get(i);

            int mini = i - 1;
            while (mini >= 0 && dists[i] - dists[mini] == 0) {
                mini--;
            }
            mini = Math.max(0, mini);

            int maxi = i + 1;
            while (maxi < dists.length && (dists[maxi] - dists[i] == 0)) {
                maxi++;
            }
            maxi = Math.min(dists.length - 1, maxi);

            double dist = dists[maxi] - dists[mini];
            if (dist > 0) {
                double dele = eles[maxi] - eles[mini];
                double grade = dele / dist;
                p.setGrade(grade, kind);

                double dt = time[maxi] - time[mini];
                p.setSpeed(1000.0 * dist / dt, kind);

                Point pmin = points.get(mini);
                Point pmax = points.get(maxi);
                Vector v_from = pmin.project();
                Vector v_to = pmax.project();
                double dy2 = v_to.getY() - v_from.getY();
                double dx2 = v_to.getX() - v_from.getX();
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
