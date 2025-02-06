package io.github.glandais.gpx.data.values.convert;

import io.github.glandais.gpx.data.values.Unit;

public class DegreesUnit extends ConvertableUnit<Double, Double> {

    public DegreesUnit() {
        super(Unit.RADIANS);
    }

    @Override
    public Double convertFromStorage(Double from) {
        return Math.toDegrees(from);
    }

    @Override
    public Double convertToStorage(Double value) {
        return Math.toRadians(value);
    }

}
