package io.github.glandais.process;

import io.github.glandais.fit.FitFileWriter;
import io.github.glandais.gpx.*;
import io.github.glandais.io.CSVFileWriter;
import io.github.glandais.io.GPXCharter;
import io.github.glandais.io.GPXFileWriter;
import io.github.glandais.io.GPXParser;
import io.github.glandais.kml.KMLFileWriter;
import io.github.glandais.map.MapImage;
import io.github.glandais.map.SRTMMapProducer;
import io.github.glandais.map.TileMapImage;
import io.github.glandais.map.TileMapProducer;
import io.github.glandais.srtm.GPXElevationFixer;
import io.github.glandais.util.SmoothService;
import io.github.glandais.util.SpeedService;
import io.github.glandais.virtual.Course;
import io.github.glandais.virtual.MaxSpeedComputer;
import io.github.glandais.virtual.PowerComputer;
import io.github.glandais.virtual.PowerProvider;
import io.github.glandais.virtual.cx.CxGuesser;
import io.github.glandais.virtual.cx.CxProvider;
import io.github.glandais.virtual.cx.CxProviderConstant;
import io.github.glandais.virtual.cx.CxProviderFromData;
import io.github.glandais.virtual.cyclist.PowerProviderConstant;
import io.github.glandais.virtual.cyclist.PowerProviderFromData;
import io.github.glandais.virtual.cyclist.WeightGuesser;
import io.github.glandais.virtual.wind.WindProviderConstant;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.io.File;
import java.time.Instant;
import java.util.Collections;
import java.util.List;

@Service
@Slf4j
public class GpxProcessor {

    private final GPXParser gpxParser;

    private final GPXElevationFixer gpxElevationFixer;

    private final SimpleTimeComputer simpleTimeComputer;

    private final MaxSpeedComputer maxSpeedComputer;

    private final PowerComputer powerComputer;

    private final GPXPerSecond gpxPerSecond;

    private final GPXCharter gpxCharter;

    private final TileMapProducer tileImageProducer;

    private final SRTMMapProducer srtmImageProducer;

    private final GPXFileWriter gpxFileWriter;

    private final KMLFileWriter kmlFileWriter;

    private final FitFileWriter fitFileWriter;

    private final SpeedService speedService;

    private final SmoothService smoothService;

    private final CSVFileWriter csvFileWriter;

    private final CxGuesser cxGuesser;

    private final WeightGuesser weightGuesser;

    public GpxProcessor(final SimpleTimeComputer simpleTimeComputer,
                        final GPXParser gpxParser,
                        final GPXElevationFixer gpxElevationFixer,
                        final MaxSpeedComputer maxSpeedComputer,
                        final PowerComputer powerComputer,
                        final FitFileWriter fitFileWriter,
                        final GPXPerSecond gpxPerSecond,
                        final GPXCharter gpxCharter,
                        final TileMapProducer tileImageProducer,
                        final SRTMMapProducer srtmImageProducer,
                        final GPXFileWriter gpxFileWriter,
                        final KMLFileWriter kmlFileWriter,
                        final SpeedService speedService,
                        final SmoothService smoothService,
                        final CSVFileWriter csvFileWriter,
                        CxGuesser cxGuesser,
                        WeightGuesser weightGuesser) {

        this.simpleTimeComputer = simpleTimeComputer;
        this.gpxParser = gpxParser;
        this.gpxElevationFixer = gpxElevationFixer;
        this.maxSpeedComputer = maxSpeedComputer;
        this.powerComputer = powerComputer;
        this.fitFileWriter = fitFileWriter;
        this.gpxPerSecond = gpxPerSecond;
        this.gpxCharter = gpxCharter;
        this.tileImageProducer = tileImageProducer;
        this.srtmImageProducer = srtmImageProducer;
        this.gpxFileWriter = gpxFileWriter;
        this.kmlFileWriter = kmlFileWriter;
        this.speedService = speedService;
        this.smoothService = smoothService;
        this.csvFileWriter = csvFileWriter;
        this.cxGuesser = cxGuesser;
        this.weightGuesser = weightGuesser;
    }

    public void process(File gpxFile, ProcessCommand options) throws Exception {

        log.info("Processing file {}", gpxFile.getName());
        List<GPXPath> paths = gpxParser.parsePaths(gpxFile);
        File gpxFolder = new File(options.getOutput(), gpxFile.getName()
                .replace(".gpx", ""));
        gpxFolder.mkdirs();
        for (GPXPath path : paths) {
            log.info("Processing path {}", path.getName());
            File pathFolder = new File(gpxFolder, path.getName());
            pathFolder.mkdirs();

            path.computeArrays();
            if (!options.isGpxData()) {
                GPXFilter.filterPointsDouglasPeucker(path);
            } else {
                speedService.computeSpeed(path, "speed");
                for (Point point : path.getPoints()) {
                    point.getDebug().putAllSuffixed(point.getData(), ".orig");
                }
            }

            if (options.isFixElevation()) {
                gpxElevationFixer.fixElevation(path, !options.isGpxData());
                log.info("D+ : {} m", path.getTotalElevation());

                if (!options.isGpxData()) {
                    GPXFilter.filterPointsDouglasPeucker(path);
                }
            }

            if (options.isSimulate()) {
                Instant start = options.getNextStart();
                if (options.getSimpleVirtualSpeed() != null) {
                    simpleTimeComputer.computeTime(path, start, options.getSimpleVirtualSpeed() / 3.6);
                } else {
                    PowerProvider powerProvider;
                    CxProvider cxProvider = new CxProviderConstant(options.getCx());
                    WindProviderConstant windProvider = new WindProviderConstant(options.getWind());
                    if (options.isGpxPower()) {
                        smoothService.smoothPower(path);
                        smoothService.smoothSpeed(path);
                        powerProvider = new PowerProviderFromData();
                        if (options.isGuessWeight()) {
                            weightGuesser.guess(path, options.getCyclist(), windProvider, cxProvider);
                        }
                        if (options.isGuessCx()) {
                            cxGuesser.guess(path, options.getCyclist(),
                                    windProvider);
                            smoothService.smoothCx(path);
                            cxProvider = new CxProviderFromData();
                        }
                        speedService.computeSpeed(path, "speed");
                    } else {
                        powerProvider = new PowerProviderConstant(options.getPowerW());
                    }
                    Course course = new Course(path, start,
                            options.getCyclist(),
                            powerProvider,
                            windProvider,
                            cxProvider);
                    maxSpeedComputer.computeMaxSpeeds(course);
                    powerComputer.computeTrack(course);
                }
                speedService.computeSpeed(path, "speed");
            }

            if (options.isSrtmMap()) {
                File file = new File(pathFolder, "srtm.png");
                MapImage map = srtmImageProducer.createSRTMMap(path, 2048, 0.2);
                map.saveImage(file);
            }
            if (options.isTileMap()) {
                File file = new File(pathFolder, "map.png");
                TileMapImage map =
                        tileImageProducer.createTileMap(path, options.getTileUrl(), 0.2, options.getWidth(), options.getHeight());
                map.saveImage(file);
            }
            if (options.isChart()) {
                gpxCharter.createChartWeb(path, new File(pathFolder, "chart.png"), 640, 480);
            }

            if (options.isPointPerSecond() && !options.isGpxData()) {
                gpxPerSecond.computeOnePointPerSecond(path);
                GPXFilter.filterPointsDouglasPeucker(path);
            }

            log.info("Writing GPX for {}", path.getName());
            gpxFileWriter.writeGpxFile(Collections.singletonList(path), new File(pathFolder, path.getName() + ".gpx"), true);

            if (options.isCsv()) {
                log.info("Writing CSV for path {}", path.getName());
                csvFileWriter.writeCsvFile(path, new File(pathFolder, path.getName() + ".csv"));
            }

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
