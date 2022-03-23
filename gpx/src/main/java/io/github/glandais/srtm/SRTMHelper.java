package io.github.glandais.srtm;

import com.graphhopper.reader.dem.*;
import io.github.glandais.gpx.Point;
import io.github.glandais.gpx.storage.ValueKind;
import org.eclipse.microprofile.config.inject.ConfigProperty;

import javax.annotation.PostConstruct;
import javax.inject.Singleton;
import java.io.File;
import java.util.ArrayList;
import java.util.List;

@Singleton
public class SRTMHelper {

    private ElevationProvider elevationProvider;

    @ConfigProperty(name = "gpx.data.cache", defaultValue = "cache")
    protected File cacheFolder = new File("cache");

    @PostConstruct
    public void init() {
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
        return (6001 * (60 - lat)) / 5;
    }

    private double lonToCol(double lon) {
        return (6001 * (180 + lon)) / 5;
    }

    private double rowToLat(double row) {
        return Math.toRadians(60 - ((row * 5.0) / 6001.0));
    }

    private double colToLon(double col) {
        return Math.toRadians(((col * 5.0) / 6001.0) - 180);
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
