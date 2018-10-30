package org.glandais.gpx.srtm;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.graphhopper.reader.dem.SRTMGL1Provider;

public class SRTMHelper {

	private static final Logger LOGGER = LoggerFactory.getLogger(SRTMHelper.class);

	private final File cache;

	private final SRTMGL1Provider srtmgl1Provider;

	public static void main(String[] args) {
		SRTMHelper srtmHelper = new SRTMHelper(new File("cache" + File.separator + "srtm"));
		LOGGER.info("{}", srtmHelper.getElevation(-5, 45));
		LOGGER.info("{}", srtmHelper.getElevation(-4.999999999999, 45.000000000001));
		LOGGER.info("{}", srtmHelper.getElevation(-0.000000000001, 49.999999999999));
		LOGGER.info("{}", srtmHelper.getElevation(0, 50));
		LOGGER.info("{}", srtmHelper.getElevation(-4.999999999999, 49.999999999999));
		LOGGER.info("{}", srtmHelper.getElevation(-5, 50));
		LOGGER.info("{}", srtmHelper.getElevation(-0.000000000001, 45.000000000001));
		LOGGER.info("{}", srtmHelper.getElevation(0, 45));
		// http://maps.google.fr/?ie=UTF8&ll=,&spn=0.008277,0.022745&z=16
		// http://maps.google.fr/?ie=UTF8&ll=47.227357,-1.547876&spn=0.008277,0.022745&z=16
		LOGGER.info("{}", srtmHelper.getElevation(-1.547876, 47.227357));
	}

	public SRTMHelper(File cache) {
		super();
		this.cache = cache;
		this.srtmgl1Provider = new SRTMGL1Provider(this.cache.getAbsolutePath());
	}

	public double getElevation(double lon, double lat) {
		return srtmgl1Provider.getEle(lat, lon);
	}

	private double latToRow(double lat) {
		return (6000 * (60 - lat)) / 5;
	}

	private double lonToCol(double lon) {
		return (6000 * (180 + lon)) / 5;
	}

	private double rowToLat(double row) {
		return 60 - ((row * 5.0) / 6000.0);
	}

	private double colToLon(double col) {
		return ((col * 5.0) / 6000.0) - 180;
	}

	public List<Point> getPointsBetween(final Point p1, Point p2) {
		List<Point> result = new ArrayList<>();
		result.add(p1);
		result.add(p2);

		p1.setZ(getElevation(p1.getLon(), p1.getLat()));
		p2.setZ(getElevation(p2.getLon(), p2.getLat()));

		double dcol1 = lonToCol(p1.getLon());
		double drow1 = latToRow(p1.getLat());

		double dcol2 = lonToCol(p2.getLon());
		double drow2 = latToRow(p2.getLat());

		int mincol = Math.min((int) Math.round(Math.floor(dcol1)), (int) Math.round(Math.floor(dcol2)));
		int maxcol = Math.max((int) Math.round(Math.floor(dcol1)), (int) Math.round(Math.floor(dcol2)));
		int minrow = Math.min((int) Math.round(Math.floor(drow1)), (int) Math.round(Math.floor(drow2)));
		int maxrow = Math.max((int) Math.round(Math.floor(drow1)), (int) Math.round(Math.floor(drow2)));

		if (Math.abs(dcol1 - dcol2) > 0.001) {
			for (int col = mincol + 1; col <= maxcol; col++) {
				double c = (col - dcol1) / (dcol2 - dcol1);
				double drow = drow1 + c * (drow2 - drow1);
				Point p = new Point(colToLon(col), rowToLat(drow));
				p.setZ(getElevation(p.getLon(), p.getLat()));
				result.add(p);
			}
		}
		if (Math.abs(drow1 - drow2) > 0.001) {
			for (int row = minrow + 1; row <= maxrow; row++) {
				double c = (row - drow1) / (drow2 - drow1);
				double dcol = dcol1 + c * (dcol2 - dcol1);
				Point p = new Point(colToLon(dcol), rowToLat(row));
				p.setZ(getElevation(p.getLon(), p.getLat()));
				result.add(p);
			}
		}

		Collections.sort(result, new Comparator<Point>() {
			public int compare(Point cp1, Point cp2) {
				double d1 = p1.distanceTo(cp1);
				double d2 = p1.distanceTo(cp2);
				return Double.compare(d1, d2);
			}
		});

		return result;
	}

}
