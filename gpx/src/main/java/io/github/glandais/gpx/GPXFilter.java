package io.github.glandais.gpx;

import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Service;

import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class GPXFilter {

	public void filterPoints(GPXPath path) {
		List<Point> points = path.getPoints();

		log.info("Filtering {} ({})", path.getName(), points.size());
		List<Point> newPoints = new ArrayList<>();
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

}
