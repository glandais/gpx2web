package io.github.glandais.gpx.srtm.mapterhorn;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.sun.net.httpserver.HttpServer;
import io.github.glandais.gpx.util.StallingServer;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.http.HttpTimeoutException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;
import java.util.Arrays;
import java.util.concurrent.atomic.AtomicInteger;
import javax.imageio.ImageIO;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

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
            if (path.endsWith("/error.webp")) {
                byte[] body = "{}".getBytes();
                exchange.sendResponseHeaders(500, body.length);
                try (OutputStream out = exchange.getResponseBody()) {
                    out.write(body);
                }
                return;
            }
            if (path.endsWith("/garbage.webp")) {
                byte[] body = "not an image".getBytes();
                exchange.sendResponseHeaders(200, body.length);
                try (OutputStream out = exchange.getResponseBody()) {
                    out.write(body);
                }
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

    private String serverUrl(String path) {
        return "http://127.0.0.1:" + server.getAddress().getPort() + path;
    }

    private File cachedTile(TileCoord coord) {
        return new File(cacheRoot.toFile(), "mapterhorn/" + coord.cacheKey() + ".webp");
    }

    /** Nothing in the tile's cache directory: neither the tile nor a leftover temporary file. */
    private void assertNothingCached(TileCoord coord) throws IOException {
        File tile = cachedTile(coord);
        assertFalse(tile.exists(), "no tile should be cached at " + tile);
        File dir = tile.getParentFile();
        if (dir.isDirectory()) {
            try (var files = Files.list(dir.toPath())) {
                assertEquals(0, files.count(), "no temporary file should be left in " + dir);
            }
        }
    }

    @Test
    void serverErrorFailsAndCachesNothing() throws IOException {
        HttpTileFetcher fetcher = new HttpTileFetcher(configFor(serverUrl("/error.webp")));
        TileCoord coord = new TileCoord(11, 3, 4);

        IOException ex = assertThrows(IOException.class, () -> fetcher.fetch(coord));
        assertTrue(ex.getMessage().contains("500"), "expected 500 in message: " + ex.getMessage());
        assertNothingCached(coord);
    }

    @Test
    @Timeout(30)
    void connectionDroppedMidBodyFailsAndCachesNothing() throws Exception {
        // com.sun.net.httpserver keeps the connection open when a handler writes less than the announced
        // Content-Length, so a raw socket plays the server that resets mid-transfer.
        try (ServerSocket socket = new ServerSocket(0, 1, InetAddress.getByName("127.0.0.1"))) {
            Thread serverThread = new Thread(() -> {
                try (Socket client = socket.accept()) {
                    InputStream in = client.getInputStream();
                    // Consume the request headers
                    int matched = 0;
                    byte[] end = "\r\n\r\n".getBytes(StandardCharsets.US_ASCII);
                    while (matched < end.length) {
                        int b = in.read();
                        if (b < 0) {
                            return;
                        }
                        matched = b == end[matched] ? matched + 1 : (b == end[0] ? 1 : 0);
                    }
                    OutputStream out = client.getOutputStream();
                    out.write(("HTTP/1.1 200 OK\r\nContent-Type: image/png\r\nContent-Length: " + tileBytes.length
                                    + "\r\n\r\n")
                            .getBytes(StandardCharsets.US_ASCII));
                    out.write(tileBytes, 0, tileBytes.length / 2);
                    out.flush();
                } catch (IOException e) {
                    // the client side reports the failure
                }
            });
            serverThread.start();

            String url = "http://127.0.0.1:" + socket.getLocalPort() + "/{z}/{x}/{y}.webp";
            HttpTileFetcher fetcher = new HttpTileFetcher(configFor(url));
            TileCoord coord = new TileCoord(11, 5, 6);

            assertThrows(IOException.class, () -> fetcher.fetch(coord));
            assertNothingCached(coord);
            serverThread.join(5_000);
        }
    }

    @Test
    void undecodableDownloadFailsAndCachesNothing() throws IOException {
        HttpTileFetcher fetcher = new HttpTileFetcher(configFor(serverUrl("/garbage.webp")));
        TileCoord coord = new TileCoord(11, 7, 8);

        IOException ex = assertThrows(IOException.class, () -> fetcher.fetch(coord));
        assertTrue(ex.getMessage().contains("Could not decode"), ex.getMessage());
        assertNothingCached(coord);
    }

    @Test
    void corruptedCachedTileIsDownloadedAgain() throws IOException {
        HttpTileFetcher fetcher = new HttpTileFetcher(configFor(serverUrl("/{z}/{x}/{y}.webp")));
        TileCoord truncated = new TileCoord(11, 9, 10);
        TileCoord garbage = new TileCoord(11, 11, 12);
        // A partial download left by an older version, and a cached error body
        File truncatedFile = cachedTile(truncated);
        truncatedFile.getParentFile().mkdirs();
        Files.write(truncatedFile.toPath(), Arrays.copyOf(tileBytes, tileBytes.length / 2));
        File garbageFile = cachedTile(garbage);
        garbageFile.getParentFile().mkdirs();
        Files.write(garbageFile.toPath(), "{}".getBytes());

        for (TileCoord coord : new TileCoord[] {truncated, garbage}) {
            int before = requestCount.get();
            TerrainTile tile = fetcher.fetch(coord);
            assertEquals(before + 1, requestCount.get(), "corrupted tile should be downloaded again");
            assertEquals(600.0, tile.getElevation(3, 3), 1e-9);
            assertArrayEquals(tileBytes, Files.readAllBytes(cachedTile(coord).toPath()));
        }
    }

    @ParameterizedTest
    @EnumSource(StallingServer.Mode.class)
    @Timeout(10)
    void stalledDownloadTimesOutAndCachesNothing(StallingServer.Mode mode) throws IOException {
        try (StallingServer stalling = new StallingServer(mode)) {
            String url = "http://127.0.0.1:" + stalling.port() + "/{z}/{x}/{y}.webp";
            HttpTileFetcher fetcher =
                    new HttpTileFetcher(configFor(url), Duration.ofSeconds(1), Duration.ofMillis(500));
            TileCoord coord = new TileCoord(11, 13, 14);

            assertThrows(HttpTimeoutException.class, () -> fetcher.fetch(coord));
            assertNothingCached(coord);
        }
    }
}
