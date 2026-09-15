package io.github.glandais.gpx.data.elevation;

import java.util.Arrays;
import java.util.stream.Collectors;
import lombok.Getter;

/**
 * Named (threshold, smoothing) pairs for {@link ElevationGain}.
 *
 * <p>Cumulative ascent is not a property of a route. It is a property of a route <i>and</i> a
 * measurement scale, the way coastline length is. A preset is therefore a <i>complete</i> answer to
 * "at what scale", never a threshold on its own.
 *
 * <p>The two knobs are not independent filters: the dead band and the smoothing kernel attack the
 * same noise, and the smoothing dominates.
 */
@Getter
public enum ElevationGainPreset {

    /** No dead band, no smoothing — reproduces the raw sum of positive deltas. The control. */
    RAW("raw", 0.0, 0.0),

    /** Strava's threshold for a device with a barometric altimeter. */
    BAROMETRIC("barometric", 2.0, 15.0),

    /**
     * The default here.
     *
     * <p>Elevation in this pipeline is DEM-derived — never barometric, never a GPS altimeter — and
     * DEM error is <i>spatially correlated</i> rather than white: consecutive points inside one
     * ~13.5 m cell interpolate the same four posts, so there is almost no point-to-point jitter for
     * a 2 m band to remove. Strava's 10 m is sized for GPS-altimeter white noise we do not have, and
     * it is destructive on gentle terrain. But DEM error is not zero either — ~1–3 m vertical RMSE
     * plus a lateral-offset-times-slope term, which is what inflates D+ in mountains.
     *
     * <p>3.0 m sits between the two and matches GoldenCheetah's shipped default, the only
     * independent prior-art value derived from corrected rather than device elevation. It is a
     * defensible starting point, not a measured one.
     */
    DEM("dem", 3.0, 30.0),

    /** Strava's threshold for a GPS-only trace: 10 m of consistent climbing. */
    GPS("gps", 10.0, 50.0);

    /** The spelling used on the CLI and in serialized options. */
    private final String id;

    /** Hysteresis dead band in meters. {@code 0} disables it. */
    private final double thresholdM;

    /** Triangular-kernel half-width in meters, applied to a private copy of the profile. {@code 0} disables it. */
    private final double smoothWindowM;

    ElevationGainPreset(String id, double thresholdM, double smoothWindowM) {
        this.id = id;
        this.thresholdM = thresholdM;
        this.smoothWindowM = smoothWindowM;
    }

    /**
     * Parse a preset from its {@link #getId() id}. Enum names are accepted too, so {@code DEM} works
     * wherever {@code dem} does.
     */
    public static ElevationGainPreset byId(String name) {
        return Arrays.stream(values())
                .filter(p -> p.id.equalsIgnoreCase(name) || p.name().equalsIgnoreCase(name))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Unknown elevation gain preset '" + name
                        + "': expected one of "
                        + Arrays.stream(values())
                                .map(ElevationGainPreset::getId)
                                .collect(Collectors.joining(", "))));
    }
}
