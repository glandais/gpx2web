package io.github.glandais;

import java.io.File;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;

import io.github.glandais.virtual.Cyclist;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;

@Data
@Slf4j
public class GpxToolOptions {

	private List<File> gpxFiles = new ArrayList<>();

	private File cache = new File("cache");

	private File output = new File("output");

	private boolean fixElevation;

	private boolean pointPerSecond;

	private boolean virtualTime;

	private Cyclist cyclist;

	private boolean srtmMap;

	private boolean tileMap;
	private String tileUrl;
	private int tileZoom;

	private boolean chart;

	private boolean kml;

	private ZonedDateTime[] starts;
	private int counter = 0;
	private int dday = 0;

	public Options getOptions() {
		Options options = new Options();
		options.addOption(Option.builder("h").longOpt("help").desc("print help").build());
		options.addOption(Option.builder("o").longOpt("output").desc("Output folder").hasArg().build());
		options.addOption(Option.builder("cache").desc("Cache folder").hasArg().build());
		options.addOption(Option.builder("no_elevation").desc("don't fix elevation").build());
		options.addOption(Option.builder("no_second_precision").desc("don't create a point per second").build());
		options.addOption(Option.builder("no_virtual_time").desc("don't add virtual time").build());
		options.addOption(Option.builder("cw").longOpt("cyclist_weight").desc("Cyclist weight (kg)").hasArg().build());
		options.addOption(Option.builder("cp").longOpt("cyclist_power").desc("Cyclist power (W)").hasArg().build());
		options.addOption(
				Option.builder("ca").longOpt("cyclist_angle_limit").desc("Cyclist max angle (°)").hasArg().build());
		options.addOption(
				Option.builder("cs").longOpt("cyclist_speed_limit").desc("Cyclist max speed (km/h)").hasArg().build());
		options.addOption(
				Option.builder("cb").longOpt("cyclist_brake_limit").desc("Cyclist max brake (g)").hasArg().build());

		options.addOption(Option.builder("srtm").desc("SRTM map").build());
		options.addOption(Option.builder("tile").desc("Create a map from tiles").build());
		options.addOption(Option.builder("tile_url").desc("Tile url").hasArg().build());
		options.addOption(Option.builder("tile_zoom").desc("Tile map zoom").hasArg().build());
		options.addOption(Option.builder("no_chart").desc("don't draw chart").build());

		options.addOption(Option.builder("kml").desc("Export KML").build());

		return options;
	}

	public void parseCommandLine(CommandLine cmd) {
		gpxFiles = new ArrayList<>();
		for (String fileName : cmd.getArgList()) {
			checkFile(new File(fileName));
		}
		String cacheValue = cmd.getOptionValue("cache", "cache");
		cache = new File(cacheValue);
		System.setProperty("gpx.data.cache", cacheValue);
		output = new File(cmd.getOptionValue("o", "output"));
		fixElevation = !cmd.hasOption("no_elevation");
		pointPerSecond = !cmd.hasOption("no_elevation");
		virtualTime = !cmd.hasOption("no_virtual_time");
		double mKg = getDoubleOption(cmd, "cw", 80);
		double powerW = getDoubleOption(cmd, "cp", 240);
		double maxAngleDeg = getDoubleOption(cmd, "ca", 15);
		double maxSpeedKmH = getDoubleOption(cmd, "cs", 90);
		double maxBrakeG = getDoubleOption(cmd, "cb", 0.3);
		cyclist = new Cyclist(mKg, powerW, maxAngleDeg, maxSpeedKmH, maxBrakeG);

		srtmMap = cmd.hasOption("srtm");
		tileMap = cmd.hasOption("tile");
		tileUrl = cmd.getOptionValue("tile_url", "https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png");
		tileZoom = (int) getDoubleOption(cmd, "tile_zoom", 12);
		chart = !cmd.hasOption("no_chart");

		kml = cmd.hasOption("kml");

		starts = new ZonedDateTime[1];
		ZonedDateTime start = ZonedDateTime.now();
		start = start.minusYears(1);
		starts[0] = start;
	}

	private double getDoubleOption(CommandLine cmd, String opt, double d) {
		double res = d;
		if (cmd.hasOption(opt)) {
			String optionValue = cmd.getOptionValue(opt);
			try {
				res = Double.parseDouble(optionValue);
			} catch (Exception e) {
				log.error("Failed to parse {} for option {}", optionValue, opt);
			}
		}
		return res;
	}

	private void checkFile(File fin) {
		if (!fin.exists()) {
			log.warn("{} doesn't exist", fin);
		} else if (fin.isFile()) {
			if (fin.getName().toLowerCase().endsWith(".gpx")) {
				gpxFiles.add(fin);
				log.info("Will process {}", fin);
			} else {
				log.warn("{} is not a GPX", fin);
			}
		} else if (fin.isDirectory()) {
			File[] listFiles = fin.listFiles();
			for (File child : listFiles) {
				checkFile(child);
			}
		}
	}

	public boolean isValid() {
		return !gpxFiles.isEmpty();
	}

	public ZonedDateTime getNextStart() {
		if (counter < starts.length) {
			ZonedDateTime start = starts[counter];
			counter++;
			return start;
		} else {
			dday++;
			return starts[starts.length - 1].plusDays(1);
		}
	}

}
