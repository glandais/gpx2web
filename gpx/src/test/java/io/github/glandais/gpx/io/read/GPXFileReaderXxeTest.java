package io.github.glandais.gpx.io.read;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import io.github.glandais.gpx.data.GPX;
import io.github.glandais.gpx.data.GPXPath;
import io.github.glandais.gpx.data.GPXWaypoint;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.xml.sax.SAXParseException;

/** Verifies the reader is hardened against XML external-entity (XXE) attacks. */
class GPXFileReaderXxeTest {

    private static final String SECRET = "top-secret-xxe-marker";

    private static final String TRACK = "<trk><name>%s</name><trkseg>"
            + "<trkpt lat=\"1.0\" lon=\"2.0\"><ele>10</ele></trkpt>"
            + "<trkpt lat=\"1.1\" lon=\"2.1\"><ele>11</ele></trkpt>"
            + "</trkseg></trk>";

    private static String gpx(String doctype, String trackName) {
        return "<?xml version=\"1.0\"?>"
                + doctype
                + "<gpx version=\"1.1\" creator=\"test\" xmlns=\"http://www.topografix.com/GPX/1/1\">"
                + TRACK.formatted(trackName)
                + "</gpx>";
    }

    private static GPX parse(String xml) throws Exception {
        return new GPXFileReader().parseGPX(new ByteArrayInputStream(xml.getBytes(StandardCharsets.UTF_8)));
    }

    /**
     * Positive control. Without this, a typo in the XML above would make every rejection test below
     * pass for the wrong reason: the parser would throw on malformed input rather than on the
     * DOCTYPE, and the suite would stay green while testing nothing.
     */
    @Test
    void parseGPX_shouldParseTrackWithoutDoctype() throws Exception {
        GPX gpx = parse(gpx("", "a track"));

        assertEquals(1, gpx.paths().size());
        assertEquals("a track", gpx.paths().get(0).getName());
        assertEquals(2, gpx.paths().get(0).getPoints().size());
    }

    /** The primary guard: a DOCTYPE is refused outright, so no entity can even be declared. */
    @Test
    void parseGPX_shouldRejectDoctype() {
        Exception thrown = assertThrows(Exception.class, () -> parse(gpx("<!DOCTYPE gpx>", "a track")));

        SAXParseException cause = assertInstanceOf(SAXParseException.class, rootCause(thrown));
        assertTrue(cause.getMessage().contains("disallow-doctype-decl"), "unexpected reason: " + cause.getMessage());
    }

    /**
     * The security property itself: a declared external entity must never reach the parsed data.
     *
     * <p>Deliberately does not assert that parsing throws. Rejecting the DOCTYPE is only the
     * mechanism we happen to use; disabling entity resolution while still accepting a DOCTYPE would
     * be equally safe. Asserting on the outcome — the file contents never surface — keeps this test
     * honest under either configuration, and red only when an entity actually resolves.
     */
    @Test
    void parseGPX_shouldNeverResolveExternalEntities() throws IOException {
        File secretFile = File.createTempFile("xxe-secret", ".txt");
        secretFile.deleteOnExit();
        Files.writeString(secretFile.toPath(), SECRET);

        String doctype = "<!DOCTYPE gpx [<!ENTITY xxe SYSTEM \"file://" + secretFile.getAbsolutePath() + "\">]>";
        String xml = gpx(doctype, "&xxe;");

        String leaked;
        try {
            leaked = allText(parse(xml));
        } catch (Exception e) {
            leaked = stackTrace(e);
        }

        assertFalse(leaked.contains(SECRET), "external entity resolved, file contents leaked: " + leaked);
    }

    /** Every piece of caller-visible text the parser produces. */
    private static String allText(GPX gpx) {
        List<String> texts = new ArrayList<>();
        texts.add(gpx.name());
        for (GPXPath path : gpx.paths()) {
            texts.add(path.getName());
        }
        for (GPXWaypoint waypoint : gpx.waypoints()) {
            texts.add(waypoint.name());
        }
        return String.join("\n", texts);
    }

    private static String stackTrace(Throwable t) {
        StringWriter sw = new StringWriter();
        t.printStackTrace(new PrintWriter(sw));
        return sw.toString();
    }

    private static Throwable rootCause(Throwable t) {
        Throwable root = t;
        while (root.getCause() != null) {
            root = root.getCause();
        }
        return root;
    }
}
