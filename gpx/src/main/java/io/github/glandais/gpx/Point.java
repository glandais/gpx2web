package io.github.glandais.gpx;

import io.github.glandais.gpx.storage.Unit;
import io.github.glandais.gpx.storage.Values;
import io.github.glandais.util.Constants;
import io.github.glandais.util.MagicPower2MapSpace;
import io.github.glandais.util.Vector;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import lombok.extern.slf4j.Slf4j;

import java.time.Instant;
import java.util.Date;

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

    public Values getData() {
        return data;
    }

    public Values getDebug() {
        return debug;
    }

    public <J> void put(String key, J value, Unit<J> unit) {
        data.put(key, value, unit);
    }

    public <J> void putDebug(String key, J value, Unit<J> unit) {
        debug.put(key, value, unit);
    }

    public void setLat(Double value) {
        data.put("lat", value, Unit.RADIANS);
    }

    public double getLat() {
        return data.get("lat", Unit.RADIANS);
    }

    public void setLon(Double value) {
        data.put("lon", value, Unit.RADIANS);
    }

    public double getLon() {
        return data.get("lon", Unit.RADIANS);
    }

    public int getLatSemi() {
        return data.get("lat", Unit.SEMI_CIRCLE);
    }

    public int getLonSemi() {
        return data.get("lon", Unit.SEMI_CIRCLE);
    }

    public double getLatDeg() {
        return data.get("lat", Unit.DEGREES);
    }

    public double getLonDeg() {
        return data.get("lon", Unit.DEGREES);
    }

    public Vector project() {

        return new Vector(MagicPower2MapSpace.INSTANCE_256.cLonToX(getLonDeg(), 12),
                MagicPower2MapSpace.INSTANCE_256.cLatToY(getLatDeg(), 12));
    }

    public void setZ(Double value) {
        data.put("z", value, Unit.METERS);
    }

    public double getZ() {
        return data.get("z", Unit.METERS);
    }

    public void setGrade(Double value) {
        data.put("grade", value, Unit.PERCENTAGE);
    }

    public double getGrade() {
        return data.get("grade", Unit.PERCENTAGE);
    }

    public void setPower(Double value) {
        data.put("power", value, Unit.WATTS);
    }

    public double getPower() {
        return data.get("power", Unit.WATTS);
    }

    public void setTime(Instant value) {
        data.put("time", value, Unit.INSTANT);
    }

    public Instant getTime() {
        return data.get("time", Unit.INSTANT);
    }

    public Date getDate() {
        return data.get("time", Unit.DATE);
    }

    public long getEpochMilli() {
        return data.get("time", Unit.EPOCH_MILLIS);
    }

    public double getMaxSpeed() {
        return data.get("max_speed", Unit.SPEED_S_M);
    }

    public void setMaxSpeed(double maxSpeed) {
        data.put("max_speed", maxSpeed, Unit.SPEED_S_M);
    }

    public double getDist() {
        return data.get("dist", Unit.METERS);
    }

    public void setDist(double dist) {
        data.put("dist", dist, Unit.METERS);
    }

    public double getSpeed() {
        return data.get("speed", Unit.SPEED_S_M);
    }

    public void setSpeed(double speed) {
        data.put("speed", speed, Unit.SPEED_S_M);
    }

    public double getBearing() {
        return data.get("bearing", Unit.RADIANS);
    }

    public void setBearing(double bearing) {
        data.put("bearing", bearing, Unit.RADIANS);
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
