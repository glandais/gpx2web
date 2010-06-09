package org.glandais.gpx.elevation.fixer;

import org.glandais.srtm.loader.Point;
import org.w3c.dom.Element;

public class GPXPoint extends Point {

	private Element eleEle;
	private Element timeEle;

	public GPXPoint(double lon, double lat, Element eleEle, Element timeEle) {
		super(lon, lat);
		this.eleEle = eleEle;
		this.timeEle = timeEle;
	}

	public Element getEleEle() {
		return eleEle;
	}

	public Element getTimeEle() {
		return timeEle;
	}

}
