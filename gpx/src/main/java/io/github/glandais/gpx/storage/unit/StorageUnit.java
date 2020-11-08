package io.github.glandais.gpx.storage.unit;

import io.github.glandais.gpx.storage.Unit;

public abstract class StorageUnit<J> implements Unit<J> {

    public abstract J interpolate(J v, J vp1, double coef);

    public abstract String formatData(J j);

    public abstract String formatHuman(J j);

}
