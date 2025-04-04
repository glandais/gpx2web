package io.github.glandais.gpx.map;

import io.github.glandais.gpx.data.GPX;
import io.github.glandais.gpx.data.GPXPath;
import io.github.glandais.gpx.data.Point;
import io.github.glandais.gpx.srtm.GpxElevationProvider;
import jakarta.inject.Singleton;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@Singleton
@Slf4j
@RequiredArgsConstructor
public class SRTMMapProducer {

    private final GpxElevationProvider gpxElevationProvider;

    @SneakyThrows
    public void createSRTMMap(File file, GPX gpx, int maxsize, double margin) {
        log.debug("start createSRTMMap");
        MapImage mapImage = new MapImage(gpx, margin, maxsize);
        fillWithEle(mapImage);
        addPoints(mapImage, gpx);
        log.debug("end createSRTMMap");
        mapImage.saveImage(file);
    }

    protected void fillWithEle(MapImage mapImage) {
        int width = mapImage.getWidth();
        int height = mapImage.getHeight();
        BufferedImage image = mapImage.getImage();
        double[][] eles = new double[width][];
        double minele = Double.MAX_VALUE;
        double maxele = -Double.MAX_VALUE;
        for (int i = 0; i < width; i++) {
            eles[i] = new double[height];
            for (int j = 0; j < height; j++) {
                double lon = mapImage.getLon(i);
                double lat = mapImage.getLat(j);
                double ele = gpxElevationProvider.getElevationDeg(lon, lat);
                if (ele < minele) {
                    minele = ele;
                }
                if (ele > maxele) {
                    maxele = ele;
                }
                eles[i][j] = ele;
            }
        }
        for (int i = 0; i < width; i++) {
            for (int j = 0; j < height; j++) {
                double ele = eles[i][j];
                int rgb = getRgb(getRelativeEle(ele, minele, maxele));
                image.setRGB(i, j, rgb);
            }
        }
    }

    private void addPoints(MapImage mapImage, GPX gpx) {
        for (GPXPath path : gpx.paths()) {
            addPoints(mapImage, path);
        }
    }

    protected void addPoints(MapImage mapImage, GPXPath path) {
        List<Point> points = path.getPoints();
        double trackminele = path.getMinElevation();
        double trackmaxele = path.getMaxElevation();

        Graphics2D graphics = mapImage.getGraphics();

        boolean first = true;
        int previ = 0;
        int prevj = 0;

        graphics.setStroke(new BasicStroke(3));
        graphics.getRenderingHints().put(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        AlphaComposite ac = AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.7f);
        graphics.setComposite(ac);

        for (Point point : points) {
            int i = mapImage.getX(point.getLonDeg());
            int j = mapImage.getY(point.getLatDeg());
            if (!first) {
                int c = getColor(getRelativeEle(point.getEle(), trackminele, trackmaxele));
                graphics.setColor(new Color(c));
                graphics.drawLine(previ, prevj, i, j);
            }
            previ = i;
            prevj = j;
            first = false;
        }
    }

    private double getRelativeEle(double ele, double minele, double maxele) {
        return (ele - minele) / (maxele - minele);
    }

    private int getRgb(double d) {
        /*
         * int r = (int) Math.round(255 * d); int g = r; int b = r;
         */
        int r = 0;
        int g = 0;
        int b = 0;
        if (d < 0.5) {
            r = (int) Math.round(511 * d);
            g = 255;
            b = 255 - r;
        } else {
            r = 255;
            b = (int) Math.round(511 * (d - 0.5));
            g = 255 - b;
        }

        return (r << 16) + (g << 8) + b;
    }

    private int getColor(double ele) {
        int r = 0;
        int g = 0;
        int b = 0;
        if (ele < 0.5) {
            r = 0;
            g = (int) Math.round(511 * ele);
            b = 255 - g;
        } else {
            r = (int) Math.round(511 * (ele - 0.5));
            g = 255 - r;
            b = 0;
        }
        return (r << 16) + (g << 8) + b;
    }
}
