package io.github.glandais.gpx.srtm.mapterhorn;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

class TerrariumDecoderTest {

    @Test
    void decodesSeaLevel() {
        // (128, 0, 0) → 128 * 256 - 32768 = 0
        assertEquals(0.0, TerrariumDecoder.decode(128, 0, 0), 1e-9);
    }

    @Test
    void decodesNegativeElevation() {
        // (127, 0, 0) → 127 * 256 - 32768 = -256
        assertEquals(-256.0, TerrariumDecoder.decode(127, 0, 0), 1e-9);
    }

    @Test
    void decodesMidElevation() {
        // (135, 200, 128) → 135 * 256 + 200 + 128 / 256 - 32768 = 1992.5
        assertEquals(1992.5, TerrariumDecoder.decode(135, 200, 128), 1e-9);
    }

    @Test
    void roundTripFromKnownEncoding() {
        // Encode 1909 m: 1909 + 32768 = 34677
        // r = 34677 / 256 = 135 (rem 117); g = 117; b = 0
        double encoded = 135 * 256 + 117;
        assertEquals(encoded, 1909.0 + 32768.0, 1e-9);
        assertEquals(1909.0, TerrariumDecoder.decode(135, 117, 0), 1e-9);
    }

    @Test
    void blueChannelContributesFraction() {
        // (128, 0, 64) → 0 + 64 / 256 = 0.25
        assertEquals(0.25, TerrariumDecoder.decode(128, 0, 64), 1e-9);
    }
}
