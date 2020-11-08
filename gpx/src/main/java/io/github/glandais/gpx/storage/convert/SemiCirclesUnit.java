package io.github.glandais.gpx.storage.convert;

import io.github.glandais.gpx.storage.Unit;

public class SemiCirclesUnit extends ConvertableUnit<Double, Integer> {

    public static int toSemiCircles(double rad) {
        return (int) (rad * 2147483648.0 / Math.PI);
    }

    public SemiCirclesUnit() {
        super(Unit.RADIANS);
    }

    @Override
    public Integer convertFromStorage(Double from) {
        return toSemiCircles(from);
    }

    @Override
    public Double convertToStorage(Integer value) {
        return value * Math.PI / 2147483648.0;
    }

}
