package io.github.glandais.gpx.data;

import io.github.glandais.gpx.data.values.PropertyKey;
import io.github.glandais.gpx.data.values.unit.DoubleUnit;
import io.github.glandais.gpx.util.Vector;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

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
    // nanos from start
    private long[] nanos;

    // Optimization: elapsed seconds array for fast average computation
    private double[] elapsedSeconds;

    // Fast index for binary search optimization
    private boolean indexComputed = false;
    private FastTimeIndex timeIndex;

    public GPXPath(String name, GPXPathType type) {
        super();
        this.name = name;
        this.type = type;
    }

    public void setPoints(List<Point> points) {
        this.points = new ArrayList<>(points);
        computeArrays();
    }

    public void addPoint(Point p) {
        points.add(p);
    }

    public void computeArrays() {
        if (points.isEmpty()) {
            dists = new double[0];
            eles = new double[0];
            nanos = new long[0];
            elapsedSeconds = new double[0];
            indexComputed = false;
            timeIndex = null;
            return;
        }

        Instant start = points.get(0).getInstant();

        Point previousPoint = null;
        dist = 0;
        dists = new double[points.size()];
        eles = new double[points.size()];
        nanos = new long[points.size()];
        elapsedSeconds = new double[points.size()];
        indexComputed = false;
        timeIndex = null; // Will be created on demand

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
            p.setInstant(start, p.getInstant());
            eles[i] = p.getEle();
            nanos[i] = p.getElapsed().toNanos();
            elapsedSeconds[i] = p.getElapsedSeconds();

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
            p.setDist(dists[i]);

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
                p.setGrade(grade);

                double dt = nanos[maxi] - nanos[i];
                if (dt > 0) {
                    double speed = 1_000_000_000.0 * dist / dt;
                    p.setSpeed(speed);
                }

                Point pmin = points.get(i);
                Point pmax = points.get(maxi);
                Vector v_from = pmin.project();
                Vector v_to = pmax.project();
                double dy2 = v_to.y() - v_from.y();
                double dx2 = v_to.x() - v_from.x();
                double bearing = Math.atan2(-dy2, dx2);

                p.setBearing(bearing);
            } else {
                p.setGrade(0.0);
                p.setBearing(0.0);
            }
        }

        log.debug("{} {} {} {} {} {}", minlon, maxlon, minlat, maxlat, minElevation, maxElevation);
    }

    /**
     * Computes the weighted average of a property between two elapsed time points with interpolation.
     * Example: for points (0,10),(10,20),(15,30) and average between 5 and 12:
     * - Interpolated points: (5,15),(10,20),(12,24)
     * - Weighted average: (5*15)/2 + (2*24)/2 = 18.5
     *
     * @param from Start elapsed time in seconds
     * @param to End elapsed time in seconds
     * @param property The property to average (e.g., power, heartRate, elevation)
     * @return Weighted average value
     */
    public double getAverage(final double from, final double to, PropertyKey<Double, DoubleUnit> property) {
        if (points.isEmpty()) {
            return 0.0;
        }
        if (from > to) {
            return getAverage(to, from, property);
        }
        // Create time index on demand for fast lookups
        if (timeIndex == null) {
            timeIndex = new FastTimeIndex(this);
        }
        if (from == to) {
            return interpolatePropertyOptimized(from, property);
        }

        // Use trapezoidal rule for accurate integration
        double totalArea = 0.0;
        double totalTime = to - from;

        // Find all time points in the range [from, to]
        List<Double> timePoints = new ArrayList<>();
        timePoints.add(from);

        // Add existing data points that fall within the range using fast index
        int startIdx = Math.max(0, timeIndex.findTimeIndex(from));
        int endIdx = Math.min(points.size() - 1, timeIndex.findTimeIndex(to) + 1);

        for (int i = startIdx; i <= endIdx; i++) {
            Double elapsed = points.get(i).getElapsedSeconds();
            if (elapsed != null && elapsed > from && elapsed < to) {
                timePoints.add(elapsed);
            }
        }

        timePoints.add(to);

        // Sort time points (should already be sorted, but ensure correctness)
        timePoints.sort(Double::compareTo);

        // Calculate trapezoidal areas between consecutive time points
        for (int i = 0; i < timePoints.size() - 1; i++) {
            double t1 = timePoints.get(i);
            double t2 = timePoints.get(i + 1);

            if (t2 > t1) {
                double v1 = interpolatePropertyOptimized(t1, property);
                double v2 = interpolatePropertyOptimized(t2, property);

                if (!Double.isNaN(v1) && !Double.isNaN(v2)) {
                    // Trapezoidal area = (v1 + v2) * (t2 - t1) / 2
                    totalArea += (v1 + v2) * (t2 - t1) / 2.0;
                }
            }
        }

        return totalTime > 0 ? totalArea / totalTime : 0.0;
    }

    /**
     * Optimized interpolation using FastTimeIndex
     */
    private double interpolatePropertyOptimized(double targetTime, PropertyKey<Double, DoubleUnit> property) {
        if (points.isEmpty()) {
            return 0.0;
        }

        int idx = timeIndex.findTimeIndex(targetTime);

        if (idx < 0) {
            // Before first point
            Double value = points.get(0).get(property);
            return value != null ? value : 0.0;
        }

        if (idx >= points.size() - 1) {
            // After last point
            Double value = points.get(points.size() - 1).get(property);
            return value != null ? value : 0.0;
        }

        // Check for exact match
        Double exactElapsed = points.get(idx).getElapsedSeconds();
        if (exactElapsed != null && exactElapsed == targetTime) {
            Double value = points.get(idx).get(property);
            return value != null ? value : 0.0;
        }

        // Linear interpolation between points idx and idx+1
        Point p1 = points.get(idx);
        Point p2 = points.get(idx + 1);

        Double t1 = p1.getElapsedSeconds();
        Double t2 = p2.getElapsedSeconds();
        Double v1 = p1.get(property);
        Double v2 = p2.get(property);

        if (t1 == null || t2 == null || v1 == null || v2 == null || t2.equals(t1)) {
            return v1 != null ? v1 : (v2 != null ? v2 : 0.0);
        }

        // Linear interpolation
        double ratio = (targetTime - t1) / (t2 - t1);
        return v1 + ratio * (v2 - v1);
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
