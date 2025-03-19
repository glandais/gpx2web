package io.github.glandais.gpx.virtual;

import io.github.glandais.gpx.data.GPXPath;
import io.github.glandais.gpx.virtual.power.aero.aero.AeroProvider;
import io.github.glandais.gpx.virtual.power.aero.wind.WindProvider;
import io.github.glandais.gpx.virtual.power.cyclist.CyclistPowerProvider;
import io.github.glandais.gpx.virtual.power.cyclist.GradeSpeeds;
import java.time.Instant;
import lombok.Data;

@Data
public class Course {

    protected final GPXPath gpxPath;

    protected final Instant start;

    protected final Cyclist cyclist;

    protected final Bike bike;

    protected final CyclistPowerProvider cyclistPowerProvider;

    protected final WindProvider windProvider;

    protected final AeroProvider aeroProvider;

    protected double rho = 1.225;

    protected GradeSpeeds gradeSpeeds;
}
