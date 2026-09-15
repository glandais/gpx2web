package io.github.glandais.gpx.data.elevation;

/**
 * What {@link ElevationGain#compute} measured.
 *
 * <p>{@code rawGainM} / {@code rawLossM} are the unfiltered sums <b>on the same (optionally
 * smoothed) profile</b>, so they isolate what the dead band did from what the smoothing did. Neither
 * is {@link io.github.glandais.gpx.data.GPXPath#getTotalElevation()}, which is the unfiltered sum on
 * the unsmoothed profile.
 *
 * @param gainM cumulative ascent in meters, always {@code >= 0}
 * @param lossM cumulative descent in meters, always {@code <= 0} — the same sign convention as
 *     {@link io.github.glandais.gpx.data.GPXPath#getTotalElevationNegative()}
 * @param legCount number of legs banked — climbs plus descents. A diagnostic: a route with 3 real
 *     climbs that reports 400 legs is telling you the threshold is too small for the noise.
 */
public record ElevationGainResult(
        double gainM,
        double lossM,
        double rawGainM,
        double rawLossM,
        double thresholdM,
        double smoothWindowM,
        int legCount) {

    public static ElevationGainResult empty(ElevationGainOptions options) {
        return new ElevationGainResult(0.0, 0.0, 0.0, 0.0, options.getThresholdM(), options.getSmoothWindowM(), 0);
    }
}
