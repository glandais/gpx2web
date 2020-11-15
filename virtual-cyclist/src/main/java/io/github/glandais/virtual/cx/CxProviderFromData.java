package io.github.glandais.virtual.cx;

import io.github.glandais.gpx.Point;
import io.github.glandais.gpx.PointField;
import io.github.glandais.gpx.storage.Unit;

public class CxProviderFromData implements CxProvider {

    @Override
    public double getCx(Point location, double ellapsed, double speed, double grade) {
        return location.getCurrent(PointField.cx, Unit.CX);
    }
}
