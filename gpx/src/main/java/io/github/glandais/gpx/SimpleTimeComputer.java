package io.github.glandais.gpx;

import java.time.ZonedDateTime;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class SimpleTimeComputer {

	@Autowired
	private GPXFilter gpxFilter;

	public void computeTime(GPXPath path, ZonedDateTime start, double speed) {
		gpxFilter.filterPoints(path);
		long startTime = 1000 * start.toEpochSecond();
		for (Point point : path.getPoints()) {
			long time = Math.round(3600 * 1000 * point.getDist() / speed);
			point.setTime(startTime + time);
		}
		path.computeArrays();
	}

}
