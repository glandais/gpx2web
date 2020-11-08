package io.github.glandais.gpx;

import io.github.glandais.gpx.storage.Unit;
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
    private double[] zs;
    // epoch millis
    private long[] time;

    public GPXPath(String name) {
        super();
        this.name = name;
    }

    public void setPoints(List<Point> points) {
        this.points = points;
        computeArrays();
    }

    public void addPoint(Point p) {
        points.add(p);
    }

    public void computeArrays() {
        Point previousPoint = null;
        dist = 0;
        dists = new double[points.size()];
        zs = new double[points.size()];
        time = new long[points.size()];
        for (int i = 0; i < points.size(); i++) {
            Point p = points.get(i);
            zs[i] = p.getZ();
            if (previousPoint != null) {
                double d = previousPoint.distanceTo(p);
                dist += d;
            }
            dists[i] = dist;
            p.setDist(dists[i]);
            zs[i] = p.getZ();
            time[i] = p.getEpochMilli();
            p.put("ellapsed", (time[i] - time[0]) / 1000.0, Unit.SECONDS);
            previousPoint = p;
        }

        minElevation = Double.MAX_VALUE;
        maxElevation = -Double.MAX_VALUE;
        totalElevation = 0;
        totalElevationNegative = 0;
        minlon = Double.MAX_VALUE;
        maxlon = -Double.MAX_VALUE;
        minlat = Double.MAX_VALUE;
        maxlat = -Double.MAX_VALUE;

        double previousElevation = 0;
        for (int j = 0; j < points.size(); j++) {
            Point p = points.get(j);
            double lon = p.getLon();
            double lat = p.getLat();
            minlon = Math.min(minlon, lon);
            maxlon = Math.max(maxlon, lon);
            minlat = Math.min(minlat, lat);
            maxlat = Math.max(maxlat, lat);

            double elevation = p.getZ();
            minElevation = Math.min(minElevation, elevation);
            maxElevation = Math.max(maxElevation, elevation);
            if (j > 0) {
                double dz = elevation - previousElevation;
                if (dz > 0) {
                    totalElevation += dz;
                } else {
                    totalElevationNegative += dz;
                }
            }
            previousElevation = elevation;
        }

        Point cur = points.get(0);
        cur.putDebug("i", 0, Unit.INT_ANY);
        cur.setGrade(0.0);
        cur.setBearing(0.0);
        for (int i = 1; i < points.size(); i++) {
            Point p = points.get(i - 1);
            p.putDebug("i", i - 1, Unit.INT_ANY);

            Point pp1 = points.get(i);
            if (dists[i] - dists[i - 1] == 0) {
                p.setGrade(cur.getGrade());
                p.setBearing(cur.getBearing());
            } else {
                double dz = pp1.getZ() - p.getZ();
                double grad = dz / (dists[i] - dists[i - 1]);

                Vector v_from = p.project();
                Vector v_to = pp1.project();
                double dy2 = v_to.getY() - v_from.getY();
                double dx2 = v_to.getX() - v_from.getX();
                double bearing = Math.atan2(-dy2, dx2);

                p.setGrade(grad);
                p.setBearing(bearing);
                cur = p;
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
