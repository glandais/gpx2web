package io.github.glandais.gpx.srtm.mapterhorn;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.sun.net.httpserver.HttpServer;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.concurrent.atomic.AtomicInteger;
import javax.imageio.ImageIO;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class HttpTileFetcherTest {

    private HttpServer server;
    private Path cacheRoot;
    private final AtomicInteger requestCount = new AtomicInteger();
    private byte[] tileBytes;

    @BeforeEach
    void setUp() throws IOException {
        cacheRoot = Files.createTempDirectory("mapterhorn-test-cache");

        // Encode a tiny 4x4 PNG with a known Terrarium pixel pattern.
        // ImageIO.read auto-detects format from magic bytes, so PNG content with a .webp
        // path works for our cache layer (we never inspect the extension).
        BufferedImage img = new BufferedImage(4, 4, BufferedImage.TYPE_INT_ARGB);
        for (int y = 0; y < 4; y++) {
            for (int x = 0; x < 4; x++) {
                // elevation = 100 * (x + y), encoded as Terrarium
                long encoded = Math.round(100.0 * (x + y) + 32768.0);
                int r = (int) ((encoded >> 8) & 0xFF);
                int g = (int) (encoded & 0xFF);
                img.setRGB(x, y, (0xFF << 24) | (r << 16) | (g << 8));
            }
        }
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        assertTrue(ImageIO.write(img, "png", baos), "PNG writer must be available");
        tileBytes = baos.toByteArray();

        server = HttpServer.create(new InetSocketAddress("127.0.0.1", 0), 0);
        server.createContext("/", exchange -> {
            requestCount.incrementAndGet();
            String path = exchange.getRequestURI().getPath();
            if (path.endsWith("/missing.webp")) {
                exchange.sendResponseHeaders(404, -1);
                exchange.close();
                return;
            }
            exchange.getResponseHeaders().set("Content-Type", "image/png");
            exchange.sendResponseHeaders(200, tileBytes.length);
            try (OutputStream out = exchange.getResponseBody()) {
                out.write(tileBytes);
            }
        });
        server.start();
    }

    @AfterEach
    void tearDown() throws IOException {
        server.stop(0);
        // best effort cleanup
        if (Files.exists(cacheRoot)) {
            Files.walk(cacheRoot)
                    .sorted((a, b) -> b.getNameCount() - a.getNameCount())
                    .forEach(p -> p.toFile().delete());
        }
    }

    private MapterhornConfig configFor(String urlTemplate) {
        return new MapterhornConfig(urlTemplate, 11, 4, 8, cacheRoot.toFile(), "test");
    }

    @Test
    void downloadsAndCachesTileOnDisk() throws IOException {
        String url = "http://127.0.0.1:" + server.getAddress().getPort() + "/{z}/{x}/{y}.webp";
        HttpTileFetcher fetcher = new HttpTileFetcher(configFor(url));

        TerrainTile tile = fetcher.fetch(new TileCoord(11, 1054, 745));
        assertEquals(4, tile.width());
        assertEquals(4, tile.height());
        // Pixel (0, 0) was encoded as elevation 0
        assertEquals(0.0, tile.getElevation(0, 0), 1e-9);
        // Pixel (3, 3) was encoded as elevation 600
        assertEquals(600.0, tile.getElevation(3, 3), 1e-9);

        File cached = new File(cacheRoot.toFile(), "mapterhorn/11/1054/745.webp");
        assertTrue(cached.isFile(), "tile should be cached on disk");
        assertTrue(cached.length() > 0);

        int after = requestCount.get();
        // Re-fetch; new HttpTileFetcher to clear in-memory state, hits the on-disk cache
        HttpTileFetcher fetcher2 = new HttpTileFetcher(configFor(url));
        fetcher2.fetch(new TileCoord(11, 1054, 745));
        assertEquals(after, requestCount.get(), "second fetch should reuse the on-disk cache");
    }

    @Test
    void throwsOn404() {
        // Map URL template directly to /missing.webp via a fake placeholder pattern
        String url = "http://127.0.0.1:" + server.getAddress().getPort() + "/missing.webp";
        HttpTileFetcher fetcher = new HttpTileFetcher(configFor(url));

        IOException ex = assertThrows(IOException.class, () -> fetcher.fetch(new TileCoord(11, 1, 2)));
        assertTrue(ex.getMessage().contains("404"), "expected 404 in message: " + ex.getMessage());
    }
}
