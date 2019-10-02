package org.glandais.pbp;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import com.graphhopper.reader.dem.SRTMGL1Provider;

import io.github.glandais.gpx.GPXPath;
import io.github.glandais.io.GPXParser;
import io.github.glandais.srtm.GPXElevationFixer;
import io.github.glandais.srtm.SRTMHelper;

public class Steps {

	public static final Steps INSTANCE = new Steps(new String[] {

			"Rambouillet",

			"Villaines",

			"Fougères",

			"Tinteniac",

			"Loudéac",

			"Carhaix",

			"Brest",

			"Carhaix",

			"Loudéac",

			"Tinteniac",

			"Fougères",

			"Villaines",

			"Mortagne",

			"Dreux",

			"Rambouillet"

	});

	private List<Step> steps;

	public Steps(String[] labels) {
		super();
		try {
			steps = new ArrayList<>();
			List<GPXPath> paths = new GPXParser().parsePaths(Steps.class.getResourceAsStream("/PBP2019.gpx"));
			paths.sort(Comparator.comparing(GPXPath::getName));
			GPXElevationFixer gpxElevationFixer = new GPXElevationFixer(new SRTMHelper(new SRTMGL1Provider("cache")));
			for (GPXPath gpxPath : paths) {
				gpxElevationFixer.fixElevation(gpxPath);
			}
			Map<Integer, GPXPath> etapes = paths.stream().collect(Collectors.toMap(this::getIndex, p -> p));
			double distanceSinceStart = 0.0;
			for (int i = 0; i < labels.length; i++) {
				Step step;
				if (i == 0) {
					step = getStep(labels[i], new GPXPath[0], distanceSinceStart);
				} else if (i == 1) {
					step = getStep(labels[i], new GPXPath[] { etapes.get(1), etapes.get(2) }, distanceSinceStart);
				} else {
					step = getStep(labels[i], new GPXPath[] { etapes.get(i + 1) }, distanceSinceStart);
				}
				distanceSinceStart = step.getTotalDistanceEnd();
				steps.add(step);
			}
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	public List<Step> getSteps() {
		return steps;
	}

	private Step getStep(String label, GPXPath[] gpxPaths, double distanceSinceStart) {
		double d = 0.0;
		for (GPXPath gpxPath : gpxPaths) {
			d = d + gpxPath.getDist();
		}
		return new Step(label, d, distanceSinceStart, distanceSinceStart + d, gpxPaths);
	}

	private Integer getIndex(GPXPath path) {
		String name = path.getName();
		name = name.substring(name.lastIndexOf(' ') + 1);
		int index = Integer.valueOf(name);
		return index;
	}
}
