package io.github.glandais.export;

import io.github.glandais.FilesMixin;
import io.github.glandais.fit.FitFileWriter;
import io.github.glandais.gpx.GPXPath;
import io.github.glandais.io.GPXCharter;
import io.github.glandais.io.GPXParser;
import io.github.glandais.kml.KMLFileWriter;
import io.github.glandais.map.MapImage;
import io.github.glandais.map.SRTMMapProducer;
import io.github.glandais.map.TileMapImage;
import io.github.glandais.map.TileMapProducer;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import picocli.CommandLine;

import java.io.File;
import java.util.List;

@Slf4j
@Component
@CommandLine.Command(name = "export", mixinStandardHelpOptions = true)
public class ExportCommand implements Runnable {

    @CommandLine.Mixin
    protected FilesMixin filesMixin;

    @CommandLine.Option(names = {"--map-srtm"}, negatable = true, description = "Elevation map")
    protected boolean srtmMap = false;

    @CommandLine.Option(names = {"--map"}, negatable = true, description = "Map")
    protected boolean tileMap = false;

    @CommandLine.Option(names = {"--map-tile-url"}, description = "Map tile URL")
    protected String tileUrl = "https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png";

    @CommandLine.Option(names = {"--map-width"}, description = "Map width")
    protected int width = 1024;

    @CommandLine.Option(names = {"--map-height"}, description = "Map height")
    protected int height = 768;

    @CommandLine.Option(names = {"--chart"}, negatable = true, description = "Chart")
    protected boolean chart = false;

    @CommandLine.Option(names = {"--kml"}, negatable = true, description = "Output KML file")
    protected boolean kml = false;

    @CommandLine.Option(names = {"--fit"}, negatable = true, description = "Output FIT file")
    protected boolean fit = false;

    @Autowired
    protected GPXParser gpxParser;

    @Autowired
    protected SRTMMapProducer srtmImageProducer;

    @Autowired
    protected TileMapProducer tileImageProducer;

    @Autowired
    protected GPXCharter gpxCharter;

    @Autowired
    protected KMLFileWriter kmlFileWriter;

    @Autowired
    protected FitFileWriter fitFileWriter;

    @Override
    public void run() {

        filesMixin.initFiles();

        filesMixin.getGpxFiles().stream().forEach(this::process);
    }

    @SneakyThrows
    protected void process(File gpxFile) {

        List<GPXPath> paths = gpxParser.parsePaths(gpxFile);
        File gpxFolder = new File(gpxFile.getParentFile(), gpxFile.getName()
                .replace(".gpx", ""));
        gpxFolder.mkdirs();
        for (GPXPath path : paths) {
            log.info("Processing path {}", path.getName());

            File pathFolder = new File(gpxFolder, path.getName());
            pathFolder.mkdirs();

            if (srtmMap) {
                File file = new File(pathFolder, "srtm.png");
                MapImage map = srtmImageProducer.createSRTMMap(path, Math.max(width, height), 0.2);
                map.saveImage(file);
            }

            if (tileMap) {
                File file = new File(pathFolder, "map.png");
                TileMapImage map =
                        tileImageProducer.createTileMap(path, tileUrl, 0.2, width, height);
                map.saveImage(file);
            }

            if (chart) {
                gpxCharter.createChartWeb(path, new File(pathFolder, "chart.png"), 640, 480);
            }

            if (kml) {
                log.info("Writing KML for path {}", path.getName());
                kmlFileWriter.writeKmlFile(path, new File(pathFolder, path.getName() + ".kml"));
            }

            if (fit) {
                log.info("Writing FIT for path {}", path.getName());
                fitFileWriter.writeFitFile(path, new File(pathFolder, path.getName() + ".fit"));
            }

        }
    }

}
