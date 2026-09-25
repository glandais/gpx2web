package io.github.glandais.gpx.srtm.mapterhorn;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.io.InterruptedIOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.file.AtomicMoveNotSupportedException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.nio.file.StandardOpenOption;
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
        if (tileFile.isFile()) {
            BufferedImage cached = readImage(tileFile);
            if (cached != null) {
                return new TerrainTile(cached);
            }
            // Not an image: a partial download or an error body cached by an older version.
            Files.deleteIfExists(tileFile.toPath());
        }
        String url = download(coord, tileFile);
        BufferedImage downloaded = readImage(tileFile);
        if (downloaded == null) {
            Files.deleteIfExists(tileFile.toPath());
            throw new IOException(
                    "Could not decode WebP tile " + coord.cacheKey() + " downloaded from " + url + " at " + tileFile);
        }
        return new TerrainTile(downloaded);
    }

    private static BufferedImage readImage(File tileFile) {
        if (tileFile.length() == 0) {
            return null;
        }
        try {
            return ImageIO.read(tileFile);
        } catch (IOException e) {
            // Corrupted or truncated image data
            return null;
        }
    }

    private String download(TileCoord coord, File tileFile) throws IOException {
        String url = cfg.tileUrlTemplate()
                .replace("{z}", Integer.toString(coord.z()))
                .replace("{x}", Integer.toString(coord.x()))
                .replace("{y}", Integer.toString(coord.y()));
        Path target = tileFile.toPath();
        Path dir = Files.createDirectories(target.toAbsolutePath().getParent());
        // Download next to the target, then move it into place: a reader never sees a partial or an error body.
        Path tmp = Files.createTempFile(dir, "." + tileFile.getName() + "-", ".part");
        try {
            HttpRequest request = HttpRequest.newBuilder(URI.create(url))
                    .setHeader("User-Agent", cfg.userAgent())
                    .build();
            HttpResponse<Path> response =
                    httpClient.send(request, HttpResponse.BodyHandlers.ofFile(tmp, StandardOpenOption.WRITE));
            int status = response.statusCode();
            if (status / 100 != 2) {
                throw new IOException("HTTP " + status + " for tile " + coord.cacheKey() + " at " + url);
            }
            try {
                Files.move(tmp, target, StandardCopyOption.ATOMIC_MOVE, StandardCopyOption.REPLACE_EXISTING);
            } catch (AtomicMoveNotSupportedException e) {
                Files.move(tmp, target, StandardCopyOption.REPLACE_EXISTING);
            }
            return url;
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            InterruptedIOException ioe =
                    new InterruptedIOException("Interrupted while downloading tile " + coord.cacheKey() + " at " + url);
            ioe.initCause(e);
            throw ioe;
        } finally {
            Files.deleteIfExists(tmp);
        }
    }
}
