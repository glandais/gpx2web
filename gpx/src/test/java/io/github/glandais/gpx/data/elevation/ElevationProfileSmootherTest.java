package io.github.glandais.gpx.data.elevation;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

class ElevationProfileSmootherTest {

    @Test
    void nonPositiveWindowMeansDoNotSmooth() {
        double[] dist = {0.0, 10.0, 20.0};
        double[] ele = {0.0, 50.0, 0.0};

        assertArrayEquals(ele, ElevationProfileSmoother.smoothProfile(dist, ele, 0.0));
        assertArrayEquals(ele, ElevationProfileSmoother.smoothProfile(dist, ele, -1.0));
    }

    @Test
    void tooFewPointsAreReturnedUnchanged() {
        double[] dist = {0.0, 10.0};
        double[] ele = {0.0, 50.0};

        assertArrayEquals(ele, ElevationProfileSmoother.smoothProfile(dist, ele, 100.0));
    }

    @Test
    void aConstantProfileIsItsOwnSmoothing() {
        double[] dist = new double[50];
        double[] ele = new double[50];
        for (int i = 0; i < 50; i++) {
            dist[i] = i * 10.0;
            ele[i] = 42.0;
        }

        double[] out = ElevationProfileSmoother.smoothProfile(dist, ele, 100.0);

        for (double v : out) {
            assertEquals(42.0, v, 1e-9);
        }
    }

    @Test
    void aSpikeIsAttenuatedButItsMeanIsPreserved() {
        int n = 41;
        double[] dist = new double[n];
        double[] ele = new double[n];
        for (int i = 0; i < n; i++) {
            dist[i] = i * 10.0;
        }
        ele[20] = 100.0;

        double[] out = ElevationProfileSmoother.smoothProfile(dist, ele, 100.0);

        assertTrue(out[20] < 25.0, "spike was " + out[20]);
        assertTrue(out[19] > 0.0 && out[21] > 0.0, "the spike must spread to its neighbours");
    }

    @Test
    void mismatchedLengthsAreRejected() {
        assertThrows(
                IllegalArgumentException.class,
                () -> ElevationProfileSmoother.smoothProfile(new double[] {0.0, 1.0}, new double[] {0.0}, 10.0));
    }
}
