package io.github.glandais.gpx;

import io.github.glandais.gpx.storage.Unit;
import io.github.glandais.gpx.storage.Value;
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
import java.util.stream.Collectors;

@ToString
@EqualsAndHashCode
@Slf4j
public class Point {

    public static Point interpolate(Point p, Point pp1, double coef, long epochMillis) {
        Point point = new Point();
        point.data = Values.interpolate(p.data, pp1.data, coef);
        point.debug = Values.interpolate(p.debug, pp1.debug, coef);
        point.setTime(Instant.ofEpochMilli(epochMillis));
        point.putDebug("coef", coef, Unit.PERCENTAGE);
        return point;
    }

    public Point() {
        super();
        setTime(Instant.EPOCH);
    }

    private Values data = new Values();

    private Values debug = new Values();

    public Values getCsvData() {
        Values values = new Values();
        values.putAll(debug.getData());
        values.putAll(data.getData());
        return values;
    }

    public Map<String, String> getGpxData() {
        Map<String, String> values = new HashMap<>();
        for (PointField field : PointField.values()) {
            if (field.isExportGpx()) {
                Value<?, ?> value = data.get(field.name());
                if (value != null) {
                    StorageUnit unit = value.getUnit();
                    values.put(field.getGpxTag(), unit.formatData(value.getValue()));
                }
            }
        }
        return values;
    }

    public <J> J get(PointField field, Unit<J> unit) {
        return data.get(field.name(), unit);
    }

    public <J> void put(PointField field, J value, Unit<J> unit) {
        data.put(field.name(), value, unit);
    }

    public <J> void putDebug(String key, J value, Unit<J> unit) {
        debug.put(key, value, unit);
    }

    public void setLat(Double value) {
        put(PointField.lat, value, Unit.RADIANS);
    }

    public double getLat() {
        return get(PointField.lat, Unit.RADIANS);
    }

    public void setLon(Double value) {
        put(PointField.lon, value, Unit.RADIANS);
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

    public void setEle(Double value) {
        put(PointField.ele, value, Unit.METERS);
    }

    public double getEle() {
        return get(PointField.ele, Unit.METERS);
    }

    public void setGrade(Double value) {
        put(PointField.grade, value, Unit.PERCENTAGE);
    }

    public double getGrade() {
        return get(PointField.grade, Unit.PERCENTAGE);
    }

    public void setPower(Double value) {
        put(PointField.power, value, Unit.WATTS);
    }

    public double getPower() {
        return get(PointField.power, Unit.WATTS);
    }

    public void setTime(Instant value) {
        put(PointField.time, value, Unit.INSTANT);
    }

    public Instant getTime() {
        return get(PointField.time, Unit.INSTANT);
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

    public double getMaxSpeed() {
        return get(PointField.max_speed, Unit.SPEED_S_M);
    }

    public void setMaxSpeed(double maxSpeed) {
        put(PointField.max_speed, maxSpeed, Unit.SPEED_S_M);
    }

    public double getDist() {
        return get(PointField.dist, Unit.METERS);
    }

    public void setDist(double dist) {
        put(PointField.dist, dist, Unit.METERS);
    }

    public double getSpeed() {
        return get(PointField.speed, Unit.SPEED_S_M);
    }

    public void setSpeed(double speed) {
        put(PointField.speed, speed, Unit.SPEED_S_M);
    }

    public double getBearing() {
        return get(PointField.bearing, Unit.RADIANS);
    }

    public void setBearing(double bearing) {
        put(PointField.bearing, bearing, Unit.RADIANS);
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

    public void backupToDebug() {
        this.debug.putAll(
                data.entrySet().stream().collect(Collectors.toMap(
                        e -> e.getKey() + ".orig",
                        Map.Entry::getValue
                ))
        );
    }

}
