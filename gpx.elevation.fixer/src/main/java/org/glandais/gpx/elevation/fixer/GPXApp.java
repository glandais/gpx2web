package org.glandais.gpx.elevation.fixer;

import java.io.File;
import java.util.Calendar;
import java.util.Collections;
import java.util.GregorianCalendar;
import java.util.List;

import javax.inject.Inject;

import org.glandais.gpx.map.MapProducer;
import org.glandais.gpx.srtm.SRTMHelper;
import org.glandais.gpx.srtm.SRTMImageProducer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.airlift.airline.Arguments;
import io.airlift.airline.Command;
import io.airlift.airline.HelpOption;
import io.airlift.airline.Option;
import io.airlift.airline.SingleCommand;

@Command(name = "gpx-app", description = "GPX tool")
public class GPXApp {

	private static final String SEPARATOR = File.separator;

	private static final Logger LOGGER = LoggerFactory.getLogger(GPXApp.class);

	@Inject
	private HelpOption helpOption;

//	@Option(description = "Track start dates")
//	public List<String> starts;

	@Option(name = { "-o", "--output" }, description = "Output folder")
	private File fout = new File("output");

	@Option(name = { "-c", "--cache" }, description = "Cache folder")
	private File cache = new File("cache");

	@Option(name = { "-z", "--fixZ" }, description = "Fix altitudes")
	private boolean fixZ = true;

	@Option(name = { "-v", "--virtualTime" }, description = "Compute virtual time")
	private boolean computeVirtualTime = true;

	@Option(name = { "-m", "--mass" }, description = "Mass (kg)")
	private double m = 95;

	@Option(name = { "-p", "--power" }, description = "Power (W)")
	private double power = 140;

	@Option(name = { "-fwp", "--freewheel_power" }, description = "Freewheel power (W)")
	private double freewheelPower = 100;

	@Option(name = { "-ma", "--max_angle" }, description = "Max lean angle (°)")
	private double maxAngle = 10;

	@Option(name = { "-ms", "--max_speed" }, description = "Maximum speed (km/h)")
	private double maxSpeed = 40;

	@Option(name = { "-mb", "--max_brake" }, description = "Maximum brake (\"g\" unit)")
	private double maxBrake = 0.1;

	@Option(name = { "-srtm", "--srtm_map" }, description = "Export SRTM map")
	private boolean srtmMap = true;

	@Option(name = { "-tile", "--tile_map" }, description = "Export tile map")
	private boolean tileMap = true;

	@Option(name = { "-charts", "--charts" }, description = "Create charts")
	private boolean createCharts = true;

	@Option(name = { "-tileUrl", "--tile_url" }, description = "Export tile map : tile URL")
	private String tileUrl =
//	"https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png";
			"https://foil.fr/magic/magicCache/{z}/{x}/{y}.png";
//	"https://api.mapbox.com/v4/mapbox.outdoors/{z}/{x}/{y}.png?access_token=pk.eyJ1IjoiZ2xhbmRhaXMiLCJhIjoiZGQxMDNjODBlN2ZkMDEyNjJjN2E5MjEzNzk2YWU0NDUifQ.YyPJXAyXxk0wuXB1DBqymg";

	@Option(name = { "-tileZoom", "--tile_zoom" }, description = "Export tile map : tile zoom")
	private int tileZoom = 12;

	@Arguments(description = "GPX files or folders", required = true)
	public List<File> files;

	private SRTMHelper srtmHelper;

	public static void main(String[] args) throws Exception {
		GPXApp gpxApp = SingleCommand.singleCommand(GPXApp.class).parse(args);

		if (gpxApp.helpOption.showHelpIfRequested()) {
			return;
		}

		gpxApp.run();
	}

	private void run() throws Exception {
		LOGGER.info("Output to {}", fout);
		GPXBikeTimeEval bikeTimeEval = new GPXBikeTimeEval(m, power, freewheelPower, maxAngle, maxSpeed, maxBrake);
		srtmHelper = new SRTMHelper(new File(cache, "srtm"));

		GregorianCalendar[] starts = getTmpStarts();

		bikeTimeEval.setStarts(starts);

		for (File file : files) {
			if (file.isDirectory()) {
				File[] listFiles = file.listFiles();
				for (File fin : listFiles) {
					process(bikeTimeEval, fin);
				}
			} else {
				process(bikeTimeEval, file);
			}
		}

	}

	private void process(GPXBikeTimeEval bikeTimeEval, File fin) throws Exception {
		if (!fin.getName().startsWith(".") && fin.getName().toLowerCase().endsWith(".gpx")) {
			processfile(fin, fout, bikeTimeEval);
		}
	}

	private static GregorianCalendar[] getTmpStarts() {
		GregorianCalendar[] starts = new GregorianCalendar[1];

		GregorianCalendar start = new GregorianCalendar();
		start.set(Calendar.DAY_OF_MONTH, 23);
		start.set(Calendar.MONTH, 4); // Jan. = 0!
		start.set(Calendar.YEAR, 2019);
		start.set(Calendar.HOUR_OF_DAY, 8);
		start.set(Calendar.MINUTE, 0);
		start.set(Calendar.SECOND, 0);
		start.set(Calendar.MILLISECOND, 0);
		starts[0] = start;

		return starts;
	}

	private void processfile(File file, File fout, GPXBikeTimeEval bikeTimeEval) throws Exception {
		LOGGER.info("Processing {}", file);
		List<GPXPath> paths = GPXParser.parsePaths(file);
		for (GPXPath gpxPath : paths) {
			if (fixZ) {
				srtmHelper.fixAltitudes(gpxPath);
				GPXPostProcessor.smoothAltitudes(gpxPath);
			}
			if (computeVirtualTime) {
				bikeTimeEval.computeVirtualTime(gpxPath);
			}
			String name = gpxPath.getName();
			LOGGER.info("stats for {}", name);
			LOGGER.info("min elevation : {}", gpxPath.getMinElevation());
			LOGGER.info("max elevation : {}", gpxPath.getMaxElevation());
			LOGGER.info("total elevation : {}", gpxPath.getTotalElevation());
			GPXFileWriter.writeGpxFile(Collections.singletonList(gpxPath),
					new File(fout.getAbsolutePath() + SEPARATOR + file.getName() + SEPARATOR + name + ".gpx"));

			if (srtmMap) {
				LOGGER.info("Creating SRTM map");
				SRTMImageProducer srtmImageProducer = new SRTMImageProducer(srtmHelper, gpxPath, 2048, 0.2);
				srtmImageProducer
						.export(fout.getAbsolutePath() + SEPARATOR + file.getName() + SEPARATOR + name + ".srtm.png");
			}
			if (tileMap) {
				LOGGER.info("Creating tile map");
				MapProducer imageProducer = new MapProducer(cache, tileUrl, gpxPath, 0.2, tileZoom);
				imageProducer
						.export(fout.getAbsolutePath() + SEPARATOR + file.getName() + SEPARATOR + name + ".map.png");
			}
			if (createCharts) {
				LOGGER.info("Creating charts");
				GPXCharter.createCharts(gpxPath, fout.getAbsolutePath() + SEPARATOR + file.getName() + SEPARATOR);
			}
		}
		GPXFileWriter.writeGpxFile(paths,
				new File(fout.getAbsolutePath() + SEPARATOR + file.getName() + SEPARATOR + file.getName()));
		LOGGER.info("Finished {}", file);
	}

}
