package io.github.glandais.virtual;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import io.github.glandais.gpx.Point;
import io.github.glandais.util.Constants;

@Service
public class PowerComputer {

	private static final Logger LOGGER = LoggerFactory.getLogger(PowerComputer.class);

	public void computeTrack(Course course) {

		List<Point> points = course.getGpxPath().getPoints();

		long currentTime = course.getStart().toInstant().toEpochMilli();

		// m/h, initial speed = 5km/h
		double v = 5.0 / 3.6;
		double odo = 0.0;
		for (int j = 0; j < points.size(); j++) {
			Point to = points.get(j);
			double dist = 0;
			if (j > 0) {
				Point from = points.get(j - 1);
				// meters
				dist = 1000.0 * from.distanceTo(to);
				if (dist > 0) {
					// point to point result
					PointToPoint result = computePointToPoint(v, odo, from, to, course);

					v = result.getEndSpeed();

					// ms ellapsed time
					long ts = Math.round(result.getTime() * 1000);
					currentTime = currentTime + ts;
				}
			}
			odo = odo + dist;
			to.setTime(currentTime);
		}
		course.getGpxPath().computeArrays();
	}

	/**
	 * @param speed           m/h
	 * @param dist            m
	 * @param grad            %
	 * @param ms2
	 * @param ms1
	 * @param max_power
	 * @param freewheel_power
	 * @param m
	 * @param maxSpeed
	 * @return
	 */
	private PointToPoint computePointToPoint(double v, double odo, Point from, Point to, Course course) {

		double dist = 1000.0 * from.distanceTo(to);

		double dz = to.getZ() - from.getZ();
		double grad = dz / dist;

		LOGGER.debug("{} {} {} {} {}", odo, v, from, to, grad);

		// max speeds
		double ms1 = from.getMaxSpeed();
		double ms2 = to.getMaxSpeed();

		// we will split the distance
		// m
		double d = 0.0;
		// s
		double t = 0.0;
		// s
		double dt = 0.25;

		double mKg = course.getMKg(from, odo);
		double cx = course.getCx(from, odo);
		double f = course.getF(from, odo);

		// OK
		// (http://fr.wikipedia.org/wiki/Puissance_musculaire_humaine_et_bicyclette)
		double p_grav, p_air, p_frot, p_cyclist, p_app, acc, c, ms, dx;
		double prev_v, prev_d, prev_t;
		while (true) {
			prev_v = v;
			prev_d = d;
			prev_t = t;
			// OK
			// (http://fr.wikipedia.org/wiki/Puissance_musculaire_humaine_et_bicyclette)
			p_grav = mKg * Constants.G * v * grad;
			p_air = cx * v * v * v;
			p_frot = f * Constants.G * mKg * v;

			p_cyclist = course.getPowerW(p_air, p_frot, p_grav, v, grad);

			// p_app = cyclist power - resistance
			p_app = p_cyclist - p_air - p_frot - p_grav;

			// m.s-2
			acc = p_app / mKg;
			v = v + acc * dt;

			// Compute max speed
			if (d > dist) {
				ms = ms2;
			} else {
				c = d / dist;
				ms = ms1 + c * (ms2 - ms1);
			}
			if (v > ms) {
				v = ms;
			}
			if (v < 0) {
				v = 0.0;
			}

			dx = dt * v;
			d = d + dx;
			t = t + dt;
			if (d > dist) {
				double ratio = (dist - prev_d) / dx;
				double lastTime = prev_t + dt * ratio;
				double lastSpeed = prev_v + acc * dt * ratio;
				if (lastSpeed < 0) {
					lastSpeed = 0.0;
				}
				return new PointToPoint(lastTime, lastSpeed);
			}
		}
	}

}
