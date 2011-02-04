public class GPXPoint {

	private double lon;
	private double lat;
	private String caption;
	private double dist;
	private long time;

	public GPXPoint(double lon, double lat) {
		super();
		this.lon = lon;
		this.lat = lat;
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

	public double distanceTo(GPXPoint otherPoint) {
		double theta = lon - otherPoint.getLon();
		double dist = Math.sin(deg2rad(lat))
				* Math.sin(deg2rad(otherPoint.getLat()))
				+ Math.cos(deg2rad(lat))
				* Math.cos(deg2rad(otherPoint.getLat()))
				* Math.cos(deg2rad(theta));
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

}
