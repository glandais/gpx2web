package io.github.glandais.srtm;

import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import io.github.glandais.gpx.GPXPath;
import io.github.glandais.gpx.Point;
import io.github.glandais.util.SmootherService;
import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class GPXElevationFixer {

	@Autowired
	private SRTMHelper srtmHelper;

	public void fixElevation(GPXPath path) {
		log.info("Fixing elevation for {}", path.getName());

		filterPoints(path);
		setZOnPath(path);
		filterPoints(path);
		smoothZ(path, 0.3);

		log.info("Fixed elevation for {}", path.getName());
	}

	private void setZOnPath(GPXPath path) {
		List<Point> points = path.getPoints();
		log.info("Setting elevations for {} ({})", path.getName(), points.size());
		List<Point> newPoints = new ArrayList<>();
		for (int j = 1; j < points.size() - 1; j++) {
			Point p0 = points.get(j - 1);
			Point p1 = points.get(j);
			List<Point> subPoints = srtmHelper.getPointsBetween(p0, p1);
			for (int i = 1; i < subPoints.size(); i++) {
				if (j == 1 && i == 1) {
					newPoints.add(subPoints.get(0));
				}
				newPoints.add(subPoints.get(i));
			}
		}
		path.setPoints(newPoints);
		log.info("Set elevations for {} ({})", path.getName(), newPoints.size());
	}

	protected void filterPoints(GPXPath path) {
		List<Point> points = path.getPoints();

		log.info("Filtering {} ({})", path.getName(), points.size());
		List<Point> newPoints = new ArrayList<Point>();
		Point lastPoint = null;
		for (int i = 0; i < points.size(); i++) {
			Point p = points.get(i);
			if (i == 0 || i == points.size() - 1) {
				newPoints.add(p);
				lastPoint = p;
			} else {
				if (lastPoint.distanceTo(p) > 0.002) {
					newPoints.add(p);
					lastPoint = p;
				}
			}
		}
		path.setPoints(newPoints);
		log.info("Filtered {} ({} -> {})", path.getName(), points.size(), newPoints.size());
	}

	public void smoothZ(GPXPath path, double buffer) {
		log.info("Smoothing zs");
		List<Point> points = path.getPoints();
		double[] zs = path.getZs();
		double[] dists = path.getDists();
		double[] newZs = new double[zs.length];
		for (int j = 0; j < newZs.length; j++) {
			newZs[j] = SmootherService.computeNewValue(j, buffer, zs, dists);
			Point p = points.get(j);
			p.setZ(newZs[j]);
		}
		path.computeArrays();
		log.info("Smoothed zs");
	}

}
