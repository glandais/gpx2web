package io.github.glandais.virtual;

import io.github.glandais.gpx.GPXPath;
import io.github.glandais.virtual.aero.cx.CxProvider;
import io.github.glandais.virtual.aero.wind.WindProvider;
import lombok.Data;

import java.time.Instant;

@Data
public class Course {

    protected final GPXPath gpxPath;

    protected final Instant start;

    protected final Cyclist cyclist;

    protected final PowerProvider cyclistPowerProvider;

    protected final WindProvider windProvider;

    protected final CxProvider cxProvider;

}
