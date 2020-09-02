package io.github.glandais;

import io.github.glandais.fit.FitFileWriter;
import io.github.glandais.gpx.GPXFilter;
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

import java.io.File;
import java.time.ZonedDateTime;
import java.util.Collections;
import java.util.List;

import org.springframework.stereotype.Service;

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

    private final GPXDataComputer gpxDataComputer;

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
            final GPXDataComputer gpxDataComputer) {

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
        this.gpxDataComputer = gpxDataComputer;
    }

    public void process(File gpxFile, GpxToolOptions options) throws Exception {

        log.info("Processing file {}", gpxFile.getName());
        List<GPXPath> paths = gpxParser.parsePaths(gpxFile);
        File gpxFolder = new File(options.getOutput(), gpxFile.getName()
                .replace(".gpx", ""));
        gpxFolder.mkdirs();
        for (GPXPath path : paths) {
            log.info("Processing path {}", path.getName());
            File pathFolder = new File(gpxFolder, path.getName());
            pathFolder.mkdirs();

            gpxDataComputer.getWindNew(path);

            if (options.isFixElevation()) {
                gpxElevationFixer.fixElevation(path);
                log.info("D+ : {} m", path.getTotalElevation());
            }
            if (options.isVirtualTime()) {
                ZonedDateTime start = options.getNextStart();
                if (options.getSimpleVirtualSpeed() != null) {
                    simpleTimeComputer.computeTime(path, start, options.getSimpleVirtualSpeed());
                } else {
                    Course course = new Course(path, options.getCyclist(), start, options.getWindSpeed(), options.getWindDirection());
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
                TileMapImage map =
                        tileImageProducer.createTileMap(path, options.getTileUrl(), 0.2, options.getWidth(), options.getHeight());
                map.saveImage(file);
            }
            if (options.isChart()) {
                gpxCharter.createChartWeb(path, new File(pathFolder, "chart.png"), 640, 480);
            }

            if (options.isPointPerSecond()) {
                gpxPerSecond.computeOnePointPerSecond(path);
            }

            if (options.isFilter()) {
                GPXFilter.filterPointsDouglasPeucker(path);
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
