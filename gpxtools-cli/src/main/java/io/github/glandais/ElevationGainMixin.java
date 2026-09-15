package io.github.glandais;

import io.github.glandais.gpx.data.elevation.ElevationGainOptions;
import io.github.glandais.gpx.data.elevation.ElevationGainPreset;
import java.util.ArrayList;
import java.util.Arrays;
import lombok.Data;
import picocli.CommandLine;

/**
 * The scale cumulative ascent is measured at.
 *
 * <p>The preset is a complete answer — a dead band <i>and</i> a smoothing window. Either flag below
 * overrides that half of it, which is what a threshold flag on top of a preset flag has to mean.
 */
@Data
public class ElevationGainMixin {

    @CommandLine.Option(
            names = {"--elevation-gain-preset"},
            converter = PresetConverter.class,
            completionCandidates = PresetCandidates.class,
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

    /**
     * Accept the preset {@link ElevationGainPreset#getId() id}, which is the spelling the help text
     * advertises. Picocli's built-in enum conversion is case-sensitive, so without this
     * {@code --elevation-gain-preset dem} would be rejected.
     */
    public static class PresetConverter implements CommandLine.ITypeConverter<ElevationGainPreset> {

        @Override
        public ElevationGainPreset convert(String value) {
            return ElevationGainPreset.byId(value);
        }
    }

    /** Shows the ids rather than the enum constant names in {@code ${COMPLETION-CANDIDATES}}. */
    public static class PresetCandidates extends ArrayList<String> {

        public PresetCandidates() {
            Arrays.stream(ElevationGainPreset.values())
                    .map(ElevationGainPreset::getId)
                    .forEach(this::add);
        }
    }
}
