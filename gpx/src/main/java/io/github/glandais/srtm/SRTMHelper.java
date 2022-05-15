package io.github.glandais.srtm;

import com.graphhopper.reader.dem.ElevationProvider;
import com.graphhopper.reader.dem.SkadiProvider;
import io.github.glandais.util.CacheFolderProvider;
import org.springframework.stereotype.Service;

import javax.inject.Singleton;
import java.io.File;

@Service
@Singleton
public class SRTMHelper {

    public static final double GRID_ARC = 5.0;
    public static final double MAX_LAT = 60.0;
    public static final double MAX_LON = 180.0;
    public static final double GRID_POINTS = 6000.0;

    private final ElevationProvider elevationProvider;

    public SRTMHelper(final CacheFolderProvider cacheFolderProvider) {
        File cacheFolder = cacheFolderProvider.getCacheFolder();
        this.elevationProvider = new SkadiProvider(new File(cacheFolder, "skadi").getAbsolutePath())
                .setInterpolate(true);
    }

    public synchronized double getElevationRad(double lon, double lat) {
        return getElevationDeg(Math.toDegrees(lon), Math.toDegrees(lat));
    }

    public synchronized double getElevationDeg(double lon, double lat) {
        double ele = elevationProvider.getEle(lat, lon);
        if (Double.isNaN(ele)) {
            return 0.0;
        }
        return ele;
    }

}
