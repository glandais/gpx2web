package io.github.glandais.gpx.data.elevation;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

class ElevationGainTest {

    private static final ElevationGainOptions NO_SMOOTHING =
            new ElevationGainOptions(ElevationGainPreset.DEM, 3.0, 0.0);

    private static double[] evenlySpaced(int n, double spacingM) {
        double[] d = new double[n];
        for (int i = 0; i < n; i++) {
            d[i] = i * spacingM;
        }
        return d;
    }

    @Test
    void emptyAndSinglePointProfilesMeasureNothing() {
        assertEquals(
                0.0,
                ElevationGain.compute(new double[0], new double[0], NO_SMOOTHING)
                        .gainM());
        assertEquals(
                0.0,
                ElevationGain.compute(new double[] {0.0}, new double[] {100.0}, NO_SMOOTHING)
                        .gainM());
    }

    @Test
    void mismatchedArrayLengthsAreRejected() {
        assertThrows(
                IllegalArgumentException.class,
                () -> ElevationGain.compute(new double[] {0.0, 1.0}, new double[] {0.0}, NO_SMOOTHING));
    }

    /**
     * The regression the turning-point accumulator exists for: a per-delta filter reports zero here,
     * because no single 2 m step ever exceeds the 3 m band.
     */
    @Test
    void smoothClimbSampledFinelyIsNotSwallowedByTheDeadBand() {
        int n = 251; // 500 m of climb in 2 m steps, sampled every 10 m
        double[] dist = evenlySpaced(n, 10.0);
        double[] ele = new double[n];
        for (int i = 0; i < n; i++) {
            ele[i] = i * 2.0;
        }

        ElevationGainResult result = ElevationGain.compute(dist, ele, NO_SMOOTHING);

        assertEquals(500.0, result.gainM(), 1e-9);
        assertEquals(0.0, result.lossM(), 1e-9);
        assertEquals(1, result.legCount());
    }

    /** The point of the whole exercise: the figure must not grow with the sampling density. */
    @Test
    void measurementIsInvariantUnderResampling() {
        double coarse = gainOfSineProfile(200);
        double fine = gainOfSineProfile(2000);

        assertEquals(coarse, fine, 0.5);
    }

    private double gainOfSineProfile(int n) {
        double[] dist = new double[n];
        double[] ele = new double[n];
        for (int i = 0; i < n; i++) {
            double x = i / (double) (n - 1);
            dist[i] = x * 10000.0;
            // Two 100 m hills, plus a 0.4 m ripple that must be filtered out at any sampling rate.
            ele[i] = 100.0 * Math.sin(2 * Math.PI * x) + 0.4 * Math.sin(200 * Math.PI * x);
        }
        return ElevationGain.compute(dist, ele, NO_SMOOTHING).gainM();
    }

    /** Sub-threshold bumps are dropped with their matching descents, so the two stay balanced. */
    @Test
    void oneClimbWithSubSummitsBanksOneLeg() {
        int n = 401;
        double[] dist = evenlySpaced(n, 5.0);
        double[] ele = new double[n];
        for (int i = 0; i < n; i++) {
            ele[i] = i * 0.5 + (i % 2 == 0 ? 0.0 : 1.0); // 1 m saw-tooth on a steady climb
        }

        ElevationGainResult result = ElevationGain.compute(dist, ele, NO_SMOOTHING);

        assertEquals(1, result.legCount());
        assertEquals(200.0, result.gainM(), 1.0);
        // The raw sum on the same profile counts every tooth: that gap is what is being removed.
        assertTrue(result.rawGainM() > 290.0, "raw sum was " + result.rawGainM());
    }

    /** A route opening with a dip must not book that dip as a climb. */
    @Test
    void openingDipIsNotCountedAsGain() {
        double[] dist = evenlySpaced(3, 100.0);
        double[] ele = {100.0, 80.0, 120.0};

        ElevationGainResult result = ElevationGain.compute(dist, ele, NO_SMOOTHING);

        assertEquals(40.0, result.gainM(), 1e-9);
        assertEquals(-20.0, result.lossM(), 1e-9);
        assertEquals(2, result.legCount());
    }

    /** Without the final flush, a route finishing at the top of its last climb loses it entirely. */
    @Test
    void legOpenAtTheEndOfTheProfileIsFlushed() {
        double[] dist = evenlySpaced(3, 100.0);
        double[] ele = {100.0, 60.0, 150.0};

        ElevationGainResult result = ElevationGain.compute(dist, ele, NO_SMOOTHING);

        assertEquals(90.0, result.gainM(), 1e-9);
        assertEquals(-40.0, result.lossM(), 1e-9);
    }

    /** Banked legs tile the profile, so gain + loss telescopes to the net displacement. */
    @Test
    void gainAndLossTelescopeToNetDisplacement() {
        double[] dist = evenlySpaced(6, 100.0);
        double[] ele = {100.0, 150.0, 120.0, 200.0, 170.0, 190.0};

        ElevationGainResult result = ElevationGain.compute(dist, ele, NO_SMOOTHING);

        assertEquals(190.0 - 100.0, result.gainM() + result.lossM(), 1e-9);
    }

    @Test
    void rawPresetReproducesThePlainSumOfPositiveDeltas() {
        double[] dist = evenlySpaced(5, 100.0);
        double[] ele = {100.0, 101.0, 100.0, 102.0, 100.0};

        ElevationGainResult result = ElevationGain.compute(dist, ele, ElevationGainOptions.RAW);

        assertEquals(3.0, result.gainM(), 1e-9);
        assertEquals(-3.0, result.lossM(), 1e-9);
        assertEquals(0, result.legCount());
        assertEquals(result.rawGainM(), result.gainM());
    }

    @Test
    void thresholdIsAllOrNothing() {
        double[] dist = evenlySpaced(3, 100.0);

        // A bump of exactly the threshold is banked in full...
        ElevationGainResult counted = ElevationGain.compute(dist, new double[] {0.0, 3.0, 0.0}, NO_SMOOTHING);
        assertEquals(3.0, counted.gainM(), 1e-9);
        assertEquals(-3.0, counted.lossM(), 1e-9);

        // ...one just under it is dropped entirely, descent included.
        ElevationGainResult dropped = ElevationGain.compute(dist, new double[] {0.0, 2.99, 0.0}, NO_SMOOTHING);
        assertEquals(0.0, dropped.gainM(), 1e-9);
        assertEquals(0.0, dropped.lossM(), 1e-9);
    }

    @Test
    void presetsAreParsedByIdAndByName() {
        assertEquals(ElevationGainPreset.DEM, ElevationGainPreset.byId("dem"));
        assertEquals(ElevationGainPreset.DEM, ElevationGainPreset.byId("DEM"));
        assertEquals(ElevationGainPreset.GPS, ElevationGainPreset.byId("gps"));
        assertThrows(IllegalArgumentException.class, () -> ElevationGainPreset.byId("strava"));
    }

    @Test
    void negativeOptionsAreRejected() {
        assertThrows(
                IllegalArgumentException.class, () -> new ElevationGainOptions(ElevationGainPreset.DEM, -1.0, 30.0));
        assertThrows(
                IllegalArgumentException.class,
                () -> new ElevationGainOptions(ElevationGainPreset.DEM, 3.0, Double.NaN));
    }
}
