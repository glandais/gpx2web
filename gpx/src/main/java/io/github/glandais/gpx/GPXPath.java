package io.github.glandais.gpx;

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
        int i = 0;
        for (Point p : points) {
            zs[i] = p.getZ();
            if (previousPoint != null) {
                double d = previousPoint.distanceTo(p);
                dist += d;
            }
            dists[i] = dist;
            p.setDist(dists[i]);
            zs[i] = p.getZ();
            time[i] = p.getTime().toEpochMilli();
            p.getData().put("ellapsed", (time[i] - time[0]) / 1000.0);
            previousPoint = p;
            i++;
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
