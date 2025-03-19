package io.github.glandais;

import io.github.glandais.gpx.virtual.Bike;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import picocli.CommandLine;

@Data
@Slf4j
public class BikeMixin {

    @CommandLine.Option(
            names = {"--bike-crr"},
            description = "Bike crr (rolling efficiency)")
    private double crr = 0.004;

    @CommandLine.Option(
            names = {"--bike-inertia-front"},
            description = "Front wheel inertia")
    private double inertiaFront = 0.05;

    @CommandLine.Option(
            names = {"--bike-inertia-rear"},
            description = "Rear wheel inertia")
    private double inertiaRear = 0.07;

    @CommandLine.Option(
            names = {"--bike-wheel-radius"},
            description = "Wheel radius (m)")
    private double wheelRadius = 0.7;

    @CommandLine.Option(
            names = {"--bike-efficiency"},
            description = "Efficiency")
    private double efficiency = 0.976;

    private Bike bike;

    public void initBike() {
        bike = new Bike(crr, inertiaFront, inertiaRear, wheelRadius, efficiency);
    }
}
