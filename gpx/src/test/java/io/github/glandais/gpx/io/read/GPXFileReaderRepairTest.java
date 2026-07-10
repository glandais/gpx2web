package io.github.glandais.gpx.io.read;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;

import io.github.glandais.gpx.data.GPX;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import org.junit.jupiter.api.Test;

class GPXFileReaderRepairTest {

    private static final String SECRET = "top-secret-xxe-marker";

    private static final String TRACK = "<trkseg>"
            + "<trkpt lat=\"1.0\" lon=\"2.0\"><ele>10</ele></trkpt>"
            + "<trkpt lat=\"1.1\" lon=\"2.1\"><ele>11</ele></trkpt>"
            + "</trkseg>";

    private static String gpx(String declaration, String doctype, String trackName) {
        return declaration
                + doctype
                + "<gpx version=\"1.1\" creator=\"test\" xmlns=\"http://www.topografix.com/GPX/1/1\">"
                + "<trk><name>"
                + trackName
                + "</name>"
                + TRACK
                + "</trk>"
                + "</gpx>";
    }

    private static GPX parse(String xml, Charset charset) throws Exception {
        return new GPXFileReader().parseGPX(new ByteArrayInputStream(xml.getBytes(charset)));
    }

    /** Exactly what the old escape() wrote: 26 files of the reference corpus look like this. */
    @Test
    void parseGPX_shouldRecoverSurrogatePairReferences() throws Exception {
        GPX gpx = parse(gpx("<?xml version=\"1.0\"?>", "", "Sortie &#55358;&#56600;"), StandardCharsets.UTF_8);

        assertEquals("Sortie 🤘", gpx.paths().get(0).getName());
    }

    @Test
    void parseGPX_shouldRecoverUndeclaredLatin1() throws Exception {
        GPX gpx = parse(gpx("<?xml version=\"1.0\"?>", "", "Bertaudière"), StandardCharsets.ISO_8859_1);

        assertEquals("Bertaudière", gpx.paths().get(0).getName());
    }

    /** A healthy file must still travel the strict path, untouched by any repair. */
    @Test
    void parseGPX_shouldStillReadValidFile() throws Exception {
        GPX gpx = parse(gpx("<?xml version=\"1.0\"?>", "", "Galibier"), StandardCharsets.UTF_8);

        assertEquals("Galibier", gpx.paths().get(0).getName());
        assertEquals(2, gpx.paths().get(0).getPoints().size());
    }

    /** A real negative fixture: it lives in the tribly project and must stay rejected. */
    @Test
    void parseGPX_shouldStillRejectGarbage() {
        assertThrows(Exception.class, () -> parse("NOT VALID XML CONTENT\n", StandardCharsets.UTF_8));
    }

    /**
     * Security regression: a file that is both XXE and latin-1 triggers the repair, hence a second
     * parsing pass. That pass must stay hardened.
     */
    @Test
    void parseGPX_shouldStillRejectXxeEvenWhenRepairTriggers() throws IOException {
        File secret = File.createTempFile("xxe-secret", ".txt");
        secret.deleteOnExit();
        Files.writeString(secret.toPath(), SECRET);

        String doctype = "<!DOCTYPE gpx [<!ENTITY xxe SYSTEM \"file://" + secret.getAbsolutePath() + "\">]>";
        String xml = gpx("<?xml version=\"1.0\"?>", doctype, "Bertaudière&xxe;");

        Exception thrown = assertThrows(Exception.class, () -> parse(xml, StandardCharsets.ISO_8859_1));

        assertFalse(stackTrace(thrown).contains(SECRET), "external entity resolved after repair");
    }

    private static String stackTrace(Throwable t) {
        StringWriter sw = new StringWriter();
        t.printStackTrace(new PrintWriter(sw));
        return sw.toString();
    }
}
