package io.github.glandais;

import io.github.glandais.virtual.Cyclist;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;

import java.io.File;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;

@Data
@Slf4j
public class GpxToolOptions {

    private List<File> gpxFiles = new ArrayList<>();

    private File cache = new File("cache");

    private File output = new File("output");

    private boolean fixElevation;

    private boolean pointPerSecond;

    private boolean filter;

    private boolean virtualTime;

    private Double simpleVirtualSpeed;

    private Cyclist cyclist;

    // m.s-2
    private double windSpeed;

    // rad (0 = N, Pi/2 = E)
    private double windDirection;

    private boolean srtmMap;

    private boolean tileMap;
    private String tileUrl;
    private int width;
    private int height;

    private boolean chart;

    private boolean kml;

    private boolean fit;

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
        options.addOption(Option.builder("filter").desc("filter output").build());
        options.addOption(Option.builder("no_virtual_time").desc("don't add virtual time").build());
        options.addOption(
                Option.builder("svs").longOpt("simple_virtual_speed").desc("Cyclist speed (km/h)").hasArg().build());
        options.addOption(Option.builder("cw").longOpt("cyclist_weight").desc("Cyclist weight (kg)").hasArg().build());
        options.addOption(Option.builder("cp").longOpt("cyclist_power").desc("Cyclist power (W)").hasArg().build());
        options.addOption(
                Option.builder("ca").longOpt("cyclist_angle_limit").desc("Cyclist max angle (°)").hasArg().build());
        options.addOption(
                Option.builder("cs").longOpt("cyclist_speed_limit").desc("Cyclist max speed (km/h)").hasArg().build());
        options.addOption(
                Option.builder("cb").longOpt("cyclist_brake_limit").desc("Cyclist max brake (g)").hasArg().build());
        options.addOption(
                Option.builder("ws").longOpt("wind_speed").desc("Wind speed (m.s-2)").hasArg().build());
        options.addOption(
                Option.builder("wd").longOpt("wind_direction").desc("Wind direction (°, clockwise, 0=N)").hasArg().build());

        options.addOption(Option.builder("srtm").desc("SRTM map").build());
        options.addOption(Option.builder("tile").desc("Create a map from tiles").build());
        options.addOption(Option.builder("tile_url").desc("Tile url").hasArg().build());
        options.addOption(Option.builder("width").desc("Tile map width").hasArg().build());
        options.addOption(Option.builder("height").desc("Tile map height").hasArg().build());
        options.addOption(Option.builder("no_chart").desc("don't draw chart").build());

        options.addOption(Option.builder("kml").desc("Export KML").build());

        options.addOption(Option.builder("fit").desc("Export Fit").build());

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
        pointPerSecond = !cmd.hasOption("no_second_precision");
        filter = cmd.hasOption("filter");
        virtualTime = !cmd.hasOption("no_virtual_time");
        if (cmd.hasOption("svs")) {
            simpleVirtualSpeed = getDoubleOption(cmd, "svs", 30);
        }
        double mKg = getDoubleOption(cmd, "cw", 80);
        double powerW = getDoubleOption(cmd, "cp", 240);
        double maxAngleDeg = getDoubleOption(cmd, "ca", 15);
        double maxSpeedKmH = getDoubleOption(cmd, "cs", 90);
        double maxBrakeG = getDoubleOption(cmd, "cb", 0.3);
        cyclist = new Cyclist(mKg, powerW, maxAngleDeg, maxSpeedKmH, maxBrakeG);
        windSpeed = getDoubleOption(cmd, "ws", 0);
        windDirection = Math.toRadians(getDoubleOption(cmd, "wd", 0));

        srtmMap = cmd.hasOption("srtm");
        tileMap = cmd.hasOption("tile");
        tileUrl = cmd.getOptionValue("tile_url", "https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png");
        width = (int) getDoubleOption(cmd, "width", 1024);
        height = (int) getDoubleOption(cmd, "height", 768);
        chart = !cmd.hasOption("no_chart");

        kml = cmd.hasOption("kml");
        fit = cmd.hasOption("fit");

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