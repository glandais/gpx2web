package io.github.glandais.gpx.srtm.mapterhorn;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import javax.imageio.ImageIO;

public class HttpTileFetcher implements TileFetcher {

    private final MapterhornConfig cfg;
    private final HttpClient httpClient;

    public HttpTileFetcher(MapterhornConfig cfg) {
        this.cfg = cfg;
        this.httpClient = HttpClient.newBuilder().build();
    }

    @Override
    public TerrainTile fetch(TileCoord coord) throws IOException {
        File tileFile =
                new File(cfg.cacheRoot(), "mapterhorn/" + coord.z() + "/" + coord.x() + "/" + coord.y() + ".webp");
        if (!tileFile.isFile() || tileFile.length() == 0) {
            download(coord, tileFile);
        }
        BufferedImage image = ImageIO.read(tileFile);
        if (image == null) {
            throw new IOException("Could not decode WebP tile " + coord.cacheKey() + " at " + tileFile);
        }
        return new TerrainTile(image);
    }

    private void download(TileCoord coord, File tileFile) throws IOException {
        String url = cfg.tileUrlTemplate()
                .replace("{z}", Integer.toString(coord.z()))
                .replace("{x}", Integer.toString(coord.x()))
                .replace("{y}", Integer.toString(coord.y()));
        File parent = tileFile.getParentFile();
        if (parent != null && !parent.isDirectory() && !parent.mkdirs()) {
            throw new IOException("Could not create cache directory " + parent);
        }
        HttpRequest request = HttpRequest.newBuilder(URI.create(url))
                .setHeader("User-Agent", cfg.userAgent())
                .build();
        try {
            HttpResponse<java.nio.file.Path> response =
                    httpClient.send(request, HttpResponse.BodyHandlers.ofFile(tileFile.toPath()));
            int status = response.statusCode();
            if (status / 100 != 2) {
                tileFile.delete();
                throw new IOException("HTTP " + status + " for tile " + coord.cacheKey() + " at " + url);
            }
        } catch (InterruptedException e) {
            tileFile.delete();
            Thread.currentThread().interrupt();
            throw new IOException("Interrupted while downloading tile " + coord.cacheKey(), e);
        }
    }
}
