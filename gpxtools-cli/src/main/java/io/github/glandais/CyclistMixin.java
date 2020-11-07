package io.github.glandais;

import io.github.glandais.virtual.Cyclist;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import picocli.CommandLine;

@Data
@Slf4j
public class CyclistMixin {

    @CommandLine.Option(names = {"--cyclist-weight"}, description = "Cyclist weight with bike (kg)")
    private double mKg = 80;

    @CommandLine.Option(names = {"--cyclist-max-angle"}, description = "Cyclist max angle (°)")
    private double maxAngleDeg = 15;

    @CommandLine.Option(names = {"--cyclist-max-speed"}, description = "Cyclist max speed (km/h)")
    private double maxSpeedKmH = 90;

    @CommandLine.Option(names = {"--cyclist-max-brake"}, description = "Cyclist max brake (g)")
    private double maxBrakeG = 0.3;

    @CommandLine.Option(names = {"--cyclist-f"}, description = "Cyclist f")
    double f = 0.005;

    private Cyclist cyclist;

    public void initCyclist() {
        cyclist = new Cyclist(mKg, maxAngleDeg, maxSpeedKmH, maxBrakeG, f);
    }
}
