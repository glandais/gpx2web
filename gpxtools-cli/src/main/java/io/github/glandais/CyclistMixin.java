package io.github.glandais;

import io.github.glandais.gpx.virtual.Cyclist;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import picocli.CommandLine;

@Data
@Slf4j
public class CyclistMixin {

    @CommandLine.Option(names = {"--cyclist-weight"}, description = "Cyclist weight with bike (kg)")
    private double mKg = 80;

    @CommandLine.Option(names = {"--cyclist-power"}, description = "Cyclist power (W)")
    private double power = 280;

    @CommandLine.Option(names = {"--cyclist-max-brake"}, description = "Cyclist max brake (g)")
    private double maxBrakeG = 0.6;

    @CommandLine.Option(names = {"--cyclist-cd"}, description = "Cyclist cd")
    private double cd = 0.7;

    @CommandLine.Option(names = {"--cyclist-a"}, description = "Cyclist a")
    private double a = 0.5;

    @CommandLine.Option(names = {"--cyclist-max-angle"}, description = "Cyclist max angle")
    double maxAngleDeg = 45;

    @CommandLine.Option(names = {"--cyclist-max-speed"}, description = "Cyclist max speed (km/h)")
    private double maxSpeedKmH = 90;

    private Cyclist cyclist;

    public void initCyclist() {
        cyclist = new Cyclist(mKg, power, false, maxBrakeG, cd, a, maxAngleDeg, maxSpeedKmH);
    }
}
