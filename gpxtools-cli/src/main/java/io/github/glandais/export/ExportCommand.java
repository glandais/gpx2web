package io.github.glandais.export;

import io.github.glandais.FilesMixin;
import io.github.glandais.gpx.GPXPerSecond;
import io.github.glandais.gpx.data.GPX;
import io.github.glandais.gpx.data.GPXPath;
import io.github.glandais.gpx.filter.GPXFilter;
import io.github.glandais.io.read.GPXFileReader;
import io.github.glandais.io.write.FitFileWriter;
import io.github.glandais.io.write.GPXFileWriter;
import io.github.glandais.map.MapImage;
import io.github.glandais.map.SRTMMapProducer;
import io.github.glandais.map.TileMapImage;
import io.github.glandais.map.TileMapProducer;
import jakarta.inject.Inject;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import picocli.CommandLine;

import java.io.File;
import java.util.Collections;
import java.util.List;

@Slf4j
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

//    @CommandLine.Option(names = {"--chart"}, negatable = true, description = "Chart")
//    protected boolean chart = false;

//    @CommandLine.Option(names = {"--kml"}, negatable = true, description = "Output KML file")
//    protected boolean kml = false;

    @CommandLine.Option(names = {"--fit"}, negatable = true, description = "Output FIT file")
    protected boolean fit = false;

    @CommandLine.Option(names = {"--gpx"}, negatable = true, description = "Output GPX file compatible with GPS and softwares")
    protected boolean gpx = false;

    @Inject
    protected GPXFileReader gpxFileReader;

    @Inject
    protected SRTMMapProducer srtmImageProducer;

    @Inject
    protected TileMapProducer tileImageProducer;

//    @Inject
//    protected GPXCharter gpxCharter;

//    @Inject
//    protected KMLFileWriter kmlFileWriter;

    @Inject
    protected FitFileWriter fitFileWriter;

    @Inject
    protected GPXPerSecond gpxPerSecond;

    @Inject
    protected GPXFileWriter gpxFileWriter;

    @Override
    public void run() {
        filesMixin.processFiles(gpxFileReader, this::process);
    }

    @SneakyThrows
    private void process(GPXPath path, File pathFolder) {

        gpxPerSecond.computeOnePointPerSecond(path);
        GPXFilter.filterPointsDouglasPeucker(path);

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

//        if (chart) {
//            gpxCharter.createChartWeb(path, new File(pathFolder, "chart.png"), 640, 480);
//            gpxCharter.createChartTime(path, new File(pathFolder, "chart-time.png"));
//        }

//        if (kml) {
//            log.info("Writing KML for path {}", path.getName());
//            kmlFileWriter.writeKmlFile(path, new File(pathFolder, path.getName() + ".kml"));
//        }

        if (fit) {
            log.info("Writing FIT for path {}", path.getName());
            fitFileWriter.writeFitFile(path, new File(pathFolder, path.getName() + ".fit"));
        }

        if (gpx) {
            log.info("Writing GPX for path {}", path.getName());
            GPX gpx = new GPX(path.getName(), Collections.singletonList(path), List.of());
            gpxFileWriter.writeGpxFile(gpx, new File(pathFolder, path.getName() + ".gpx"));
        }

    }

}
