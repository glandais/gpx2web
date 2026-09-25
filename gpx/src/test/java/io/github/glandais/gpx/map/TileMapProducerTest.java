package io.github.glandais.gpx.map;

import static org.junit.jupiter.api.Assertions.*;

import com.sun.net.httpserver.HttpServer;
import io.github.glandais.gpx.data.GPX;
import io.github.glandais.gpx.data.GPXPath;
import io.github.glandais.gpx.data.GPXPathType;
import io.github.glandais.gpx.data.Point;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Instant;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Stream;
import javax.imageio.ImageIO;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class TileMapProducerTest {

    private static final int TILE_RGB = 0x3366CC;

    @TempDir
    Path cacheDir;

    @TempDir
    Path outDir;

    private HttpServer server;
    private final AtomicInteger requests = new AtomicInteger();
    private volatile int status = 200;
    private volatile byte[] body;

    private TileMapProducer producer;
    private String urlPattern;

    @BeforeEach
    void setUp() throws IOException {
        body = pngTile();
        server = HttpServer.create(new InetSocketAddress(InetAddress.getLoopbackAddress(), 0), 0);
        server.createContext("/", exchange -> {
            requests.incrementAndGet();
            byte[] b = body;
            exchange.sendResponseHeaders(status, b.length);
            try (OutputStream os = exchange.getResponseBody()) {
                os.write(b);
            }
        });
        server.start();
        urlPattern = "http://127.0.0.1:" + server.getAddress().getPort() + "/{z}/{x}/{y}.png";
        producer = new TileMapProducer(() -> cacheDir.toFile());
    }

    @AfterEach
    void tearDown() {
        server.stop(0);
    }

    @Test
    void serverErrorFailsAndCachesNothing() throws IOException {
        status = 500;
        body = "{}".getBytes(StandardCharsets.UTF_8);
        File out = outDir.resolve("map.png").toFile();

        IOException e = assertThrows(IOException.class, () -> render(out));

        assertTrue(e.getMessage().contains("HTTP 500"), e.getMessage());
        assertTrue(e.getMessage().contains("http://127.0.0.1:"), e.getMessage());
        assertEquals(List.of(), cachedFiles());
        assertFalse(out.exists());
    }

    @Test
    void notFoundFailsAndCachesNothing() throws IOException {
        status = 404;
        body = new byte[0];

        IOException e = assertThrows(
                IOException.class, () -> render(outDir.resolve("map.png").toFile()));

        assertTrue(e.getMessage().contains("HTTP 404"), e.getMessage());
        assertEquals(List.of(), cachedFiles());
    }

    @Test
    void pngTilesAreRenderedAndCached() throws IOException {
        File out = outDir.resolve("map.png").toFile();

        render(out);

        int downloaded = requests.get();
        assertTrue(downloaded > 0);
        List<Path> cached = cachedFiles();
        assertEquals(downloaded, cached.size());
        for (Path p : cached) {
            assertNotNull(ImageIO.read(p.toFile()), p.toString());
            assertFalse(p.getFileName().toString().endsWith(".part"), p.toString());
        }
        BufferedImage map = ImageIO.read(out);
        // A corner is far from the (red) track: it shows the tile, not the black background.
        assertEquals(TILE_RGB, map.getRGB(0, 0) & 0xFFFFFF);

        // Second rendering is served from the cache.
        render(outDir.resolve("map2.png").toFile());
        assertEquals(downloaded, requests.get());
    }

    @Test
    void cachedNonImageIsDeletedAndDownloadedAgain() throws IOException {
        render(outDir.resolve("warmup.png").toFile());
        List<Path> cached = cachedFiles();
        int tiles = cached.size();
        // Simulate an error body cached by an older version, and an empty placeholder.
        Files.writeString(cached.get(0), "{}");
        if (tiles > 1) {
            Files.write(cached.get(1), new byte[0]);
        }
        requests.set(0);

        File out = outDir.resolve("map.png").toFile();
        render(out);

        assertEquals(Math.min(tiles, 2), requests.get());
        for (Path p : cachedFiles()) {
            assertNotNull(ImageIO.read(p.toFile()), p.toString());
        }
        assertEquals(TILE_RGB, ImageIO.read(out).getRGB(0, 0) & 0xFFFFFF);
    }

    @Test
    void cachedNonImageFailsWhenServerStillReturnsNonImage() throws IOException {
        render(outDir.resolve("warmup.png").toFile());
        Path first = cachedFiles().get(0);
        Files.writeString(first, "{}");
        body = "not an image".getBytes(StandardCharsets.UTF_8);

        IOException e = assertThrows(
                IOException.class, () -> render(outDir.resolve("map.png").toFile()));

        assertTrue(e.getMessage().contains("not a decodable image"), e.getMessage());
        assertFalse(Files.exists(first));
    }

    private void render(File out) throws IOException {
        producer.createTileMap(out, gpx(), urlPattern, 0.2, 256, 256, List.of(Color.RED));
    }

    private List<Path> cachedFiles() throws IOException {
        try (Stream<Path> s = Files.walk(cacheDir)) {
            return s.filter(Files::isRegularFile).sorted().toList();
        }
    }

    private static GPX gpx() {
        GPXPath path = new GPXPath("track", GPXPathType.TRACK);
        Instant t = Instant.parse("2025-01-01T10:00:00Z");
        Point p1 = new Point();
        p1.setInstant(t, t);
        p1.setLat(Math.toRadians(45.0));
        p1.setLon(Math.toRadians(5.0));
        Point p2 = new Point();
        p2.setInstant(t, t.plusSeconds(60));
        p2.setLat(Math.toRadians(45.002));
        p2.setLon(Math.toRadians(5.002));
        path.setPoints(List.of(p1, p2));
        return new GPX("test", List.of(path), List.of());
    }

    private static byte[] pngTile() throws IOException {
        BufferedImage img = new BufferedImage(256, 256, BufferedImage.TYPE_INT_RGB);
        Graphics2D g = img.createGraphics();
        g.setColor(new Color(TILE_RGB));
        g.fillRect(0, 0, 256, 256);
        g.dispose();
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        ImageIO.write(img, "png", bos);
        return bos.toByteArray();
    }
}
