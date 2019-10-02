package io.github.glandais.util;

import org.springframework.stereotype.Component;

import io.github.glandais.gpx.GPXPath;
import io.github.glandais.gpx.Point;

@Component
public class SpeedService {

	public void computeSpeed(GPXPath path, String attribute) {
		for (int i = 0; i < path.size(); i++) {
			Point point = path.getPoints().get(i);
			double v;
			if (i == 0) {
				v = 0.0;
			} else {
				double d = path.getDists()[i] - path.getDists()[i - 1];
				double dt = path.getTime()[i] - path.getTime()[i - 1];
				if (dt >= 100) {
					if (d > 0.002) {
						v = (3600 * 1000 * d) / dt;
					} else {
						v = 0.0;
					}
				} else {
					v = path.getPoints().get(i - 1).getData().get(attribute);
				}
			}
			point.getData().put(attribute, v);
		}
	}
	
}
