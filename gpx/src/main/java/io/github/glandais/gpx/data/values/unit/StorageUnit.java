package io.github.glandais.gpx.data.values.unit;

import io.github.glandais.gpx.data.values.Unit;

public abstract class StorageUnit<J> implements Unit<J> {

    public abstract J interpolate(J v, J vp1, double coef);

    public abstract String formatData(J j);

    public abstract String formatHuman(J j);

    public abstract String getFormat();

    public abstract String getFormulaPartHumanToSI();

    public abstract String getFormulaPartSIToHuman();
}
