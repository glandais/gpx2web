package io.github.glandais.gpx;

import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Service;

import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class GPXPerSecond {

	public void computeOnePointPerSecond(GPXPath path) {
		log.info("A point per second for {}", path.getName());
		List<Point> points = path.getPoints();
		List<Point> newPoints = new ArrayList<>();

		long[] time = path.getTime();
		long s = 1000 * (long) Math.ceil(time[0] / 1000.0);
		long e = 1000 * (long) Math.floor(time[time.length - 1] / 1000.0);
		int i = 0;

		while (s <= e) {
			while ((i + 1) < time.length && time[i + 1] < s) {
				i++;
			}
			if ((i + 1) < time.length) {
				if (time[i] <= s && s <= time[i + 1]) {
					Point p = points.get(i);
					if (time[i + 1] - time[i] > 1) {
						double c = (s - time[i]) / (1.0 * time[i + 1] - time[i]);
						Point pp1 = points.get(i + 1);
						double lon = p.getLon() + c * (pp1.getLon() - p.getLon());
						double lat = p.getLat() + c * (pp1.getLat() - p.getLat());
						double z = p.getZ() + c * (pp1.getZ() - p.getZ());
						newPoints.add(new Point(lon, lat, z, s));
					} else {
						newPoints.add(p);
					}
				} else {
					log.error("strange");
				}
			}
			s = s + 1000;
		}

		path.setPoints(newPoints);
		log.info("Done - a point per second for {}", path.getName());
	}

}