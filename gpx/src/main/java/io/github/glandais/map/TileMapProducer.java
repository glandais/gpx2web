package io.github.glandais.map;

import io.github.glandais.gpx.data.GPXPath;
import io.github.glandais.gpx.data.Point;
import io.github.glandais.util.CacheFolderProvider;
import io.github.glandais.util.Vector;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FileUtils;
import org.springframework.stereotype.Service;

import javax.imageio.ImageIO;
import jakarta.inject.Singleton;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

@Service
@Singleton
@Slf4j
public class TileMapProducer {

    public static final String USER_AGENT = "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/76.0.3809.132 Safari/537.36";
    protected final HttpClient httpClient;

    protected static final String SEPARATOR = File.separator;

    protected static final String ABC = "abc";

    protected final File cacheFolder;

    public TileMapProducer(final CacheFolderProvider cacheFolderProvider) {
        super();
        this.cacheFolder = cacheFolderProvider.getCacheFolder();
        this.httpClient = HttpClient.newBuilder().build();
    }

    public TileMapImage createTileMap(GPXPath path, String urlPattern, double margin, Integer width, Integer height)
            throws IOException {
        log.debug("start createTileMap");
        TileMapImage tileMapImage = new TileMapImage(path, margin, cacheFolder, urlPattern, width, height);
        return doCreateTileMap(path, tileMapImage);
    }

    public TileMapImage createTileMap(GPXPath path, String urlPattern, double margin, int maxSize) throws IOException {
        log.debug("start createTileMap");
        TileMapImage tileMapImage = new TileMapImage(path, margin, maxSize, cacheFolder, urlPattern);
        return doCreateTileMap(path, tileMapImage);
    }

    public TileMapImage createTileMap(GPXPath path, String urlPattern, int zoom, double margin) throws IOException {
        log.debug("start createTileMap");
        TileMapImage tileMapImage = new TileMapImage(path, margin, cacheFolder, urlPattern, zoom);
        return doCreateTileMap(path, tileMapImage);
    }

    private TileMapImage doCreateTileMap(GPXPath path, TileMapImage tileMapImage) throws IOException {
        log.debug("Creating a map of {}x{} pixels", tileMapImage.getWidth(), tileMapImage.getHeight());

        fillWithImages(tileMapImage);
        addPoints(tileMapImage, path);

        log.debug("end createTileMap");
        return tileMapImage;
    }

    protected void fillWithImages(TileMapImage tileMapImage) throws IOException {
        double startx = tileMapImage.getStartx();
        double starty = tileMapImage.getStarty();

        int timin = (int) Math.floor(tileMapImage.getTileI(tileMapImage.getMinlon()));
        int timax = (int) Math.ceil(tileMapImage.getTileI(tileMapImage.getMaxlon()));

        int tjmin = (int) Math.floor(tileMapImage.getTileJ(tileMapImage.getMaxlat()));
        int tjmax = (int) Math.ceil(tileMapImage.getTileJ(tileMapImage.getMinlat()));

        for (int i = timin; i < timax; i++) {
            for (int j = tjmin; j < tjmax; j++) {
                BufferedImage img = getImage(tileMapImage, i, j);
                if (img != null) {
                    double x = i * 256 - startx;
                    double y = j * 256 - starty;
                    tileMapImage.getGraphics().drawImage(img, (int) x, (int) y, null);
                }
            }
        }
    }

    protected BufferedImage getImage(TileMapImage tileMapImage, int i, int j) throws IOException {
        File cache = tileMapImage.getCache();
        int zoom = tileMapImage.getZoom();
        String urlPattern = tileMapImage.getUrlPattern();
        File tile = new File(cache, zoom + SEPARATOR + i + SEPARATOR + j);
        if (!tile.exists()) {
            downloadTile(i, j, zoom, urlPattern, tile);
        }
        if (tile.length() == 0) {
            return null;
        } else {
            return ImageIO.read(tile);
        }
    }

    private synchronized void downloadTile(int i, int j, int zoom, String urlPattern, File tile) throws IOException {
        String url = urlPattern.replace("{z}", "" + zoom).replace("{x}", "" + i).replace("{y}", "" + j).replace("{s}",
                "" + ABC.charAt(ThreadLocalRandom.current().nextInt(3)));
        tile.getParentFile().mkdirs();
        log.debug("Downloading {}", url);
        try {
            HttpRequest request = HttpRequest.newBuilder(URI.create(url))
                    .setHeader("User-Agent", USER_AGENT)
                    .build();
            httpClient.send(request, HttpResponse.BodyHandlers.ofFile(tile.toPath())).body();
        } catch (FileNotFoundException | InterruptedException e) {
            FileUtils.touch(tile);
        }
    }

    protected void addPoints(TileMapImage tileMapImage, GPXPath path) {
        Graphics2D graphics = tileMapImage.getGraphics();

        graphics.setStroke(new BasicStroke(5, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        graphics.getRenderingHints().put(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        AlphaComposite ac = AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.6f);
        graphics.setComposite(ac);
        graphics.setColor(Color.RED);

        drawPath(tileMapImage, path);
//		drawArrows(tileMapImage, path);
    }

    private void drawPath(TileMapImage tileMapImage, GPXPath path) {
        List<Point> points = path.getPoints();

        int[] xPoints = new int[points.size()];
        int[] yPoints = new int[points.size()];
        int c = 0;
        for (Point point : points) {
            int i = tileMapImage.getX(point.getLonDeg());
            int j = tileMapImage.getY(point.getLatDeg());
            xPoints[c] = i;
            yPoints[c] = j;
            c++;
        }
        tileMapImage.getGraphics().drawPolyline(xPoints, yPoints, points.size());
    }

    protected void drawArrows(TileMapImage tileMapImage, GPXPath gpxPath) {
        Graphics2D graphics = tileMapImage.getGraphics();
        double[] dists = gpxPath.getDists();
        double length = dists[dists.length - 1];
        int count = 5;
        double arrowSize = Math.min(tileMapImage.getWidth(), tileMapImage.getWidth()) / 10.0;
        double arrowSizeX = arrowSize;
        double arrowSizeY = arrowSize;
        List<Point> points = gpxPath.getPoints();
        int c = 0;
        List<Vector> checkpoints = new ArrayList<>();

        double[] targetDists = new double[count * 3];
        for (int i = 0; i < count; i++) {
            double d = (i * 2 + 1) * (length / (2.0 * count));
            targetDists[i * 3 + 1] = d;
            targetDists[i * 3] = d - 0.5;
            targetDists[i * 3 + 2] = d + 0.5;
        }

        for (int i = 0; i < dists.length - 1; i++) {
            if (dists[i] >= targetDists[c]) {
                Point p = points.get(i);
                int x = tileMapImage.getX(p.getLonDeg());
                int y = tileMapImage.getY(p.getLatDeg());
                checkpoints.add(new Vector(x, y));
                c++;
                if (c == count * 3) {
                    break;
                }
            }
        }

        if (c == count * 3) {
            int[] x = new int[3];
            int[] y = new int[3];
            for (int i = 0; i < count; i++) {
                Vector pm1 = checkpoints.get(i * 3);
                Vector p = checkpoints.get(i * 3 + 1);
                Vector pp1 = checkpoints.get(i * 3 + 2);

                double x0 = p.getX();
                double y0 = p.getY();

                double xdx = pp1.getX() - pm1.getX();
                double xdy = pp1.getY() - pm1.getY();
                double l = Math.sqrt(xdx * xdx + xdy * xdy);
                xdx = xdx / l;
                xdy = xdy / l;

                double ydx = -xdy;
                double ydy = xdx;

                x[0] = (int) (x0 - arrowSizeX * xdx / 2.0 + arrowSizeY * ydx);
                y[0] = (int) (y0 - arrowSizeX * xdy / 2.0 + arrowSizeY * ydy);

                x[1] = (int) (x0 + arrowSizeX * xdx / 2.0);
                y[1] = (int) (y0 + arrowSizeX * xdy / 2.0);

                x[2] = (int) (x0 - arrowSizeX * xdx / 2.0 - arrowSizeY * ydx);
                y[2] = (int) (y0 - arrowSizeX * xdy / 2.0 - arrowSizeY * ydy);

                graphics.drawPolyline(x, y, 3);
            }
        }
    }
}
