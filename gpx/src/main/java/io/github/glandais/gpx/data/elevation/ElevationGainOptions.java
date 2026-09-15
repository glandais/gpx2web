package io.github.glandais.gpx.data.elevation;

import lombok.Getter;

/**
 * How {@link ElevationGain} should measure cumulative ascent.
 *
 * <p>{@code thresholdM} and {@code smoothWindowM} default to the preset's values; setting either
 * explicitly overrides that half of the preset, which is what an {@code --elevation-gain-threshold}
 * flag on top of an {@code --elevation-gain-preset} has to mean.
 */
@Getter
public class ElevationGainOptions {

    public static final ElevationGainOptions DEFAULT = new ElevationGainOptions(ElevationGainPreset.DEM);

    /** Reproduces the raw sum of positive deltas: no dead band, no smoothing. */
    public static final ElevationGainOptions RAW = new ElevationGainOptions(ElevationGainPreset.RAW);

    private final ElevationGainPreset preset;
    private final double thresholdM;
    private final double smoothWindowM;

    public ElevationGainOptions(ElevationGainPreset preset) {
        this(preset, preset.getThresholdM(), preset.getSmoothWindowM());
    }

    public ElevationGainOptions(ElevationGainPreset preset, double thresholdM, double smoothWindowM) {
        if (preset == null) {
            throw new IllegalArgumentException("preset must not be null");
        }
        if (!(thresholdM >= 0.0) || !Double.isFinite(thresholdM)) {
            throw new IllegalArgumentException("thresholdM must be finite and >= 0, was " + thresholdM);
        }
        if (!(smoothWindowM >= 0.0) || !Double.isFinite(smoothWindowM)) {
            throw new IllegalArgumentException("smoothWindowM must be finite and >= 0, was " + smoothWindowM);
        }
        this.preset = preset;
        this.thresholdM = thresholdM;
        this.smoothWindowM = smoothWindowM;
    }

    public static ElevationGainOptions of(ElevationGainPreset preset) {
        return new ElevationGainOptions(preset);
    }

    public ElevationGainOptions withThresholdM(double thresholdM) {
        return new ElevationGainOptions(preset, thresholdM, smoothWindowM);
    }

    public ElevationGainOptions withSmoothWindowM(double smoothWindowM) {
        return new ElevationGainOptions(preset, thresholdM, smoothWindowM);
    }
}
