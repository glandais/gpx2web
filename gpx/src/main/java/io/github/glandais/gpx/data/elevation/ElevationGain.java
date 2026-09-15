package io.github.glandais.gpx.data.elevation;

import io.github.glandais.gpx.data.GPXPath;
import io.github.glandais.gpx.data.Point;
import io.github.glandais.gpx.data.values.PropertyKeys;
import java.util.List;
import lombok.experimental.UtilityClass;

/**
 * Cumulative ascent and descent with a hysteresis dead band, on a profile smoothed at its own scale.
 *
 * <h2>Why this is not {@link GPXPath#getTotalElevation()}</h2>
 *
 * A plain sum of positive deltas counts every wiggle, so it grows without bound as the sampling gets
 * finer — the coastline problem. This reports a figure that is stable under resampling.
 *
 * <h2>Why it is not a per-delta filter either</h2>
 *
 * The tempting one-liner is {@code if (dEle > threshold) gain += dEle}. It is wrong: on a smooth
 * 500 m climb sampled at 2 m spacing, no single delta ever exceeds 3 m, so it reports <b>zero</b>.
 * The accumulator below tracks <i>turning points</i> instead — a leg is banked once, in full, when
 * the profile reverses by the threshold — which makes it depend only on local extrema and therefore
 * invariant to how densely the ground between them is sampled.
 *
 * <h2>Why it smooths its own copy</h2>
 *
 * The pipeline's 150 m kernel exists to give the physics stable gradients, and it has an effective
 * averaging length of {@code 150/√6 ≈ 61 m} — inside the band where real terrain lives. Reading D+
 * off that profile runs systematically low. So this stage smooths a private copy at
 * {@link ElevationGainOptions#getSmoothWindowM()}, ~30 m by default, and never mutates the path.
 */
@UtilityClass
public class ElevationGain {

    /**
     * Measure {@code path} with the default options. Reads distances and elevations; writes nothing.
     */
    public static ElevationGainResult compute(GPXPath path) {
        return compute(path, ElevationGainOptions.DEFAULT);
    }

    /**
     * Measure {@code path}. Reads distances and elevations; writes nothing.
     *
     * <p>Prefers {@code sourceEle} — the altitude before the pipeline's 150 m kernel — when it is
     * present, which is what keeps this measurement independent of a window chosen for the physics.
     */
    public static ElevationGainResult compute(GPXPath path, ElevationGainOptions options) {
        List<Point> points = path.getPoints();
        if (points.size() < 2) {
            return ElevationGainResult.empty(options);
        }
        double[] distanceM = new double[points.size()];
        double[] elevationM = new double[points.size()];
        for (int i = 0; i < points.size(); i++) {
            Point p = points.get(i);
            // Same value as path.getDists()[i]: computeArrays() writes both from one loop.
            distanceM[i] = p.getDist();
            Double source = p.get(PropertyKeys.sourceEle);
            elevationM[i] = source == null || source.isNaN() ? p.getEle() : source;
        }
        return compute(distanceM, elevationM, options);
    }

    /**
     * {@link #compute(GPXPath, ElevationGainOptions)} the path, then cache the result on it as
     * {@link GPXPath#getElevationGainFiltered()} / {@link GPXPath#getElevationLossFiltered()}, and
     * return what was measured.
     */
    public static ElevationGainResult annotate(GPXPath path, ElevationGainOptions options) {
        ElevationGainResult result = compute(path, options);
        path.setFilteredElevation(result.gainM(), result.lossM());
        return result;
    }

    public static ElevationGainResult annotate(GPXPath path) {
        return annotate(path, ElevationGainOptions.DEFAULT);
    }

    /**
     * The kernel, on flat arrays. {@code distanceM} must be non-decreasing and the same length as
     * {@code elevationM}.
     */
    public static ElevationGainResult compute(double[] distanceM, double[] elevationM, ElevationGainOptions options) {
        if (distanceM.length != elevationM.length) {
            throw new IllegalArgumentException("distanceM (" + distanceM.length + ") and elevationM ("
                    + elevationM.length + ") must have the same length");
        }
        if (elevationM.length < 2) {
            return ElevationGainResult.empty(options);
        }

        double[] profile = ElevationProfileSmoother.smoothProfile(distanceM, elevationM, options.getSmoothWindowM());

        double rawGain = 0.0;
        double rawLoss = 0.0;
        for (int i = 1; i < profile.length; i++) {
            double d = profile[i] - profile[i - 1];
            if (d > 0.0) {
                rawGain += d;
            } else {
                rawLoss += d;
            }
        }

        if (options.getThresholdM() <= 0.0) {
            return new ElevationGainResult(rawGain, rawLoss, rawGain, rawLoss, 0.0, options.getSmoothWindowM(), 0);
        }

        Banked banked = accumulate(profile, options.getThresholdM());
        return new ElevationGainResult(
                banked.gainM,
                banked.lossM,
                rawGain,
                rawLoss,
                options.getThresholdM(),
                options.getSmoothWindowM(),
                banked.legCount);
    }

    private static class Banked {
        private final double gainM;
        private final double lossM;
        private final int legCount;

        Banked(double gainM, double lossM, int legCount) {
            this.gainM = gainM;
            this.lossM = lossM;
            this.legCount = legCount;
        }
    }

    /**
     * The turning-point accumulator.
     *
     * <p>State is a confirmed turning point {@code ref}, a running extremum {@code ext} since
     * {@code ref}, and a direction. A leg {@code [ref, ext]} is banked when the profile retraces by
     * {@code threshold} from {@code ext}, which is what makes the count all-or-nothing: a bump of
     * exactly {@code threshold} is counted in full, one of {@code threshold - ε} is dropped entirely
     * <i>including its matching descent</i>, so gain and loss stay balanced.
     *
     * <p>Banked legs are intervals between consecutive confirmed turning points, so they tile the
     * profile disjointly — a climb with twenty 1 m sub-summits banks one leg, not twenty, and
     * nothing is ever counted twice. Consequently {@code gain + loss} telescopes to
     * {@code last - first}, to within one unconfirmed final wiggle ({@code threshold}).
     *
     * <p>The {@code dir == 0} prologue exists because the first leg's direction is unknown until the
     * profile has moved {@code threshold} in <i>some</i> direction; tracking both extrema and their
     * indices until then is what stops a route that opens with a dip from booking that dip as a
     * climb.
     */
    private static Banked accumulate(double[] h, double threshold) {
        double gain = 0.0;
        double loss = 0.0;
        int legCount = 0;

        double hi = h[0];
        double lo = h[0];
        int iHi = 0;
        int iLo = 0;
        int dir = 0;
        double ref = h[0];
        double ext = h[0];

        for (int i = 1; i < h.length; i++) {
            double e = h[i];
            if (dir == 0) {
                if (e > hi) {
                    hi = e;
                    iHi = i;
                }
                if (e < lo) {
                    lo = e;
                    iLo = i;
                }
                if (hi - lo >= threshold) {
                    if (iHi > iLo) {
                        dir = 1;
                        ref = lo;
                        ext = Math.max(e, hi);
                    } else {
                        dir = -1;
                        ref = hi;
                        ext = Math.min(e, lo);
                    }
                }
            } else if (dir > 0) {
                if (e > ext) {
                    ext = e;
                } else if (ext - e >= threshold) {
                    gain += ext - ref;
                    legCount++;
                    ref = ext;
                    ext = e;
                    dir = -1;
                }
            } else {
                if (e < ext) {
                    ext = e;
                } else if (e - ext >= threshold) {
                    loss += ext - ref;
                    legCount++;
                    ref = ext;
                    ext = e;
                    dir = 1;
                }
            }
        }

        // Flush the leg still open at the end of the profile. Without this a route that finishes
        // at the top of its final climb loses that whole climb.
        if (dir > 0 && ext > ref) {
            gain += ext - ref;
            legCount++;
        } else if (dir < 0 && ext < ref) {
            loss += ext - ref;
            legCount++;
        }

        return new Banked(gain, loss, legCount);
    }
}
