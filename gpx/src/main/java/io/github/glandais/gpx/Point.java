package io.github.glandais.gpx;

import io.github.glandais.util.Constants;
import lombok.Builder;
import lombok.Data;

import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.HashMap;
import java.util.Map;

@Data
@Builder
public class Point {

    public static final ZonedDateTime EPOCH = ZonedDateTime.ofInstant(Instant.EPOCH, ZoneId.of("UTC"));

    public static int toSemiCircles(double rad) {
        return (int) (rad * 2147483648.0 / Math.PI);
    }

    // deg
    private double lon;
    // deg
    private double lat;
    // m
    private double z;

    @Builder.Default
    private ZonedDateTime time = EPOCH;

    @Builder.Default
    private Map<String, Double> data = new HashMap<>();

    public double distanceTo(Point otherPoint) {
        double lat2 = otherPoint.getLat();
        double lon2 = otherPoint.getLon();

        // great circle distance in radians
        double a = Math.sin(lat) * Math.sin(lat2)
                + Math.cos(lat) * Math.cos(lat2) * Math.cos(lon - lon2);
        double alpha = Math.acos(Math.max(-1.0, Math.min(1.0, a)));
        // WGS-84 semi-major axis
        return alpha * Constants.SEMI_MAJOR_AXIS;
    }


    public int getLatSemi() {
        return toSemiCircles(lat);
    }

    public int getLonSemi() {
        return toSemiCircles(lon);
    }

    public double getLatDeg() {
        return Math.toDegrees(lat);
    }

    public double getLonDeg() {
        return Math.toDegrees(lon);
    }

    public double getMaxSpeed() {
        return data.get("max_speed");
    }

    public void setMaxSpeed(double maxSpeed) {
        data.put("max_speed", maxSpeed);
        data.put("max_speed_kmh", maxSpeed * 3.6);
    }

    public double getDist() {
        return data.get("dist");
    }

    public void setDist(double dist) {
        data.put("dist", dist);
        data.put("dist_km", dist / 1000.0);
    }

    public double getSpeed() {
        return data.get("speed");
    }

    public void setSpeed(double speed) {
        data.put("speed", speed);
        data.put("speed_kmh", speed * 3.6);
    }
}
