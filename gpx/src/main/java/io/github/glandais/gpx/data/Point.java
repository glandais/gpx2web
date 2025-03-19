package io.github.glandais.gpx.data;

import io.github.glandais.gpx.data.values.PropertyKey;
import io.github.glandais.gpx.data.values.PropertyKeys;
import io.github.glandais.gpx.data.values.converter.Converter;
import io.github.glandais.gpx.data.values.converter.Converters;
import io.github.glandais.gpx.data.values.unit.Unit;
import io.github.glandais.gpx.io.GPXField;
import io.github.glandais.gpx.util.Constants;
import io.github.glandais.gpx.util.MagicPower2MapSpace;
import io.github.glandais.gpx.util.Vector;
import java.time.Duration;
import java.time.Instant;
import java.util.*;
import java.util.stream.Collectors;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

@Getter
@EqualsAndHashCode
@Slf4j
public class Point {

    public static Point interpolate(Point p, Point pp1, double coef) {
        Point point = new Point();
        for (PropertyKey<?, ?> propertyKey : PropertyKeys.getList()) {
            interpolate(propertyKey, point, p, pp1, coef);
        }
        return point;
    }

    private static <S, U extends Unit<S>> void interpolate(
            PropertyKey<S, U> propertyKey, Point target, Point p, Point pp1, double coef) {
        S interpolated = interpolate(p, pp1, propertyKey, coef);
        if (interpolated != null) {
            target.rawPut(propertyKey, interpolated);
        }
    }

    private static <S, U extends Unit<S>> S interpolate(
            Point properties, Point data, PropertyKey<S, U> propertyKey, double coef) {
        S s1 = properties.get(propertyKey);
        S s2 = data.get(propertyKey);
        if (s1 != null && s2 != null) {
            return propertyKey.interpolate(s1, s2, coef);
        } else {
            return null;
        }
    }

    private static final int VALUES_SIZE = PropertyKeys.getList().size();

    private final Object[] values;

    public Point() {
        super();
        this.values = new Object[VALUES_SIZE];
        setInstant(null, Instant.EPOCH);
    }

    public Point(Point point) {
        super();
        this.values = Arrays.copyOf(point.values, VALUES_SIZE);
    }

    private <S, U extends Unit<S>> int getIndex(PropertyKey<S, U> key) {
        return key.getOrdinal();
    }

    public <S, U extends Unit<S>, V> void put(PropertyKey<S, U> key, Converter<S, U, V> converter, V value) {
        S storageValue = value == null ? null : converter.convertToStorage(value);
        put(key, storageValue);
    }

    public <S, U extends Unit<S>> void put(PropertyKey<S, U> key, S value) {
        rawPut(key, value);
    }

    private <S, U extends Unit<S>> void rawPut(PropertyKey<S, U> key, S value) {
        values[getIndex(key)] = value;
    }

    public <S, U extends Unit<S>, V> V get(PropertyKey<S, U> key, Converter<S, U, V> converter) {
        S storageValue = get(key);
        return storageValue == null ? null : converter.convertFromStorage(storageValue);
    }

    public <S, U extends Unit<S>> S get(PropertyKey<S, U> key) {
        return (S) values[getIndex(key)];
    }

    public <S, U extends Unit<S>> void putDebug(PropertyKey<S, U> key, S value) {
        if (Constants.DEBUG) {
            this.put(key, value);
        }
    }

    public void setLat(Double value) {
        put(PropertyKeys.lat, value);
    }

    public double getLat() {
        return get(PropertyKeys.lat);
    }

    public void setLon(Double value) {
        put(PropertyKeys.lon, value);
    }

    public double getLon() {
        return get(PropertyKeys.lon);
    }

    public int getLatSemi() {
        return get(PropertyKeys.lat, Converters.SEMI_CIRCLES_CONVERTER);
    }

    public int getLonSemi() {
        return get(PropertyKeys.lon, Converters.SEMI_CIRCLES_CONVERTER);
    }

    public double getLatDeg() {
        return get(PropertyKeys.lat, Converters.DEGREES_CONVERTER);
    }

    public double getLonDeg() {
        return get(PropertyKeys.lon, Converters.DEGREES_CONVERTER);
    }

    public void setEle(Double value) {
        put(PropertyKeys.ele, value);
    }

    public double getEle() {
        Double ele = get(PropertyKeys.ele);
        return ele == null ? 0.0 : ele;
    }

    public void setGrade(Double value) {
        put(PropertyKeys.grade, value);
    }

    public double getGrade() {
        return get(PropertyKeys.grade);
    }

    public void setPower(Double value) {
        put(PropertyKeys.power, value);
    }

    public Double getPower() {
        return get(PropertyKeys.power);
    }

    public void setInstant(Instant start, Instant value) {
        put(PropertyKeys.time, value);
        if (start != null) {
            Duration duration = Duration.between(start, value);
            put(PropertyKeys.elapsed, duration);
        }
    }

    public Instant getInstant() {
        return get(PropertyKeys.time);
    }

    public Duration getElapsed() {
        return get(PropertyKeys.elapsed);
    }

    public Double getElapsedSeconds() {
        return get(PropertyKeys.elapsed, Converters.DURATION_SECONDS_CONVERTER);
    }

    public Date getDate() {
        return get(PropertyKeys.time, Converters.DATE_CONVERTER);
    }

    public double getSpeedMax() {
        return get(PropertyKeys.speed_max);
    }

    public void setSpeedMax(double maxSpeed) {
        put(PropertyKeys.speed_max, maxSpeed);
    }

    public double getDist() {
        return get(PropertyKeys.dist);
    }

    public void setDist(double dist) {
        put(PropertyKeys.dist, dist);
    }

    public Double getSpeed() {
        return get(PropertyKeys.speed);
    }

    public void setSpeed(double speed) {
        put(PropertyKeys.speed, speed);
    }

    public double getBearing() {
        return get(PropertyKeys.bearing);
    }

    public void setBearing(double bearing) {
        put(PropertyKeys.bearing, bearing);
    }

    public Vector project() {

        return new Vector(
                MagicPower2MapSpace.INSTANCE_256.cLonToX(getLonDeg(), 12),
                MagicPower2MapSpace.INSTANCE_256.cLatToY(getLatDeg(), 12));
    }

    public double distanceTo(Point otherPoint) {
        double lat = getLat();
        double lon = getLon();
        double lat2 = otherPoint.getLat();
        double lon2 = otherPoint.getLon();

        // great circle distance in radians
        double a = Math.sin(lat) * Math.sin(lat2) + Math.cos(lat) * Math.cos(lat2) * Math.cos(lon - lon2);
        double alpha = Math.acos(Math.max(-1.0, Math.min(1.0, a)));
        // WGS-84 semi-major axis
        return alpha * Constants.SEMI_MAJOR_AXIS;
    }

    public Point copy() {
        return new Point(this);
    }

    public Map<String, String> getGpxData() {
        Map<String, String> values = new HashMap<>();
        for (GPXField field : GPXField.values()) {
            PropertyKey<Double, ?> propertyKey = field.getPropertyKey();
            Double value = get(propertyKey);
            if (value != null) {
                values.put(field.getGpxTag(), propertyKey.formatHuman(value));
            }
        }
        return values;
    }

    @Override
    public String toString() {
        return PropertyKeys.getList().stream()
                .map(pk -> {
                    Object v = get(pk);
                    if (v != null) {
                        return "[" + pk.getPropertyKeyName() + "]=" + v;
                    } else {
                        return null;
                    }
                })
                .filter(Objects::nonNull)
                .collect(Collectors.joining(","));
    }
}
