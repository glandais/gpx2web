package org.glandais.gpx.tour;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.glandais.gpx.elevation.fixer.GPXFileWriter;
import org.glandais.gpx.elevation.fixer.GPXPath;
import org.glandais.srtm.loader.Point;
import org.glandais.srtm.loader.SRTMException;

import com.fasterxml.jackson.databind.ObjectMapper;

public class Parser {

	private static final ObjectMapper MAPPER = new ObjectMapper();

	public static void main(String[] args) throws Exception {
		Map<Long, Map<Long, Point>> points = new HashMap<>();
		Stream<Path> files = Files.walk(Paths.get("D:\\tour"));
		files.filter(p -> p.toFile().getAbsolutePath().endsWith("_riders.json")).forEach(p -> parse(p, points));
		points.forEach((l, m) -> exportGPX(l, m));
	}

	private static void exportGPX(Long l, Map<Long, Point> m) {
		try {
			List<GPXPath> cTrackPoints = new ArrayList<>();
			GPXPath path = new GPXPath("TDF_" + l);
			m.forEach((ts, point) -> processPoint(path, point.getLon(), point.getLat(), ts));
			cTrackPoints.add(path);
			GPXFileWriter.writeGpxFile(cTrackPoints, new File("D:\\tourGPX\\" + l + ".gpx"));
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private static void processPoint(GPXPath path, double lon, double lat, Long ts) {
		try {
			path.processPoint(lon, lat, 0, ts, false);
		} catch (SRTMException e) {
			e.printStackTrace();
		}
	}

	private static void parse(Path path, Map<Long, Map<Long, Point>> points) {
		try {
			Map<String, Object> all = MAPPER.readValue(path.toFile(), Map.class);
			List<Map<String, Object>> riders = (List<Map<String, Object>>) all.get("Riders");
			Map<Long, Point> positions = riders.stream()
					.collect(Collectors.toMap(m -> ((Number) m.get("Bib")).longValue(),
							m -> new Point(((Number) m.get("Longitude")).doubleValue(),
									((Number) m.get("Latitude")).doubleValue())));
			long epoch = ((Number) all.get("TimeStampEpoch")).longValue() * 1000;
			positions.forEach((l, point) -> addPoint(points, epoch, l, point));
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private static void addPoint(Map<Long, Map<Long, Point>> points, long epoch, Long l, Point p) {
		Map<Long, Point> lPoints = points.get(l);
		if (lPoints == null) {
			lPoints = new TreeMap<>();
			points.put(l, lPoints);
		}
		lPoints.put(epoch, p);
	}

}
