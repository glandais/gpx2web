package io.github.glandais.gpx.srtm;

import io.github.glandais.gpx.srtm.mapterhorn.MapterhornConfig;
import io.github.glandais.gpx.srtm.mapterhorn.MapterhornElevationSource;
import io.github.glandais.gpx.util.CacheFolderProvider;
import jakarta.inject.Singleton;
import java.io.IOException;
import java.io.UncheckedIOException;
import org.springframework.stereotype.Service;

/**
 * Elevation lookups backed by Mapterhorn terrain-RGB tiles.
 *
 * <p>Failures while fetching or decoding a tile propagate as {@link UncheckedIOException}; there is
 * no silent fallback to zero. Callers iterating over many points (e.g. an elevation-fixing pass)
 * should be prepared to abort the pass on the first failure.
 */
@Service
@Singleton
public class GpxElevationProvider {

    private final MapterhornElevationSource source;

    public GpxElevationProvider(final CacheFolderProvider cacheFolderProvider) {
        this.source = new MapterhornElevationSource(MapterhornConfig.defaults(cacheFolderProvider.getCacheFolder()));
    }

    public synchronized double getElevationRad(double lon, double lat) {
        return getElevationDeg(Math.toDegrees(lon), Math.toDegrees(lat));
    }

    public synchronized double getElevationDeg(double lon, double lat) {
        try {
            return source.getEle(lat, lon);
        } catch (IOException e) {
            throw new UncheckedIOException(
                    "Failed to retrieve Mapterhorn elevation for lat=" + lat + ", lon=" + lon, e);
        }
    }
}
