package io.github.glandais;

import java.time.ZonedDateTime;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import io.github.glandais.gpx.GPXPath;
import io.github.glandais.gpx.GPXPerSecond;
import io.github.glandais.srtm.GPXElevationFixer;
import io.github.glandais.virtual.Course;
import io.github.glandais.virtual.Cyclist;
import io.github.glandais.virtual.MaxSpeedComputer;
import io.github.glandais.virtual.PowerComputer;

@Service
public class GPXPathEnhancer {

	private final GPXElevationFixer gpxElevationFixer;

	private final MaxSpeedComputer maxSpeedComputer;

	private final PowerComputer powerComputer;

	private final GPXPerSecond gpxPerSecond;

	public GPXPathEnhancer(final GPXElevationFixer gpxElevationFixer,
			final MaxSpeedComputer maxSpeedComputer,
			final PowerComputer powerComputer,
			final GPXPerSecond gpxPerSecond) {

		this.gpxElevationFixer = gpxElevationFixer;
		this.maxSpeedComputer = maxSpeedComputer;
		this.powerComputer = powerComputer;
		this.gpxPerSecond = gpxPerSecond;
	}

	public void virtualize(GPXPath gpxPath) {
		gpxElevationFixer.fixElevation(gpxPath);
		double mKg = 72;
		double powerW = 280;
		double maxAngleDeg = 15;
		double maxSpeedKmH = 90;
		double maxBrakeG = 0.3;
		Cyclist cyclist = new Cyclist(mKg, powerW, maxAngleDeg, maxSpeedKmH, maxBrakeG);
		Course course = new Course(gpxPath, cyclist, ZonedDateTime.now(), 0, 0);
		maxSpeedComputer.computeMaxSpeeds(course);
		powerComputer.computeTrack(course);
		gpxPerSecond.computeOnePointPerSecond(gpxPath);
	}
}
