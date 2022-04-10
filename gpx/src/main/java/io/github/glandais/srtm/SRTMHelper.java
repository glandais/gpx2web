package io.github.glandais.srtm;

import com.graphhopper.reader.dem.ElevationProvider;
import com.graphhopper.reader.dem.SkadiProvider;
import io.github.glandais.gpx.Point;
import io.github.glandais.gpx.storage.ValueKind;
import io.github.glandais.util.CacheFolderProvider;
import org.springframework.stereotype.Service;

import javax.inject.Singleton;
import java.io.File;
import java.util.ArrayList;
import java.util.List;

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
        this.elevationProvider = new SkadiProvider(new File(cacheFolder, "skadi").getAbsolutePath());
//        this.elevationProvider = new MultiSourceElevationProvider(
//                new CGIARProvider(new File(cacheFolder, "cgiar").getAbsolutePath()),
//                new GMTEDProvider(new File(cacheFolder, "gtemd").getAbsolutePath())
//        );
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

    private double latToRow(double lat) {
        return (GRID_POINTS * (MAX_LAT - lat)) / GRID_ARC;
    }

    private double lonToCol(double lon) {
        return (GRID_POINTS * (MAX_LON + lon)) / GRID_ARC;
    }

    private double rowToLat(double row) {
        return Math.toRadians(MAX_LAT - ((row * GRID_ARC) / GRID_POINTS));
    }

    private double colToLon(double col) {
        return Math.toRadians(((col * GRID_ARC) / GRID_POINTS) - MAX_LON);
    }

    public List<Point> getPointsBetween(final Point p1, Point p2) {
        List<Point> result = new ArrayList<>();
        result.add(p1);
        result.add(p2);

        p1.setEle(getElevationRad(p1.getLon(), p1.getLat()), ValueKind.srtm);
        p2.setEle(getElevationRad(p2.getLon(), p2.getLat()), ValueKind.srtm);

        double dcol1 = lonToCol(p1.getLonDeg());
        double drow1 = latToRow(p1.getLatDeg());

        double dcol2 = lonToCol(p2.getLonDeg());
        double drow2 = latToRow(p2.getLatDeg());

        int mincol = Math.min((int) Math.round(Math.floor(dcol1)), (int) Math.round(Math.floor(dcol2)));
        int maxcol = Math.max((int) Math.round(Math.floor(dcol1)), (int) Math.round(Math.floor(dcol2)));
        int minrow = Math.min((int) Math.round(Math.floor(drow1)), (int) Math.round(Math.floor(drow2)));
        int maxrow = Math.max((int) Math.round(Math.floor(drow1)), (int) Math.round(Math.floor(drow2)));

        if (Math.abs(dcol1 - dcol2) > 0.001) {
            for (int col = mincol + 1; col <= maxcol; col++) {
                double c = (col - dcol1) / (dcol2 - dcol1);
                double drow = drow1 + c * (drow2 - drow1);
                Point p = new Point();
                p.setLon(colToLon(col));
                p.setLat(rowToLat(drow));
                p.setEle(getElevationRad(p.getLon(), p.getLat()), ValueKind.srtm);
                result.add(p);
            }
        }
        if (Math.abs(drow1 - drow2) > 0.001) {
            for (int row = minrow + 1; row <= maxrow; row++) {
                double c = (row - drow1) / (drow2 - drow1);
                double dcol = dcol1 + c * (dcol2 - dcol1);
                Point p = new Point();
                p.setLon(colToLon(dcol));
                p.setLat(rowToLat(row));
                p.setEle(getElevationRad(p.getLon(), p.getLat()), ValueKind.srtm);
                result.add(p);
            }
        }

        result.sort((cp1, cp2) -> {
            double d1 = p1.distanceTo(cp1);
            double d2 = p1.distanceTo(cp2);
            return Double.compare(d1, d2);
        });

        return result;
    }

}
