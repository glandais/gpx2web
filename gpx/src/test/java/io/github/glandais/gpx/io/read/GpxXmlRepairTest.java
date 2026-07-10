package io.github.glandais.gpx.io.read;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import org.junit.jupiter.api.Test;

class GpxXmlRepairTest {

    private static String repaired(String source, Charset charset) {
        byte[] out = GpxXmlRepair.repair(source.getBytes(charset));
        return out == null ? null : new String(out, StandardCharsets.UTF_8);
    }

    /** Nothing to repair: the reader must rethrow the original error rather than retry. */
    @Test
    void repair_shouldReturnNullWhenNothingToFix() {
        assertNull(repaired("<gpx><trk><name>Galibier</name></trk></gpx>", StandardCharsets.UTF_8));
    }

    @Test
    void repair_shouldRecombineDecimalSurrogatePair() {
        assertEquals("<name>&#129304;</name>", repaired("<name>&#55358;&#56600;</name>", StandardCharsets.UTF_8));
    }

    @Test
    void repair_shouldRecombineHexSurrogatePair() {
        assertEquals("<name>&#129304;</name>", repaired("<name>&#xD83E;&#xDD18;</name>", StandardCharsets.UTF_8));
    }

    /** The first reference is not a high surrogate: the real pair follows and must still be seen. */
    @Test
    void repair_shouldNotBeConfusedByAPrecedingReference() {
        assertEquals(
                "<name>&#65;&#129304;</name>", repaired("<name>&#65;&#55358;&#56600;</name>", StandardCharsets.UTF_8));
    }

    /** A lone surrogate stays invalid: leave it, so parsing fails and the original error surfaces. */
    @Test
    void repair_shouldLeaveLoneSurrogateAlone() {
        assertNull(repaired("<name>&#55358;</name>", StandardCharsets.UTF_8));
    }

    @Test
    void repair_shouldLeaveOrdinaryReferencesAlone() {
        assertNull(repaired("<name>&#232;&#233;</name>", StandardCharsets.UTF_8));
    }

    @Test
    void repair_shouldDecodeUndeclaredLatin1() {
        String out = repaired("<?xml version=\"1.0\"?><name>Bertaudière</name>", StandardCharsets.ISO_8859_1);
        assertEquals("<?xml version=\"1.0\"?><name>Bertaudière</name>", out);
    }

    /** Re-encoding to UTF-8 without fixing the declaration would produce mojibake. */
    @Test
    void repair_shouldRewriteDeclaredEncodingWhenReEncoding() {
        String out = repaired(
                "<?xml version=\"1.0\" encoding=\"ISO-8859-1\"?><name>Bertaudière&#55358;&#56600;</name>",
                StandardCharsets.ISO_8859_1);
        assertEquals("<?xml version=\"1.0\" encoding=\"UTF-8\"?><name>Bertaudière&#129304;</name>", out);
    }

    @Test
    void repair_shouldHandleAbsurdlyLargeReferenceWithoutCrashing() {
        assertNull(repaired("<name>&#999999999999;</name>", StandardCharsets.UTF_8));
    }
}
