package org.glandais.gpx.srtm;

public class Point {

	private double lon;
	private double lat;
	private double z;
	private String caption;
	private double dist;
	private long time;
	private double maxSpeed;

	public double getZ() {
		return z;
	}

	public void setZ(double z) {
		this.z = z;
	}

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

	public Point() {
		super();
	}

	public Point(Point point) {
		super();
		this.set(point);
	}

	public double getLon() {
		return lon;
	}

	public void setLon(double lon) {
		this.lon = lon;
	}

	public double getLat() {
		return lat;
	}

	public void setLat(double lat) {
		this.lat = lat;
	}

	public double distanceTo(Point otherPoint) {
		double theta = lon - otherPoint.getLon();
		double dist = Math.sin(deg2rad(lat)) * Math.sin(deg2rad(otherPoint.getLat()))
				+ Math.cos(deg2rad(lat)) * Math.cos(deg2rad(otherPoint.getLat())) * Math.cos(deg2rad(theta));
		dist = Math.max(-1.0, Math.min(1.0, dist));
		dist = Math.acos(dist);
		dist = rad2deg(dist);
		dist = dist * 60 * 1.1515;
		dist = dist * 1.609344;
		return (dist);

	}

	private double deg2rad(double deg) {
		return (deg * Math.PI / 180.0);
	}

	private double rad2deg(double rad) {
		return (rad * 180.0 / Math.PI);
	}

	public String getCaption() {
		return caption;
	}

	public void setCaption(String caption) {
		this.caption = caption;
	}

	public double getDist() {
		return dist;
	}

	public void setDist(double dist) {
		this.dist = dist;
	}

	public long getTime() {
		return time;
	}

	public void setTime(long time) {
		this.time = time;
	}

	public double getMaxSpeed() {
		return maxSpeed;
	}

	public void setMaxSpeed(double maxSpeed) {
		this.maxSpeed = maxSpeed;
	}

	public Point set(Point point) {
		this.time = point.time;
		return set(point.lon, point.lat, point.z);
	}

	public Point set(double lon, double lat, double z) {
		this.lon = lon;
		this.lat = lat;
		this.z = z;
		return this;
	}

	public Point sub(Point point) {
		return sub(point.lon, point.lat, point.z);
	}

	public Point sub(double lon, double lat, double z) {
		return this.set(this.lon - lon, this.lat - lat, this.z - z);
	}

	public Point mul(float f) {
		return this.set(this.lon * f, this.lat * f, this.z * f);
	}

	public Point copy() {
		return new Point(this);
	}

	public Point add(Point p) {
		return this.add(p.lon, p.lat, p.z);
	}

	private Point add(double lon2, double lat2, double z2) {
		return this.set(this.lon + lon2, this.lat + lat2, this.z + z2);
	}

	@Override
	public String toString() {
		return "Point [lon=" + lon + ", lat=" + lat + ", z=" + z + "]";
	}

}
