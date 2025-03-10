package io.github.glandais.gpx.data;

import io.github.glandais.gpx.data.values.*;
import io.github.glandais.gpx.data.values.unit.StorageUnit;
import io.github.glandais.gpx.util.Constants;
import io.github.glandais.gpx.util.MagicPower2MapSpace;
import io.github.glandais.gpx.util.Vector;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import lombok.extern.slf4j.Slf4j;

import java.time.Duration;
import java.time.Instant;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

@ToString
@EqualsAndHashCode
@Slf4j
public class Point {

    public static Point interpolate(Point p, Point pp1, double coef, long epochMillis) {
        Point point = new Point();
        point.data = p.data.interpolate(pp1.data, coef);
        point.setInstant(Instant.ofEpochMilli(epochMillis), ValueKind.computed);
        return point;
    }

    public static Point interpolate(Point p, Point pp1, double coef) {
        Point point = new Point();
        point.data = p.data.interpolate(pp1.data, coef);
        return point;
    }

    private Values data;

    public Point() {
        super();
//        if (Constants.DEBUG) {
//            this.data = new ValuesWithKind();
//        } else {
        this.data = new ValuesSimple();
//        }
        setInstant(Instant.EPOCH, ValueKind.computed);
    }

    public Values getCsvData() {
        return data;
    }

    public Map<String, String> getGpxData() {
        Map<String, String> values = new HashMap<>();
        for (PointField field : PointField.values()) {
            if (field.isExportGpx()) {
                Value<?, ?> value = data.getCurrent(field.getValueKey());
                if (value != null) {
                    StorageUnit unit = value.unit();
                    values.put(field.getGpxTag(), unit.formatData(value.value()));
                }
            }
        }
        return values;
    }

    public <J> J get(PointField field, Unit<J> unit) {
        return get(field.getValueKey(), unit);
    }

    public <J> J get(ValueKey key, Unit<J> unit) {
        return data.get(key, unit);
    }

    public <J> void put(PointField field, J value, Unit<J> unit, ValueKind kind) {
        data.put(field.getValueKey(), value, unit, kind);
    }

    public <J> void putDebug(ValueKey key, J value, Unit<J> unit) {
        if (Constants.DEBUG) {
            data.put(key, value, unit, ValueKind.debug);
        }
    }

    public void setLat(Double value) {
        put(PointField.lat, value, Unit.RADIANS, ValueKind.source);
    }

    public double getLat() {
        return get(PointField.lat, Unit.RADIANS);
    }

    public void setLon(Double value) {
        put(PointField.lon, value, Unit.RADIANS, ValueKind.source);
    }

    public double getLon() {
        return get(PointField.lon, Unit.RADIANS);
    }

    public int getLatSemi() {
        return get(PointField.lat, Unit.SEMI_CIRCLE);
    }

    public int getLonSemi() {
        return get(PointField.lon, Unit.SEMI_CIRCLE);
    }

    public double getLatDeg() {
        return get(PointField.lat, Unit.DEGREES);
    }

    public double getLonDeg() {
        return get(PointField.lon, Unit.DEGREES);
    }

    public void setEle(Double value, ValueKind kind) {
        put(PointField.ele, value, Unit.METERS, kind);
    }

    public double getEle() {
        return get(PointField.ele, Unit.METERS);
    }

    public void setGrade(Double value, ValueKind kind) {
        put(PointField.grade, value, Unit.PERCENTAGE, kind);
    }

    public double getGrade() {
        return get(PointField.grade, Unit.PERCENTAGE);
    }

    public void setPower(Double value, ValueKind kind) {
        put(PointField.power, value, Unit.WATTS, kind);
    }

    public Double getPower() {
        return get(PointField.power, Unit.WATTS);
    }

    public void setInstant(Instant value, ValueKind kind) {
        put(PointField.time, value, Unit.INSTANT, kind);
    }

    public void computeElapsedTime(Instant start, ValueKind kind) {
        Duration duration = Duration.between(start, getInstant());
        long seconds = duration.getSeconds();
        int nanoAdjustment = duration.getNano();
        double elapsed = seconds + (nanoAdjustment / 1_000_000_000.0);
        put(PointField.elapsed, elapsed, Unit.SECONDS, kind);
    }

    public Instant getInstant() {
        return get(PointField.time, Unit.INSTANT);
    }

    public Double getElapsed() {
        return get(PointField.elapsed, Unit.SECONDS);
    }

    public Date getDate() {
        return get(PointField.time, Unit.DATE);
    }

    public long getEpochMilli() {
        return get(PointField.time, Unit.EPOCH_MILLIS);
    }

    public double getEpochSeconds() {
        return get(PointField.time, Unit.EPOCH_SECONDS);
    }

    public double getSpeedMax() {
        return get(PointField.speed_max, Unit.SPEED_S_M);
    }

    public void setSpeedMax(double maxSpeed) {
        put(PointField.speed_max, maxSpeed, Unit.SPEED_S_M, ValueKind.computed);
    }

    public double getDist() {
        return get(PointField.dist, Unit.METERS);
    }

    public void setDist(double dist, ValueKind kind) {
        put(PointField.dist, dist, Unit.METERS, kind);
    }

    public Double getSpeed() {
        return get(PointField.speed, Unit.SPEED_S_M);
    }

    public void setSpeed(double speed, ValueKind kind) {
        put(PointField.speed, speed, Unit.SPEED_S_M, kind);
    }

    public double getBearing() {
        return get(PointField.bearing, Unit.RADIANS);
    }

    public void setBearing(double bearing, ValueKind kind) {
        put(PointField.bearing, bearing, Unit.RADIANS, kind);
    }

    public Vector project() {

        return new Vector(MagicPower2MapSpace.INSTANCE_256.cLonToX(getLonDeg(), 12),
                MagicPower2MapSpace.INSTANCE_256.cLatToY(getLatDeg(), 12));
    }

    public double distanceTo(Point otherPoint) {
        double lat = getLat();
        double lon = getLon();
        double lat2 = otherPoint.getLat();
        double lon2 = otherPoint.getLon();

        // great circle distance in radians
        double a = Math.sin(lat) * Math.sin(lat2)
                + Math.cos(lat) * Math.cos(lat2) * Math.cos(lon - lon2);
        double alpha = Math.acos(Math.max(-1.0, Math.min(1.0, a)));
        // WGS-84 semi-major axis
        return alpha * Constants.SEMI_MAJOR_AXIS;
    }

    public Point copy() {
        Point point = new Point();
        point.data = this.data.copy();
        return point;
    }
}
