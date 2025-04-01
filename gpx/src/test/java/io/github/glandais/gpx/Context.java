package io.github.glandais.gpx;

import eu.lestard.easydi.EasyDI;
import io.github.glandais.gpx.climb.ClimbDetector;
import io.github.glandais.gpx.filter.GPXPerDistance;
import io.github.glandais.gpx.io.read.GPXFileReader;
import io.github.glandais.gpx.io.write.FitFileWriter;
import io.github.glandais.gpx.io.write.GPXFileWriter;
import io.github.glandais.gpx.io.write.JsonFileWriter;
import io.github.glandais.gpx.io.write.tabular.CSVFileWriter;
import io.github.glandais.gpx.map.SRTMMapProducer;
import io.github.glandais.gpx.map.TileMapProducer;
import io.github.glandais.gpx.srtm.GPXElevationFixer;
import io.github.glandais.gpx.util.CacheFolderProvider;
import io.github.glandais.gpx.virtual.GPXEnhancer;
import io.github.glandais.gpx.virtual.StartTimeProvider;
import lombok.Getter;

@Getter
public class Context {

    public static final Context INSTANCE = new Context();

    private final GPXFileReader gpxFileReader;
    private final GPXPerDistance gpxPerDistance;
    private final GPXElevationFixer gpxElevationFixer;
    private final ClimbDetector climbDetector;
    private final GPXEnhancer gpxEnhancer;
    private final GPXFileWriter gpxFileWriter;
    private final JsonFileWriter jsonFileWriter;
    private final CSVFileWriter csvFileWriter;
    private final FitFileWriter fitFileWriter;
    private final StartTimeProvider startTimeProvider;
    private final SRTMMapProducer srtmMapProducer;
    private final TileMapProducer tileMapProducer;

    Context() {
        EasyDI easyDI = new EasyDI();
        easyDI.bindInterface(CacheFolderProvider.class, CacheFolderProviderImpl.class);
        this.gpxFileReader = easyDI.getInstance(GPXFileReader.class);
        this.gpxPerDistance = easyDI.getInstance(GPXPerDistance.class);
        this.gpxElevationFixer = easyDI.getInstance(GPXElevationFixer.class);
        this.climbDetector = easyDI.getInstance(ClimbDetector.class);
        this.gpxEnhancer = easyDI.getInstance(GPXEnhancer.class);
        this.gpxFileWriter = easyDI.getInstance(GPXFileWriter.class);
        this.jsonFileWriter = easyDI.getInstance(JsonFileWriter.class);
        this.csvFileWriter = easyDI.getInstance(CSVFileWriter.class);
        this.fitFileWriter = easyDI.getInstance(FitFileWriter.class);
        this.startTimeProvider = easyDI.getInstance(StartTimeProvider.class);
        this.srtmMapProducer = easyDI.getInstance(SRTMMapProducer.class);
        this.tileMapProducer = easyDI.getInstance(TileMapProducer.class);
    }
}
