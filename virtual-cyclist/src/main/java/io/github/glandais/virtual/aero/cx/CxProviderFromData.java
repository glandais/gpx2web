package io.github.glandais.virtual.aero.cx;

import io.github.glandais.gpx.data.Point;
import io.github.glandais.gpx.data.PointField;
import io.github.glandais.gpx.data.values.Unit;

public class CxProviderFromData implements CxProvider {

    @Override
    public double getCx(Point location, double ellapsed, double speed, double grade) {
        return location.getCurrent(PointField.cx, Unit.CX);
    }
}
