package io.github.glandais.gpx;

import io.github.glandais.gpx.storage.Unit;
import io.github.glandais.gpx.storage.Value;
import io.github.glandais.gpx.storage.ValueKind;
import io.github.glandais.gpx.storage.Values;
import io.github.glandais.gpx.storage.unit.StorageUnit;
import io.github.glandais.util.Constants;
import io.github.glandais.util.MagicPower2MapSpace;
import io.github.glandais.util.Vector;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import lombok.extern.slf4j.Slf4j;

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
        point.data = Values.interpolate(p.data, pp1.data, coef);
        point.setTime(Instant.ofEpochMilli(epochMillis), ValueKind.computed);
        return point;
    }

    public Point() {
        super();
        setTime(Instant.EPOCH, ValueKind.computed);
    }

    private Values data = new Values();

    public Values getCsvData() {
        return data;
    }

    public Map<String, String> getGpxData() {
        Map<String, String> values = new HashMap<>();
        for (PointField field : PointField.values()) {
            if (field.isExportGpx()) {
                Value<?, ?> value = data.getCurrent(field.name());
                if (value != null) {
                    StorageUnit unit = value.getUnit();
                    values.put(field.getGpxTag(), unit.formatData(value.getValue()));
                }
            }
        }
        return values;
    }

    public <J> J getCurrent(PointField field, Unit<J> unit) {
        return data.get(field.name(), unit);
    }

    public <J> void put(PointField field, J value, Unit<J> unit, ValueKind kind) {
        data.put(field.name(), value, unit, kind);
    }

    public <J> void putDebug(String key, J value, Unit<J> unit) {
        data.put(key, value, unit, ValueKind.debug);
    }

    public void setLat(Double value) {
        put(PointField.lat, value, Unit.RADIANS, ValueKind.source);
    }

    public double getLat() {
        return getCurrent(PointField.lat, Unit.RADIANS);
    }

    public void setLon(Double value) {
        put(PointField.lon, value, Unit.RADIANS, ValueKind.source);
    }

    public double getLon() {
        return getCurrent(PointField.lon, Unit.RADIANS);
    }

    public int getLatSemi() {
        return getCurrent(PointField.lat, Unit.SEMI_CIRCLE);
    }

    public int getLonSemi() {
        return getCurrent(PointField.lon, Unit.SEMI_CIRCLE);
    }

    public double getLatDeg() {
        return getCurrent(PointField.lat, Unit.DEGREES);
    }

    public double getLonDeg() {
        return getCurrent(PointField.lon, Unit.DEGREES);
    }

    public void setEle(Double value, ValueKind kind) {
        put(PointField.ele, value, Unit.METERS, kind);
    }

    public double getEle() {
        return getCurrent(PointField.ele, Unit.METERS);
    }

    public void setGrade(Double value, ValueKind kind) {
        put(PointField.grade, value, Unit.PERCENTAGE, kind);
    }

    public double getGrade() {
        return getCurrent(PointField.grade, Unit.PERCENTAGE);
    }

    public void setPower(Double value, ValueKind kind) {
        put(PointField.power, value, Unit.WATTS, kind);
    }

    public double getPower() {
        return getCurrent(PointField.power, Unit.WATTS);
    }

    public void setTime(Instant value, ValueKind kind) {
        put(PointField.time, value, Unit.INSTANT, kind);
    }

    public Instant getTime() {
        return getCurrent(PointField.time, Unit.INSTANT);
    }

    public Date getDate() {
        return getCurrent(PointField.time, Unit.DATE);
    }

    public long getEpochMilli() {
        return getCurrent(PointField.time, Unit.EPOCH_MILLIS);
    }

    public double getEpochSeconds() {
        return getCurrent(PointField.time, Unit.EPOCH_SECONDS);
    }

    public double getMaxSpeed() {
        return getCurrent(PointField.max_speed, Unit.SPEED_S_M);
    }

    public void setMaxSpeed(double maxSpeed) {
        put(PointField.max_speed, maxSpeed, Unit.SPEED_S_M, ValueKind.computed);
    }

    public double getDist() {
        return getCurrent(PointField.dist, Unit.METERS);
    }

    public void setDist(double dist, ValueKind kind) {
        put(PointField.dist, dist, Unit.METERS, kind);
    }

    public double getSpeed() {
        return getCurrent(PointField.speed, Unit.SPEED_S_M);
    }

    public void setSpeed(double speed, ValueKind kind) {
        put(PointField.speed, speed, Unit.SPEED_S_M, kind);
    }

    public double getBearing() {
        return getCurrent(PointField.bearing, Unit.RADIANS);
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

}
