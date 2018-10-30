package org.glandais.gpx.elevation.fixer;

import java.util.ArrayList;
import java.util.List;

import org.glandais.gpx.srtm.Point;

public class GPXPostProcessor {

	protected GPXPostProcessor() {
		super();
	}

	public static void filterPoints(GPXPath path) {
		List<Point> points = path.getPoints();
		List<Point> newPoints = new ArrayList<>();
		Point lastPoint = null;
		for (int i = 0; i < points.size(); i++) {
			Point p = points.get(i);
			if (i == 0 || i == points.size() - 1) {
				newPoints.add(p);
				lastPoint = p;
			} else {
				double distance = lastPoint.distanceTo(p);
				if (!Double.toString(distance).contains("NaN") || distance >= 0.002) {
					newPoints.add(p);
					lastPoint = p;
				}
			}
		}
		path.setPoints(newPoints);
	}

	public static void smoothAltitudes(GPXPath gpxPath) {
		double[] zs = gpxPath.getZs();
		double[] dists = gpxPath.getDists();
		double[] newZs = new double[zs.length];
		for (int j = 0; j < zs.length; j++) {
			newZs[j] = computeNewValue(j, 1, 1, zs, dists);
			Point p = gpxPath.getPoints().get(j);
			p.setZ(newZs[j]);
			p.setDist(dists[j]);
		}
		gpxPath.updateArrays();
	}

	public static double computeNewValue(int i, double before, double after, double[] data, double[] dists) {
		double ac = dists[i];

		int mini = i - 1;
		while (mini >= 0 && (ac - dists[mini]) <= before) {
			mini--;
		}
		mini++;

		int maxi = i + 1;
		while (maxi < data.length && (dists[maxi] - ac) <= after) {
			maxi++;
		}

		double totc = 0;
		double totz = 0;
		for (int j = mini; j < maxi; j++) {
			double c = 1 - (Math.abs(dists[j] - ac) / Math.max(after, before));
			totc = totc + c;
			totz = totz + data[j] * c;
		}

		if (totc == 0) {
			return data[i];
		} else {
			return totz / totc;
		}

	}
}
