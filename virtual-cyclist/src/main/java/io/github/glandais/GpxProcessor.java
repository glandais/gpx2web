package io.github.glandais;

import java.io.File;
import java.time.ZonedDateTime;
import java.util.Collections;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import io.github.glandais.fit.FitFileWriter;
import io.github.glandais.gpx.GPXPath;
import io.github.glandais.gpx.GPXPerSecond;
import io.github.glandais.gpx.SimpleTimeComputer;
import io.github.glandais.io.GPXCharter;
import io.github.glandais.io.GPXFileWriter;
import io.github.glandais.io.GPXParser;
import io.github.glandais.kml.KMLFileWriter;
import io.github.glandais.map.MapImage;
import io.github.glandais.map.SRTMMapProducer;
import io.github.glandais.map.TileMapImage;
import io.github.glandais.map.TileMapProducer;
import io.github.glandais.srtm.GPXElevationFixer;
import io.github.glandais.virtual.Course;
import io.github.glandais.virtual.MaxSpeedComputer;
import io.github.glandais.virtual.PowerComputer;
import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class GpxProcessor {

	@Autowired
	private GPXParser gpxParser;

	@Autowired
	private GPXElevationFixer gpxElevationFixer;

	@Autowired
	private SimpleTimeComputer simpleTimeComputer;

	@Autowired
	private MaxSpeedComputer maxSpeedComputer;

	@Autowired
	private PowerComputer powerComputer;

	@Autowired
	private GPXPerSecond gpxPerSecond;

	@Autowired
	private GPXCharter gpxCharter;

	@Autowired
	private TileMapProducer tileImageProducer;

	@Autowired
	private SRTMMapProducer srtmImageProducer;

	@Autowired
	private GPXFileWriter gpxFileWriter;

	@Autowired
	private KMLFileWriter kmlFileWriter;

	@Autowired
	private FitFileWriter fitFileWriter;

	public void process(File gpxFile, GpxToolOptions options) throws Exception {
		log.info("Processing file {}", gpxFile.getName());
		List<GPXPath> paths = gpxParser.parsePaths(gpxFile);
		File gpxFolder = new File(options.getOutput(), gpxFile.getName().replace(".gpx", ""));
		gpxFolder.mkdirs();
		for (GPXPath path : paths) {
			log.info("Processing path {}", path.getName());
			File pathFolder = new File(gpxFolder, path.getName());
			pathFolder.mkdirs();
			if (options.isFixElevation()) {
				gpxElevationFixer.fixElevation(path);
			}
			if (options.isVirtualTime()) {
				ZonedDateTime start = options.getNextStart();
				if (options.getSimpleVirtualSpeed() != null) {
					simpleTimeComputer.computeTime(path, start, options.getSimpleVirtualSpeed());
				} else {
					Course course = new Course(path, options.getCyclist(), start);
					maxSpeedComputer.computeMaxSpeeds(course);
					powerComputer.computeTrack(course);
				}
			}

			if (options.isSrtmMap()) {
				File file = new File(pathFolder, "srtm.png");
				MapImage map = srtmImageProducer.createSRTMMap(path, 2048, 0.2);
				map.saveImage(file);
			}
			if (options.isTileMap()) {
				File file = new File(pathFolder, "map.png");
				TileMapImage map = tileImageProducer.createTileMap(path, options.getTileUrl(), options.getTileZoom(),
						0.2);
				map.saveImage(file);
			}
			if (options.isChart()) {
				gpxCharter.createChartWeb(path, new File(pathFolder, "chart.png"));
			}

			if (options.isPointPerSecond()) {
				gpxPerSecond.computeOnePointPerSecond(path);
			}

			log.info("Writing GPX for {}", path.getName());
			gpxFileWriter.writeGpxFile(Collections.singletonList(path), new File(pathFolder, path.getName() + ".gpx"));

			if (options.isKml()) {
				log.info("Writing KML for path {}", path.getName());
				kmlFileWriter.writeKmlFile(path, new File(pathFolder, path.getName() + ".kml"));
			}

			if (options.isFit()) {
				log.info("Writing FIT for path {}", path.getName());
				fitFileWriter.writeFitFile(path, new File(pathFolder, path.getName() + ".fit"));
			}

			log.info("Processed path {}", path.getName());
		}
		gpxFileWriter.writeGpxFile(paths, new File(gpxFolder, gpxFile.getName()));
		log.info("Processed file {}", gpxFile.getName());
	}

}
