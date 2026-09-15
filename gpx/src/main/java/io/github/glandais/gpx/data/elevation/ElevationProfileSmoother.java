package io.github.glandais.gpx.data.elevation;

import lombok.experimental.UtilityClass;

/**
 * Distance-based elevation smoothing with a triangular kernel, on flat arrays.
 *
 * <p>Same kernel as {@link io.github.glandais.gpx.util.SmoothService}, but operating on
 * {@code double[]} rather than on a {@link io.github.glandais.gpx.data.GPXPath}, because the
 * cumulative-ascent accumulator needs a private copy of the profile and must never mutate the path.
 */
@UtilityClass
public class ElevationProfileSmoother {

    /** Below this many points there is nothing a kernel can average. */
    public static final int MIN_SMOOTHING_POINTS = 3;

    /**
     * Smooth {@code elevationM} over {@code distanceM}, weighting each neighbour within
     * {@code windowSize} meters by {@code 1 - d / windowSize}.
     *
     * <p>{@code distanceM} must be non-decreasing and the same length as {@code elevationM}. The
     * window is a <b>half-width</b> applied on each side, so a {@code windowSize} of 150 spans 300 m
     * of path — the extreme members carry a weight of ~0.
     *
     * <p>Returns a copy of {@code elevationM} when there are too few points or when
     * {@code windowSize} is not strictly positive: a non-positive window means "do not smooth",
     * which is a legal request here (the {@code RAW} gain preset makes it).
     */
    public static double[] smoothProfile(double[] distanceM, double[] elevationM, double windowSize) {
        if (distanceM.length != elevationM.length) {
            throw new IllegalArgumentException("distanceM (" + distanceM.length + ") and elevationM ("
                    + elevationM.length + ") must have the same length");
        }
        if (elevationM.length < MIN_SMOOTHING_POINTS || windowSize <= 0.0) {
            return elevationM.clone();
        }

        int n = elevationM.length;
        double[] out = new double[n];
        // The bounds are monotone in `i`, so the two cursors sweep the profile once between them
        // rather than being re-searched per point: O(n * pointsInWindow), not O(n^2).
        int startIndex = 0;
        int endIndex = 0;
        for (int i = 0; i < n; i++) {
            double current = distanceM[i];
            while (current - distanceM[startIndex] > windowSize) {
                startIndex++;
            }
            if (endIndex < i) {
                endIndex = i;
            }
            while (endIndex < n - 1 && distanceM[endIndex + 1] - current <= windowSize) {
                endIndex++;
            }

            double totalWeight = 0.0;
            double weightedSum = 0.0;
            for (int j = startIndex; j <= endIndex; j++) {
                double weight = 1.0 - Math.abs(distanceM[j] - current) / windowSize;
                totalWeight += weight;
                weightedSum += elevationM[j] * weight;
            }
            out[i] = totalWeight > 0.0 ? weightedSum / totalWeight : elevationM[i];
        }
        return out;
    }
}
