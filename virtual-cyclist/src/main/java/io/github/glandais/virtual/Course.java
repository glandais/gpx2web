package io.github.glandais.virtual;

import io.github.glandais.gpx.GPXPath;
import io.github.glandais.virtual.cx.CxProvider;
import io.github.glandais.virtual.power.PowerProvider;
import io.github.glandais.virtual.wind.WindProvider;
import lombok.Data;

import java.time.ZonedDateTime;

@Data
public class Course {

    protected final GPXPath gpxPath;

    protected final ZonedDateTime start;

    protected final Cyclist cyclist;

    protected final PowerProvider powerProvider;

    protected final WindProvider windProvider;

    protected final CxProvider cxProvider;

}
