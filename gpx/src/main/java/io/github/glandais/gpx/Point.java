package io.github.glandais.gpx;

import java.util.HashMap;
import java.util.Map;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class Point {

	private double lon;
	private double lat;
	private double z;
	private String caption;
	private double dist;
	private long time;
	private double maxSpeed;

	private Map<String, Double> data = new HashMap<>();

	public Point(double lon, double lat) {
		super();
		this.lon = lon;
		this.lat = lat;
	}

	public Point(double lon, double lat, double z) {
		super();
		this.lon = lon;
		this.lat = lat;
		this.z = z;
	}

	public Point(double lon, double lat, double z, long time) {
		super();
		this.lon = lon;
		this.lat = lat;
		this.z = z;
		this.time = time;
	}

	public double distanceTo(Point otherPoint) {
		double theta = lon - otherPoint.getLon();
		double distance = Math.sin(deg2rad(lat)) * Math.sin(deg2rad(otherPoint.getLat()))
				+ Math.cos(deg2rad(lat)) * Math.cos(deg2rad(otherPoint.getLat())) * Math.cos(deg2rad(theta));
		distance = Math.max(-1.0, Math.min(1.0, distance));
		distance = Math.acos(distance);
		distance = rad2deg(distance);
		distance = distance * 60 * 1.1515;
		distance = distance * 1.609344;
		return distance;
	}

	private double deg2rad(double deg) {
		return (deg * Math.PI / 180.0);
	}

	private double rad2deg(double rad) {
		return (rad * 180.0 / Math.PI);
	}

}
