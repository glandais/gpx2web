package org.glandais.pbp;

import io.github.glandais.gpx.GPXPath;
import lombok.Data;

@Data
public class Step {

	private final String label;

	private final double distance;

	private final double totalDistanceStart;

	private final double totalDistanceEnd;

	private final GPXPath[] paths;

}
