package io.github.glandais;

import io.github.glandais.gpx.data.elevation.ElevationGainOptions;
import io.github.glandais.gpx.data.elevation.ElevationGainPreset;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import picocli.CommandLine;

/**
 * The scale cumulative ascent is measured at.
 *
 * <p>The preset is a complete answer — a dead band <i>and</i> a smoothing window. Either flag below
 * overrides that half of it, which is what a threshold flag on top of a preset flag has to mean.
 */
@Data
@Slf4j
public class ElevationGainMixin {

    @CommandLine.Option(
            names = {"--elevation-gain-preset"},
            description = "Elevation gain measurement scale: ${COMPLETION-CANDIDATES} (default: dem)")
    private ElevationGainPreset preset = ElevationGainPreset.DEM;

    @CommandLine.Option(
            names = {"--elevation-gain-threshold"},
            description = "Hysteresis dead band in meters, overrides the preset. 0 disables it")
    private Double thresholdM;

    @CommandLine.Option(
            names = {"--elevation-gain-smooth-window"},
            description = "Triangular kernel half-width in meters, overrides the preset. 0 disables it")
    private Double smoothWindowM;

    public ElevationGainOptions getElevationGainOptions() {
        return new ElevationGainOptions(
                preset,
                thresholdM == null ? preset.getThresholdM() : thresholdM,
                smoothWindowM == null ? preset.getSmoothWindowM() : smoothWindowM);
    }
}
